package net.paradisu.ParadisuPlugin.warps;

public class WarpAlias {
    private String Alias;
    private int warpId;

    public WarpAlias(String alias, int warpId) {
        this.Alias = alias;
        this.warpId = warpId;
    }

    public String getAlias() {
        return Alias;
    }
    public int getWarpId() {
        return warpId;
    }

    
}
