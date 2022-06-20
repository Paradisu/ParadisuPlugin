package net.paradisu.paradisuplugin.velocity.commands.util;

import java.util.Arrays;
import java.util.stream.Collectors;

import com.velocitypowered.api.command.CommandSource;

import cloud.commandframework.CommandManager;
import cloud.commandframework.minecraft.extras.MinecraftHelp;
import net.kyori.adventure.text.Component;
import net.paradisu.paradisuplugin.velocity.Paradisu;

public abstract class AbstractCommand {
    protected final  Paradisu paradisu;
    protected final CommandManager<CommandSource> commandManager;
    protected final MinecraftHelp<CommandSource> minecraftHelp;

    public AbstractCommand(Paradisu paradisu) {
        this.paradisu = paradisu;
        this.commandManager = paradisu.commandManager();
        this.minecraftHelp = new MinecraftHelp<>("/vparadisu help", p -> p, commandManager);
        this.minecraftHelp.messageProvider((sender, key, args) ->
            Component.translatable(
                key,
                Arrays.stream(args).map(Component::text).collect(Collectors.toList())
        ));
        this.minecraftHelp.descriptionDecorator((sender, description) ->
            Component.translatable(description)
        );
    }

    public abstract void register();
}