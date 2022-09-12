package net.paradisu.bukkit.items.common.menu;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import net.paradisu.bukkit.playerdata.PlayerDataGetter;

public class ParadisuHead extends ItemStack {
    public ParadisuHead (Player player){
        super(Material.PLAYER_HEAD, 1);
        SkullMeta meta = (SkullMeta) this.getItemMeta();
        meta.setOwningPlayer(player);
        switch(PlayerDataGetter.GetPlayerTopRank(player)){
            case "meta.rank.owner" -> meta.setDisplayName("§3\ue006 §f" + player.getName());
            case "meta.rank.dev" -> meta.setDisplayName("§x§f§8§9§9§1§d\ue002 §f" + player.getName());
            case "meta.rank.builders" -> meta.setDisplayName("§x§f§3§6§c§3§6\ue001 §f" + player.getName());
            case "meta.rank.staff" -> meta.setDisplayName("§3\ue007 §f" + player.getName());
            case "meta.rank.supporters" -> meta.setDisplayName("§d\ue008 §f" + player.getName());
            default -> meta.setDisplayName("§7\ue00a §f" + player.getName());
        }
        this.setItemMeta(meta);
    }
}
