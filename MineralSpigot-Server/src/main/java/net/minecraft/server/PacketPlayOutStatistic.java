package net.minecraft.server;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.IOException;
import java.util.Iterator;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PacketPlayOutStatistic implements Packet<PacketListenerPlayOut> {

    private Object2IntOpenHashMap<Statistic> a;

    public void a(PacketListenerPlayOut packetlistenerplayout) {
        packetlistenerplayout.a(this);
    }

    public void a(PacketDataSerializer packetdataserializer) throws IOException {
        int i = packetdataserializer.e();

        this.a = new Object2IntOpenHashMap<>();

        for (int j = 0; j < i; ++j) {
            Statistic statistic = StatisticList.getStatistic(packetdataserializer.c(32767));
            int k = packetdataserializer.e();

            if (statistic != null)
                this.a.put(statistic, k);
        }

    }

    public void b(PacketDataSerializer packetdataserializer) throws IOException {
        packetdataserializer.b(this.a.size());
        Iterator<it.unimi.dsi.fastutil.objects.Object2IntMap.Entry<Statistic>> iterator = this.a.object2IntEntrySet()
                .iterator();

        while (iterator.hasNext()) {
            it.unimi.dsi.fastutil.objects.Object2IntMap.Entry<Statistic> entry = iterator.next();

            packetdataserializer.a(entry.getKey().name);
            packetdataserializer.b(entry.getIntValue());
        }

    }
}
