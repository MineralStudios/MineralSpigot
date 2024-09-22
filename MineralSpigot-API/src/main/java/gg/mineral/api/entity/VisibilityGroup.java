package gg.mineral.api.entity;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class VisibilityGroup {
    private final Map<UUID, Boolean> uuids = new HashMap<>();

    public boolean canSee(UUID p1, UUID p2) {
        return uuids.containsKey(p1) && uuids.containsKey(p2);
    }

    public boolean canSeeOnTab(UUID player) {
        return uuids.getOrDefault(player, false);
    }

    public void addUUID(UUID player, boolean visibleOnTab) {
        Player p = Bukkit.getPlayer(player);

        if (p == null)
            return;
        if (p.getVisibilityGroup() != null)
            p.getVisibilityGroup().removeUUID(player);
        p.setVisibilityGroup(this);

        for (Entry<UUID, Boolean> e : uuids.entrySet()) {
            if (e.getValue()) {
                Player p2 = Bukkit.getPlayer(e.getKey());
                if (p2 != null) {
                    p.showPlayer(p2);
                    p2.showPlayer(p);
                }
            }
        }

        uuids.put(player, visibleOnTab);
    }

    public void removeUUID(UUID player) {

        Player p = Bukkit.getPlayer(player);

        if (p != null) {
            p.setVisibilityGroup(null);
            for (Entry<UUID, Boolean> e : uuids.entrySet()) {
                if (e.getValue()) {
                    Player p2 = Bukkit.getPlayer(e.getKey());
                    if (p2 != null) {
                        p.hidePlayer(p2, !canSeeOnTab(e.getKey()));
                        p2.hidePlayer(p, !canSeeOnTab(player));
                    }
                }
            }
        }

        uuids.remove(player);
    }

}
