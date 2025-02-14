package gg.mineral.server.combat;

import java.util.concurrent.ConcurrentLinkedQueue;

import gg.mineral.server.config.GlobalConfig;
import lombok.Getter;
import net.minecraft.server.Packet;
import net.minecraft.server.PacketListenerPlayIn;

@Getter
public class BacktrackSystem {

    @Getter
    private ConcurrentLinkedQueue<PacketRecieveTask> packetReadTasks = new ConcurrentLinkedQueue<>();

    // config
    private boolean random = GlobalConfig.getInstance().isBacktrackRandom();
    private boolean enabled = GlobalConfig.getInstance().isBacktrackEnabled();
    private boolean comboMode = GlobalConfig.getInstance().isComboMode();
    private double delayDistanceMin = GlobalConfig.getInstance().getDelayDistanceMin();
    private double delayDistanceMax = GlobalConfig.getInstance().getDelayDistanceMax();
    private int delayFactor = GlobalConfig.getInstance().getDelayFactor();
    private int decayFactor = GlobalConfig.getInstance().getDecayFactor();
    private int maxDelayMs = GlobalConfig.getInstance().getMaxDelayMs();
    private int delayResetTime = GlobalConfig.getInstance().getDelayResetTime();
    private int rMin, rMax = 0;

    public void setRMin(int rMin) {
        this.rMin = rMin;
        resetValues();
    }

    public void setRMax(int rMax) {
        this.rMax = rMax;
        resetValues();
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        resetValues();
    }

    public void setComboMode(boolean comboMode) {
        this.comboMode = comboMode;
        resetValues();
    }

    public void setDelayDistanceMin(double delayDistanceMin) {
        this.delayDistanceMin = delayDistanceMin;
        resetValues();
    }

    public void setDelayDistanceMax(double delayDistanceMax) {
        this.delayDistanceMax = delayDistanceMax;
        resetValues();
    }

    public void setRandom(boolean random) {
        this.random = random;
        resetValues();
    }

    public void setDelayFactor(int delayFactor) {
        this.delayFactor = delayFactor;
        resetValues();
    }

    public void setDecayFactor(int decayFactor) {
        this.decayFactor = decayFactor;
        resetValues();
    }

    public void setMaxDelayMs(int maxDelayMs) {
        this.maxDelayMs = maxDelayMs;
        resetValues();
    }

    public void setDelayResetTime(int delayResetTime) {
        this.delayResetTime = delayResetTime;
        resetValues();
    }

    // state
    private int currentDelay; // in milliseconds
    private long lastAttackTime; // timestamp of the last attack
    private int comboHits; // count of successive hits
    private int tickCounter; // counter for ticks

    public void resetValues() {
        this.lastAttackTime = 0;
        this.comboHits = 0;
        this.tickCounter = 0;
        this.currentDelay = 0;
    }

    boolean inRange = false;

    public void onAttack() {
        comboHits = 0; // Reset combo counter if damage is taken
    }

    public void onDamage(double attackerDistance) {
        long currentTime = System.currentTimeMillis();
        if (random) {
            inRange = attackerDistance >= delayDistanceMin && attackerDistance <= delayDistanceMax;
            lastAttackTime = currentTime;
            return;
        }

        if (attackerDistance >= delayDistanceMin && attackerDistance <= delayDistanceMax) {
            if (currentTime - lastAttackTime <= 1000) {
                comboHits++;
                if (!comboMode || comboHits >= 3) {
                    currentDelay += delayFactor;
                    if (currentDelay > maxDelayMs)
                        currentDelay = maxDelayMs;
                }
            } else
                comboHits = 1; // Reset combo counter if too much time has passed

            lastAttackTime = currentTime;
        } else
            comboHits = 0; // Reset combo counter if out of range
    }

    int largestDelay = 0;

    public void onTick() {
        long currentTime = System.currentTimeMillis();

        if (random) {
            if (!inRange || currentTime - lastAttackTime > 3000) { // Make it not delay if out of range or too much time
                // has passed since last attacked
                currentDelay -= decayFactor;
                if (currentDelay < 0)
                    currentDelay = 0;
                return;
            }

            if (largestDelay > 0)
                largestDelay -= 25;
            int rMin = Math.max(largestDelay, this.rMin);

            currentDelay = rMin + (int) (Math.random() * (rMax - rMin + 1));
            if (currentDelay > largestDelay)
                largestDelay = currentDelay;
            return;
        }

        tickCounter++;
        if (tickCounter >= delayResetTime) {
            tickCounter = 0;
            if (currentTime - lastAttackTime > 1000) {
                currentDelay -= decayFactor;
                if (currentDelay < 0)
                    currentDelay = 0;
            }
        }
    }

    public record PacketRecieveTask(Packet<PacketListenerPlayIn> packet, PacketListenerPlayIn packetListener,
                                    long sendTime) {

        public boolean shouldSend() {
            return System.currentTimeMillis() >= sendTime;
        }

        public void process() {
            packet.a(packetListener);
        }
    }

    // Every millisecond, check if packets should be sent
    // iterate over player's records for each player
    // for each, check if packets should be sent (handle should be called)
    // take note if in that iteration, handle is called
    // after looping packets, call flush (whenever packets require sending)

    public void tickBacktrack() {

        // Do it so the oldest packets have to send first to maintain packet order
        // break the loop when the oldest packet is not ready to send yet (this ignores
        // any newer packets that are ready to send, maintaining the order)
        for (int i = 0; i < packetReadTasks.size(); i++) {
            var task = packetReadTasks.peek();

            if (task == null || !task.shouldSend())
                break;

            task = packetReadTasks.poll();
            assert task != null;
            task.process();
        }
    }
}
