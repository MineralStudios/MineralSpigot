package net.minecraft.server;

import java.util.Iterator;
import java.util.List;

import org.bukkit.craftbukkit.entity.CraftLivingEntity;
import org.bukkit.entity.LivingEntity;
// CraftBukkit end

import gg.mineral.server.config.GlobalConfig;
import it.unimi.dsi.fastutil.objects.Object2DoubleOpenHashMap;

public class EntityPotion extends EntityProjectile {

    public ItemStack item;

    public EntityPotion(World world) {
        super(world);
    }

    public EntityPotion(World world, EntityLiving entityliving, int i) {
        this(world, entityliving, new ItemStack(Items.POTION, 1, i));
    }

    public EntityPotion(World world, EntityLiving entityliving, ItemStack itemstack) {
        super(world, entityliving);
        this.item = itemstack;
    }

    public EntityPotion(World world, double d0, double d1, double d2, ItemStack itemstack) {
        super(world, d0, d1, d2);
        this.item = itemstack;
    }

    protected float m() {
        return 0.05F;
    }

    protected float j() {
        return 0.5F;
    }

    protected float l() {
        return -20.0F;
    }

    public void setPotionValue(int i) {
        if (this.item == null) {
            this.item = new ItemStack(Items.POTION, 1, 0);
        }

        this.item.setData(i);
    }

    public int getPotionValue() {
        if (this.item == null) {
            this.item = new ItemStack(Items.POTION, 1, 0);
        }

        return this.item.getData();
    }

    protected void a(MovingObjectPosition movingobjectposition) {
        if (!this.world.isClientSide) {
            List list = Items.POTION.h(this.item);

            if (true || list != null && !list.isEmpty()) { // CraftBukkit - Call event even if no effects to apply
                AxisAlignedBB axisalignedbb = this.getBoundingBox().grow(4.0D, 2.0D, 4.0D);
                List list1 = this.world.a(EntityLiving.class, axisalignedbb);

                if (true || !list1.isEmpty()) { // CraftBukkit - Run code even if there are no entities around
                    Iterator iterator = list1.iterator();

                    // CraftBukkit
                    Object2DoubleOpenHashMap<LivingEntity> affected = new Object2DoubleOpenHashMap<>();

                    while (iterator.hasNext()) {
                        EntityLiving entityliving = (EntityLiving) iterator.next();
                        double d0 = this.h(entityliving);

                        if (d0 < 16.0D) {
                            double d1 = 1.0D - Math.sqrt(d0) / 4.0D;

                            if (entityliving == movingobjectposition.entity) {
                                d1 = 1.0D;
                            }

                            if (GlobalConfig.getInstance().isEntityHider() && entityliving instanceof EntityPlayer
                                    && !((EntityPlayer) entityliving).getBukkitEntity()
                                            .canSee(this.getBukkitEntity())) {
                                continue;
                            }

                            // CraftBukkit start
                            affected.put((LivingEntity) entityliving.getBukkitEntity(), d1);
                        }
                    }

                    org.bukkit.event.entity.PotionSplashEvent event = org.bukkit.craftbukkit.event.CraftEventFactory
                            .callPotionSplashEvent(this, affected);
                    if (!event.isCancelled() && list != null && !list.isEmpty()) { // do not process effects if there
                                                                                   // are no effects to process
                        for (LivingEntity victim : event.getAffectedEntities()) {
                            if (!(victim instanceof CraftLivingEntity)) {
                                continue;
                            }

                            EntityLiving entityliving = ((CraftLivingEntity) victim).getHandle();
                            double d1 = event.getIntensity(victim);
                            // CraftBukkit end

                            Iterator iterator1 = list.iterator();

                            while (iterator1.hasNext()) {
                                MobEffect mobeffect = (MobEffect) iterator1.next();
                                int i = mobeffect.getEffectId();

                                // CraftBukkit start - Abide by PVP settings - for players only!
                                if (!this.world.pvpMode && this.getShooter() instanceof EntityPlayer
                                        && entityliving instanceof EntityPlayer && entityliving != this.getShooter()) {
                                    // Block SLOWER_MOVEMENT, SLOWER_DIG, HARM, BLINDNESS, HUNGER, WEAKNESS and
                                    // POISON potions
                                    if (i == 2 || i == 4 || i == 7 || i == 15 || i == 17 || i == 18 || i == 19)
                                        continue;
                                }
                                // CraftBukkit end

                                if (MobEffectList.byId[i].isInstant()) {
                                    MobEffectList.byId[i].applyInstantEffect(this, this.getShooter(), entityliving,
                                            mobeffect.getAmplifier(), d1);
                                } else {
                                    int j = (int) (d1 * (double) mobeffect.getDuration() + 0.5D);

                                    if (j > 20) {
                                        entityliving.addEffect(new MobEffect(i, j, mobeffect.getAmplifier()));
                                    }
                                }
                            }
                        }
                    }
                }
            }

            this.world.triggerEffect(2002, new BlockPosition(this), this.getPotionValue());
            this.die();
        }

    }

    public void a(NBTTagCompound nbttagcompound) {
        super.a(nbttagcompound);
        if (nbttagcompound.hasKeyOfType("Potion", 10)) {
            this.item = ItemStack.createStack(nbttagcompound.getCompound("Potion"));
        } else {
            this.setPotionValue(nbttagcompound.getInt("potionValue"));
        }

        if (this.item == null) {
            this.die();
        }

    }

    public void b(NBTTagCompound nbttagcompound) {
        super.b(nbttagcompound);
        if (this.item != null) {
            nbttagcompound.set("Potion", this.item.save(new NBTTagCompound()));
        }

    }
}
