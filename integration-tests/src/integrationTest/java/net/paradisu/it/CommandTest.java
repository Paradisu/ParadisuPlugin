package net.paradisu.it;

import net.paradisu.mcdevtools.testkit.bot.Bot;
import net.paradisu.mcdevtools.testkit.bot.BotOptions;
import net.paradisu.mcdevtools.testkit.env.TestEnvironment.Via;
import net.paradisu.mcdevtools.testkit.junit.Bots;
import net.paradisu.mcdevtools.testkit.junit.McAssertions;
import net.paradisu.mcdevtools.testkit.junit.McDevTest;
import net.paradisu.mcdevtools.testkit.junit.Network;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/** A player command answers in chat with the text of the language file, behind its permission. */
@McDevTest
class CommandTest {
    private static final String PLAYER = "itCmd";

    /** The English text of a message key, read from the plugin's own language file so the test follows edits. */
    private static String english(String key) throws IOException {
        Properties properties = new Properties();
        Path file = Path.of("..", "core", "src", "main", "resources", "messages_en.properties");
        try (var in = Files.newBufferedReader(file)) {
            properties.load(in);
        }
        return properties.getProperty(key);
    }

    @AfterEach
    void cleanUp(Network network) {
        network.instance("lobby").console("lp user " + PLAYER + " clear");
    }

    @Test
    void hatReplacesTheHelmetAndSaysSo(Network network, Bots bots) throws IOException {
        Bot bot = bots.join(BotOptions.named(PLAYER).target("lobby").via(Via.DIRECT));
        McAssertions.assertConsole(
                network.instance("lobby"), "lp user " + PLAYER + " permission set paradisu.hat true", "paradisu.hat");
        McAssertions.assertChat(
                bot, java.util.regex.Pattern.quote(english("paradisu.command.output.hat")), () -> bot.command("hat"));
    }
}
