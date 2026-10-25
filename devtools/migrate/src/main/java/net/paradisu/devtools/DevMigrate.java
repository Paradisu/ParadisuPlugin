/*
 * The official plugin for the Paradisu server. Copyright (C) 2026 Paradisu. https://paradisu.net
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

package net.paradisu.devtools;

import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Liquibase;
import liquibase.changelog.ChangeSet;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.List;

/**
 * Development-only Liquibase runner used by the {@code dev db} commands. Never shaded into the plugin jar.
 *
 * <p>Usage: {@code devmigrate [--url u] [--user u] [--password p] <command> [arg]} where the command is one of
 * {@code update-file <db/changelog/00N.xml>}, {@code update-all}, {@code unrun} or {@code files}. Connection settings
 * default to the {@code PARADISU_DB_*} environment variables ({@code PARADISU_DB_URL} overrides host, port and name).
 */
public final class DevMigrate {
    private static final String MASTER = "db/migrations.xml";

    private DevMigrate() {}

    /**
     * Entry point.
     *
     * @param args command line arguments
     * @throws Exception if the command fails
     */
    public static void main(String[] args) throws Exception {
        String url = env(
                "PARADISU_DB_URL",
                "jdbc:postgresql://" + env("PARADISU_DB_HOST", "localhost") + ":" + env("PARADISU_DB_PORT", "5432")
                        + "/" + env("PARADISU_DB_NAME", "paradisu"));
        String user = env("PARADISU_DB_USER", "paradisu");
        String password = env("PARADISU_DB_PASSWORD", "paradisu");

        List<String> positional = new ArrayList<>();
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "--url" -> url = args[++i];
                case "--user" -> user = args[++i];
                case "--password" -> password = args[++i];
                default -> positional.add(args[i]);
            }
        }
        if (positional.isEmpty()) {
            throw new IllegalArgumentException("Missing command: update-file <file> | update-all | unrun | files");
        }

        switch (positional.get(0)) {
            case "files" -> files();
            case "update-file" -> {
                if (positional.size() < 2) {
                    throw new IllegalArgumentException("update-file requires a changelog path");
                }
                update(url, user, password, positional.get(1));
            }
            case "update-all" -> update(url, user, password, MASTER);
            case "unrun" -> unrun(url, user, password);
            default -> throw new IllegalArgumentException("Unknown command: " + positional.get(0));
        }
    }

    private static String env(String name, String fallback) {
        String value = System.getenv(name);
        return value == null || value.isBlank() ? fallback : value;
    }

    /** Prints the changelog files included by the master changelog, in order. */
    private static void files() throws Exception {
        try (var in = DevMigrate.class.getClassLoader().getResourceAsStream(MASTER)) {
            if (in == null) {
                throw new IllegalStateException("Could not find " + MASTER + " on the classpath");
            }
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            NodeList includes = factory.newDocumentBuilder().parse(in).getElementsByTagNameNS("*", "include");
            for (int i = 0; i < includes.getLength(); i++) {
                System.out.println(((Element) includes.item(i)).getAttribute("file"));
            }
        }
    }

    private static void update(String url, String user, String password, String changelog) throws Exception {
        try (Connection connection = DriverManager.getConnection(url, user, password)) {
            Database database =
                    DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(connection));
            try (Liquibase liquibase = new Liquibase(changelog, new ClassLoaderResourceAccessor(), database)) {
                liquibase.update(new Contexts(), new LabelExpression());
            }
        }
    }

    /** Prints {@code id:file} for every changeset in the master changelog that has not run yet. */
    private static void unrun(String url, String user, String password) throws Exception {
        try (Connection connection = DriverManager.getConnection(url, user, password)) {
            Database database =
                    DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(connection));
            try (Liquibase liquibase = new Liquibase(MASTER, new ClassLoaderResourceAccessor(), database)) {
                for (ChangeSet changeSet : liquibase.listUnrunChangeSets(new Contexts(), new LabelExpression())) {
                    System.out.println(changeSet.getId() + ":" + changeSet.getFilePath());
                }
            }
        }
    }
}
