package net.minecraft.server;

import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.github.paperspigot.exception.ServerInternalException;

import com.google.common.collect.Lists;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ShortOpenHashMap;

public class PersistentCollection {

    private IDataManager b;
    protected Map<String, PersistentBase> a = new Object2ObjectOpenHashMap<>();
    public List<PersistentBase> c = Lists.newArrayList(); // Spigot
    private Object2ShortOpenHashMap<String> d = new Object2ShortOpenHashMap<>() {
        {
            defaultReturnValue((short) -1);
        }
    };

    public PersistentCollection(IDataManager idatamanager) {
        this.b = idatamanager;
        this.b();
    }

    public PersistentBase get(Class<? extends PersistentBase> oclass, String s) {
        PersistentBase persistentbase = (PersistentBase) this.a.get(s);

        if (persistentbase != null) {
            return persistentbase;
        } else {
            if (this.b != null) {
                try {
                    File file = this.b.getDataFile(s);

                    if (file != null && file.exists()) {
                        try {
                            persistentbase = (PersistentBase) oclass.getConstructor(new Class[] { String.class })
                                    .newInstance(new Object[] { s });
                        } catch (Exception exception) {
                            throw new RuntimeException("Failed to instantiate " + oclass.toString(), exception);
                        }

                        FileInputStream fileinputstream = new FileInputStream(file);
                        NBTTagCompound nbttagcompound = NBTCompressedStreamTools.a((InputStream) fileinputstream);

                        fileinputstream.close();
                        persistentbase.a(nbttagcompound.getCompound("data"));
                    }
                } catch (Exception exception1) {
                    exception1.printStackTrace();
                }
            }

            if (persistentbase != null) {
                this.a.put(s, persistentbase);
                this.c.add(persistentbase);
            }

            return persistentbase;
        }
    }

    public void a(String s, PersistentBase persistentbase) {
        if (this.a.containsKey(s)) {
            this.c.remove(this.a.remove(s));
        }

        this.a.put(s, persistentbase);
        this.c.add(persistentbase);
    }

    public void a() {
        for (int i = 0; i < this.c.size(); ++i) {
            PersistentBase persistentbase = (PersistentBase) this.c.get(i);

            if (persistentbase.d()) {
                this.a(persistentbase);
                persistentbase.a(false);
            }
        }

    }

    private void a(PersistentBase persistentbase) {
        if (this.b != null) {
            try {
                File file = this.b.getDataFile(persistentbase.id);

                if (file != null) {
                    NBTTagCompound nbttagcompound = new NBTTagCompound();

                    persistentbase.b(nbttagcompound);
                    NBTTagCompound nbttagcompound1 = new NBTTagCompound();

                    nbttagcompound1.set("data", nbttagcompound);
                    FileOutputStream fileoutputstream = new FileOutputStream(file);

                    NBTCompressedStreamTools.a(nbttagcompound1, (OutputStream) fileoutputstream);
                    fileoutputstream.close();
                }
            } catch (Exception exception) {
                exception.printStackTrace();
                ServerInternalException.reportInternalException(exception);
            }

        }
    }

    private void b() {
        try {
            this.d.clear();
            if (this.b == null) {
                return;
            }

            File file = this.b.getDataFile("idcounts");

            if (file != null && file.exists()) {
                DataInputStream datainputstream = new DataInputStream(new FileInputStream(file));
                NBTTagCompound nbttagcompound = NBTCompressedStreamTools.a(datainputstream);

                datainputstream.close();
                Iterator iterator = nbttagcompound.c().iterator();

                while (iterator.hasNext()) {
                    String s = (String) iterator.next();
                    NBTBase nbtbase = nbttagcompound.get(s);

                    if (nbtbase instanceof NBTTagShort) {
                        NBTTagShort nbttagshort = (NBTTagShort) nbtbase;
                        short short0 = nbttagshort.e();

                        this.d.put(s, short0);
                    }
                }
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }

    }

    public int a(String s) {
        short oshort = (short) (this.d.getShort(s) + 1);

        this.d.put(s, oshort);
        if (this.b == null) {
            return oshort;
        } else {
            try {
                File file = this.b.getDataFile("idcounts");

                if (file != null) {
                    NBTTagCompound nbttagcompound = new NBTTagCompound();
                    Iterator iterator = this.d.keySet().iterator();

                    while (iterator.hasNext()) {
                        String s1 = (String) iterator.next();
                        short short0 = this.d.getShort(s1);

                        nbttagcompound.setShort(s1, short0);
                    }

                    DataOutputStream dataoutputstream = new DataOutputStream(new FileOutputStream(file));

                    NBTCompressedStreamTools.a(nbttagcompound, (DataOutput) dataoutputstream);
                    dataoutputstream.close();
                }
            } catch (Exception exception) {
                exception.printStackTrace();
                ServerInternalException.reportInternalException(exception); // Paper
            }

            return oshort;
        }
    }
}
