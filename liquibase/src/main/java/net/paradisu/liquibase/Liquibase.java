package net.paradisu.liquibase;

import liquibase.Scope;
import liquibase.ext.hibernate.customfactory.CustomMetadataFactory;
import liquibase.ext.hibernate.database.HibernateDatabase;
import liquibase.ext.hibernate.database.connection.HibernateConnection;
import net.paradisu.database.Models;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.dialect.PostgreSQLDialect;
import org.hibernate.hikaricp.internal.HikariCPConnectionProvider;
import org.postgresql.Driver;

import java.lang.reflect.Field;

public class Liquibase implements CustomMetadataFactory {

    @Override
    public Metadata getMetadata(HibernateDatabase hibernateDatabase, HibernateConnection connection) {

        final StandardServiceRegistryBuilder registryBuilder = new StandardServiceRegistryBuilder()
            .applySetting(AvailableSettings.CONNECTION_PROVIDER, HikariCPConnectionProvider.class.getName())
            .applySetting(AvailableSettings.DIALECT, PostgreSQLDialect.class.getName())
            .applySetting(AvailableSettings.JAKARTA_JDBC_DRIVER, Driver.class.getName())
            .applySetting(AvailableSettings.JAKARTA_JDBC_URL, "jdbc:postgresql://localhost:5432/paradisu")
            .applySetting(AvailableSettings.JAKARTA_JDBC_USER, "paradisu")
            .applySetting(AvailableSettings.JAKARTA_JDBC_PASSWORD, "paradisu");

        MetadataSources metadataSources = new MetadataSources(registryBuilder.build());
        metadataSources.addAnnotatedClasses(Models.models());

        try {
            Field dialectField = HibernateDatabase.class.getDeclaredField("dialect");
            dialectField.setAccessible(true);
            dialectField.set(hibernateDatabase, new PostgreSQLDialect());
        } catch (NoSuchFieldException | IllegalAccessException e) {
            Scope.getCurrentScope().getLog(getClass()).severe("Failed to set dialect via reflection", e);
        }

        return metadataSources.buildMetadata();
    }
}