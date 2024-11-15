package net.minecraft.server;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.base.Predicate;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

public class BlockStatePredicate implements Predicate<IBlockData> {

    private final BlockStateList a;
    private final Map<IBlockState, Predicate> b = new Object2ObjectOpenHashMap<>();

    private BlockStatePredicate(BlockStateList blockstatelist) {
        this.a = blockstatelist;
    }

    public static BlockStatePredicate a(Block block) {
        return new BlockStatePredicate(block.P());
    }

    public boolean a(IBlockData iblockdata) {
        if (iblockdata != null && iblockdata.getBlock().equals(this.a.getBlock())) {
            Iterator iterator = this.b.entrySet().iterator();

            Entry entry;
            Comparable comparable;

            do {
                if (!iterator.hasNext()) {
                    return true;
                }

                entry = (Entry) iterator.next();
                comparable = iblockdata.get((IBlockState) entry.getKey());
            } while (((Predicate) entry.getValue()).apply(comparable));

            return false;
        } else {
            return false;
        }
    }

    public <V extends Comparable<V>> BlockStatePredicate a(IBlockState<V> iblockstate,
            Predicate<? extends V> predicate) {
        if (!this.a.d().contains(iblockstate)) {
            throw new IllegalArgumentException(this.a + " cannot support property " + iblockstate);
        } else {
            this.b.put(iblockstate, predicate);
            return this;
        }
    }

    public boolean apply(IBlockData object) {
        return this.a(object);
    }
}
