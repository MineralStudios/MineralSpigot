package gg.mineral.server.world;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import lombok.experimental.UtilityClass;
import lombok.val;
import net.minecraft.server.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@UtilityClass
public final class Schematic {

    public static Map<String, SchematicFile> SCHEMATICS = new Object2ObjectOpenHashMap<>();

    public static SchematicFile get(String name) {
        return SCHEMATICS.get(name);
    }

    /**
     * Loads a schematic from a source file, returning a {@link SchematicFile}
     * containing all blocks in a single list.
     *
     * @param source The .schematic file to load
     * @return {@link SchematicFile} containing dimensions and block data
     * @throws IOException if an I/O error occurs during reading
     */
    public static SchematicFile load(File source) throws IOException {
        try (FileInputStream stream = new FileInputStream(source)) {
            NBTTagCompound nbt = NBTCompressedStreamTools.readNBT(stream);
            Map<String, NBTBase> nbtValue = nbt.getMap();

            val widthTag = nbtValue.get("Width");
            val heightTag = nbtValue.get("Height");
            val lengthTag = nbtValue.get("Length");

            if (widthTag == null || heightTag == null || lengthTag == null) {
                throw new IllegalArgumentException("Invalid schematic file: missing dimensions");
            }
            if (!(widthTag instanceof NBTTagShort widthShort)
                    || !(heightTag instanceof NBTTagShort heightShort)
                    || !(lengthTag instanceof NBTTagShort lengthShort)) {
                throw new IllegalArgumentException("Invalid schematic file: invalid dimension types");
            }

            short width = widthShort.e();
            short height = heightShort.e();
            short length = lengthShort.e();

            val blocksTag = nbtValue.get("Blocks");
            val dataTag = nbtValue.get("Data");
            if (blocksTag == null || dataTag == null) {
                throw new IllegalArgumentException("Invalid schematic file: missing block data");
            }
            if (!(blocksTag instanceof NBTTagByteArray blocksByteArrTag) || !(dataTag instanceof NBTTagByteArray dataByteArrTag)) {
                throw new IllegalArgumentException("Invalid schematic file: block/data tags not byte arrays");
            }

            byte[] blockArray = blocksByteArrTag.c();
            byte[] dataArray = dataByteArrTag.c();

            // Prepare a list to hold all the blocks
            List<SchematicBlock> blocks = new ArrayList<>(width * height * length);

            // Convert the block/data arrays into a list of SchematicBlock objects
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    for (int z = 0; z < length; z++) {
                        int index = (y * length + z) * width + x;
                        int type = blockArray[index] & 0xFF;      // Convert signed byte to unsigned int
                        byte data = (byte) (dataArray[index] & 0x0F);

                        blocks.add(new SchematicBlock(x, y, z, type, data));
                    }
                }
            }

            return new SchematicFile(source, width, height, length, blocks);
        }
    }
}
