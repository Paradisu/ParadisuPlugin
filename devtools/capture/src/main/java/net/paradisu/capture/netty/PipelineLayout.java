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

package net.paradisu.capture.netty;

import java.util.List;
import java.util.function.Predicate;

/**
 * Where capture handlers sit in a platform's netty pipeline. This is the only place pipeline handler names live.
 *
 * <p>Invariant: the inbound capture handler sits immediately before the packet decoder and the outbound one immediately
 * before the packet encoder, in pipeline list order. Inbound data flows head to tail and outbound data tail to head, so
 * both then see decoded frames, {@code [packet id][payload]}, after decryption and decompression and before compression
 * and encryption. If a platform renames its handlers, {@code dev capture doctor} shows the live pipeline; fix the names
 * here.
 *
 * @param name platform label for log messages
 * @param decoderAnchors candidate decoder handler names, first match wins
 * @param encoderAnchors candidate encoder handler names, first match wins
 * @param reanchorEvent user events after which the platform inserts handlers next to the codecs (compression), so the
 *     capture handlers must move back to just before them
 */
public record PipelineLayout(
        String name, List<String> decoderAnchors, List<String> encoderAnchors, Predicate<Object> reanchorEvent) {

    /** Paper/Purpur: names from {@code net.minecraft.network.HandlerNames}; codecs are replaced in place. */
    public static final PipelineLayout PAPER = new PipelineLayout(
            "paper", List.of("decoder", "inbound_config"), List.of("encoder", "outbound_config"), event -> false);

    /**
     * Velocity: names from {@code com.velocitypowered.proxy.network.Connections}. Enabling compression adds
     * {@code compression-decoder}/{@code -encoder} directly before the codecs and then fires a
     * {@code VelocityConnectionEvent.COMPRESSION_*} user event.
     */
    public static final PipelineLayout VELOCITY = new PipelineLayout(
            "velocity",
            List.of("minecraft-decoder"),
            List.of("minecraft-encoder"),
            event -> event instanceof Enum<?> e && e.name().startsWith("COMPRESSION_"));
}
