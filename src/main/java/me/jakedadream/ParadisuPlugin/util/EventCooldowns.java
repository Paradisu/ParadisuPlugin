package me.jakedadream.ParadisuPlugin.util;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class EventCooldowns {

    Map<UUID, Long> cooldowns = new HashMap<UUID, Long>();

    public void PutPlayerOnCooldown(UUID uuid, Long mili) {
        cooldowns.put(uuid, System.currentTimeMillis() + (mili));
    }

    public boolean CheckPlayerOnCooldown(UUID uuid) {
        if (cooldowns.containsKey(uuid)) {
            if (cooldowns.get(uuid) > System.currentTimeMillis()) {
                return false;
            }
        }
        return true;
    }

    public Long TimeLeftOnCooldown(UUID uuid) {
        Long time_left = (cooldowns.get(uuid) - System.currentTimeMillis());

        return time_left;
    }

}
