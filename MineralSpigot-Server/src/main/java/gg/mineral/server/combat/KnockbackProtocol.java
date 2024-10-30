package gg.mineral.server.combat;

import net.minecraft.server.EntityLiving;

public interface KnockbackProtocol {
    void firstStage(EntityLiving attacker, EntityLiving victim);

    void secondStage(EntityLiving attacker, EntityLiving victim, int knockbackEnchantLevel);
}
