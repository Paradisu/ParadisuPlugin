package net.paradisu.core.config;

public interface ConfigManager {
    public void loadConfigs();
    public <T> T getConfig(String name, Class<T> type);
}