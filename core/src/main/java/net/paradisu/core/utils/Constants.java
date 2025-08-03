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

public final class Constants {
    public final class Plugin {
        public static final String ID = "${plugin.id}";
        public static final String NAME = "${plugin.name}";
        public static final String VERSION = "${plugin.version}";
        public static final String DESCRIPTION = "${plugin.description}";
        public static final String URL = "${plugin.url}";
        public static final String AUTHORS = "${plugin.authors}";
    }

    public final class Git {
        public static final String BRANCH = "${git.branch}";
        public static final String COMMIT = "${git.commit}";
        public static final String COMMIT_ABBREV = "${git.commitAbbrev}";
        public static final int BUILD_NUMBER = Integer.parseInt("${git.buildNumber}");
        public static final String COMMIT_MESSAGE = "${git.commitMessage}";
        public static final String REPOSITORY = "${git.repository}";
    }
}
