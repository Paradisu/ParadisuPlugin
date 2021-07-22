package me.jakedadream.ParadisuPlugin.events;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class chatevents implements Listener {




    // ====================================================
    // TABLE FLIP
    // ====================================================
    @EventHandler
    public static void TableFlipEvent1(AsyncPlayerChatEvent pchat) {
        String msg = pchat.getMessage();
            msg = msg.replace(":tableflip:", "§d(╯°□°）╯︵ ┻━┻§f");
            pchat.setMessage(msg);
    }
    //
    //
    // ====================================================
    // SHRUG
    // ====================================================
    @EventHandler
    public static void DabEvent1(AsyncPlayerChatEvent pchat) {
        pchat.getMessage().toString().toLowerCase();
        String msg = pchat.getMessage();
        msg = msg.replace(":shrug:", "§d¯⧹_(ツ)_/¯§f");
        pchat.setMessage(msg);
    }
    //
    //
    // ====================================================
    // HAPPY
    // ====================================================
    @EventHandler
    public static void HappyEvent1(AsyncPlayerChatEvent pchat) {
        pchat.getMessage().toString().toLowerCase();
        String msg = pchat.getMessage();
        msg = msg.replace(":happy:", "§d(^⌣^)§f");
        pchat.setMessage(msg);
    }
    //
    //
    // ====================================================
    // INTERROBANG
    // ====================================================
    @EventHandler
    public static void BangEvent1(AsyncPlayerChatEvent pchat) {
        pchat.getMessage().toString().toLowerCase();
        String msg = pchat.getMessage();
        msg = msg.replace(":interrobang:", "§4§l!?§f");
        pchat.setMessage(msg);
    }
    //
    //
    // ====================================================
    // WAVE
    // ====================================================
    @EventHandler
    public static void WaveEvent1(AsyncPlayerChatEvent pchat) {
        pchat.getMessage().toString().toLowerCase();
        String msg = pchat.getMessage();
        msg = msg.replace(":wave:", "§d(^⌣^)ノ§f");
        pchat.setMessage(msg);
    }
    //
    //
    // ====================================================
    // UNFLIP
    // ====================================================
    @EventHandler
    public static void UnflipEvent(AsyncPlayerChatEvent pchat) {
        pchat.getMessage().toString().toLowerCase();
        String msg = pchat.getMessage();
        msg = msg.replace(":unflip:", "§d┬─┬ノ( º _ ºノ)§f");
        pchat.setMessage(msg);
    }
    //
    //
    // ====================================================
    // WELCOME
    // ====================================================
    @EventHandler
    public static void welcomeevent(AsyncPlayerChatEvent pchat) {
        pchat.getMessage().toString().toLowerCase();
        String msg = pchat.getMessage();
        msg = msg.replace(":welcome:", "§dWelcome to Paradisu §bツ§f");
        pchat.setMessage(msg);
    }



    // add new above
}
