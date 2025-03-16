package net.paradisu.core.commands;

import java.util.stream.Collectors;

import org.incendo.cloud.minecraft.extras.ImmutableMinecraftHelp;
import org.incendo.cloud.minecraft.extras.MinecraftHelp;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;

public class HelpManager<T> {
    private MinecraftHelp<T> minecraftHelp;

    public HelpManager(MinecraftHelp<T> minecraftHelp) {
        this.minecraftHelp = ImmutableMinecraftHelp.<T>builder()
            .commandManager(minecraftHelp.commandManager())
            .audienceProvider(minecraftHelp.audienceProvider())
            .commandPrefix(minecraftHelp.commandPrefix())
            .messageProvider((sender, key, args) ->
                Component.text()
                    .append(MiniMessage.miniMessage().deserialize(key))
                    .append(args.values().stream()
                        .map(Component::text)
                        .collect(Collectors.toList()))
                    .build()
            )
            .descriptionDecorator((sender, description) ->
                MiniMessage.miniMessage().deserialize(description)
            )
            .colors(MinecraftHelp.helpColors(
                NamedTextColor.GOLD,
                NamedTextColor.WHITE,
                NamedTextColor.WHITE,
                NamedTextColor.GRAY,
                NamedTextColor.DARK_GRAY
            ))
            .build();
    }
    
    public MinecraftHelp<T> getMinecraftHelp() {
        return minecraftHelp;
    }
}
