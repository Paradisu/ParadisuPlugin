package net.paradisu.paper.sync;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import net.paradisu.database.models.playerdata.PlayerInventoryModel;
import net.paradisu.database.models.playerdata.PlayerModel;
import net.paradisu.paper.ParadisuPaper;
import org.bukkit.entity.Player;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
public class PlayerQuitSync implements Runnable {
        private final ParadisuPaper paradisu;
        private final UUID playerUUID;
        private final byte[] inventoryBytes;
        private final byte[] armorBytes;
        private final byte[] offHandBytes;
        private final byte[] enderChestBytes;
        private final int heldSlot;
        private final Instant calltime;

        public PlayerQuitSync(ParadisuPaper paradisu, Player player) {
            this(paradisu,
                    new UUID(player.getUniqueId().getMostSignificantBits(), player.getUniqueId().getLeastSignificantBits()),
                    paradisu.safeItemSerializer().serializeItemsAsBytes(player.getInventory().getContents()),
                    paradisu.safeItemSerializer().serializeItemsAsBytes(player.getInventory().getArmorContents()),
                    paradisu.safeItemSerializer().serializeBytes(player.getInventory().getItemInOffHand()),
                    paradisu.safeItemSerializer().serializeItemsAsBytes(player.getEnderChest().getContents()),
                    player.getInventory().getHeldItemSlot(),
                    Instant.now());
        }

        /**
         * Saves the player's inventory and updates their session data when they quit.
         */
        @Override
        public void run() {
            try (
                EntityManager entityManager = paradisu.databaseSession().factory().createEntityManager();
            ) {
                entityManager.getTransaction().begin();

                PlayerModel playerModel = entityManager.find(PlayerModel.class, playerUUID);
                if (playerModel == null) {
                    return;
                }

                List<UUID> outdatedIds = entityManager.createQuery(
                    "SELECT p.id FROM PlayerInventoryModel p WHERE p.player.uuid = :uuid AND p.context = :context ORDER BY p.created DESC",
                    UUID.class)
                .setParameter("uuid", playerUUID)
                .setParameter("context", paradisu.paradisuConfig().context())
                .setFirstResult(16)
                .getResultList();

                if (!outdatedIds.isEmpty()) {
                    entityManager.createQuery("DELETE FROM PlayerInventoryModel p WHERE p.id IN :ids")
                        .setParameter("ids", outdatedIds)
                        .executeUpdate();
                }
    
                PlayerInventoryModel playerInventoryModel = PlayerInventoryModel.builder()
                        .player(playerModel)
                        .inventory(inventoryBytes)
                        .armor(armorBytes)
                        .offHand(offHandBytes)
                        .enderChest(enderChestBytes)
                        .heldSlot(heldSlot)
                        .created(calltime)
                        .context(paradisu.paradisuConfig().context().inventory())
                        .build();
    
                entityManager.persist(playerInventoryModel);

                entityManager.createQuery("UPDATE PlayerServerSessionModel p SET p.left = :left WHERE p.player.uuid = :uuid AND p.server = :server AND p.left IS NULL")
                        .setParameter("left", calltime)
                        .setParameter("uuid", playerUUID)
                        .setParameter("server", paradisu.paradisuConfig().context().server())
                        .executeUpdate();

                entityManager.merge(playerModel);

                entityManager.getTransaction().commit();
            }
        }
}
