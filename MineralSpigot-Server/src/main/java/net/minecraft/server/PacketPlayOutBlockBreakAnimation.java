package net.minecraft.server;

import java.io.IOException;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PacketPlayOutBlockBreakAnimation implements Packet<PacketListenerPlayOut> {

    private int a;
    private BlockPosition b;
    private int c;

    public PacketPlayOutBlockBreakAnimation(int i, BlockPosition blockposition, int j) {
        this.a = i;
        this.b = blockposition;
        this.c = j;
    }

    public void a(PacketDataSerializer packetdataserializer) throws IOException {
        this.a = packetdataserializer.e();
        this.b = packetdataserializer.c();
        this.c = packetdataserializer.readUnsignedByte();
    }

    public void b(PacketDataSerializer packetdataserializer) throws IOException {
        packetdataserializer.b(this.a);
        packetdataserializer.a(this.b);
        packetdataserializer.writeByte(this.c);
    }

    public void a(PacketListenerPlayOut packetlistenerplayout) {
        packetlistenerplayout.a(this);
    }
}
