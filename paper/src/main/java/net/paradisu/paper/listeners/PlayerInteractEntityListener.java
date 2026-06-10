package net.paradisu.paper.listeners;

import jakarta.persistence.EntityManager;
import lombok.AllArgsConstructor;
import net.kyori.adventure.text.Component;
import net.paradisu.database.models.playerdata.PlayerModel;
import net.paradisu.paper.ParadisuPaper;
import net.paradisu.paper.sync.PlayerJoinSync;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;

@AllArgsConstructor
public class PlayerInteractEntityListener implements Listener {
    private final ParadisuPaper paradisu;

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();

        if(event.getRightClicked().getType() != EntityType.MANNEQUIN) return;
        if(event.getHand() != EquipmentSlot.HAND) return;
        if(!event.getRightClicked().getName().equalsIgnoreCase("shoptest1")) return;

        long balance;
        try (EntityManager entityManager = paradisu.databaseSession().factory().createEntityManager()) {
            entityManager.getTransaction().begin();
            PlayerModel playerModel = entityManager.find(PlayerModel.class, player.getUniqueId());

            if(playerModel == null) {
                return;
            }

            balance = playerModel.balance();
            if(balance < 20) {
                player.sendMessage("This item costs 20, you only have " + balance);
            } else {
                playerModel.balance(balance - 20);
                player.sendMessage("New balance is now " + playerModel.balance());
                player.getInventory().addItem(new ItemStack(Material.DIAMOND));
            }
            entityManager.getTransaction().commit();
        }
    }
}
