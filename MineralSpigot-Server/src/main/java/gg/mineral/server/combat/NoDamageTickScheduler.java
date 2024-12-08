package gg.mineral.server.combat;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.bukkit.Bukkit;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import lombok.Getter;
import lombok.val;

public class NoDamageTickScheduler {
    @Getter
    private static final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    public static Thread THREAD;

    @Getter
    private static final Object2IntOpenHashMap<Runnable> tasks = new Object2IntOpenHashMap<>();

    static {
        scheduler.execute(() -> THREAD = Thread.currentThread());

        scheduler.scheduleAtFixedRate(() -> {
            try {
                for (val player : Bukkit.getOnlinePlayers())
                    if (player.getNoDamageTicks() > 0)
                        player.setNoDamageTicks(player.getNoDamageTicks() - 1);

                val iter = tasks.object2IntEntrySet().fastIterator();

                while (iter.hasNext()) {
                    val entry = iter.next();
                    if (entry.getIntValue() > 0)
                        entry.setValue(entry.getIntValue() - 1);
                    else {
                        entry.getKey().run();
                        iter.remove();
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 0, 50, TimeUnit.MILLISECONDS);

    }

    public static void init() {

    }

    public static boolean isMainThread() {
        return Thread.currentThread() == THREAD;
    }
}
