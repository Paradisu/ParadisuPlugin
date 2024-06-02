package net.paradisu.database.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.time.Instant;
import java.util.UUID;

@Accessors(fluent = true)
@Getter
@Entity
@Table(name = "PLAYERS")
public class PlayerModel {
    public PlayerModel() {
    }

    @Id @Setter @Column(name = "uuid", nullable = false)
    private UUID uuid;

    @Setter @Column(name = "display_name")
    private String displayName;

    @Setter @Column(name = "first_joined", columnDefinition = "TIMESTAMP")
    private Instant firstJoined;

    @Setter @Column(name = "last_joined", columnDefinition = "TIMESTAMP")
    private Instant lastJoined;
}
