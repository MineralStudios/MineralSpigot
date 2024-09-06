package org.spigotmc;

import gg.mineral.server.config.GlobalConfig;
import net.minecraft.server.Entity;
import net.minecraft.server.EntityExperienceOrb;
import net.minecraft.server.EntityGhast;
import net.minecraft.server.EntityItem;
import net.minecraft.server.EntityItemFrame;
import net.minecraft.server.EntityPainting;
import net.minecraft.server.EntityPlayer;

public class TrackingRange {

    /**
     * Gets the range an entity should be 'tracked' by players and visible in
     * the client.
     *
     * @param entity
     * @param defaultRange Default range defined by Mojang
     * @return
     */
    public static int getEntityTrackingRange(Entity entity, int defaultRange) {

        if (entity instanceof EntityPlayer) {
            return GlobalConfig.getInstance().getPlayerTrackingRange();
        } else if (entity.activationType == 1) {
            return GlobalConfig.getInstance().getMonsterTrackingRange();
        } else if (entity instanceof EntityGhast) {
            if (GlobalConfig.getInstance().getMonsterTrackingRange() > GlobalConfig.getInstance()
                    .getMonsterActivationRange()) {
                return GlobalConfig.getInstance().getMonsterTrackingRange();
            } else {
                return GlobalConfig.getInstance().getMonsterActivationRange();
            }
        } else if (entity.activationType == 2) {
            return GlobalConfig.getInstance().getAnimalTrackingRange();
        } else if (entity instanceof EntityItemFrame || entity instanceof EntityPainting || entity instanceof EntityItem
                || entity instanceof EntityExperienceOrb) {
            return GlobalConfig.getInstance().getMiscTrackingRange();
        } else {
            return GlobalConfig.getInstance().getOtherTrackingRange();
        }
    }
}
