package net.paradisu.core.commands;

import java.util.Arrays;
import java.util.stream.Collectors;

import cloud.commandframework.minecraft.extras.MinecraftHelp;
import cloud.commandframework.minecraft.extras.MinecraftHelp.HelpColors;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;

public class HelpManager<T> {
    private MinecraftHelp<T> minecraftHelp;

    public HelpManager(MinecraftHelp<T> minecraftHelp) {
        // Initialize the help command system
        this.minecraftHelp = minecraftHelp;

        // Allow use of translation keys in the help command
        this.minecraftHelp.messageProvider((sender, key, args) ->
            Component.text()
                .append(MiniMessage.miniMessage().deserialize(key))
                .append(Arrays.stream(args).map(Component::text).collect(Collectors.toList()))
                .build()
        );
        this.minecraftHelp.descriptionDecorator((sender, description) ->
            MiniMessage.miniMessage().deserialize(description)
        );

        // Set help command colors
        this.minecraftHelp.setHelpColors(HelpColors.of(
            NamedTextColor.GOLD,
            NamedTextColor.WHITE,
            NamedTextColor.WHITE,
            NamedTextColor.GRAY,
            NamedTextColor.DARK_GRAY
            ));
    }
    
    public MinecraftHelp<T> getMinecraftHelp() {
        return minecraftHelp;
    }
}
