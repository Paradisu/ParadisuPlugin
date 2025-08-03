/*
 * The official plugin for the Paradisu server. Copyright (C) 2025 Paradisu. https://paradisu.net
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

package net.paradisu.core.locale;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.TextComponent;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.Component.translatable;

public interface Messages {
    Component GENERAL_PREFIX = translatable("paradisu.general.prefix");

    /**
     * Take an Adeventure text component as an inuput and prepends the specified prefix to it.
     *
     * @param component Adventure text component to be prefixed
     * @return A built Adventure text component with the prefix
     */
    static TextComponent prefixed(ComponentLike component) {
        return text().append(GENERAL_PREFIX)
                .append(Component.space())
                .append(component)
                .build();
    }
}
