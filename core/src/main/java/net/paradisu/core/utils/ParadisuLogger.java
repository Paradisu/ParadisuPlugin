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

package net.paradisu.core.utils;

public interface ParadisuLogger {
    /**
     * Logs a severe message to console
     *
     * @param message the message to log
     */
    public void severe(String message);

    /**
     * Logs a severe message and an exception to console
     *
     * @param message the message to log
     * @param error the error to throw
     */
    public void severe(String message, Throwable error);

    /**
     * Logs an error message to console
     *
     * @param message the message to log
     */
    public void error(String message);

    /**
     * Logs an error message and an exception to console
     *
     * @param message the message to log
     * @param error the error to throw
     */
    public void error(String message, Throwable error);

    /**
     * Logs a warning message to console
     *
     * @param message the message to log
     */
    public void warn(String message);

    /**
     * Logs a warning message to console
     *
     * @param message the message to log
     */
    public void warn(String message, Throwable error);

    /**
     * Logs an info message to console
     *
     * @param message the message to log
     */
    public void info(String message);
}
