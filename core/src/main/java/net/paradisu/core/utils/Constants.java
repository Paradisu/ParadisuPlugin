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
