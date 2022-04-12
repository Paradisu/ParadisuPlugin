package me.jakedadream.ParadisuPlugin.databaseHandlers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import me.jakedadream.ParadisuPlugin.ParadisuMain;

public class WarpsDataHandler {

    private static ResultSet rs;
    private static Connection connection;
    
    public static ResultSet getWarpData(){
        if(rs == null){
            try(Connection conn = ParadisuMain.getDBCon().getConnection(); PreparedStatement ps = conn.prepareStatement("SELECT * FROM Warps", ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY)){
                rs = ps.executeQuery();
            } catch (SQLException e){
                e.printStackTrace();
            }
        }
        return rs;
    }

    public static void updateWarpData(){
        try(Connection conn = ParadisuMain.getDBCon().getConnection();PreparedStatement ps = conn.prepareStatement("SELECT * FROM Warps")){
         
            rs = ps.executeQuery();
        } catch (SQLException e){
            e.printStackTrace();
        }
    }
}