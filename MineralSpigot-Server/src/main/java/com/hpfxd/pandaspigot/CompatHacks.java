package com.hpfxd.pandaspigot;

import org.bukkit.Bukkit;

public class CompatHacks {
    private CompatHacks() {}
    public static boolean hasProtocolSupport() {
        // Remove due to blocking netty threads when checking for plugin.
        return false; /*Bukkit.getPluginManager().isPluginEnabled("ProtocolSupport");*/
    }
}

