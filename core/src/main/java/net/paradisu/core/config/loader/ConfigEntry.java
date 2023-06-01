package net.paradisu.core.config.loader;

import org.spongepowered.configurate.ConfigurateException;

import java.nio.file.Path;

public class ConfigEntry<T> {
        private final ConfigLoader<T> configLoader;
        private T config;
        private final Class<T> type;

        public ConfigEntry(Class<T> configClass, Path path, String header) {
            this.configLoader = new ConfigLoader<>(
                    configClass,
                    path,
                    options -> options.header(header)
            );
            this.type = configClass;
        }

        public void loadConfig() throws ConfigurateException {
            this.config = this.configLoader.load();
            this.configLoader.save(this.config);
        }

        public T getConfig() {
            if (this.config == null) {
                throw new IllegalStateException("Config has not yet been loaded");
            }
            return this.config;
        }

        public Class<T> getType() {
            return type;
        }
}
