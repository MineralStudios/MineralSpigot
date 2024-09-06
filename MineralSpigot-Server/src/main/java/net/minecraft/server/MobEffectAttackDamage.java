package net.minecraft.server;

import gg.mineral.server.config.GlobalConfig;

public class MobEffectAttackDamage extends MobEffectList {

    protected MobEffectAttackDamage(int i, MinecraftKey minecraftkey, boolean flag, int j) {
        super(i, minecraftkey, flag, j);
    }

    public double a(int i, AttributeModifier attributemodifier) {
        // PaperSpigot - Configurable modifiers for strength and weakness effects
        return this.id == MobEffectList.WEAKNESS.id
                ? (double) (GlobalConfig.getInstance().getWeaknessEffectModifier() * (float) (i + 1))
                : GlobalConfig.getInstance().getStrengthEffectModifier() * (double) (i + 1);
    }
}
