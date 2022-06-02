package me.jakedadream.ParadisuPlugin.items.common.menu;

import java.util.ArrayList;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffectType;

public class ParadisuEffects extends ItemStack{
    public ParadisuEffects(Player player){
        super(Material.SLIME_BALL, 1);
        ItemMeta meta = this.getItemMeta();
        meta.setDisplayName("§a§lPlayer Potion Effects");
        ArrayList<String> lore = new ArrayList<String>();
        lore.add("§f§o");
        if(player.hasPotionEffect(PotionEffectType.SPEED)) lore.add("§f§oSpeed§3 \uE00D §f§l" + (player.getPotionEffect(PotionEffectType.SPEED).getAmplifier() + 1));
        if(player.hasPotionEffect(PotionEffectType.JUMP)) lore.add("§f§oJump§3 \uE00D §f§l" + (player.getPotionEffect(PotionEffectType.JUMP).getAmplifier() + 1));
        if(player.hasPotionEffect(PotionEffectType.INVISIBILITY)) lore.add("§f§oInvisibility§3 \uE00D §f§l" + (player.getPotionEffect(PotionEffectType.INVISIBILITY).getAmplifier() + 1));
        if(lore.size() == 1) lore.add("§f§oNo effects");
        meta.setLore(lore);
        this.setItemMeta(meta);
    }
}
