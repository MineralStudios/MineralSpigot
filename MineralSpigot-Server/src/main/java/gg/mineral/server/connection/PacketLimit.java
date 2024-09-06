package gg.mineral.server.connection;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class PacketLimit {
    public final double packetLimitInterval, maxPacketRate;
    public final ViolateAction violateAction;

    public enum ViolateAction {
        KICK, DROP;
    }
}
