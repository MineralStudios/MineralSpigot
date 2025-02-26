package org.bukkit.craftbukkit.scoreboard;

import java.util.*;

import org.apache.commons.lang.Validate;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.craftbukkit.util.WeakCollection;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.ScoreboardManager;

import gg.mineral.server.config.GlobalConfig;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.server.EntityPlayer;
import net.minecraft.server.IScoreboardCriteria;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PacketPlayOutScoreboardObjective;
import net.minecraft.server.PacketPlayOutScoreboardTeam;
import net.minecraft.server.Scoreboard;
import net.minecraft.server.ScoreboardObjective;
import net.minecraft.server.ScoreboardScore;
import net.minecraft.server.ScoreboardServer;
import net.minecraft.server.ScoreboardTeam;

public final class CraftScoreboardManager implements ScoreboardManager {
    private final CraftScoreboard mainScoreboard;
    private final MinecraftServer server;
    private final Collection<CraftScoreboard> scoreboards = new WeakCollection<CraftScoreboard>();
    private final Map<UUID, CraftScoreboard> playerBoards = new Object2ObjectOpenHashMap<>();

    public CraftScoreboardManager(MinecraftServer minecraftserver, net.minecraft.server.Scoreboard scoreboardServer) {
        mainScoreboard = new CraftScoreboard(scoreboardServer);
        mainScoreboard.registeredGlobally = true; // PandaSpigot
        server = minecraftserver;
        scoreboards.add(mainScoreboard);
    }

    public CraftScoreboard getMainScoreboard() {
        return mainScoreboard;
    }

    public CraftScoreboard getNewScoreboard() {
        org.spigotmc.AsyncCatcher.catchOp("scoreboard creation"); // Spigot
        CraftScoreboard scoreboard = new CraftScoreboard(new ScoreboardServer(server));
        // PandaSpigot start
        if (GlobalConfig.getInstance().isTrackPluginScoreboards()) {
            scoreboard.registeredGlobally = true;
            scoreboards.add(scoreboard);
        }
        // PandaSpigot end
        return scoreboard;
    }

    // PandaSpigot start
    public void registerScoreboardForVanilla(CraftScoreboard scoreboard) {
        org.spigotmc.AsyncCatcher.catchOp("scoreboard registration");
        this.scoreboards.add(scoreboard);
    }
    // PandaSpigot end

    // CraftBukkit method
    public CraftScoreboard getPlayerBoard(CraftPlayer player) {
        CraftScoreboard board = playerBoards.get(player.getUniqueId());
        return (CraftScoreboard) (board == null ? getMainScoreboard() : board);
    }

    // CraftBukkit method
    public void setPlayerBoard(CraftPlayer player, org.bukkit.scoreboard.Scoreboard bukkitScoreboard)
            throws IllegalArgumentException {
        Validate.isTrue(bukkitScoreboard instanceof CraftScoreboard,
                "Cannot set player scoreboard to an unregistered Scoreboard");

        CraftScoreboard scoreboard = (CraftScoreboard) bukkitScoreboard;
        net.minecraft.server.Scoreboard oldboard = getPlayerBoard(player).getHandle();
        net.minecraft.server.Scoreboard newboard = scoreboard.getHandle();
        EntityPlayer entityplayer = player.getHandle();

        if (oldboard == newboard) {
            return;
        }

        if (scoreboard == mainScoreboard) {
            playerBoards.remove(player.getUniqueId());
        } else {
            playerBoards.put(player.getUniqueId(), scoreboard);
        }

        // Old objective tracking
        HashSet<ScoreboardObjective> removed = new HashSet<ScoreboardObjective>();
        for (int i = 0; i < 3; ++i) {
            ScoreboardObjective scoreboardobjective = oldboard.getObjectiveForSlot(i);
            if (scoreboardobjective != null && !removed.contains(scoreboardobjective)) {
                entityplayer.playerConnection.sendPacket(new PacketPlayOutScoreboardObjective(scoreboardobjective, 1));
                removed.add(scoreboardobjective);
            }
        }

        // Old team tracking
        Iterator<?> iterator = oldboard.getTeams().iterator();
        while (iterator.hasNext()) {
            ScoreboardTeam scoreboardteam = (ScoreboardTeam) iterator.next();
            entityplayer.playerConnection.sendPacket(new PacketPlayOutScoreboardTeam(scoreboardteam, 1));
        }

        // The above is the reverse of the below method.
        server.getPlayerList().sendScoreboard((ScoreboardServer) newboard, player.getHandle());
    }

    // CraftBukkit method
    public void removePlayer(Player player) {
        playerBoards.remove(player.getUniqueId());
    }

    // CraftBukkit method
    public Collection<ScoreboardScore> getScoreboardScores(IScoreboardCriteria criteria, String name,
                                                           Collection<ScoreboardScore> collection) {
        for (CraftScoreboard scoreboard : scoreboards) {
            Scoreboard board = scoreboard.board;
            for (ScoreboardObjective objective : board
                    .getObjectivesForCriteria(criteria)) {
                collection.add(board.getPlayerScoreForObjective(name, objective));
            }
        }
        return collection;
    }

    // CraftBukkit method
    public void updateAllScoresForList(IScoreboardCriteria criteria, String name, List<EntityPlayer> of) {
        for (ScoreboardScore score : getScoreboardScores(criteria, name, new ArrayList<ScoreboardScore>())) {
            score.updateForList((List) of);
        }
    }
}
