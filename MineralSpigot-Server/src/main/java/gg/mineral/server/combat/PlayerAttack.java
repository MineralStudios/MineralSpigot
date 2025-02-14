package gg.mineral.server.combat;

import java.util.*;
import java.util.Map.Entry;

import com.google.common.base.Function;

import com.google.common.base.Functions;
import gg.mineral.api.collection.GlueList;
import lombok.val;
import net.minecraft.server.*;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;


import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.event.entity.EntityCombustByEntityEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDamageEvent.DamageModifier;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerVelocityEvent;


public class PlayerAttack implements Runnable {
    EntityPlayer attacker, entity;
    DamageSource damageSource;
    KnockbackProfile knockback;
    short extraKnockbackMagnitude = 0;
    float extraDamage = 0.0F;
    int fireAspectLevel = 0;
    private static final Random random = new Random();
    private static final Function<? super Double, Double> ZERO_FUNCTION = Functions.constant(-0.0);

    public PlayerAttack(EntityPlayer attacker, EntityPlayer entity, KnockbackProfile knockback) {
        this.knockback = knockback;
        this.attacker = attacker;
        this.entity = entity;
        this.damageSource = DamageSource.playerAttack(attacker);
        ItemStack itemstack = getItemInHand(attacker);

        if (itemstack == null) {
            return;
        }

        NBTTagList nbttaglist = itemstack.getEnchantments();

        if (nbttaglist == null) {
            return;
        }

        boolean breakLoop1 = false;
        boolean breakLoop2 = false;
        boolean breakLoop3 = false;

        for (int i = 0; i < nbttaglist.size(); ++i) {
            if (breakLoop1 && breakLoop2 && breakLoop3) {
                return;
            }

            NBTTagCompound nbtTagCompound = nbttaglist.get(i);
            short id = nbtTagCompound.getShort("id");

            if (id == 16) {
                extraDamage = nbtTagCompound.getShort("lvl") * 1.25F;
                breakLoop1 = true;
                continue;
            }

            if (id == 19) {
                extraKnockbackMagnitude = nbtTagCompound.getShort("lvl");
                breakLoop2 = true;
                continue;
            }

            if (id == 20) {
                fireAspectLevel = nbtTagCompound.getShort("lvl");
                breakLoop3 = true;
            }
        }
    }

    public static float getAttackDamage(int id) {
        return switch (id) {
            case 276 -> 7.0F;
            case 267, 279 -> 6.0F;
            case 258, 278, 272 -> 5.0F;
            case 275, 257, 268, 277, 283 -> 4.0F;
            case 274, 286, 256, 271 -> 3.0F;
            case 270, 273, 285 -> 2.0F;
            default -> 1.0F;
        };
    }

    @Override
    public void run() {

        if ((float) entity.noDamageTicks > (float) entity.maxNoDamageTicks / 2.0F) {
            return;
        }

        if (attacker.playerInteractManager.getGameMode() == WorldSettings.EnumGamemode.SPECTATOR) {
            attacker.setSpectatorTarget(entity);
            return;
        }

        if (entity.playerInteractManager.getGameMode() == WorldSettings.EnumGamemode.CREATIVE) {
            return;
        }

        if (entity.getHealth() <= 0.0F) {
            return;
        }

        float attackDamage = 1.0F;

        ItemStack i = getItemInHand(attacker);

        if (i != null) {
            attackDamage = getAttackDamage(Item.getId(i.getItem()));
        }

        for (Entry<Integer, MobEffect> entry : attacker.effects.entrySet()) {
            Integer key = entry.getKey();
            if (key == 5) {
                float multipliedDamage = attackDamage * 1.3F;
                float strengthAmount = entry.getValue().getAmplifier() * multipliedDamage;
                attackDamage += strengthAmount;
                break;
            }

            if (key == 18) {
                float weaknessAmount = entry.getValue().getAmplifier() * 0.5F;
                attackDamage -= weaknessAmount;
                break;
            }
        }

        if (attackDamage < 0.0F && extraDamage < 0.0F) {
            return;
        }

        boolean inAir = !attacker.onGround && attacker.fallDistance > 0.0F;

        if (inAir) {
            attackDamage *= 1.5F;
        }

        attackDamage += extraDamage;

        if (entity.isInvulnerable(damageSource)) {
            return;
        }

        entity.aB = 1.5F;

        if (!damageAllowed(attackDamage)) {
            return;
        }

        entity.lastDamage = attackDamage;
        entity.noDamageTicks = entity.maxNoDamageTicks;
        entity.hurtTicks = entity.av = 10;

        executeKnockback();
        entity.world.broadcastEntityEffect(entity, (byte) 2);
        entity.makeSound("game.player.hurt", 1.0F, (random.nextFloat()
                - random.nextFloat()) * 0.2F + 1.0F);

        if (fireAspectLevel > 0 && !entity.isBurning()) {
            EntityCombustByEntityEvent combustEvent = new EntityCombustByEntityEvent(attacker.getBukkitEntity(),
                    entity.getBukkitEntity(), 1);
            Bukkit.getPluginManager().callEvent(combustEvent);

            if (!combustEvent.isCancelled()) {
                entity.setOnFire(combustEvent.getDuration());
            }
        }

        entity.aw = 0.0F;
        entity.lastDamager = attacker;
        entity.hurtTimestamp = entity.ticksLived;
        entity.killer = attacker;

        if (entity.getHealth() <= 0.0F) {
            this.die();
        }

        if (inAir) {
            attacker.b(entity);
        }

        if (extraDamage > 0.0F) {
            attacker.c(entity);
        }

        attacker.p(entity);
        EnchantmentManager.a(entity, attacker);
        EnchantmentManager.b(attacker, entity);

        if (fireAspectLevel > 0) {
            EntityCombustByEntityEvent combustEvent = new EntityCombustByEntityEvent(attacker.getBukkitEntity(),
                    entity.getBukkitEntity(), fireAspectLevel * 4);
            Bukkit.getPluginManager().callEvent(combustEvent);

            if (!combustEvent.isCancelled()) {
                entity.setOnFire(combustEvent.getDuration());
            }
        }

        attacker.applyExhaustion(0.3F);
    }

    public void executeKnockback() {
        double oldX = entity.motX;
        double oldY = entity.motY;
        double oldZ = entity.motZ;

        knockback.callFirstStage(attacker, entity);

        entity.velocityChanged = true;

        knockback.callSecondStage(attacker, entity, extraKnockbackMagnitude);

        val event = new PlayerVelocityEvent(entity.getBukkitEntity(),
                entity.getBukkitEntity().getVelocity());
        Bukkit.getPluginManager().callEvent(event);
        if (!event.isCancelled()) {
            entity.getBukkitEntity().setVelocityDirect(event.getVelocity());
            entity.playerConnection.sendPacket(new PacketPlayOutEntityVelocity(entity));
        }

        entity.velocityChanged = false;
        entity.fallDistance = 0.0F;
        entity.motX = oldX;
        entity.motY = oldY;
        entity.motZ = oldZ;
    }

    public ItemStack getItemInHand(EntityHuman e) {
        return e.inventory.getItemInHand();
    }

    public boolean damageAllowed(double attackDamage) {
        double originalDamage = attackDamage;

        Function<Double, Double> hardHat = new Function<Double, Double>() {
            @Override
            public Double apply(Double f) {
                return -0.0;
            }
        };

        float hardHatModifier = hardHat.apply(attackDamage).floatValue();
        attackDamage += hardHatModifier;

        Function<Double, Double> blocking = f -> {
            if (entity.isBlocking() && f > 0.0F) {
                return -(f - ((1.0F + f) * 0.7F));
            }

            return -0.0;
        };

        float blockingModifier = blocking.apply(attackDamage).floatValue();
        attackDamage += blockingModifier;

        Function<Double, Double> armor = new Function<Double, Double>() {
            @Override
            public Double apply(Double f) {
                return -(f - applyArmorModifier(damageSource, f.floatValue()));
            }
        };

        float armorModifier = armor.apply(attackDamage).floatValue();
        attackDamage += armorModifier;

        Function<Double, Double> resistance = new Function<Double, Double>() {
            @Override
            public Double apply(Double f) {
                if (entity.hasEffect(MobEffectList.RESISTANCE)) {
                    int i = (entity.getEffect(MobEffectList.RESISTANCE).getAmplifier() + 1) * 5;
                    int j = 25 - i;
                    float f1 = f.floatValue() * (float) j;
                    return -(f - (f1 / 25.0F));
                }
                return -0.0;
            }
        };

        float resistanceModifier = resistance.apply(attackDamage).floatValue();
        attackDamage += resistanceModifier;

        Function<Double, Double> magic = f -> -(f - applyMagicModifier(damageSource, f.floatValue()));

        float magicModifier = magic.apply(attackDamage).floatValue();
        attackDamage += magicModifier;

        Function<Double, Double> absorption = f -> -(Math.max(f - Math.max(f - entity.getAbsorptionHearts(), 0.0F), 0.0F));

        float absorptionModifier = absorption.apply(attackDamage).floatValue();
        EntityDamageEvent event = handleLivingEntityDamageEvent(originalDamage, hardHatModifier, blockingModifier,
                armorModifier, resistanceModifier, magicModifier, absorptionModifier, hardHat, blocking, armor,
                resistance, magic, absorption);

        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled()) {
            event.getEntity().setLastDamageCause(event);
            return false;
        }

        // Backtrack system
        attacker.getBacktrackSystem()
                .onAttack();

        double dX = entity.locX - attacker.locX;
        double dZ = entity.locZ - attacker.locZ;
        double dY = entity.locY - attacker.locY;
        entity.getBacktrackSystem()
                .onDamage(net.minecraft.server.MathHelper.sqrt(dX * dX + dZ * dZ + dY * dY));
        // Backtrack system end

        attackDamage = event.getFinalDamage();
        float armorDamage = (float) (event.getDamage() + event.getDamage(DamageModifier.BLOCKING)
                + event.getDamage(DamageModifier.HARD_HAT));
        damageArmor(armorDamage);
        absorptionModifier = (float) -event.getDamage(DamageModifier.ABSORPTION);
        entity.setAbsorptionHearts(Math.max(entity.getAbsorptionHearts() - absorptionModifier, 0.0F));

        if (attackDamage != 0.0F) {
            entity.applyExhaustion(damageSource.getExhaustionCost());
            float health = entity.getHealth();
            entity.setHealth((float) (health - attackDamage));
            entity.bs().a(damageSource, health, (float) attackDamage);
            return true;
        }

        return true;
    }

    public EntityDamageEvent handleLivingEntityDamageEvent(double rawDamage, double hardHatModifier,
                                                           double blockingModifier, double armorModifier, double resistanceModifier, double magicModifier,
                                                           double absorptionModifier, Function<Double, Double> hardHat, Function<Double, Double> blocking,
                                                           Function<Double, Double> armor, Function<Double, Double> resistance, Function<Double, Double> magic,
                                                           Function<Double, Double> absorption) {
        Map<DamageModifier, Double> modifiers = new EnumMap<DamageModifier, Double>(DamageModifier.class);
        Map<DamageModifier, Function<? super Double, Double>> modifierFunctions = new EnumMap<DamageModifier, Function<? super Double, Double>>(
                DamageModifier.class);
        modifiers.put(DamageModifier.BASE, rawDamage);
        modifierFunctions.put(DamageModifier.BASE, ZERO_FUNCTION);
        modifiers.put(DamageModifier.BLOCKING, blockingModifier);
        modifierFunctions.put(DamageModifier.BLOCKING, blocking);
        modifiers.put(DamageModifier.ARMOR, armorModifier);
        modifierFunctions.put(DamageModifier.ARMOR, armor);
        modifiers.put(DamageModifier.RESISTANCE, resistanceModifier);
        modifierFunctions.put(DamageModifier.RESISTANCE, resistance);
        modifiers.put(DamageModifier.MAGIC, magicModifier);
        modifierFunctions.put(DamageModifier.MAGIC, magic);
        modifiers.put(DamageModifier.ABSORPTION, absorptionModifier);
        modifierFunctions.put(DamageModifier.ABSORPTION, absorption);
        return new EntityDamageByEntityEvent(attacker.getBukkitEntity(), entity.getBukkitEntity(),
                DamageCause.ENTITY_ATTACK, modifiers, modifierFunctions);
    }

    public void die() {
        if (entity.dead) {
            return;
        }

        List<org.bukkit.inventory.ItemStack> loot = new GlueList<>();
        boolean keepInventory = entity.world.getGameRules().getBoolean("keepInventory");

        if (!keepInventory) {

            for (int i = 0; i < entity.inventory.items.length; ++i) {
                ItemStack itemStack = entity.inventory.items[i];

                if (itemStack != null) {
                    loot.add(CraftItemStack.asCraftMirror(itemStack));
                }
            }

            for (int i = 0; i < entity.inventory.armor.length; ++i) {
                ItemStack itemStack = entity.inventory.armor[i];

                if (itemStack != null) {
                    loot.add(CraftItemStack.asCraftMirror(itemStack));
                }
            }
        }

        String originalDeathMessage = entity.getName() + " died";

        PlayerDeathEvent event = callPlayerDeathEvent(entity, loot, originalDeathMessage, keepInventory);

        String eventDeathMessage = event.getDeathMessage();
        String deathMessage = eventDeathMessage != null && !eventDeathMessage.isEmpty() ? eventDeathMessage
                : originalDeathMessage;

        if (entity.world.getGameRules().getBoolean("showDeathMessages")) {
            Bukkit.getServer().broadcastMessage(deathMessage);
        }

        if (!event.getKeepInventory()) {
            Arrays.fill(entity.inventory.items, null);

            Arrays.fill(entity.inventory.armor, null);
        }

        entity.closeInventory();
        entity.setSpectatorTarget(entity);

        entity.bs().g();
    }

    public float applyArmorModifier(DamageSource damagesource, float f) {
        if (!damagesource.ignoresArmor()) {
            int i = 25 - entity.br();
            float f1 = f * (float) i;

            f = f1 / 25.0F;
        }

        return f;
    }

    public void damageArmor(float f) {
        entity.inventory.a(f);
    }

    public float applyMagicModifier(DamageSource damagesource, float f) {
        if (damagesource.isStarvation()) {
            return f;
        }

        int i;
        int j;
        float f1;

        if (f <= 0.0F) {
            return 0.0F;
        }

        i = EnchantmentManager.a(entity.getEquipment(), damagesource);

        if (i > 20) {
            i = 20;
        }

        if (i > 0) {
            j = 25 - i;
            f1 = f * (float) j;
            f = f1 / 25.0F;
        }

        return f;
    }

    public static PlayerDeathEvent callPlayerDeathEvent(EntityPlayer victim, List<org.bukkit.inventory.ItemStack> drops,
                                                        String deathMessage, boolean keepInventory) {
        CraftPlayer entity = victim.getBukkitEntity();
        PlayerDeathEvent event = new PlayerDeathEvent(entity, drops, victim.getExpReward(), 0, deathMessage);
        event.setKeepInventory(keepInventory);
        World world = entity.getWorld();
        Bukkit.getPluginManager().callEvent(event);
        victim.keepLevel = event.getKeepLevel();
        victim.newLevel = event.getNewLevel();
        victim.newTotalExp = event.getNewTotalExp();
        victim.expToDrop = event.getDroppedExp();
        victim.newExp = event.getNewExp();

        if (event.getKeepInventory()) {
            return event;
        }

        for (org.bukkit.inventory.ItemStack stack : event.getDrops()) {
            if (stack == null || stack.getType() == Material.AIR)
                continue;
            world.dropItemNaturally(entity.getLocation(), stack);
        }

        return event;
    }
}