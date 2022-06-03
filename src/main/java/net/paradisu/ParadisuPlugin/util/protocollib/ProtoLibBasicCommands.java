package net.paradisu.ParadisuPlugin.util.protocollib;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import net.paradisu.ParadisuPlugin.ParadisuMain;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ProtoLibBasicCommands implements CommandExecutor {

    ProtocolManager pcmanager = ProtocolLibrary.getProtocolManager();

    String cmdprefix = ParadisuMain.CommandPrefix();
    String cmdemph = ParadisuMain.CommandEmph();
    String nopermsmsg = ParadisuMain.NoPermsMessage();
    String noargsmsg = ParadisuMain.NoArgsMessage();

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if (!(sender instanceof Player)) { return false; }
        //Player player = (Player) sender;

        /*
        switch (command.getName().toLowerCase()) {

            case "entitycontrol": // Thank you pocketkid2
                if (player.hasPermission("snw.entitycontrol")) {

                    RayTraceResult Get_Looked_At = player.getWorld().rayTraceEntities(player.getEyeLocation().add(player.getLocation().getDirection()), player.getLocation().getDirection(), 10.0D);

                    if (Get_Looked_At != null && Get_Looked_At.getHitEntity() != null) {
                        EntityType type = Get_Looked_At.getHitEntity().getType();
                        if (!(type == EntityType.ARMOR_STAND || type == EntityType.LEASH_HITCH)) {

                            Get_Looked_At.getHitEntity().addPassenger(player);
                            player.sendMessage(cmdprefix + "§fNow riding on a " + cmdemph + type + "§f.");

                        } else { player.sendMessage(cmdprefix + "§fYou're trying to ride on a disallowed entity."); }
                    } else { player.sendMessage(cmdprefix + "§fWe didn't find the target you're looking at."); }
                } else { player.sendMessage(nopermsmsg); }

        }
*/
        return false;


    }
}
