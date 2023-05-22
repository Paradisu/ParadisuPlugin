package net.paradisu.core.config;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;

public interface MessagesConfig {
    public Utility utility();
    public Commands commands();

    @ConfigSerializable
    public static interface Utility {
        public String messageDivider();
        public String messagePrefix();
        public String listPrefixes(int index);
    }

    @ConfigSerializable
    public static interface Commands {}
}
