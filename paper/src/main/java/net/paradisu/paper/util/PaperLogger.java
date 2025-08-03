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

package net.paradisu.paper.util;

import lombok.AllArgsConstructor;
import net.paradisu.core.utils.ParadisuLogger;

import java.util.logging.Level;
import java.util.logging.Logger;

@AllArgsConstructor
public class PaperLogger implements ParadisuLogger {
    private final Logger logger;

    @Override
    public void severe(String message) {
        logger.severe(message);
    }

    @Override
    public void severe(String message, Throwable error) {
        logger.log(Level.SEVERE, message, error);
    }

    @Override
    public void error(String message) {
        logger.warning(message);
    }

    @Override
    public void error(String message, Throwable error) {
        logger.log(Level.WARNING, message, error);
    }

    @Override
    public void warn(String message) {
        error(message);
    }

    @Override
    public void warn(String message, Throwable error) {
        error(message, error);
    }

    @Override
    public void info(String message) {
        logger.info(message);
    }
}
