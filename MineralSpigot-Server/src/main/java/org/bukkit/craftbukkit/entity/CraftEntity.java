package org.bukkit.craftbukkit.entity;

import com.google.common.base.Preconditions;

import gg.mineral.server.config.GlobalConfig;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import net.minecraft.server.*;

import org.bukkit.EntityEffect;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.permissions.PermissibleBase;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.permissions.ServerOperator;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;

public abstract class CraftEntity implements org.bukkit.entity.Entity {
    private static final PermissibleBase perm = new PermissibleBase(new ServerOperator() {

        @Override
        public boolean isOp() {
            return false;
        }

        @Override
        public void setOp(boolean value) {

        }
    });

    protected final CraftServer server;
    protected Entity entity;
   // private EntityDamageEvent lastDamageEvent;

    public CraftEntity(final CraftServer server, final Entity entity) {
        this.server = server;
        this.entity = entity;
    }

    public static CraftEntity getEntity(CraftServer server, Entity entity) {
        /**
         * Order is *EXTREMELY* important -- keep it right! =D
         */
        if (entity instanceof EntityLiving) {
            // Players
            if (entity instanceof EntityHuman) {
                if (entity instanceof EntityPlayer) {
                    return new CraftPlayer(server, (EntityPlayer) entity);
                } else {
                    return new CraftHumanEntity(server, (EntityHuman) entity);
                }
            }
            // Water Animals
            else if (entity instanceof EntityWaterAnimal) {
                if (entity instanceof EntitySquid) {
                    return new CraftSquid(server, (EntitySquid) entity);
                } else {
                    return new CraftWaterMob(server, (EntityWaterAnimal) entity);
                }
            } else if (entity instanceof EntityCreature) {
                // Animals
                if (entity instanceof EntityAnimal) {
                    if (entity instanceof EntityChicken) {
                        return new CraftChicken(server, (EntityChicken) entity);
                    } else if (entity instanceof EntityCow) {
                        if (entity instanceof EntityMushroomCow) {
                            return new CraftMushroomCow(server, (EntityMushroomCow) entity);
                        } else {
                            return new CraftCow(server, (EntityCow) entity);
                        }
                    } else if (entity instanceof EntityPig) {
                        return new CraftPig(server, (EntityPig) entity);
                    } else if (entity instanceof EntityTameableAnimal) {
                        if (entity instanceof EntityWolf) {
                            return new CraftWolf(server, (EntityWolf) entity);
                        } else if (entity instanceof EntityOcelot) {
                            return new CraftOcelot(server, (EntityOcelot) entity);
                        }
                    } else if (entity instanceof EntitySheep) {
                        return new CraftSheep(server, (EntitySheep) entity);
                    } else if (entity instanceof EntityHorse) {
                        return new CraftHorse(server, (EntityHorse) entity);
                    } else if (entity instanceof EntityRabbit) {
                        return new CraftRabbit(server, (EntityRabbit) entity);
                    } else {
                        return new CraftAnimals(server, (EntityAnimal) entity);
                    }
                }
                // Monsters
                else if (entity instanceof EntityMonster) {
                    if (entity instanceof EntityZombie) {
                        if (entity instanceof EntityPigZombie) {
                            return new CraftPigZombie(server, (EntityPigZombie) entity);
                        } else {
                            return new CraftZombie(server, (EntityZombie) entity);
                        }
                    } else if (entity instanceof EntityCreeper) {
                        return new CraftCreeper(server, (EntityCreeper) entity);
                    } else if (entity instanceof EntityEnderman) {
                        return new CraftEnderman(server, (EntityEnderman) entity);
                    } else if (entity instanceof EntitySilverfish) {
                        return new CraftSilverfish(server, (EntitySilverfish) entity);
                    } else if (entity instanceof EntityGiantZombie) {
                        return new CraftGiant(server, (EntityGiantZombie) entity);
                    } else if (entity instanceof EntitySkeleton) {
                        return new CraftSkeleton(server, (EntitySkeleton) entity);
                    } else if (entity instanceof EntityBlaze) {
                        return new CraftBlaze(server, (EntityBlaze) entity);
                    } else if (entity instanceof EntityWitch) {
                        return new CraftWitch(server, (EntityWitch) entity);
                    } else if (entity instanceof EntityWither) {
                        return new CraftWither(server, (EntityWither) entity);
                    } else if (entity instanceof EntitySpider) {
                        if (entity instanceof EntityCaveSpider) {
                            return new CraftCaveSpider(server, (EntityCaveSpider) entity);
                        } else {
                            return new CraftSpider(server, (EntitySpider) entity);
                        }
                    } else if (entity instanceof EntityEndermite) {
                        return new CraftEndermite(server, (EntityEndermite) entity);
                    } else if (entity instanceof EntityGuardian) {
                        return new CraftGuardian(server, (EntityGuardian) entity);
                    }

                    else {
                        return new CraftMonster(server, (EntityMonster) entity);
                    }
                } else if (entity instanceof EntityGolem) {
                    if (entity instanceof EntitySnowman) {
                        return new CraftSnowman(server, (EntitySnowman) entity);
                    } else if (entity instanceof EntityIronGolem) {
                        return new CraftIronGolem(server, (EntityIronGolem) entity);
                    }
                } else if (entity instanceof EntityVillager) {
                    return new CraftVillager(server, (EntityVillager) entity);
                } else {
                    return new CraftCreature(server, (EntityCreature) entity);
                }
            }
            // Slimes are a special (and broken) case
            else if (entity instanceof EntitySlime) {
                if (entity instanceof EntityMagmaCube) {
                    return new CraftMagmaCube(server, (EntityMagmaCube) entity);
                } else {
                    return new CraftSlime(server, (EntitySlime) entity);
                }
            }
            // Flying
            else if (entity instanceof EntityFlying) {
                if (entity instanceof EntityGhast) {
                    return new CraftGhast(server, (EntityGhast) entity);
                } else {
                    return new CraftFlying(server, (EntityFlying) entity);
                }
            } else if (entity instanceof EntityEnderDragon) {
                return new CraftEnderDragon(server, (EntityEnderDragon) entity);
            }
            // Ambient
            else if (entity instanceof EntityAmbient) {
                if (entity instanceof EntityBat) {
                    return new CraftBat(server, (EntityBat) entity);
                } else {
                    return new CraftAmbient(server, (EntityAmbient) entity);
                }
            } else if (entity instanceof EntityArmorStand) {
                return new CraftArmorStand(server, (EntityArmorStand) entity);
            } else {
                return new CraftLivingEntity(server, (EntityLiving) entity);
            }
        } else if (entity instanceof EntityComplexPart) {
            EntityComplexPart part = (EntityComplexPart) entity;
            if (part.owner instanceof EntityEnderDragon) {
                return new CraftEnderDragonPart(server, (EntityComplexPart) entity);
            } else {
                return new CraftComplexPart(server, (EntityComplexPart) entity);
            }
        } else if (entity instanceof EntityExperienceOrb) {
            return new CraftExperienceOrb(server, (EntityExperienceOrb) entity);
        } else if (entity instanceof EntityArrow) {
            return new CraftArrow(server, (EntityArrow) entity);
        } else if (entity instanceof EntityBoat) {
            return new CraftBoat(server, (EntityBoat) entity);
        } else if (entity instanceof EntityProjectile) {
            if (entity instanceof EntityEgg) {
                return new CraftEgg(server, (EntityEgg) entity);
            } else if (entity instanceof EntitySnowball) {
                return new CraftSnowball(server, (EntitySnowball) entity);
            } else if (entity instanceof EntityPotion) {
                return new CraftThrownPotion(server, (EntityPotion) entity);
            } else if (entity instanceof EntityEnderPearl) {
                return new CraftEnderPearl(server, (EntityEnderPearl) entity);
            } else if (entity instanceof EntityThrownExpBottle) {
                return new CraftThrownExpBottle(server, (EntityThrownExpBottle) entity);
            }
        } else if (entity instanceof EntityFallingBlock) {
            return new CraftFallingSand(server, (EntityFallingBlock) entity);
        } else if (entity instanceof EntityFireball) {
            if (entity instanceof EntitySmallFireball) {
                return new CraftSmallFireball(server, (EntitySmallFireball) entity);
            } else if (entity instanceof EntityLargeFireball) {
                return new CraftLargeFireball(server, (EntityLargeFireball) entity);
            } else if (entity instanceof EntityWitherSkull) {
                return new CraftWitherSkull(server, (EntityWitherSkull) entity);
            } else {
                return new CraftFireball(server, (EntityFireball) entity);
            }
        } else if (entity instanceof EntityEnderSignal) {
            return new CraftEnderSignal(server, (EntityEnderSignal) entity);
        } else if (entity instanceof EntityEnderCrystal) {
            return new CraftEnderCrystal(server, (EntityEnderCrystal) entity);
        } else if (entity instanceof EntityFishingHook) {
            return new CraftFish(server, (EntityFishingHook) entity);
        } else if (entity instanceof EntityItem) {
            return new CraftItem(server, (EntityItem) entity);
        } else if (entity instanceof EntityWeather) {
            if (entity instanceof EntityLightning) {
                return new CraftLightningStrike(server, (EntityLightning) entity);
            } else {
                return new CraftWeather(server, (EntityWeather) entity);
            }
        } else if (entity instanceof EntityMinecartAbstract) {
            if (entity instanceof EntityMinecartFurnace) {
                return new CraftMinecartFurnace(server, (EntityMinecartFurnace) entity);
            } else if (entity instanceof EntityMinecartChest) {
                return new CraftMinecartChest(server, (EntityMinecartChest) entity);
            } else if (entity instanceof EntityMinecartTNT) {
                return new CraftMinecartTNT(server, (EntityMinecartTNT) entity);
            } else if (entity instanceof EntityMinecartHopper) {
                return new CraftMinecartHopper(server, (EntityMinecartHopper) entity);
            } else if (entity instanceof EntityMinecartMobSpawner) {
                return new CraftMinecartMobSpawner(server, (EntityMinecartMobSpawner) entity);
            } else if (entity instanceof EntityMinecartRideable) {
                return new CraftMinecartRideable(server, (EntityMinecartRideable) entity);
            } else if (entity instanceof EntityMinecartCommandBlock) {
                return new CraftMinecartCommand(server, (EntityMinecartCommandBlock) entity);
            }
        } else if (entity instanceof EntityHanging) {
            if (entity instanceof EntityPainting) {
                return new CraftPainting(server, (EntityPainting) entity);
            } else if (entity instanceof EntityItemFrame) {
                return new CraftItemFrame(server, (EntityItemFrame) entity);
            } else if (entity instanceof EntityLeash) {
                return new CraftLeash(server, (EntityLeash) entity);
            } else {
                return new CraftHanging(server, (EntityHanging) entity);
            }
        } else if (entity instanceof EntityTNTPrimed) {
            return new CraftTNTPrimed(server, (EntityTNTPrimed) entity);
        } else if (entity instanceof EntityFireworks) {
            return new CraftFirework(server, (EntityFireworks) entity);
        }

        throw new AssertionError("Unknown entity " + (entity == null ? null : entity.getClass()));
    }

    public Location getLocation() {
        return new Location(getWorld(), entity.locX, entity.locY, entity.locZ, entity.yaw, entity.pitch);
    }

    public Location getLocation(Location loc) {
        if (loc != null) {
            loc.setWorld(getWorld());
            loc.setX(entity.locX);
            loc.setY(entity.locY);
            loc.setZ(entity.locZ);
            loc.setYaw(entity.yaw);
            loc.setPitch(entity.pitch);
        }

        return loc;
    }

    public Vector getVelocity() {
        return new Vector(entity.motX, entity.motY, entity.motZ);
    }

    public void setVelocity(Vector vel) {
        // Paper start - warn server owners when plugins try to set super high
        // velocities
        if (GlobalConfig.getInstance().isWarnForExcessiveVelocity()) {
            if (vel.getX() > 4 || vel.getX() < -4 || vel.getY() > 4 || vel.getY() < -4 || vel.getZ() > 4
                    || vel.getZ() < -4) {
                getServer().getLogger().warning("Excessive velocity set detected: tried to set velocity of entity #"
                        + getEntityId() + " to (" + vel.getX() + "," + vel.getY() + "," + vel.getZ() + ").");
                Thread.dumpStack();
            }
        }
        // Paper end

        entity.motX = vel.getX();
        entity.motY = vel.getY();
        entity.motZ = vel.getZ();
        entity.velocityChanged = true;
    }

    public boolean isOnGround() {
        if (entity instanceof EntityArrow) {
            return ((EntityArrow) entity).isInGround();
        }
        return entity.onGround;
    }

    public World getWorld() {
        return entity.world.getWorld();
    }

    public boolean teleport(Location location) {
        return teleport(location, TeleportCause.PLUGIN);
    }

    public boolean teleport(Location location, TeleportCause cause) {
        if (entity.passenger != null || entity.dead) {
            return false;
        }

        // If this entity is riding another entity, we must dismount before teleporting.
        entity.mount(null);

        // Spigot start
        if (!location.getWorld().equals(getWorld())) {
            entity.teleportTo(location, cause.equals(TeleportCause.NETHER_PORTAL));
            return true;
        }

        // entity.world = ((CraftWorld) location.getWorld()).getHandle();
        // Spigot end
        entity.setLocation(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
        entity.world.entityJoinedWorld(entity, false); // PaperSpigot - Fix issues with entities not being switched to
                                                       // their new chunk
        // entity.setLocation() throws no event, and so cannot be cancelled
        return true;
    }

    public boolean teleport(org.bukkit.entity.Entity destination) {
        return teleport(destination.getLocation());
    }

    public boolean teleport(org.bukkit.entity.Entity destination, TeleportCause cause) {
        return teleport(destination.getLocation(), cause);
    }

    public List<org.bukkit.entity.Entity> getNearbyEntities(double x, double y, double z) {
        List<Entity> notchEntityList = entity.world.a(entity, entity.getBoundingBox().grow(x, y, z), null);
        List<org.bukkit.entity.Entity> bukkitEntityList = new java.util.ArrayList<org.bukkit.entity.Entity>(
                notchEntityList.size());

        for (Entity e : notchEntityList) {
            bukkitEntityList.add(e.getBukkitEntity());
        }
        return bukkitEntityList;
    }

    public int getEntityId() {
        return entity.getId();
    }

    public int getFireTicks() {
        return entity.fireTicks;
    }

    public int getMaxFireTicks() {
        return entity.maxFireTicks;
    }

    public void setFireTicks(int ticks) {
        entity.fireTicks = ticks;
    }

    public void remove() {
        entity.dead = true;
    }

    public boolean isDead() {
        return !entity.isAlive();
    }

    public boolean isValid() {
        return entity.isAlive() && entity.valid;
    }

    public Server getServer() {
        return server;
    }

    public Vector getMomentum() {
        return getVelocity();
    }

    public void setMomentum(Vector value) {
        setVelocity(value);
    }

    public org.bukkit.entity.Entity getPassenger() {
        return isEmpty() ? null : getHandle().passenger.getBukkitEntity();
    }

    public boolean setPassenger(org.bukkit.entity.Entity passenger) {
        Preconditions.checkArgument(!this.equals(passenger), "Entity cannot ride itself.");
        if (passenger instanceof CraftEntity) {
            ((CraftEntity) passenger).getHandle().mount(getHandle());
            return true;
        } else {
            return false;
        }
    }

    public boolean isEmpty() {
        return getHandle().passenger == null;
    }

    public boolean eject() {
        if (getHandle().passenger == null) {
            return false;
        }

        getHandle().passenger.mount(null);
        return true;
    }

    public float getFallDistance() {
        return getHandle().fallDistance;
    }

    public void setFallDistance(float distance) {
        getHandle().fallDistance = distance;
    }

    public void setLastDamageCause(EntityDamageEvent event) {
        //lastDamageEvent = event;
    }

    public EntityDamageEvent getLastDamageCause() {
        //return lastDamageEvent;
        return null;
    }

    public UUID getUniqueId() {
        return getHandle().getUniqueID();
    }

    public int getTicksLived() {
        return getHandle().ticksLived;
    }

    public void setTicksLived(int value) {
        if (value <= 0) {
            throw new IllegalArgumentException("Age must be at least 1 tick");
        }
        getHandle().ticksLived = value;
    }

    public Entity getHandle() {
        return entity;
    }

    public void playEffect(EntityEffect type) {
        this.getHandle().world.broadcastEntityEffect(getHandle(), type.getData());
    }

    public void setHandle(final Entity entity) {
        this.entity = entity;
    }

    @Override
    public String toString() {
        return "CraftEntity{" + "id=" + getEntityId() + '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final CraftEntity other = (CraftEntity) obj;
        return (this.getEntityId() == other.getEntityId());
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 29 * hash + this.getEntityId();
        return hash;
    }

    public void setMetadata(String metadataKey, MetadataValue newMetadataValue) {
        server.getEntityMetadata().setMetadata(this, metadataKey, newMetadataValue);
    }

    public List<MetadataValue> getMetadata(String metadataKey) {
        return server.getEntityMetadata().getMetadata(this, metadataKey);
    }

    public boolean hasMetadata(String metadataKey) {
        return server.getEntityMetadata().hasMetadata(this, metadataKey);
    }

    public void removeMetadata(String metadataKey, Plugin owningPlugin) {
        server.getEntityMetadata().removeMetadata(this, metadataKey, owningPlugin);
    }

    public boolean isInsideVehicle() {
        return getHandle().vehicle != null;
    }

    public boolean leaveVehicle() {
        if (getHandle().vehicle == null) {
            return false;
        }

        getHandle().mount(null);
        return true;
    }

    public org.bukkit.entity.Entity getVehicle() {
        if (getHandle().vehicle == null) {
            return null;
        }

        return getHandle().vehicle.getBukkitEntity();
    }

    @Override
    public void setCustomName(String name) {
        if (name == null) {
            name = "";
        }

        getHandle().setCustomName(name);
    }

    @Override
    public String getCustomName() {
        String name = getHandle().getCustomName();

        if (name == null || name.length() == 0) {
            return null;
        }

        return name;
    }

    @Override
    public void setCustomNameVisible(boolean flag) {
        getHandle().setCustomNameVisible(flag);
    }

    @Override
    public boolean isCustomNameVisible() {
        return getHandle().getCustomNameVisible();
    }

    @Override
    public void sendMessage(String message) {

    }

    @Override
    public void sendMessage(String[] messages) {

    }

    @Override
    public String getName() {
        return getHandle().getName();
    }

    @Override
    public boolean isPermissionSet(String name) {
        return perm.isPermissionSet(name);
    }

    @Override
    public boolean isPermissionSet(Permission perm) {
        return CraftEntity.perm.isPermissionSet(perm);
    }

    @Override
    public boolean hasPermission(String name) {
        return perm.hasPermission(name);
    }

    @Override
    public boolean hasPermission(Permission perm) {
        return this.perm.hasPermission(perm);
    }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin, String name, boolean value) {
        return perm.addAttachment(plugin, name, value);
    }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin) {
        return perm.addAttachment(plugin);
    }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin, String name, boolean value, int ticks) {
        return perm.addAttachment(plugin, name, value, ticks);
    }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin, int ticks) {
        return perm.addAttachment(plugin, ticks);
    }

    @Override
    public void removeAttachment(PermissionAttachment attachment) {
        perm.removeAttachment(attachment);
    }

    @Override
    public void recalculatePermissions() {
        perm.recalculatePermissions();
    }

    @Override
    public Set<PermissionAttachmentInfo> getEffectivePermissions() {
        return perm.getEffectivePermissions();
    }

    @Override
    public boolean isOp() {
        return perm.isOp();
    }

    @Override
    public void setOp(boolean value) {
        perm.setOp(value);
    }

    // Spigot start
    private final Spigot spigot = new Spigot() {
        @Override
        public boolean isInvulnerable() {
            return getHandle().isInvulnerable(net.minecraft.server.DamageSource.GENERIC);
        }
    };

    public Spigot spigot() {
        return spigot;
    }
    // Spigot end
}
