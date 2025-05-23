package net.minecraft.server;

import java.io.IOException;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
public class PacketPlayInSetCreativeSlot implements Packet<PacketListenerPlayIn> {

    private int slot;
    private ItemStack b;

    public void a(PacketListenerPlayIn packetlistenerplayin) {
        packetlistenerplayin.a(this);
    }

    public void a(PacketDataSerializer packetdataserializer) throws IOException {
        this.slot = packetdataserializer.readShort();
        this.b = packetdataserializer.i();
    }

    public void b(PacketDataSerializer packetdataserializer) throws IOException {
        packetdataserializer.writeShort(this.slot);
        packetdataserializer.a(this.b);
    }

    public int a() {
        return this.slot;
    }

    public ItemStack getItemStack() {
        return this.b;
    }
}
