package net.paradisu.paradisuplugin.velocity;

import com.google.inject.Inject;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;
import org.slf4j.Logger;

@Plugin(id = "ourfirstplugin", name = "Our First Plugin", version = "1.0-SNAPSHOT",
        description = "I did it!", authors = {"Us"})
public class VelocityTest {
    private final ProxyServer server;
    private final Logger logger;

    @Inject
    public VelocityTest(ProxyServer server, Logger logger) {
        this.server = server;
        this.logger = logger;

        logger.info("Hello there, it's a test plugin we made!");
    }
}