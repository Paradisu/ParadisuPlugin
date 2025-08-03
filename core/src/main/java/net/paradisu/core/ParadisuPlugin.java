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

package net.paradisu.core;

import de.themoep.connectorplugin.ConnectorPlugin;
import net.paradisu.core.config.ConfigManager;
import net.paradisu.core.database.DatabaseSession;
import net.paradisu.core.locale.TranslationManager;
import net.paradisu.core.utils.ParadisuLogger;
import org.incendo.cloud.CommandManager;

import java.nio.file.Path;

public interface ParadisuPlugin {
    public ParadisuLogger logger();

    public Path dataDirectory();

    public CommandManager<?> commandManager();

    public ConfigManager configManager();

    public TranslationManager translationManager();

    public ConnectorPlugin<?> connector();

    public DatabaseSession databaseSession();

    public void reload();
}
