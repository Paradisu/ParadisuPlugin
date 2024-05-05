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
    @Setting("resource-pack-urls") private List<String> resourcePackUrls = List.of("https://github.com/Paradisu/ParadisuResourcePack/releases/latest/download/ParadisuResourcePack.zip");
}