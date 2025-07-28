package net.paradisu.database;

public class Models {
    public static Class<?>[] models() {
        return new Class<?>[] {
            net.paradisu.database.models.playerdata.PlayerModel.class,
            net.paradisu.database.models.playerdata.PlayerServerSessionModel.class,
            net.paradisu.database.models.playerdata.PlayerProxySessionModel.class,
            net.paradisu.database.models.playerdata.PlayerInventoryModel.class,
            net.paradisu.database.models.WarpModel.class
        };
    }
}
