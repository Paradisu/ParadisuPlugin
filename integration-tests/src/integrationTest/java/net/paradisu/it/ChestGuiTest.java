package net.paradisu.it;

import net.paradisu.mcdevtools.testkit.bot.Bot;
import net.paradisu.mcdevtools.testkit.bot.BotOptions;
import net.paradisu.mcdevtools.testkit.bot.screen.Screen;
import net.paradisu.mcdevtools.testkit.env.TestEnvironment.Via;
import net.paradisu.mcdevtools.testkit.junit.Bots;
import net.paradisu.mcdevtools.testkit.junit.ManualReview;
import net.paradisu.mcdevtools.testkit.junit.McAssertions;
import net.paradisu.mcdevtools.testkit.junit.McDevTest;
import net.paradisu.mcdevtools.testkit.junit.Network;
import org.geysermc.mcprotocollib.protocol.data.game.entity.object.Direction;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** A container screen: open a chest, read its slots, take an item with a shift-click. */
@McDevTest
class ChestGuiTest {
    /**
     * A fresh player name per run: Paradisu stores inventories by player and restores them shortly after the join, so a
     * reused name would bring back the items an earlier run left behind (and `clear` at the start can race with that
     * restore).
     */
    private final String player =
            "itGui" + Integer.toHexString(ThreadLocalRandom.current().nextInt(0x100000, 0xFFFFFF));

    private int[] chest;

    @AfterEach
    void cleanUp(Network network) {
        var lobby = network.instance("lobby");
        if (chest != null) {
            lobby.console("setblock %d %d %d air".formatted(chest[0], chest[1], chest[2]));
        }
    }

    @Test
    @ManualReview("The chest opens in front of the player and the diamonds land in their inventory")
    void takingItemsFromAChest(Network network, Bots bots) {
        // Join first: a chunk is only loaded around a player, and the spawn point varies
        Bot bot = bots.join(BotOptions.named(player).target("lobby").via(Via.DIRECT));
        double[] at = bot.position();
        chest = new int[] {(int) Math.floor(at[0]) + 1, (int) Math.floor(at[1]), (int) Math.floor(at[2])};
        String pos = "%d %d %d".formatted(chest[0], chest[1], chest[2]);

        var lobby = network.instance("lobby");
        McAssertions.assertConsole(lobby, "setblock " + pos + " chest", "Changed the block");
        McAssertions.assertConsole(lobby, "item replace block " + pos + " container.0 with diamond 5", "Replaced");

        bot.interactBlock(chest[0], chest[1], chest[2], Direction.UP);
        Screen screen = bot.awaitScreen(s -> s.type().equals("GENERIC_9X3"), Duration.ofSeconds(10));
        assertEquals("minecraft:diamond", screen.slot(0).id());
        assertEquals(5, screen.slot(0).count());

        // The server answers a click with several updates (the emptied slot, then the slot that received the stack),
        // so wait for the end state instead of reading right after the first update
        bot.click(0, "shift");
        screen.await(
                s -> s.slot(0) == null && s.count("minecraft:diamond") == 5,
                5000,
                "the stack to move from the chest into the player's slots");
    }
}
