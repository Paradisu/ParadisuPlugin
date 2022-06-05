package net.paradisu.paradisuplugin.bukkit.warps;

import java.util.ArrayList;

public class Warp {
    private int id;
    private int xPos;
    private int yPos;
    private int zPos;
    private double pitch;
    private double yaw;
    private String world;
    private String permission;
    private String displayName;
    private String name;
    private ArrayList<String> aliases;


    
    public Warp(int id, int xPos, int yPos , int zPos, double pitch, double yaw, String world, String permission, String displayName, String name){
        this.id = id;
        this.xPos = xPos;
        this.yPos = yPos;
        this.zPos = zPos;
        this.pitch = pitch;
        this.yaw = yaw;
        this.world = world;
        this.permission = permission;
        this.displayName = displayName;
        this.name = name;
        aliases = new ArrayList<String>();
    }

    public Warp(int id, int xPos, int yPos, int zPos, double pitch, double yaw, String world, String permission, String displayname, String name, String[] aliases){
        this(id, xPos, yPos, zPos, pitch, yaw, world, permission, displayname, name);
        for(String alias : aliases){
            this.aliases.add(alias);
        }
    }

    public int getId() {
        return id;
    }
    public int getXPos() {
        return xPos;
    }
    public int getYPos() {
        return yPos;
    }
    public int getZPos() {
        return zPos;
    }
    public double getPitch() {
        return pitch;
    }
    public double getYaw() {
        return yaw;
    }
    public String getWorld() {
        return world;
    }
    public String getPermission() {
        return permission;
    }
    public String getDisplayName() {
        return displayName;
    }
    public String getName() {
        return name;
    }
    public ArrayList<String> getAliases(){
        return aliases;
    }

    public void addAlias(String alias){
        aliases.add(alias);
    }


    @Override
    public String toString(){
        return "Warp: " + name + " | " + displayName + " | " + permission + " | " + world + " | " + xPos + " | " + yPos + " | " + zPos + " | " + pitch + " | " + yaw;
    }

}