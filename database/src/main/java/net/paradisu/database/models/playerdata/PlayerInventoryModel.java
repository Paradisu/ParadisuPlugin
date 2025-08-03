/*
 * The official plugin for the Paradisu server. Copyright (C) 2025 Paradisu. https://paradisu.net
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package net.paradisu.database.models.playerdata;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.time.Instant;
import java.util.UUID;

@Accessors(fluent = true)
@Getter
@Entity
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "PLAYER_INVENTORIES")
public class PlayerInventoryModel {
    @Id
    @GeneratedValue
    @Column(name = "id", nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_player", nullable = false)
    @Setter
    private PlayerModel player;

    @Setter
    @Column(name = "inventory")
    private byte[] inventory;

    @Setter
    @Column(name = "armor")
    private byte[] armor;

    @Setter
    @Column(name = "ender_chest")
    private byte[] enderChest;

    @Setter
    @Column(name = "off_hand")
    private byte[] offHand;

    @Setter
    @Column(name = "held_slot")
    private int heldSlot;

    @Setter
    @Column(name = "context")
    private String context;

    @Setter
    @Column(name = "created", columnDefinition = "TIMESTAMP", nullable = false)
    private Instant created;
}
