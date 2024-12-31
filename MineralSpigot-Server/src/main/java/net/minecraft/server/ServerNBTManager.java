package net.minecraft.server;

import java.io.File;

import lombok.val;

public class ServerNBTManager extends WorldNBTStorage {

    public ServerNBTManager(File file, String s, boolean flag, boolean ram) {
        super(file, s, flag,ram);
    }

    public IChunkLoader createChunkLoader(WorldProvider worldprovider) {
        val file = this.getDirectory();
        File file1;

        if (worldprovider instanceof WorldProviderHell) {
            file1 = new File(file, "DIM-1");
            if (!ram)file1.mkdirs();
            return new ChunkRegionLoader(file1,ram);
        } else if (worldprovider instanceof WorldProviderTheEnd) {
            file1 = new File(file, "DIM1");
            if (!ram)file1.mkdirs();
            return new ChunkRegionLoader(file1,ram);
        } else {
            return new ChunkRegionLoader(file,ram);
        }
    }

    public void saveWorldData(WorldData worlddata, NBTTagCompound nbttagcompound) {
        worlddata.e(19133);
        super.saveWorldData(worlddata, nbttagcompound);
    }

    public void a() {
        if (ram) return;

        RegionFileCache.a();
    }
}
