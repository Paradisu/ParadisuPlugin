package me.jakedadream.ParadisuPlugin.paradisu_mysql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class WarpsDataHandler {

    private static ResultSet rs;
    private static Connection connection;
    
    public static ResultSet getWarpData(){
        if(rs == null){
            try{
                
                if(connection == null) connection = DBConnections.ParadisuSQLCon();
                PreparedStatement ps = connection.prepareStatement("SELECT * FROM Warps", ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
                rs = ps.executeQuery();
            } catch (SQLException e){
                e.printStackTrace();

            }

        }
        return rs;
    }

    public static ResultSet updateWarpData(){
        try{
                
            if(connection == null) connection = DBConnections.ParadisuSQLCon();
            PreparedStatement ps = connection.prepareStatement("SELECT * FROM Warps");
            rs = ps.executeQuery();
        } catch (SQLException e){
            e.printStackTrace();

        }
        return rs;
    }

}