package gg.mineral.server.config;

import java.io.File;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Warning.WarningState;
import org.bukkit.command.Command;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.generator.ChunkGenerator;
import org.spigotmc.AntiXray;
import org.spigotmc.RestartCommand;
import org.spigotmc.TicksPerSecondCommand;
import org.spigotmc.WatchdogThread;

import gg.mineral.server.connection.PacketLimit;
import groovy.util.ConfigObject;
import groovy.util.ConfigSlurper;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import lombok.Data;
import lombok.Getter;
import net.minecraft.server.AttributeRanged;
import net.minecraft.server.EnumDifficulty;
import net.minecraft.server.GenericAttributes;
import net.minecraft.server.Items;
import net.minecraft.server.Packet;
import net.minecraft.server.WorldSettings;

@Data
public class GlobalConfig {

    private static org.apache.logging.log4j.Logger logger = LogManager.getLogger(GlobalConfig.class);

    @Getter
    private static GlobalConfig instance = new GlobalConfig();
    final Object2ObjectOpenHashMap<String, Command> commands = new Object2ObjectOpenHashMap<>();

    @Setting("server")
    private String brandName = "Mineral";
    @Setting("server")
    private String motd = "A Minecraft Server";
    @Setting("server")
    private String serverName = "Unknown Server";
    @Setting("server")
    private String serverId = "";
    @Setting("server")
    private int maxPlayers = 20;
    @Setting("server")
    private boolean whitelist = false;
    @Setting("server")
    private boolean disableEndCredits = false;
    @Setting("server")
    private String whitelistMessage = "You are not whitelisted on this server!";
    @Setting("server")
    private String serverFullMessage = "The server is full!";
    @Setting("server")
    private String outdatedClientMessage = "Outdated client! Please use {0}";
    @Setting("server")
    private String outdatedServerMessage = "Outdated server! I\'m still on {0}";
    @Setting("server")
    private String restartMessage = "Server is restarting";
    @Setting("server")
    private String shutdownMessage = "Server closed";
    @Setting("server")
    private String restartScript = "./start.sh";
    @Setting("server")
    private int playerSample = 12;
    @Setting("server")
    private int playerShuffleFrequency = 0;
    @Setting("server")
    private String permissionsFile = "permissions.yml";
    @Setting("server")
    private boolean enableQuery = false;
    @Setting("server")
    private boolean enableRcon = false;
    @Setting("rcon")
    private int rconPort = 0;
    @Setting("rcon")
    private String rconPassword = "";
    @Setting("query")
    private int queryPort = 0;
    @Setting("watchdog")
    private int timeoutTime = 60;
    @Setting("watchdog")
    private boolean restartOnCrash = true;
    @Setting("command")
    private boolean silentCommandBlocks = false;
    @Setting("command")
    private String[] spamExclusions = new String[0];
    @Setting("command")
    private String unknownCommandMessage = "Unknown command. Type \"/help\" for help.";
    @Setting("command")
    private int tabComplete = 0;
    @Setting("command")
    private boolean logCommands = true;
    @Setting("command")
    private String[] replaceCommands = new String[] { "setblock", "summon", "testforblock", "tellraw" };
    @Setting("command")
    private String[] commandBlockOverrides = new String[0];
    @Setting("command")
    private boolean overrideAllCommandBlockCommands = false;
    @Setting("command")
    private Map<String, String[]> commandAliases = new Object2ObjectLinkedOpenHashMap<String, String[]>();
    @Setting("command")
    private boolean enableCommandBlock = false;
    @Setting("permission")
    private int opPermissionLevel = 4;
    @Setting("permission")
    private boolean broadcastRconToOps = true;
    @Setting("permission")
    private boolean broadcastConsoleToOps = true;
    @Setting("performance")
    private boolean trackPluginScoreboards = false;
    @Setting("performance")
    private boolean optimizeTntMovement = false;
    @Setting("performance")
    private boolean optimizeLiquidExplosions = false;
    @Setting("performance")
    private boolean optimizeArmorStandMovement = false;
    @Setting("performance")
    private boolean interactLimitEnabled = true;
    @Setting("performance")
    private boolean useAsyncLighting = true;
    @Setting("performance")
    private boolean optimizeExplosions = true;
    @Setting("performance")
    private boolean cacheChunkMaps = true;
    @Setting("performance")
    private int containerUpdateTickRate = 1;
    @Setting("performance")
    private int userCacheCap = 1000;
    @Setting("performance")
    private boolean saveUserCacheOnStopOnly = false;
    @Setting("performance")
    private int intCacheLimit = 1024;
    @Setting("performance")
    private int chunksPerTick = 650;
    @Setting("performance")
    private int maxBulkChunk = 10;
    @Setting("performance")
    private boolean clearChunksOnTick = false;
    @Setting("performance")
    private int maxCollisionsPerEntity = 8;
    @Setting("performance")
    private int maxTntTicksPerTick = 100;
    @Setting("performance")
    private int tileMaxTickTime = 50;
    @Setting("performance")
    private int entityMaxTickTime = 50;
    @Setting("performance")
    private int monsterSpawnLimit = 70;
    @Setting("performance")
    private int animalSpawnLimit = 15;
    @Setting("performance")
    private int waterAnimalSpawnLimit = 5;
    @Setting("performance")
    private int ambientSpawnLimit = 15;
    @Setting("performance")
    private int chunkGCPeriod = 600;
    @Setting("performance")
    private int chunkGCLoadThresh = 0;
    @Setting("performance")
    private long maxTickTime = TimeUnit.MINUTES.toMillis(1L);
    @Setting("performance")
    private boolean disableArmorStands = true;
    @Setting("player")
    private boolean smoothTeleportation = false;
    @Setting("player")
    private boolean announcePlayerAchievements = true;
    @Setting("player")
    private boolean disablePlayerData = false;
    @Setting("player")
    private float blockBreakExhaustion = 0.025f;
    @Setting("player")
    private float playerSwimmingExhaustion = 0.015f;
    @Setting("player")
    private boolean allowBlockLocationTabCompletion = false;
    @Setting("player")
    private boolean disableStatSaving = false;
    @Setting("player")
    private Object2IntOpenHashMap<String> forcedStats = new Object2IntOpenHashMap<>();
    @Setting("player")
    private boolean filterCreativeItems = true;
    @Setting("player")
    private int viewDistance = 10;
    @Setting("player")
    private int playerTrackingRange = 48;
    @Setting("player")
    private boolean useExactLoginLocation = false;
    @Setting("player")
    private boolean pvp = true;
    @Setting("player")
    private boolean allowFlight = false;
    @Setting("player")
    private String resourcePack = "", resourcePackHash = "";
    @Setting("player")
    private boolean forceGamemode = false;
    @Setting("player")
    private int relativeMoveFrequency = 1;
    @Setting("player")
    private int idleTimeout = 0;
    @Setting("player")
    private int gamemode = WorldSettings.EnumGamemode.SURVIVAL.getId();
    @Setting("player.hunger")
    private float walkExhaustion = 0.2f;
    @Setting("player.hunger")
    private float sprintExhaustion = 0.8f;
    @Setting("player.hunger")
    private float combatExhaustion = 0.3f;
    @Setting("player.hunger")
    private float regenExhaustion = 3.0f;
    @Setting("combat")
    private float playerBlockingDamageMultiplier = 0.5f;
    @Setting("combat")
    private boolean disablePlayerCrits = false;
    @Setting("combat.backtrack")
    private boolean backtrackRandom = false;
    @Setting("combat.backtrack")
    private boolean backtrackEnabled = false;
    @Setting("combat.backtrack")
    private boolean comboMode = false;
    @Setting("combat.backtrack")
    private double delayDistanceMin; // in blocks
    @Setting("combat.backtrack")
    private double delayDistanceMax; // in blocks
    @Setting("combat.backtrack")
    private int delayFactor; // in milliseconds
    @Setting("combat.backtrack")
    private int decayFactor; // in milliseconds
    @Setting("combat.backtrack")
    private int maxDelayMs; // in milliseconds
    @Setting("combat.backtrack")
    private int delayResetTime; // in ticks (20 ticks per second)
    @Setting("combat.backtrack")
    private int rMin, rMax;
    @Setting("effect")
    private double strengthEffectModifier = 1.3D;
    @Setting("effect")
    private double weaknessEffectModifier = -0.5D;
    @Setting("entity")
    private boolean disableEntityAi = false;
    @Setting("entity")
    private int ticksPerAnimalSpawns = 400;
    @Setting("entity")
    private int ticksPerMonsterSpawns = 1;
    @Setting("entity")
    private double babyZombieMovementSpeed = 0.5d;
    @Setting("entity")
    private boolean allowUndeadHorseLeashing = false;
    @Setting("entity")
    private double squidMinSpawnHeight = 45.0d;
    @Setting("entity")
    private double squidMaxSpawnHeight = 63.0d;
    @Setting("entity")
    private int softDespawnDistance = 32;
    @Setting("entity")
    private int hardDespawnDistance = 128;
    @Setting("entity")
    private int tntEntityHeightNerf = 0;
    @Setting("entity")
    private boolean removeUnloadedEnderPearls = true;
    @Setting("entity")
    private boolean removeUnloadedTNTEntities = true;
    @Setting("entity")
    private boolean removeUnloadedFallingBlocks = true;
    @Setting("entity")
    private boolean loadUnloadedEnderPearls = false;
    @Setting("entity")
    private boolean loadUnloadedTNTEntities = false;
    @Setting("entity")
    private boolean loadUnloadedFallingBlocks = false;
    @Setting("entity")
    private boolean disableChestCatDetection = false;
    @Setting("entity")
    private boolean fixCannons = false;
    @Setting("entity")
    private boolean disableExplosionKnockback = false;
    @Setting("entity")
    private boolean disablePearlKnockback = false;
    @Setting("entity")
    private boolean disableTeleportationSuffocationCheck = false;
    @Setting("entity")
    private double maxHealth = 2048.0d;
    @Setting("entity")
    private double movementSpeed = 2048.0d;
    @Setting("entity")
    private double attackDamage = 2048.0d;
    @Setting("entity")
    private double expMerge = 3.0d;
    @Setting("entity")
    private byte mobSpawnRange = 4;
    @Setting("entity")
    private int animalActivationRange = 32;
    @Setting("entity")
    private int monsterActivationRange = 32;
    @Setting("entity")
    private int miscActivationRange = 16;
    @Setting("entity")
    private int animalTrackingRange = 48;
    @Setting("entity")
    private int monsterTrackingRange = 48;
    @Setting("entity")
    private int miscTrackingRange = 32;
    @Setting("entity")
    private int otherTrackingRange = 64;
    @Setting("entity")
    private boolean zombieAggressiveTowardsVillager = true;
    @Setting("entity")
    private boolean enableZombiePigmenPortalSpawns = true;
    @Setting("entity")
    private int hangingTickFrequency = 100;
    @Setting("entity")
    private boolean spawnAnimals = true;
    @Setting("entity")
    private boolean spawnNPCS = true;
    @Setting("entity")
    private boolean spawnMonsters = true;
    @Setting("entity")
    private boolean entityHider = true;
    @Setting("item")
    private int[] dataValueAllowedItems = new int[0];
    @Setting("item")
    private boolean stackableLavaBuckets = false, stackableWaterBuckets = false, stackableMilkBuckets = false,
            boatsDropBoats = false;
    @Setting("item")
    private double itemMerge = 2.5d;
    @Setting("item")
    private int itemDespawnRate = 6000, arrowDespawnRate = 1200;
    @Setting("projectile")
    private float arrowTrajectoryRandomness = 1.0f;
    @Setting("projectile")
    private boolean randomArrowDamage = true;
    @Setting("connection")
    private boolean logPlayerIpAddresses = true;
    @Setting("connection")
    boolean proxyOnlineMode = true;
    @Setting("connection")
    private boolean onlineMode = true;
    @Setting("connection")
    private boolean bungee = false;
    @Setting("connection")
    private int nettyThreads = 4;
    @Setting("connection")
    private boolean lateBind = false;
    @Setting("connection")
    private int connectionThrottle = 4000;
    @Setting("connection")
    private String serverIp = "";
    @Setting("connection")
    private int port = 25565;
    @Setting("connection")
    private boolean useNativeTransport = true;
    @Setting("connection")
    private int networkCompressionThreshold = 256;
    @Setting("connection.packet-limiter")
    private String kickMessage = "Too many packets sent!";
    @Setting("connection.packet-limiter")
    private PacketLimit allPacketsLimit = null;
    @Setting("connection.packet-limiter")
    private Map<Class<? extends Packet<?>>, PacketLimit> packetSpecificLimits = Map.of();
    @Setting("exploit")
    private int maxBookPageSize = 2560;
    @Setting("exploit")
    private double maxBookTotalSizeMultiplier = 0.98d;
    @Setting("exploit")
    private boolean warnForExcessiveVelocity = true;
    @Setting("exploit")
    private double movedWronglyThreshold = 0.0625d;
    @Setting("exploit")
    private double movedTooQuicklyThreshold = 100.0d;
    @Setting("plugin")
    private boolean queryPlugins = true;
    @Setting("plugin")
    private String updateFolder = "update";
    @Setting("plugin")
    private List<String> hiddenPlugins = Arrays.asList("SpookyAC");
    @Setting("sound")
    private boolean disableMoodSounds = false;
    @Setting("sound")
    private int dragonDeathSoundRadius = 0;
    @Setting("sound")
    private int witherSpawnSoundRadius = 0;
    @Setting("world")
    private int timeUpdateFrequency = 100;
    @Setting("world")
    private int maxWorldSize = 29999984;
    @Setting("world")
    private int spawnProtection = 16;
    @Setting("world")
    private int difficulty = EnumDifficulty.NORMAL.a();
    @Setting("world")
    private boolean hardcore = false;
    @Setting("world")
    private String levelName = "world";
    @Setting("world")
    private String levelSeed = "";
    @Setting("world")
    private String levelType = "DEFAULT";
    @Setting("world")
    private String generatorSettings = "";
    @Setting("world")
    private int maxBuildHeight = 256;
    @Setting("world")
    private boolean allowEnd = true;
    @Setting("world")
    private boolean allowNether = true;
    @Setting("world")
    private int ticksPerAutosave = 6000;
    @Setting("world")
    private boolean disableChunkSaving = false;
    @Setting("world")
    private boolean fallingBlocksCollideWithSigns = false;
    @Setting("world")
    private int cactusMaxHeight = 3;
    @Setting("world")
    private int reedMaxHeight = 3;
    @Setting("world")
    private int fishingMinTicks = 100;
    @Setting("world")
    private int fishingMaxTicks = 900;
    @Setting("world")
    private boolean keepSpawnInMemory = false;
    @Setting("world")
    private int fallingBlockHeightNerf = 0;
    @Setting("world")
    private int waterOverLavaFlowSpeed = 5;
    @Setting("world")
    private boolean removeInvalidMobSpawnerTEs = true;
    @Setting("world")
    private boolean netherVoidTopDamage = false;
    @Setting("world")
    private int tickNextTickCap = 10000;
    @Setting("world")
    private boolean tickNextTickListCapIgnoresRedstone = false;
    @Setting("world")
    private boolean fastDrainLava = false;
    @Setting("world")
    private boolean fastDrainWater = false;
    @Setting("world")
    private int lavaFlowSpeedNormal = 30;
    @Setting("world")
    private int lavaFlowSpeedNether = 10;
    @Setting("world")
    private boolean disableThunder = false;
    @Setting("world")
    private boolean disableIceAndSnow = false;
    @Setting("world")
    private int mobSpawnerTickRate = 1;
    @Setting("world")
    private float tntExplosionVolume = 4.0f;
    @Setting("world")
    private boolean useHopperCheck = false;
    @Setting("world")
    private int hopperTransfer = 8;
    @Setting("world")
    private int hopperCheck = 8;
    @Setting("world")
    private int hopperAmount = 1;
    @Setting("world")
    private boolean randomLightUpdates = false;
    @Setting("world")
    private boolean allChunksAreSlimeChunks = false;
    @Setting("world")
    private int portalSearchRadius = 128;
    @Setting("world")
    private boolean nerfSpawnerMobs = false;
    @Setting("world")
    private boolean saveStructureInfo = true;
    @Setting("world")
    private int villageSeed = 10387312;
    @Setting("world")
    private int largeFeatureSeed = 14357617;
    @Setting("world")
    private String worldsFolder = "worlds";
    @Setting("world.anti-xray")
    private boolean antiXray = false;
    @Setting("world.anti-xray")
    private int engineMode = 1;
    @Setting("world.anti-xray")
    private int[] hiddenBlocks = new int[] { 14, 15, 16, 21, 48, 49, 54, 56, 73, 74, 82, 129, 130 };
    @Setting("world.anti-xray")
    private int[] replaceBlocks = new int[] { 1, 5 };
    @Setting("world.crop")
    private int cactusModifier = 100;
    @Setting("world.crop")
    private int caneModifier = 100;
    @Setting("world.crop")
    private int melonModifier = 100;
    @Setting("world.crop")
    private int mushroomModifier = 100;
    @Setting("world.crop")
    private int pumpkinModifier = 100;
    @Setting("world.crop")
    private int saplingModifier = 100;
    @Setting("world.crop")
    private int wheatModifier = 100;
    @Setting("world.crop")
    private int wartModifier = 100;
    @Setting("world.generator")
    private boolean generateStructures = true;
    @Setting("world.generator")
    private boolean generateCanyon = true;
    @Setting("world.generator")
    private boolean generateCaves = true;
    @Setting("world.generator")
    private boolean generateDungeon = true;
    @Setting("world.generator")
    private boolean generateFortress = true;
    @Setting("world.generator")
    private boolean generateMineshaft = true;
    @Setting("world.generator")
    private boolean generateMonument = true;
    @Setting("world.generator")
    private boolean generateStronghold = true;
    @Setting("world.generator")
    private boolean generateTemple = true;
    @Setting("world.generator")
    private boolean generateVillage = true;
    @Setting("world.generator")
    private boolean generateFlatBedrock = false;
    @Setting("world.generator")
    private ChunkGenerator generator = null;
    @Setting("debug")
    private boolean debug = false;
    @Setting("debug")
    private WarningState warningState = WarningState.DEFAULT;
    @Setting("debug")
    private boolean warnOnOverload = true;
    @Setting("compatibility")
    private List<String> botIncompatiblePlugins = Arrays.asList("LPX");

    private AntiXray antiXrayInstance;
    public int currentPrimedTnt = 0;

    public boolean isProxyOnlineMode() {
        return Bukkit.getOnlineMode() || (bungee && this.proxyOnlineMode);
    }

    private final File configFile = new File("config.groovy");

    public void reload() {
        ConfigSlurper configSlurper = new ConfigSlurper();

        if (!configFile.exists()) {
            try {
                configFile.createNewFile();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        try {
            ConfigObject config = configSlurper.parse(configFile.toURI().toURL());

            for (Field field : GlobalConfig.class.getDeclaredFields()) {
                field.setAccessible(true);
                Setting setting = field.getDeclaredAnnotation(Setting.class);
                if (setting != null) {
                    String[] keys = setting.value().split("\\.");
                    ConfigObject subConfig = null, lastSubConfig = config;

                    for (String key : keys) {
                        ConfigObject nextObject = (ConfigObject) lastSubConfig.get(key);

                        if (nextObject == null)
                            break;

                        subConfig = nextObject;
                        lastSubConfig = subConfig;
                    }

                    if (subConfig == null)
                        continue;

                    Object value = subConfig.get(field.getName());

                    if (value == null)
                        continue;

                    System.out.println(setting.value() + "." + field.getName() + "=" + value);

                    try {
                        field.set(this, value);
                    } catch (IllegalArgumentException | IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        init();
    }

    @SuppressWarnings("deprecation")
    private void init() {

        int size = Material.BUCKET.getMaxStackSize();
        if (stackableLavaBuckets) {
            Material.LAVA_BUCKET.setMaxStackSize(size);
            Items.LAVA_BUCKET.c(size);
        }

        if (stackableWaterBuckets) {
            Material.WATER_BUCKET.setMaxStackSize(size);
            Items.WATER_BUCKET.c(size);
        }

        if (stackableMilkBuckets) {
            Material.MILK_BUCKET.setMaxStackSize(size);
            Items.MILK_BUCKET.c(size);
        }

        if (softDespawnDistance > hardDespawnDistance) {
            softDespawnDistance = hardDespawnDistance;
        }

        softDespawnDistance = softDespawnDistance * softDespawnDistance;
        hardDespawnDistance = hardDespawnDistance * hardDespawnDistance;

        WatchdogThread.doStart(timeoutTime, restartOnCrash);

        System.setProperty("io.netty.eventLoopThreads", Integer.toString(nettyThreads));

        if (disableStatSaving && forcedStats.getOrDefault("achievement.openInventory", 0) < 1)
            logger.warn(
                    "*** WARNING *** stats.disable-saving is true but stats.forced-stats.achievement.openInventory" +
                            " isn't set to 1. Disabling stat saving without forcing the achievement may cause it to get stuck on the player's "
                            +
                            "screen.");

        ((AttributeRanged) GenericAttributes.maxHealth).b = maxHealth;
        ((AttributeRanged) GenericAttributes.MOVEMENT_SPEED).b = movementSpeed;
        ((AttributeRanged) GenericAttributes.ATTACK_DAMAGE).b = attackDamage;

        if (debug && !LogManager.getRootLogger().isTraceEnabled()) {
            // Enable debug logging
            LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
            Configuration conf = ctx.getConfiguration();
            conf.getLoggerConfig(LogManager.ROOT_LOGGER_NAME).setLevel(org.apache.logging.log4j.Level.ALL);
            ctx.updateLoggers(conf);
        }

        antiXrayInstance = new AntiXray(engineMode, hiddenBlocks, replaceBlocks);

        if (!saveStructureInfo) {
            logger.warn(
                    "*** WARNING *** You have selected to NOT save structure info. This may cause structures such as fortresses to not spawn mobs!");
            logger.warn(
                    "*** WARNING *** Please use this option with caution, SpigotMC is not responsible for any issues this option may cause in the future!");
        }

        if (disableChunkSaving) {
            logger.warn(
                    "*** WARNING *** You have selected to NOT save chunks to disk. ");
        }
    }

    public void registerCommands(CraftServer server) {

        commands.put("restart", new RestartCommand("restart"));
        commands.put("tps", new TicksPerSecondCommand("tps"));

        for (Map.Entry<String, Command> entry : commands.entrySet())
            server.getCommandMap().register(entry.getKey(), "Spigot", entry.getValue());
    }
}
