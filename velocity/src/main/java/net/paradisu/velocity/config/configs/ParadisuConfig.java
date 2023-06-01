package net.paradisu.velocity.config.configs;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Setting;

@ConfigSerializable
public final class ParadisuConfig {
    @Setting("testBool") private boolean testBool = true;
}
