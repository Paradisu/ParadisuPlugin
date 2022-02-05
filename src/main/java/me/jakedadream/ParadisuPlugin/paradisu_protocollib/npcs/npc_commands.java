package me.jakedadream.ParadisuPlugin.paradisu_protocollib.npcs;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Cat;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class npc_commands implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if (!(sender instanceof Player)) { return false; }
        Player player = (Player) sender;

        if (command.getName().equals("spawncat")) {

            player.getLineOfSight(null, 10).stream().filter(block ->  block.getType() != Material.AIR).forEach(block -> {

                Location block_location = block.getLocation();
                UUID uuid = UUID.randomUUID();


                ProtocolManager pcmanager = ProtocolLibrary.getProtocolManager();



                PacketContainer entitypacket = pcmanager.createPacket(PacketType.Play.Server.SPAWN_ENTITY_LIVING);
                // Still need to set entities
                entitypacket.getDoubles().write(0, block_location.getX());
                entitypacket.getDoubles().write(1, block_location.getY());
                entitypacket.getDoubles().write(2, block_location.getZ());
                entitypacket.getUUIDs().write(0, uuid);


            });


        }






        return false;
    }

}