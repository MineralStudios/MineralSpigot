package org.bukkit;

import it.unimi.dsi.fastutil.bytes.Byte2ObjectOpenHashMap;

/**
 * Represents the two types of coal
 */
public enum CoalType {
    COAL(0x0),
    CHARCOAL(0x1);

    private final byte data;
    private final static Byte2ObjectOpenHashMap<CoalType> BY_DATA = new Byte2ObjectOpenHashMap<>();

    private CoalType(final int data) {
        this.data = (byte) data;
    }

    /**
     * Gets the associated data value representing this type of coal
     *
     * @return A byte containing the data value of this coal type
     * @deprecated Magic value
     */
    @Deprecated
    public byte getData() {
        return data;
    }

    /**
     * Gets the type of coal with the given data value
     *
     * @param data Data value to fetch
     * @return The {@link CoalType} representing the given value, or null if
     *         it doesn't exist
     * @deprecated Magic value
     */
    @Deprecated
    public static CoalType getByData(final byte data) {
        return BY_DATA.get(data);
    }

    static {
        for (CoalType type : values()) {
            BY_DATA.put(type.data, type);
        }
    }
}
