package gg.mineral.server.world;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SchematicBlock {
    private int x;
    private int y;
    private int z;
    private int type;
    private byte data;
}
