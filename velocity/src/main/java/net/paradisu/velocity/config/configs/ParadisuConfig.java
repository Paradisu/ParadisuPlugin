package net.paradisu.velocity.config.configs;

import lombok.Getter;
import lombok.experimental.Accessors;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Setting;

import java.util.List;

@Accessors(fluent = true)
@Getter
@ConfigSerializable
public final class ParadisuConfig {
    @Setting("resource-pack-urls") 
    private List<String> resourcePackUrls = List.of("https://github.com/Paradisu/ParadisuResourcePack/releases/latest/download/ParadisuResourcePack.zip");

    private Database database = new Database();

    @Getter
    @ConfigSerializable
    public static final class Database {
        @Setting("url") private String url = "";
        @Setting("username") private String username = "";
        @Setting("password") private String password = "";
    }

    private LimboServer limboServer = new LimboServer();

    @Getter
    @ConfigSerializable
    public static final class LimboServer {
        @Setting("host") private String host = "172.18.0.1";
        @Setting("port") private int port = 30000;
    }

    private TransferReturnTarget transferReturnTarget = new TransferReturnTarget();

    @Getter
    @ConfigSerializable
    public static final class TransferReturnTarget {
        @Setting("host") private String host = "paradisu.net";
        @Setting("port") private int port = 25565;
    }
}