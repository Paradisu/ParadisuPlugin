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

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@Accessors(fluent = true)
@Getter
@Entity
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "PLAYERS")
public class PlayerModel {
    @Id
    @Setter
    @Column(name = "uuid", nullable = false)
    private UUID uuid;

    @Setter
    @Column(name = "display_name")
    private String displayName;

    @Setter
    @Column(name = "online")
    private boolean online;

    @Setter
    @Column(name = "first_joined", columnDefinition = "TIMESTAMP")
    private Instant firstJoined;

    @Setter
    @Column(name = "last_joined", columnDefinition = "TIMESTAMP")
    private Instant lastJoined;

    @Setter
    @Column(name = "last_seen", columnDefinition = "TIMESTAMP")
    private Instant lastSeen;

    @Setter
    @Column(name = "last_server")
    private String lastServer;

    @Setter
    @Column(name = "playtime")
    private long playtime;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "player", cascade = CascadeType.ALL, orphanRemoval = true)
    @Setter
    private Set<PlayerServerSessionModel> serverSessions;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "player", cascade = CascadeType.ALL, orphanRemoval = true)
    @Setter
    private Set<PlayerProxySessionModel> proxySessions;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "player", cascade = CascadeType.ALL, orphanRemoval = true)
    @Setter
    private Set<PlayerInventoryModel> inventories;
}
