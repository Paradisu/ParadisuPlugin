package net.paradisu.it;

import net.paradisu.mcdevtools.testkit.junit.McAssertions;
import net.paradisu.mcdevtools.testkit.junit.McDevTest;
import net.paradisu.mcdevtools.testkit.junit.Network;
import org.junit.jupiter.api.Test;

/** The plugin loads on the proxy and answers on its console. */
@McDevTest
class ProxyAboutTest {
    @Test
    void aboutNamesThePlugin(Network network) {
        McAssertions.assertConsole(network.instance("proxy"), "vparadisu about", "Paradisu Velocity Plugin");
    }
}
