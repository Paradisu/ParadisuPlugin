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

package net.paradisu.paper.commands;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.paradisu.core.commands.AbstractCommand;
import net.paradisu.core.commands.HelpManager;
import net.paradisu.paper.ParadisuPaper;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.minecraft.extras.MinecraftHelp;

public abstract class AbstractPaperCommand implements AbstractCommand {
    protected final ParadisuPaper paradisu;
    protected final CommandManager<CommandSourceStack> commandManager;
    protected final HelpManager<CommandSourceStack> helpManager;

    public AbstractPaperCommand(ParadisuPaper paradisu) {
        this.paradisu = paradisu;
        this.commandManager = paradisu.commandManager();
        this.helpManager = new HelpManager<CommandSourceStack>(
                MinecraftHelp.create("/paradisu help", this.commandManager, p -> p.getSender()));
    }

    @Override
    public abstract void register();
}
