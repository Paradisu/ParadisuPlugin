package net.paradisu.database;

public class Models {
    public static Class<?>[] models() {
        return new Class<?>[] {
            net.paradisu.database.models.PlayerModel.class,
            net.paradisu.database.models.WarpModel.class
        };
    }
}
