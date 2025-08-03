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

package net.paradisu.core.database;

import jakarta.persistence.EntityManagerFactory;
import lombok.Getter;
import lombok.experimental.Accessors;
import net.paradisu.core.ParadisuPlugin;
import net.paradisu.database.Models;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.hikaricp.internal.HikariCPConnectionProvider;
import org.postgresql.Driver;

@Accessors(fluent = true)
@Getter
public class DatabaseSession {
    private ParadisuPlugin plugin;
    private EntityManagerFactory factory;
    private boolean open;

    public DatabaseSession(ParadisuPlugin plugin, String url, String username, String password) {
        this.plugin = plugin;

        final StandardServiceRegistryBuilder registryBuilder = new StandardServiceRegistryBuilder()
                .applySetting(AvailableSettings.CONNECTION_PROVIDER, HikariCPConnectionProvider.class.getName())
                .applySetting(AvailableSettings.JAKARTA_JDBC_DRIVER, Driver.class.getName())
                .applySetting(AvailableSettings.JAKARTA_JDBC_URL, url)
                .applySetting(AvailableSettings.JAKARTA_JDBC_USER, username)
                .applySetting(AvailableSettings.JAKARTA_JDBC_PASSWORD, password);

        final StandardServiceRegistry registry = registryBuilder.build();

        try {
            final MetadataSources sources = new MetadataSources(registry);
            sources.addAnnotatedClasses(Models.models());
            Metadata metadata = sources.getMetadataBuilder().build();
            this.factory = metadata.getSessionFactoryBuilder().build();
            this.open = true;
            this.plugin.logger().info("Database connection established.");
        } catch (Exception e) {
            this.open = false;
            this.plugin.logger().severe("Failed to establish database connection:", e);
            StandardServiceRegistryBuilder.destroy(registry);
        }
    }
}
