package net.paradisu.database.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.UUID;

@Accessors(fluent = true)
@Getter
@Entity
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "WARPS")
public class WarpModel {
    @Id
    @GeneratedValue
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "context", nullable = false)
    private String context;

    @Column(name = "x", nullable = false)
    float x;

    @Column(name = "y", nullable = false)
    float y;

    @Column(name = "z", nullable = false)
    float z;

    @Column(name = "yaw", nullable = false)
    float yaw;

    @Column(name = "pitch", nullable = false)
    float pitch;

    @Column(name = "permission")
    String permssion;
}
