package net.paradisu.bukkit.warps;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import org.bukkit.Bukkit;

import net.paradisu.bukkit.ParadisuMain;

public class WarpsDataHandler {

    private static ArrayList<Warp> warps = new ArrayList<Warp>();
    
    public static ArrayList<Warp> getWarpData(){
        //send message to console
        if(warps.size() == 0)
            updateWarpData();
        return warps;
    }

    public static void updateWarpData(){
        try(Connection conn = ParadisuMain.getDBCon().getConnection();PreparedStatement ps = conn.prepareStatement("SELECT * FROM Warps", ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY); ResultSet rs = ps.executeQuery()){
            //send message to console
            Bukkit.getConsoleSender().sendMessage("[Paradisu] Updating Warp Data...");
            warps.clear();
            readResultSet(rs);
            addAliases(conn);
            
        } catch (SQLException e){
            e.printStackTrace();
        }
    }

    private static void readResultSet(ResultSet rs){
        try{
            while(rs.next()){
                int id = rs.getInt("id");
                int xPos = rs.getInt("X_Pos");
                int yPos = rs.getInt("Y_Pos");
                int zPos = rs.getInt("Z_Pos");
                double pitch = rs.getDouble("Pitch");
                double yaw = rs.getDouble("Yaw");
                String world = rs.getString("World");
                String permission = rs.getString("Permission");
                String displayName = rs.getString("DisplayName");
                String name = rs.getString("WarpName");

                


                //creat warp wAdd
                Warp wAdd = new Warp(id, xPos, yPos, zPos, pitch, yaw, world, permission, displayName, name);
                // Bukkit.getConsoleSender().sendMessage(wAdd.toString());
                warps.add(wAdd);
            }
        } catch (SQLException e){
            e.printStackTrace();
        }
    }

    private static void addAliases(Connection conn){
        ArrayList<WarpAlias> aliases = new ArrayList<WarpAlias>();
        try(PreparedStatement ps = conn.prepareStatement("SELECT * FROM WarpAliases", ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY); ResultSet rs = ps.executeQuery()){
            while(rs.next()){
                int warpId = rs.getInt("warpId");
                String alias = rs.getString("warpAliasName");
                WarpAlias wa = new WarpAlias(alias, warpId);
                aliases.add(wa);
            }
        } catch (SQLException e){
            e.printStackTrace();
        }
        for(Warp w : warps){
            for(WarpAlias wa : aliases){
                if(w.getId() == wa.getWarpId()){
                    w.addAlias(wa.getAlias());
                }
            }
        }

    }
}