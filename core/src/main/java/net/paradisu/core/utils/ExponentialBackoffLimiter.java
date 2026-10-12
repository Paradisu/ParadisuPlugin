/*
 * The official plugin for the Paradisu server. Copyright (C) 2026 Paradisu. https://paradisu.net
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package net.paradisu.core.utils;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.LongSupplier;

/**
 * Per-key rate limiter that allows short bursts and locks out spammers for exponentially growing periods.
 *
 * <p>Each key has a token bucket holding up to {@code burst} tokens, refilled by one every {@code refillInterval}. An
 * attempt with a token available is allowed. An attempt with an empty bucket is a <em>strike</em>: the key is locked
 * out for {@code baseTimeout * 2^(strikes so far)}, capped at {@code maxTimeout}. Attempts during a lockout are denied
 * but do not add strikes. When a lockout ends the key gets a single token, so one more attempt is allowed while
 * continued spam earns the next, doubled lockout. One strike is forgiven for every {@code strikeDecay} without a new
 * strike.
 *
 * <p>Thread-safe. Idle keys (full bucket, no strikes, not locked out) are dropped periodically during
 * {@link #tryAcquire}, so memory stays proportional to recently active keys.
 *
 * @param <K> the key type, e.g. a player UUID
 */
public final class ExponentialBackoffLimiter<K> {
    /** Number of {@link #tryAcquire} calls between sweeps for idle keys. */
    private static final int PURGE_INTERVAL = 256;
    /** Highest strike count used as an exponent; beyond this the timeout is already at its cap. */
    private static final int MAX_EXPONENT = 30;

    private final Settings settings;
    private final LongSupplier nanoClock;
    private final Map<K, State> states = new ConcurrentHashMap<>();
    private final AtomicInteger callsSincePurge = new AtomicInteger();

    /**
     * Creates a limiter using {@link System#nanoTime()}.
     *
     * @param settings the limiter settings
     */
    public ExponentialBackoffLimiter(Settings settings) {
        this(settings, System::nanoTime);
    }

    /**
     * Creates a limiter with a custom clock, e.g. for tests.
     *
     * @param settings the limiter settings
     * @param nanoClock monotonic time source in nanoseconds
     */
    public ExponentialBackoffLimiter(Settings settings, LongSupplier nanoClock) {
        this.settings = settings;
        this.nanoClock = nanoClock;
    }

    /**
     * Records an attempt for a key.
     *
     * @param key the key
     * @return whether the attempt is allowed, and if not, how long until it may be retried
     */
    public Result tryAcquire(K key) {
        long now = this.nanoClock.getAsLong();
        if (this.callsSincePurge.incrementAndGet() >= PURGE_INTERVAL) {
            this.callsSincePurge.set(0);
            this.purgeIdle(now);
        }

        Result[] result = new Result[1];
        this.states.compute(key, (k, state) -> {
            if (state == null) {
                state = new State(this.settings.burst(), now);
            }
            result[0] = state.acquire(now, this.settings);
            return state;
        });
        return result[0];
    }

    /**
     * Forgets a key, clearing its tokens, strikes and lockout.
     *
     * @param key the key
     */
    public void reset(K key) {
        this.states.remove(key);
    }

    /**
     * Returns the settings of this limiter.
     *
     * @return the settings
     */
    public Settings settings() {
        return this.settings;
    }

    /**
     * Returns the number of keys currently tracked.
     *
     * @return the number of tracked keys
     */
    public int size() {
        return this.states.size();
    }

    private void purgeIdle(long now) {
        for (K key : this.states.keySet()) {
            this.states.computeIfPresent(key, (k, state) -> state.isIdle(now, this.settings) ? null : state);
        }
    }

    /**
     * Limiter settings.
     *
     * @param burst attempts allowed in quick succession; at least 1
     * @param refillInterval time to regain one attempt
     * @param baseTimeout lockout after the first strike
     * @param maxTimeout longest lockout
     * @param strikeDecay time without a new strike after which one strike is forgiven
     */
    public record Settings(
            int burst, Duration refillInterval, Duration baseTimeout, Duration maxTimeout, Duration strikeDecay) {
        public Settings {
            if (burst < 1) {
                throw new IllegalArgumentException("burst must be at least 1");
            }
            for (Duration duration : new Duration[] {refillInterval, baseTimeout, maxTimeout, strikeDecay}) {
                if (duration.isNegative() || duration.isZero()) {
                    throw new IllegalArgumentException("Limiter durations must be positive");
                }
            }
            if (maxTimeout.compareTo(baseTimeout) < 0) {
                throw new IllegalArgumentException("maxTimeout must not be shorter than baseTimeout");
            }
        }
    }

    /**
     * Outcome of an attempt.
     *
     * @param allowed whether the attempt is allowed
     * @param retryAfter when denied, the time until the next attempt may succeed; zero when allowed
     */
    public record Result(boolean allowed, Duration retryAfter) {
        private static final Result ALLOWED = new Result(true, Duration.ZERO);

        /**
         * Returns the retry delay rounded up to whole seconds, for display.
         *
         * @return the seconds until a retry, at least 1 when denied
         */
        public long retryAfterSeconds() {
            if (this.allowed) {
                return 0;
            }
            return Math.max(1, (this.retryAfter.toMillis() + 999) / 1000);
        }
    }

    /** Mutable per-key state; only accessed inside {@link ConcurrentHashMap#compute}. */
    private static final class State {
        private double tokens;
        private long lastRefill;
        private int strikes;
        private long lockedUntil;
        private long lastDecay;

        private State(int tokens, long now) {
            this.tokens = tokens;
            this.lastRefill = now;
            this.lockedUntil = now;
            this.lastDecay = now;
        }

        private Result acquire(long now, Settings settings) {
            this.decay(now, settings);
            if (now < this.lockedUntil) {
                return new Result(false, Duration.ofNanos(this.lockedUntil - now));
            }

            this.refill(now, settings);
            if (this.tokens >= 1) {
                this.tokens -= 1;
                return Result.ALLOWED;
            }

            long timeout = timeout(this.strikes, settings);
            this.strikes++;
            this.lockedUntil = now + timeout;
            this.lastDecay = this.lockedUntil;
            // One attempt is allowed when the lockout ends; refilling resumes from then
            this.tokens = 1;
            this.lastRefill = this.lockedUntil;
            return new Result(false, Duration.ofNanos(timeout));
        }

        private void refill(long now, Settings settings) {
            if (now <= this.lastRefill) {
                return;
            }
            double regained =
                    (double) (now - this.lastRefill) / settings.refillInterval().toNanos();
            this.tokens = Math.min(settings.burst(), this.tokens + regained);
            this.lastRefill = now;
        }

        /** Forgives one strike per full {@code strikeDecay} since the last strike or decay, once not locked out. */
        private void decay(long now, Settings settings) {
            if (this.strikes == 0 || now < this.lockedUntil) {
                return;
            }
            long decayNanos = settings.strikeDecay().toNanos();
            long periods = (now - this.lastDecay) / decayNanos;
            if (periods > 0) {
                this.strikes = (int) Math.max(0, this.strikes - periods);
                this.lastDecay += periods * decayNanos;
            }
        }

        private boolean isIdle(long now, Settings settings) {
            this.decay(now, settings);
            this.refill(now, settings);
            return this.strikes == 0 && now >= this.lockedUntil && this.tokens >= settings.burst();
        }

        private static long timeout(int strikes, Settings settings) {
            long base = settings.baseTimeout().toNanos();
            long max = settings.maxTimeout().toNanos();
            int exponent = Math.min(strikes, MAX_EXPONENT);
            // Saturate instead of overflowing
            if (base > (max >> exponent)) {
                return max;
            }
            return Math.min(max, base << exponent);
        }
    }
}
