package net.minecraft.server;

import com.google.common.base.Predicates;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;

public class RegistryID<T> implements Registry<T> {

    // Blossom start
    private short[] indexes = new short[0];
    private T[] storage = (T[]) new Object[0];

    public RegistryID() {
    }

    public void a(T t0, int i) {
        // Eh- huh... what even is this?!
        if (i >= storage.length) {
            storage = java.util.Arrays.copyOf(storage, i + 1);
        }

        storage[i] = t0;

        int hash = t0.hashCode();
        if (hash >= indexes.length) {
            assert hash < Short.MAX_VALUE : String.format("(%d->%s->%d) index too high", hash, t0, i);
            indexes = java.util.Arrays.copyOf(indexes, hash + 1);
        }

        indexes[hash] = (short) i;
    }

    public int b(T t0) {
        // todo: merge into return below, git didn't want to update the patch for some
        // reason
        if (t0 == null)
            return -1;
        return indexes[t0.hashCode()];
    }

    public final T a(int i) {
        return i >= 0 && i < storage.length ? storage[i] : null;
    }

    public Iterator<T> iterator() {
        return new Iterator<T>() {
            private int index = -1;

            @Override
            public boolean hasNext() {
                while (++index < storage.length) {
                    if (storage[index] != null) {
                        return true;
                    }
                }

                return false;
            }

            @Override
            public T next() {
                return storage[index];
            }
        };
        // Blossom end
    }
}
