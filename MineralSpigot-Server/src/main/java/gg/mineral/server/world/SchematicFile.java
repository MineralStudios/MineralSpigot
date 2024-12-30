package gg.mineral.server.world;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.val;
import net.minecraft.server.MinecraftServer;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.generator.ChunkGenerator;

import java.io.File;
import java.util.List;
import java.util.Random;

@Data
@AllArgsConstructor
public class SchematicFile {

    private File source;
    private short xSize;
    private short ySize;
    private short zSize;
    private List<SchematicBlock> blocks;

    public boolean isEmpty() {
        return blocks == null || blocks.isEmpty();
    }

    public World generateWorld(String suffix) {

        val name = source.getName();

        val worldName = name.substring(0, name.length() - ".schematic".length()) + suffix;
        val world = new WorldCreator(worldName).ram(true).type(org.bukkit.WorldType.FLAT).generateStructures(false).generator(new ChunkGenerator() {
            @Override
            public byte[] generate(org.bukkit.World world, Random random, int x, int z) {
                return new byte[32768];
            }
        }).createWorld();

        val blocks = this.getBlocks();

        for (val block : blocks) {
            val x = block.getX();
            val y = block.getY();
            val z = block.getZ();

            world.getBlockAt(x, y, z).setTypeIdAndData(block.getType(), block.getData(), false);
        }
        MinecraftServer.LOGGER.info("Loading schematic with {} blocks ({}) as world {}", blocks.size(), name, world.getName());

        return world;
    }
}
