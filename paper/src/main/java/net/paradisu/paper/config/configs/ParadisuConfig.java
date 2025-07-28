package net.paradisu.paper.config.configs;

import lombok.Getter;
import lombok.experimental.Accessors;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Setting;

@Accessors(fluent = true)
@Getter
@ConfigSerializable
public final class ParadisuConfig {
    private Context context = new Context();
    private Database database = new Database();

    @Getter
    @ConfigSerializable
    public static final class Database {
        @Setting("url") private String url = "";
        @Setting("username") private String username = "";
        @Setting("password") private String password = "";
    }

    @Getter
    @ConfigSerializable
    public static final class Context {
        @Setting("server") private String server = "paradisu";
        @Setting("warp") private String warp = "paradisu";
        @Setting("inventory") private String inventory = "paradisu";
    }
}
