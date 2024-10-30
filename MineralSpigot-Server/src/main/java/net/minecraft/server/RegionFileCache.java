package net.minecraft.server;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import lombok.val;

import org.github.paperspigot.exception.ServerInternalException;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

public class RegionFileCache {

    private static final com.google.common.cache.LoadingCache<File, RegionFile> cache = com.google.common.cache.CacheBuilder
            .newBuilder()
            .expireAfterAccess(1, java.util.concurrent.TimeUnit.MINUTES)
            .removalListener(new com.google.common.cache.RemovalListener<File, RegionFile>() {
                @Override
                public void onRemoval(
                        com.google.common.cache.RemovalNotification<File, RegionFile> removalNotification) {
                    try {
                        removalNotification.getValue().c();
                    } catch (IOException exception) {
                        throw com.google.common.base.Throwables.propagate(exception);
                    }
                }
            })
            .build(new com.google.common.cache.CacheLoader<File, RegionFile>() {
                @Override
                public RegionFile load(File file) throws Exception {
                    return new RegionFile(file);
                }
            });

    // PaperSpigot start
    public static synchronized RegionFile a(File file, int i, int j) {
        return a(file, i, j, true);
    }

    public static synchronized RegionFile a(File file, int i, int j, boolean create) {
        // PaperSpigot end
        val file1 = new File(file, "region");
        val file2 = new File(file1, "r." + (i >> 5) + "." + (j >> 5) + ".mca");
        val regionfile = RegionFileCache.cache.getIfPresent(file2);

        if (regionfile != null)
            return regionfile;

        if (!create && !file2.exists())
            return null;
        // PaperSpigot
        if (!file1.exists())
            file1.mkdirs();

        if (true) {
            try {
                return RegionFileCache.cache.get(file2);
            } catch (Exception exception) {
                throw com.google.common.base.Throwables.propagate(exception);
            }
        }

        RegionFile regionfile1 = new RegionFile(file2);

        return regionfile1;
    }

    public static void a() {
    }

    public static DataInputStream c(File file, int i, int j) {
        RegionFile regionfile = a(file, i, j);

        return regionfile.a(i & 31, j & 31);
    }

    public static DataOutputStream d(File file, int i, int j) {
        RegionFile regionfile = a(file, i, j);

        return regionfile.b(i & 31, j & 31);
    }
}
