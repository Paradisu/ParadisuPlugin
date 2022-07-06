package net.paradisu.paradisuplugin.velocity.commands.util;

import com.velocitypowered.api.proxy.Player;

public class TeleportRequestHeader {
    private Player requestor;
    private Player requestee;

    public void setRequestHeader(Player requestor, Player requestee) {
        this.requestor = requestor;
        this.requestee = requestee;
    }

    public Player getRequestor() {
        return requestor;
    }

    public Player getRequestee() {
        return requestee;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((requestee == null) ? 0 : requestee.getUniqueId().hashCode());
        result = prime * result + ((requestor == null) ? 0 : requestor.getUniqueId().hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        TeleportRequestHeader other = (TeleportRequestHeader) obj;
        if (requestee == null) {
            if (other.requestee != null)
                return false;
        } else if (!requestee.getUniqueId().equals(other.requestee.getUniqueId()))
            return false;
        if (requestor == null) {
            if (other.requestor != null)
                return false;
        } else if (!requestor.getUniqueId().equals(other.requestor.getUniqueId()))
            return false;
        return true;
    }
}
