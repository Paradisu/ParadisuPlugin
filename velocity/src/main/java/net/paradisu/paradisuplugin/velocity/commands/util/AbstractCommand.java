package net.paradisu.paradisuplugin.velocity.commands.util;

import java.util.Arrays;
import java.util.stream.Collectors;

import com.velocitypowered.api.command.CommandSource;

import cloud.commandframework.CommandManager;
import cloud.commandframework.minecraft.extras.MinecraftHelp;
import cloud.commandframework.minecraft.extras.MinecraftHelp.HelpColors;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.paradisu.paradisuplugin.velocity.Paradisu;

public abstract class AbstractCommand {
    protected final Paradisu paradisu;
    protected final CommandManager<CommandSource> commandManager;
    protected MinecraftHelp<CommandSource> minecraftHelp;

    /**
     * Constructor for the AbstractCommand class.
     * @param paradisu The instance of the Paradisu plugin.
     */
    public AbstractCommand(Paradisu paradisu) {
        // Initialize the command manager
        this.paradisu = paradisu;
        this.commandManager = paradisu.commandManager();

        // Initialize the help command system
        this.minecraftHelp = new MinecraftHelp<>("/vparadisu help", p -> p, commandManager);

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

    public abstract void register();
}