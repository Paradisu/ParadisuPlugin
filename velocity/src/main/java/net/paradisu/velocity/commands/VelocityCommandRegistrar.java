package net.paradisu.velocity.commands;

import net.paradisu.velocity.ParadisuVelocity;
import net.paradisu.velocity.commands.command.*;

import java.util.stream.Stream;

public class VelocityCommandRegistrar {
    /**
     * Register commands; all must be included here
     *
     * @param paradisu ParadisuVelocity instance
     */
    public static void registerCommands(ParadisuVelocity paradisu) {
        Stream.of(
            new BackCommand(paradisu),
            new ListCommand(paradisu),
            new PacksCommand(paradisu),
            new LocateCommand(paradisu),
            new TeleportAcceptCommand(paradisu),
            new TeleportCancelCommand(paradisu),
            new TeleportDenyCommand(paradisu),
            new TeleportCommand(paradisu),
            new TeleportHereCommand(paradisu),
            new TeleportHereRequestCommand(paradisu),
            new TeleportPositionCommand(paradisu),
            new TeleportRequestCommand(paradisu),
            new VParadisuCommand(paradisu),
            new WarpCommand(paradisu)
        ).forEach(AbstractVelocityCommand::register);
    }
}
