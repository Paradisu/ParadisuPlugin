package net.paradisu.it;

import net.paradisu.mcdevtools.testkit.bot.Bot;
import net.paradisu.mcdevtools.testkit.bot.BotOptions;
import net.paradisu.mcdevtools.testkit.env.TestEnvironment.Via;
import net.paradisu.mcdevtools.testkit.junit.Bots;
import net.paradisu.mcdevtools.testkit.junit.McAssertions;
import net.paradisu.mcdevtools.testkit.junit.McDevTest;
import net.paradisu.mcdevtools.testkit.junit.Network;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertTrue;

/** A player can join a backend, shows up in the server's player list, and nothing is logged as an error. */
@McDevTest
class JoinTest {
    @Test
    void aBotJoinsTheLobby(Network network, Bots bots) {
        Bot bot = bots.join(BotOptions.named("itJoin").target("lobby").via(Via.DIRECT));
        assertTrue(bot.inGame());
        McAssertions.assertConsole(network.instance("lobby"), "list", "itJoin");
    }

    @Test
    void aBotSeesItsOwnJoinMessage(Bots bots) {
        Bot bot = bots.join(BotOptions.named("itJoinMsg").target("lobby").via(Via.DIRECT));
        McAssertions.assertChat(bot, "itJoinMsg joined the game", Duration.ofSeconds(5), 0);
    }
}
