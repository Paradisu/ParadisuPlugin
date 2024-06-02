package net.paradisu.core.database;

import jakarta.persistence.EntityManagerFactory;
import lombok.Getter;
import lombok.experimental.Accessors;
import net.paradisu.core.ParadisuPlugin;
import net.paradisu.database.Models;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.cfg.Configuration;
import org.hibernate.dialect.PostgreSQLDialect;
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
            .applySetting(AvailableSettings.DIALECT, PostgreSQLDialect.class.getName())
            .applySetting(AvailableSettings.JAKARTA_JDBC_DRIVER, Driver.class.getName())
            .applySetting(AvailableSettings.JAKARTA_JDBC_URL, url)
            .applySetting(AvailableSettings.JAKARTA_JDBC_USER, username)
            .applySetting(AvailableSettings.JAKARTA_JDBC_PASSWORD, password);

        try {
            Configuration configuration = new Configuration(
                new MetadataSources(registryBuilder.build()).addAnnotatedClasses(Models.models()));
            this.factory = configuration.buildSessionFactory();
            this.open = true;
            this.plugin.logger().info("Database connection established.");
        } catch (Exception e) {
            this.open = false;
            this.plugin.logger().severe("Failed to establish database connection:", e);
        }
    }
}
