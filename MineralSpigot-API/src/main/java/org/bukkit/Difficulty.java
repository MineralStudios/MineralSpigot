package org.bukkit;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

/**
 * Represents the various difficulty levels that are available.
 */
public enum Difficulty {
    /**
     * Players regain health over time, hostile mobs don't spawn, the hunger
     * bar does not deplete.
     */
    PEACEFUL(0),

    /**
     * Hostile mobs spawn, enemies deal less damage than on normal difficulty,
     * the hunger bar does deplete and starving deals up to 5 hearts of
     * damage. (Default value)
     */
    EASY(1),

    /**
     * Hostile mobs spawn, enemies deal normal amounts of damage, the hunger
     * bar does deplete and starving deals up to 9.5 hearts of damage.
     */
    NORMAL(2),

    /**
     * Hostile mobs spawn, enemies deal greater damage than on normal
     * difficulty, the hunger bar does deplete and starving can kill players.
     */
    HARD(3);

    private final int value;
    private final static Int2ObjectOpenHashMap<Difficulty> BY_ID = new Int2ObjectOpenHashMap<>();

    private Difficulty(final int value) {
        this.value = value;
    }

    /**
     * Gets the difficulty value associated with this Difficulty.
     *
     * @return An integer value of this difficulty
     * @deprecated Magic value
     */
    @Deprecated
    public int getValue() {
        return value;
    }

    /**
     * Gets the Difficulty represented by the specified value
     *
     * @param value Value to check
     * @return Associative {@link Difficulty} with the given value, or null if
     *         it doesn't exist
     * @deprecated Magic value
     */
    @Deprecated
    public static Difficulty getByValue(final int value) {
        return BY_ID.get(value);
    }

    static {
        for (Difficulty diff : values()) {
            BY_ID.put(diff.value, diff);
        }
    }
}
