package net.paradisu.paper.commands;

import net.paradisu.paper.ParadisuPaper;
import net.paradisu.paper.commands.command.*;

import java.util.stream.Stream;

public class PaperCommandRegistrar {
    /**
     * Register commands; all must be included here
     *
     * @param paradisu ParadisuPaper instance
     */
    public static void registerCommands(ParadisuPaper paradisu) {
        Stream.of(
            new ParadisuCommand(paradisu),
            new HatCommand(paradisu)
        ).forEach(AbstractPaperCommand::register);
    }
}