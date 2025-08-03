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
                        new WarpCommand(paradisu))
                .forEach(AbstractVelocityCommand::register);
    }
}
