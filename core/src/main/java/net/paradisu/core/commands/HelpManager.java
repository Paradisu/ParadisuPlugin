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

package net.paradisu.core.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.incendo.cloud.minecraft.extras.ImmutableMinecraftHelp;
import org.incendo.cloud.minecraft.extras.MinecraftHelp;

import java.util.stream.Collectors;

public class HelpManager<T> {
    private MinecraftHelp<T> minecraftHelp;

    public HelpManager(MinecraftHelp<T> minecraftHelp) {
        this.minecraftHelp = ImmutableMinecraftHelp.<T>builder()
                .commandManager(minecraftHelp.commandManager())
                .audienceProvider(minecraftHelp.audienceProvider())
                .commandPrefix(minecraftHelp.commandPrefix())
                .messageProvider((sender, key, args) -> Component.text()
                        .append(MiniMessage.miniMessage().deserialize(key))
                        .append(args.values().stream().map(Component::text).collect(Collectors.toList()))
                        .build())
                .descriptionDecorator(
                        (sender, description) -> MiniMessage.miniMessage().deserialize(description))
                .colors(MinecraftHelp.helpColors(
                        NamedTextColor.GOLD,
                        NamedTextColor.WHITE,
                        NamedTextColor.WHITE,
                        NamedTextColor.GRAY,
                        NamedTextColor.DARK_GRAY))
                .build();
    }

    public MinecraftHelp<T> getMinecraftHelp() {
        return minecraftHelp;
    }
}
