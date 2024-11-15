package org.bukkit;

import it.unimi.dsi.fastutil.bytes.Byte2ObjectOpenHashMap;

/**
 * Represents the three different types of Sandstone
 */
public enum SandstoneType {
    CRACKED(0x0),
    GLYPHED(0x1),
    SMOOTH(0x2);

    private final byte data;
    private final static Byte2ObjectOpenHashMap<SandstoneType> BY_DATA = new Byte2ObjectOpenHashMap<>();

    private SandstoneType(final int data) {
        this.data = (byte) data;
    }

    /**
     * Gets the associated data value representing this type of sandstone
     *
     * @return A byte containing the data value of this sandstone type
     * @deprecated Magic value
     */
    @Deprecated
    public byte getData() {
        return data;
    }

    /**
     * Gets the type of sandstone with the given data value
     *
     * @param data Data value to fetch
     * @return The {@link SandstoneType} representing the given value, or null
     *         if it doesn't exist
     * @deprecated Magic value
     */
    @Deprecated
    public static SandstoneType getByData(final byte data) {
        return BY_DATA.get(data);
    }

    static {
        for (SandstoneType type : values()) {
            BY_DATA.put(type.data, type);
        }
    }
}
