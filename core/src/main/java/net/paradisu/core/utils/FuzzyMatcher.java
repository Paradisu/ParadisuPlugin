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

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.regex.Pattern;

/**
 * Immutable index for finding the value whose key is most similar to user input, e.g. a mistyped warp name.
 *
 * <p>Keys are normalized once when the index is built (lower case, accents and punctuation removed). A lookup scores
 * every key in tiers, from best to worst:
 *
 * <ol>
 *   <li>equal after normalization ({@code "my Warp"} = {@code "My Warp"})
 *   <li>equal ignoring spaces and punctuation ({@code "mywarp"} = {@code "My Warp"})
 *   <li>key starts with the input ({@code "cind"} → {@code "Cinderella Castle"})
 *   <li>every input word starts a word of the key ({@code "cas"} or {@code "cin cas"} → {@code "Cinderella Castle"})
 *   <li>key contains the input (inputs of 3+ characters)
 *   <li>within a small edit distance of the whole key or of its start ({@code "cindrella"} → {@code "Cinderella
 *       Castle"})
 * </ol>
 *
 * <p>The cost of a lookup is bounded: input longer than {@link #MAX_INPUT_LENGTH} is rejected, the edit distance is
 * capped at {@link #MAX_DISTANCE} and computed only within a diagonal band, and keys whose length cannot be within the
 * cap are skipped. A lookup is therefore linear in the number of keys with a small constant factor.
 *
 * <p>Exact lookups are cheaper with a map, so callers should check for an exact key first and fall back to this.
 *
 * @param <T> the value type
 */
public final class FuzzyMatcher<T> {
    /** Longest input, after normalization, that is matched; longer input never matches. */
    public static final int MAX_INPUT_LENGTH = 64;
    /** Largest edit distance accepted for long input. */
    public static final int MAX_DISTANCE = 3;

    private static final Pattern MARKS = Pattern.compile("\\p{M}+");
    private static final Pattern WHITESPACE = Pattern.compile("\\s+");
    private static final Pattern NON_ALPHANUMERIC = Pattern.compile("[^\\p{L}\\p{N}]+");

    /** Score step between tiers; larger than any in-tier penalty. */
    private static final int TIER = 64;

    private final List<Entry<T>> entries;

    private FuzzyMatcher(List<Entry<T>> entries) {
        this.entries = List.copyOf(entries);
    }

    /**
     * Creates a builder for a matcher.
     *
     * @param <T> the value type
     * @return a new builder
     */
    public static <T> Builder<T> builder() {
        return new Builder<>();
    }

    /**
     * Creates an empty matcher.
     *
     * @param <T> the value type
     * @return a matcher that never matches
     */
    public static <T> FuzzyMatcher<T> empty() {
        return new FuzzyMatcher<>(List.of());
    }

    /**
     * Finds the value whose key best matches the input.
     *
     * @param input the user input
     * @param filter only values passing this filter are considered, e.g. a permission check
     * @param tieBreaker orders values whose keys match equally well; the smallest wins
     * @return the best match, or empty if no key is similar enough
     */
    public Optional<T> match(String input, Predicate<? super T> filter, Comparator<? super T> tieBreaker) {
        if (input == null) {
            return Optional.empty();
        }
        String normalized = normalize(input);
        String compact = compact(normalized);
        if (compact.isEmpty() || compact.length() > MAX_INPUT_LENGTH) {
            return Optional.empty();
        }
        String[] tokens = tokens(normalized);
        int maxDistance = maxDistance(compact.length());

        Entry<T> best = null;
        int bestScore = Integer.MAX_VALUE;
        for (Entry<T> entry : this.entries) {
            int score = score(entry, normalized, compact, tokens, maxDistance);
            if (score == Integer.MAX_VALUE || score > bestScore) {
                continue;
            }
            if (best == null || score < bestScore || tieBreaker.compare(entry.value(), best.value()) < 0) {
                if (!filter.test(entry.value())) {
                    continue;
                }
                best = entry;
                bestScore = score;
            }
        }
        return best == null ? Optional.empty() : Optional.of(best.value());
    }

    /**
     * Scores a key against the input; lower is better.
     *
     * @return the score, or {@link Integer#MAX_VALUE} if the key does not match
     */
    private static int score(Entry<?> entry, String normalized, String compact, String[] tokens, int maxDistance) {
        int penalty = entry.secondary() ? 1 : 0;
        if (entry.normalized().equals(normalized)) {
            return penalty;
        }
        if (entry.compact().equals(compact)) {
            return TIER + penalty;
        }
        if (entry.compact().startsWith(compact)) {
            return 2 * TIER + penalty;
        }
        if (tokenPrefixes(entry.tokens(), tokens)) {
            return 3 * TIER + penalty;
        }
        if (compact.length() >= 3 && entry.compact().contains(compact)) {
            return 4 * TIER + penalty;
        }
        if (maxDistance == 0) {
            return Integer.MAX_VALUE;
        }

        int distance = boundedDistance(compact, entry.compact(), maxDistance, false);
        if (distance <= maxDistance) {
            return 5 * TIER + distance * 4 + penalty;
        }
        if (compact.length() >= 4) {
            distance = boundedDistance(compact, entry.compact(), maxDistance, true);
            if (distance <= maxDistance) {
                return 5 * TIER + distance * 4 + 2 + penalty;
            }
        }
        return Integer.MAX_VALUE;
    }

    /** Whether every input token is a prefix of a distinct key token, in order. */
    private static boolean tokenPrefixes(String[] keyTokens, String[] inputTokens) {
        if (inputTokens.length == 0 || inputTokens.length > keyTokens.length) {
            return false;
        }
        int k = 0;
        for (String inputToken : inputTokens) {
            while (k < keyTokens.length && !keyTokens[k].startsWith(inputToken)) {
                k++;
            }
            if (k == keyTokens.length) {
                return false;
            }
            k++;
        }
        return true;
    }

    /**
     * Optimal string alignment distance (Levenshtein plus adjacent transpositions), computed only within a band of
     * {@code max} around the diagonal.
     *
     * @param a the input
     * @param b the key
     * @param max the largest distance of interest
     * @param prefix whether to match {@code a} against the best prefix of {@code b} instead of all of it
     * @return the distance, or {@code max + 1} if it exceeds {@code max}
     */
    static int boundedDistance(String a, String b, int max, boolean prefix) {
        int n = a.length();
        int m = prefix ? Math.min(b.length(), n + max) : b.length();
        if (Math.abs(n - m) > max && !prefix) {
            return max + 1;
        }
        if (prefix && m < n - max) {
            return max + 1;
        }

        int over = max + 1;
        int[] twoBack = new int[m + 1];
        int[] previous = new int[m + 1];
        int[] current = new int[m + 1];
        for (int j = 0; j <= m; j++) {
            previous[j] = j <= max ? j : over;
        }

        for (int i = 1; i <= n; i++) {
            Arrays.fill(current, over);
            if (i <= max) {
                current[0] = i;
            }
            int from = Math.max(1, i - max);
            int to = Math.min(m, i + max);
            int rowMin = current[0];
            char ca = a.charAt(i - 1);
            for (int j = from; j <= to; j++) {
                char cb = b.charAt(j - 1);
                int cost = ca == cb ? 0 : 1;
                int value = Math.min(Math.min(previous[j] + 1, current[j - 1] + 1), previous[j - 1] + cost);
                if (i > 1 && j > 1 && ca == b.charAt(j - 2) && a.charAt(i - 2) == cb) {
                    value = Math.min(value, twoBack[j - 2] + 1);
                }
                current[j] = Math.min(value, over);
                rowMin = Math.min(rowMin, current[j]);
            }
            if (rowMin > max) {
                return over;
            }
            int[] recycled = twoBack;
            twoBack = previous;
            previous = current;
            current = recycled;
        }

        if (!prefix) {
            return previous[m];
        }
        int best = over;
        for (int j = Math.max(0, n - max); j <= m; j++) {
            best = Math.min(best, previous[j]);
        }
        return best;
    }

    /** Largest edit distance tolerated for input of the given compact length. */
    private static int maxDistance(int length) {
        if (length <= 2) {
            return 0;
        }
        if (length <= 5) {
            return 1;
        }
        if (length <= 10) {
            return 2;
        }
        return MAX_DISTANCE;
    }

    /**
     * Lower-cases text, strips accents, trims it and collapses whitespace.
     *
     * @param text the text
     * @return the normalized text
     */
    public static String normalize(String text) {
        String stripped =
                MARKS.matcher(Normalizer.normalize(text, Normalizer.Form.NFD)).replaceAll("");
        return WHITESPACE.matcher(stripped.toLowerCase(Locale.ROOT).trim()).replaceAll(" ");
    }

    private static String compact(String normalized) {
        return NON_ALPHANUMERIC.matcher(normalized).replaceAll("");
    }

    private static String[] tokens(String normalized) {
        return Arrays.stream(NON_ALPHANUMERIC.split(normalized))
                .filter(token -> !token.isEmpty())
                .toArray(String[]::new);
    }

    private record Entry<T>(T value, String normalized, String compact, String[] tokens, boolean secondary) {}

    /** Collects keys for a {@link FuzzyMatcher}. */
    public static final class Builder<T> {
        private final List<Entry<T>> entries = new ArrayList<>();

        private Builder() {}

        /**
         * Adds a primary key, such as a name.
         *
         * @param key the key
         * @param value the value it resolves to
         * @return this builder
         */
        public Builder<T> add(String key, T value) {
            return this.add(key, value, false);
        }

        /**
         * Adds a key. A secondary key, such as an alias, loses to a primary key that matches equally well.
         *
         * @param key the key
         * @param value the value it resolves to
         * @param secondary whether the key is secondary
         * @return this builder
         */
        public Builder<T> add(String key, T value, boolean secondary) {
            String normalized = normalize(key);
            String compact = compact(normalized);
            if (!compact.isEmpty()) {
                this.entries.add(new Entry<>(value, normalized, compact, tokens(normalized), secondary));
            }
            return this;
        }

        /**
         * Builds the matcher.
         *
         * @return the matcher
         */
        public FuzzyMatcher<T> build() {
            return new FuzzyMatcher<>(this.entries);
        }
    }
}
