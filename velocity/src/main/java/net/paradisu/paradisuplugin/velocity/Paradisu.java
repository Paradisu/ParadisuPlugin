package net.paradisu.paradisuplugin.velocity;

import com.google.inject.Inject;
import cloud.commandframework.CommandManager;
import cloud.commandframework.execution.CommandExecutionCoordinator;
import cloud.commandframework.velocity.VelocityCommandManager;
import net.paradisu.paradisuplugin.velocity.commands.util.AbstractCommand;
import net.paradisu.paradisuplugin.velocity.locale.TranslationManager;
import net.paradisu.paradisuplugin.velocity.commands.command.*;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;

import java.nio.file.Path;
import java.util.function.Function;
import java.util.stream.Stream;

import org.slf4j.Logger;

public final class Paradisu {

    private final ProxyServer server;
    private final Logger logger;
    private final Path dataDirectory;
    private TranslationManager translationManager;
    
    private VelocityCommandManager<CommandSource> commandManager;
    
    @Inject
    public Paradisu(ProxyServer server, Logger logger, @DataDirectory Path dataDirectory) {
        this.server = server;
        this.logger = logger;
        this.dataDirectory = dataDirectory;
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        this.translationManager = new TranslationManager(this);
        this.translationManager.reload();

        this.commandManager = new VelocityCommandManager<>(
            this.server.getPluginManager().ensurePluginContainer(this), 
            this.server, 
            CommandExecutionCoordinator.simpleCoordinator(), 
            Function.identity(), 
            Function.identity()
            );
        registerCommands();
    }

    
    public Logger logger() {
        return this.logger;
    }

    public Path dataDirectory() {
        return this.dataDirectory;
    }

    
    public CommandManager<CommandSource> commandManager() {
        return this.commandManager;
    }

    private void registerCommands() {
        Stream.of(
            new LocateCommand(this),
            new TeleportCommand(this),
            new VParadisuCommand(this),
            new WarpCommand(this)
        ).forEach(AbstractCommand::register);
    }

    public void reload() {
        this.translationManager.reload();
    }

}