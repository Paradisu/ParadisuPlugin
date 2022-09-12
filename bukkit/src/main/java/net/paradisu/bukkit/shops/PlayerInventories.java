package net.paradisu.bukkit.shops;

import net.paradisu.bukkit.ParadisuMain;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
public class PlayerInventories {


    String cmdprefix = ParadisuMain.CommandPrefix();
    String cmdemph = ParadisuMain.CommandEmph();



    public boolean hasSpace(Player player) {
        PlayerInventory inv = player.getInventory();
        int firstEmpty = inv.firstEmpty();
        return firstEmpty != -1;
    }

    public int getGoldNuggets(Player player){
        int runningCurreny = 0;
        PlayerInventory inv = player.getInventory();
        for (ItemStack content : inv.getContents()) {
            if (content != null){
                if (content.getType() == Material.GOLD_NUGGET){
                    int n = content.getAmount();
                    runningCurreny += n;
                }
            }
        }
        return runningCurreny;
    }

    public void spendGoldNuggets(Player player, int remainingCurrency){
        PlayerInventory inv = player.getInventory();
        for (ItemStack content : inv.getContents()){
            if (content.getType() == Material.GOLD_NUGGET){
                int amount = content.getAmount();
                if (amount < remainingCurrency){
                    content.setAmount(0);
                    remainingCurrency -= amount;
                } else if (amount == remainingCurrency){
                    content.setAmount(0);
                    break;
                } else {    
                    content.setAmount(amount - remainingCurrency);
                    break;
                }
            }
        }
    }
}
