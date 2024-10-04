package net.minecraft.server;

import com.google.common.collect.Lists;
import org.github.paperspigot.exception.ServerInternalException;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.List;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;

public class RegionFile {

    private static final byte[] a = new byte[4096]; // Spigot - note: if this ever changes to not be 4096 bytes, update
                                                    // constructor! // PAIL: empty 4k block
    private final File b;
    private RandomAccessFile c;
    private final int[] d = new int[1024];
    private final int[] e = new int[1024];
    private List<Boolean> f;
    private int g;
    private long h;

    // New caches for chunk lengths and compression types
    private final int[] chunkLengths = new int[1024];
    private final byte[] chunkCompressionTypes = new byte[1024];

    public RegionFile(File file) {
        this.b = file;
        this.g = 0;

        try {
            if (file.exists()) {
                this.h = file.lastModified();
            }

            this.c = new RandomAccessFile(file, "rw");
            int i;

            if (this.c.length() < 4096L) {
                // Spigot - more effecient chunk zero'ing
                this.c.write(RegionFile.a); // Spigot
                this.c.write(RegionFile.a); // Spigot

                this.g += 8192;
            }

            if ((this.c.length() & 4095L) != 0L) {
                for (i = 0; (long) i < (this.c.length() & 4095L); ++i) {
                    this.c.write(0);
                }
            }

            i = (int) this.c.length() / 4096;
            this.f = Lists.newArrayListWithCapacity(i);

            int j;

            for (j = 0; j < i; ++j) {
                this.f.add(Boolean.TRUE);
            }

            this.f.set(0, Boolean.FALSE);
            this.f.set(1, Boolean.FALSE);
            this.c.seek(0L);

            // PandaSpigot start - Reduce IO ops
            ByteBuffer header = ByteBuffer.allocate(8192);
            while (header.hasRemaining()) {
                if (this.c.getChannel().read(header) == -1)
                    throw new EOFException();
            }
            header.clear(); // cast required, due to Java 9+ changing return type
            IntBuffer headerAsInts = header.asIntBuffer();
            // PandaSpigot end
            int k;

            for (j = 0; j < 1024; ++j) {
                k = headerAsInts.get(); // PandaSpigot
                this.d[j] = k;
                if (k != 0 && (k >> 8) + (k & 255) <= this.f.size()) {
                    for (int l = 0; l < (k & 255); ++l) {
                        this.f.set((k >> 8) + l, Boolean.FALSE);
                    }
                }
            }

            for (j = 0; j < 1024; ++j) {
                k = headerAsInts.get(); // PandaSpigot
                this.e[j] = k;
            }

            // Initialize the chunk length and compression type caches
            for (int chunkX = 0; chunkX < 32; ++chunkX) {
                for (int chunkZ = 0; chunkZ < 32; ++chunkZ) {
                    int index = chunkX + chunkZ * 32;
                    int offsetSector = this.d[index];

                    if (offsetSector != 0) {
                        int sectorNumber = offsetSector >> 8;
                        int numSectors = offsetSector & 0xFF;

                        if (sectorNumber + numSectors <= this.f.size()) {
                            this.c.seek((long) sectorNumber * 4096);
                            int length = this.c.readInt();
                            if (length > 0 && length <= 4096 * numSectors) {
                                byte compressionType = this.c.readByte();
                                chunkLengths[index] = length;
                                chunkCompressionTypes[index] = compressionType;
                            }
                        }
                    }
                }
            }
        } catch (IOException ioexception) {
            ioexception.printStackTrace();
            ServerInternalException.reportInternalException(ioexception); // Paper
        }
    }

    // Optimized chunkExists method using cached data
    public synchronized boolean chunkExists(int i, int j) {
        if (this.d(i, j)) {
            return false;
        } else {
            int index = i + j * 32;
            int k = this.e(i, j);

            if (k == 0) {
                return false;
            } else {
                int l = k >> 8;
                int i1 = k & 255;

                if (l + i1 > this.f.size()) {
                    return false;
                }

                int j1 = chunkLengths[index];
                byte b0 = chunkCompressionTypes[index];

                if (j1 == 0) {
                    return false;
                }

                if (j1 > 4096 * i1 || j1 <= 0) {
                    return false;
                }

                return b0 == 1 || b0 == 2;
            }
        }
    }

    public synchronized DataInputStream a(int i, int j) {
        if (this.d(i, j)) {
            return null;
        } else {
            try {
                int k = this.e(i, j);

                if (k == 0) {
                    return null;
                } else {
                    int l = k >> 8;
                    int i1 = k & 255;

                    if (l + i1 > this.f.size()) {
                        return null;
                    } else {
                        this.c.seek((long) (l * 4096));
                        int j1 = this.c.readInt();

                        if (j1 > 4096 * i1) {
                            return null;
                        } else if (j1 <= 0) {
                            return null;
                        } else {
                            byte b0 = this.c.readByte();
                            byte[] abyte;

                            if (b0 == 1) {
                                abyte = new byte[j1 - 1];
                                this.c.readFully(abyte);
                                return new DataInputStream(
                                        new BufferedInputStream(new GZIPInputStream(new ByteArrayInputStream(abyte))));
                            } else if (b0 == 2) {
                                abyte = new byte[j1 - 1];
                                this.c.readFully(abyte);
                                return new DataInputStream(new BufferedInputStream(
                                        new InflaterInputStream(new ByteArrayInputStream(abyte))));
                            } else {
                                return null;
                            }
                        }
                    }
                }
            } catch (IOException ioexception) {
                return null;
            }
        }
    }

    public DataOutputStream b(int i, int j) { // PAIL: getChunkOutputStream
        // PAIL: isInvalidRegion
        return this.d(i, j) ? null
                : new DataOutputStream(
                        new BufferedOutputStream(new DeflaterOutputStream(new RegionFile.ChunkBuffer(i, j))));
    }

    protected synchronized void a(int i, int j, byte[] abyte, int k) {
        try {
            int l = this.e(i, j);
            int i1 = l >> 8;
            int j1 = l & 255;
            int k1 = (k + 5) / 4096 + 1;

            if (k1 >= 256) {
                return;
            }

            if (i1 != 0 && j1 == k1) {
                this.a(i1, abyte, k);
            } else {
                int l1;

                for (l1 = 0; l1 < j1; ++l1) {
                    this.f.set(i1 + l1, Boolean.TRUE);
                }

                l1 = this.f.indexOf(Boolean.TRUE);
                int i2 = 0;
                int j2;

                if (l1 != -1) {
                    for (j2 = l1; j2 < this.f.size(); ++j2) {
                        if (i2 != 0) {
                            if (this.f.get(j2)) {
                                ++i2;
                            } else {
                                i2 = 0;
                            }
                        } else if (this.f.get(j2)) {
                            l1 = j2;
                            i2 = 1;
                        }

                        if (i2 >= k1) {
                            break;
                        }
                    }
                }

                if (i2 >= k1) {
                    i1 = l1;
                    this.a(i, j, i1 << 8 | k1);

                    for (j2 = 0; j2 < k1; ++j2) {
                        this.f.set(i1 + j2, Boolean.FALSE);
                    }

                    this.a(i1, abyte, k);
                } else {
                    this.c.seek(this.c.length());
                    i1 = this.f.size();

                    for (j2 = 0; j2 < k1; ++j2) {
                        this.c.write(RegionFile.a);
                        this.f.add(Boolean.FALSE);
                    }

                    this.g += 4096 * k1;
                    this.a(i1, abyte, k);
                    this.a(i, j, i1 << 8 | k1);
                }
            }

            this.b(i, j, (int) (MinecraftServer.az() / 1000L));

            // Update caches after writing chunk
            int index = i + j * 32;
            chunkLengths[index] = k + 1; // Length including compression type byte
            chunkCompressionTypes[index] = 2; // Assuming compression type is always 2 here

        } catch (IOException ioexception) {
            ioexception.printStackTrace();
            ServerInternalException.reportInternalException(ioexception); // Paper
        }
    }

    private void a(int i, byte[] abyte, int j) throws IOException {
        this.c.seek((long) (i * 4096));
        this.c.writeInt(j + 1);
        this.c.writeByte(2); // Assuming compression type is always 2 here
        this.c.write(abyte, 0, j);
    }

    private boolean d(int i, int j) {
        return i < 0 || i >= 32 || j < 0 || j >= 32;
    }

    private int e(int i, int j) {
        return this.d[i + j * 32];
    }

    public boolean c(int i, int j) {
        return this.e(i, j) != 0;
    }

    private void a(int i, int j, int k) throws IOException {
        this.d[i + j * 32] = k;
        this.c.seek((long) ((i + j * 32) * 4));
        this.c.writeInt(k);
    }

    private void b(int i, int j, int k) throws IOException {
        this.e[i + j * 32] = k;
        this.c.seek((long) (4096 + (i + j * 32) * 4));
        this.c.writeInt(k);
    }

    public void c() throws IOException {
        if (this.c != null) {
            this.c.close();
        }
    }

    class ChunkBuffer extends ByteArrayOutputStream {

        private final int b;
        private final int c;

        public ChunkBuffer(int i, int j) {
            super(8096);
            this.b = i;
            this.c = j;
        }

        public void close() throws IOException {
            RegionFile.this.a(this.b, this.c, this.buf, this.count);
        }
    }
}
