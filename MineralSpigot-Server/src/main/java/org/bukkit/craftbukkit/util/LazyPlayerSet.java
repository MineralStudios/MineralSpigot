package org.bukkit.craftbukkit.util;

import java.util.List;
import java.util.Set;

import org.bukkit.entity.Player;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.server.EntityPlayer;
import net.minecraft.server.MinecraftServer;

public class LazyPlayerSet extends LazyHashSet<Player> {

    @Override
    Set<Player> makeReference() {
        if (reference != null) {
            throw new IllegalStateException("Reference already created!");
        }
        List<EntityPlayer> players = MinecraftServer.getServer().getPlayerList().players;
        Set<Player> reference = new ObjectOpenHashSet<Player>(players.size());
        for (EntityPlayer player : players) {
            reference.add(player.getBukkitEntity());
        }
        return reference;
    }

}
