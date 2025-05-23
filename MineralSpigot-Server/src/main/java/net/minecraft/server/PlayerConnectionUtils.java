package net.minecraft.server;

public class PlayerConnectionUtils {

    public static <T extends PacketListener> void ensureMainThread(final Packet<T> packet, final T packetlistener,
            IAsyncTaskHandler iasynctaskhandler) throws CancelledPacketHandleException {
        if (!iasynctaskhandler.isMainThread()) {
            iasynctaskhandler.postToMainThread(() -> packet.a(packetlistener));
            throw CancelledPacketHandleException.INSTANCE;
        }
    }
}
