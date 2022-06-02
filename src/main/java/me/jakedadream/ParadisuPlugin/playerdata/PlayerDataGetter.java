package me.jakedadream.ParadisuPlugin.playerdata;

import org.bukkit.entity.Player;

public class PlayerDataGetter {

    public String GetPlayerTopRank(Player player) {
        String TOP_RANK = "";


        if (player.hasPermission("meta.rank.owner")) {
            TOP_RANK = "meta.rank.owner";
        } else if (player.hasPermission("meta.rank.dev")) {
            TOP_RANK = "meta.rank.dev";
        } else if (player.hasPermission("meta.rank.builder")) {
            TOP_RANK = "meta.rank.builder";
        } else if (player.hasPermission("meta.group.ae")) {
            TOP_RANK = "meta.rank.ae";
        } else if (player.hasPermission("meta.rank.gd")) {
            TOP_RANK = "meta.rank.gd";
        } else if (player.hasPermission("meta.rank.modeler")) {
            TOP_RANK = "meta.rank.modeler";
        } else if (player.hasPermission("meta.rank.mod")) {
            TOP_RANK = "meta.rank.mod";
        } else if (player.hasPermission("meta.rank.ta")) {
            TOP_RANK = "meta.rank.ta";
        } else if (player.hasPermission("meta.rank.staff")) {
            TOP_RANK = "meta.rank.staff";
        } else if (player.hasPermission("meta.rank.supporter")) {
            TOP_RANK = "meta.rank.supporter";
        } else if (player.hasPermission("meta.rank.alt")) {
            TOP_RANK = "meta.rank.alt";
        } else if (player.hasPermission("meta.rank.visitor")) {
            TOP_RANK = "meta.rank.visitor";
        } else {
            TOP_RANK = "meta.rank.visitor";
        }
        return TOP_RANK;
    }

    public Boolean isStaffCheck(Player player) {

        Boolean isStaff = false;

        if (player.hasPermission("group.staff")) {
            isStaff = true;
        }

        return isStaff;
    }




}
