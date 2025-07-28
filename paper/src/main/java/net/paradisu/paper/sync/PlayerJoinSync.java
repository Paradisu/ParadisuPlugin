package net.paradisu.paper.sync;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import net.paradisu.database.models.playerdata.PlayerInventoryModel;
import net.paradisu.database.models.playerdata.PlayerModel;
import net.paradisu.database.models.playerdata.PlayerServerSessionModel;
import net.paradisu.paper.ParadisuPaper;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.time.Instant;

public class PlayerJoinSync implements Runnable {
        private final ParadisuPaper paradisu;
        private final Player player;
        private final Instant calltime;

        public PlayerJoinSync(ParadisuPaper paradisu, Player player) {
            this.paradisu = paradisu;
            this.player = player;
            this.calltime = Instant.now();

            player.getInventory().clear();
            player.getEnderChest().clear();
        }

        /**
         * Loads the player's inventory and session data when they join.
         */
        @Override
        public void run() {
            PlayerInventoryModel model = null;
            try (
                EntityManager entityManager = paradisu.databaseSession().factory().createEntityManager();
            ) {
                PlayerModel playerModel = entityManager.find(PlayerModel.class, player.getUniqueId());
                PlayerModel.PlayerModelBuilder builder;

                entityManager.getTransaction().begin();

                if (playerModel == null) {
                    builder = PlayerModel.builder()
                        .uuid(player.getUniqueId())
                        .firstJoined(calltime);
                    entityManager.persist(builder.build());
                } else {
                    builder = playerModel.toBuilder();
                }
                builder.lastJoined(calltime);
                entityManager.merge(builder.build());

                PlayerServerSessionModel sessionModel = PlayerServerSessionModel.builder()
                    .player(builder.build())
                    .joined(calltime)
                    .server(paradisu.paradisuConfig().context().server())
                    .build();
                entityManager.persist(sessionModel);
                entityManager.getTransaction().commit();

                model = entityManager.createQuery(
                    "SELECT p FROM PlayerInventoryModel p WHERE p.player.uuid = :uuid AND p.context = :context ORDER BY p.created DESC",
                    PlayerInventoryModel.class)
                    .setParameter("uuid", player.getUniqueId())
                    .setParameter("context", paradisu.paradisuConfig().context().inventory())
                    .setMaxResults(1)
                    .getSingleResult();
            } catch (NoResultException e) {
                return;
            } catch (Exception e) {
                Bukkit.getScheduler().runTask(paradisu, () ->
                    player.kick()
                );
                return;
            }

            final PlayerInventoryModel playerInventoryModel = model;

            Bukkit.getScheduler().runTask(paradisu, () -> {
                player.getInventory().setArmorContents(paradisu.safeItemSerializer().deserializeItemsFromBytes(playerInventoryModel.armor()));
                player.getInventory().setContents(paradisu.safeItemSerializer().deserializeItemsFromBytes(playerInventoryModel.inventory()));
                player.getInventory().setItemInOffHand(paradisu.safeItemSerializer().deserializeBytes(playerInventoryModel.offHand()));
                player.getInventory().setHeldItemSlot(playerInventoryModel.heldSlot());
                player.getEnderChest().setContents(paradisu.safeItemSerializer().deserializeItemsFromBytes(playerInventoryModel.enderChest()));
            });
        }
}
