package org.bukkit.block;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

public enum PistonMoveReaction {

    /**
     * Indicates that the block can be pushed or pulled.
     */
    MOVE(0),
    /**
     * Indicates the block is fragile and will break if pushed on.
     */
    BREAK(1),
    /**
     * Indicates that the block will resist being pushed or pulled.
     */
    BLOCK(2);

    private int id;
    private static Int2ObjectOpenHashMap<PistonMoveReaction> byId = new Int2ObjectOpenHashMap<>();
    static {
        for (PistonMoveReaction reaction : PistonMoveReaction.values()) {
            byId.put(reaction.id, reaction);
        }
    }

    private PistonMoveReaction(int id) {
        this.id = id;
    }

    /**
     * @return The ID of the move reaction
     * @deprecated Magic value
     */
    @Deprecated
    public int getId() {
        return this.id;
    }

    /**
     * @param id An ID
     * @return The move reaction with that ID
     * @deprecated Magic value
     */
    @Deprecated
    public static PistonMoveReaction getById(int id) {
        return byId.get(id);
    }
}
