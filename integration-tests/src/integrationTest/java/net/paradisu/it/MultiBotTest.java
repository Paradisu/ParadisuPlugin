package net.paradisu.it;

import net.paradisu.mcdevtools.testkit.bot.Bot;
import net.paradisu.mcdevtools.testkit.junit.Bots;
import net.paradisu.mcdevtools.testkit.junit.McAssertions;
import net.paradisu.mcdevtools.testkit.junit.McDevTest;
import net.paradisu.mcdevtools.testkit.junit.Network;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Several players at once. */
@McDevTest
class MultiBotTest {
    @Test
    void threeBotsAreOnlineTogether(Network network, Bots bots) {
        var joined = bots.joinAll(3, "itm");
        assertEquals(3, joined.stream().filter(Bot::inGame).count());
        McAssertions.assertConsole(network.instance(joined.get(0).server()), "list", "There are 3 of a max");
    }
}
