package net.paradisu.database.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.experimental.Accessors;

import java.util.UUID;

@Accessors(fluent = true)
@Getter
@Entity
@Table(name = "WARPS")
public class WarpModel {
    public WarpModel() {
    }

    @Id @GeneratedValue @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "server_type", nullable = false)
    private String serverType;

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
