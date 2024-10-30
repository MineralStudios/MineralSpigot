package org.bukkit.craftbukkit;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import org.apache.commons.lang.Validate;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.UnsafeValues;
import org.bukkit.Warning.WarningState;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.WorldCreator;
import org.bukkit.command.Command;
import org.bukkit.command.CommandException;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.SimpleCommandMap;

import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.conversations.Conversable;
import org.bukkit.craftbukkit.command.VanillaCommandWrapper;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.craftbukkit.generator.CraftChunkData;
import org.bukkit.craftbukkit.help.SimpleHelpMap;
import org.bukkit.craftbukkit.inventory.CraftFurnaceRecipe;
import org.bukkit.craftbukkit.inventory.CraftInventoryCustom;
import org.bukkit.craftbukkit.inventory.CraftItemFactory;
import org.bukkit.craftbukkit.inventory.CraftRecipe;
import org.bukkit.craftbukkit.inventory.CraftShapedRecipe;
import org.bukkit.craftbukkit.inventory.CraftShapelessRecipe;
import org.bukkit.craftbukkit.inventory.RecipeIterator;
import org.bukkit.craftbukkit.map.CraftMapView;
import org.bukkit.craftbukkit.metadata.EntityMetadataStore;
import org.bukkit.craftbukkit.metadata.PlayerMetadataStore;
import org.bukkit.craftbukkit.metadata.WorldMetadataStore;
import org.bukkit.craftbukkit.potion.CraftPotionBrewer;
import org.bukkit.craftbukkit.scheduler.CraftScheduler;
import org.bukkit.craftbukkit.scoreboard.CraftScoreboardManager;
import org.bukkit.craftbukkit.util.CraftIconCache;
import org.bukkit.craftbukkit.util.CraftMagicNumbers;
import org.bukkit.craftbukkit.util.DatFileFilter;
import org.bukkit.craftbukkit.util.Versioning;
import org.bukkit.craftbukkit.util.permissions.CraftDefaultPermissions;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerChatTabCompleteEvent;
import org.bukkit.event.world.WorldInitEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldUnloadEvent;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.permissions.Permissible;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginLoadOrder;
import org.bukkit.plugin.ServicesManager;
import org.bukkit.plugin.SimplePluginManager;
import org.bukkit.plugin.SimpleServicesManager;
import org.bukkit.plugin.java.JavaPluginLoader;
import org.bukkit.plugin.messaging.Messenger;
import org.bukkit.plugin.messaging.StandardMessenger;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitWorker;
import org.bukkit.util.StringUtil;
import org.bukkit.util.permissions.DefaultPermissions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;
import org.yaml.snakeyaml.error.MarkedYAMLException;

import com.google.common.base.Charsets;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.MapMaker;
import com.mojang.authlib.GameProfile;

import gg.mineral.server.config.GlobalConfig;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.base64.Base64;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import lombok.Getter;
//import jline.console.ConsoleReader; // PandaSpigot - comment out
import net.md_5.bungee.api.chat.BaseComponent;
import net.minecraft.server.BlockPosition;
import net.minecraft.server.ChunkProviderServer;
import net.minecraft.server.ChunkRegionLoader;
import net.minecraft.server.CommandAbstract;
import net.minecraft.server.CommandDispatcher;
import net.minecraft.server.Convertable;
import net.minecraft.server.CraftingManager;
import net.minecraft.server.DedicatedPlayerList;
import net.minecraft.server.Enchantment;
import net.minecraft.server.EntityPlayer;
import net.minecraft.server.EntityTracker;
import net.minecraft.server.EnumDifficulty;
import net.minecraft.server.ExceptionWorldConflict;
import net.minecraft.server.ICommand;
import net.minecraft.server.IDataManager;
import net.minecraft.server.IProgressUpdate;
import net.minecraft.server.Items;
import net.minecraft.server.JsonListEntry;
import net.minecraft.server.LocaleI18n;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.MobEffectList;
import net.minecraft.server.PersistentCollection;
import net.minecraft.server.PlayerList;

import net.minecraft.server.RecipesFurnace;
import net.minecraft.server.ServerCommand;
import net.minecraft.server.ServerNBTManager;
import net.minecraft.server.WorldData;
import net.minecraft.server.WorldLoaderServer;
import net.minecraft.server.WorldManager;
import net.minecraft.server.WorldMap;
import net.minecraft.server.WorldNBTStorage;
import net.minecraft.server.WorldServer;
import net.minecraft.server.WorldSettings;
import net.minecraft.server.WorldType;

public final class CraftServer implements Server {
    private static final Player[] EMPTY_PLAYER_ARRAY = new Player[0];
    private final String serverName = GlobalConfig.getInstance().getBrandName(); // PandaSpigot
    private final String serverVersion;
    @Getter
    private final String bukkitVersion = Versioning.getBukkitVersion();
    private final Logger logger = Logger.getLogger("Minecraft");
    @Getter
    private final ServicesManager servicesManager = new SimpleServicesManager();
    @Getter
    private final CraftScheduler scheduler = new CraftScheduler();
    @Getter
    private final SimpleCommandMap commandMap = new SimpleCommandMap(this);
    @Getter
    private final SimpleHelpMap helpMap = new SimpleHelpMap(this);
    private final StandardMessenger messenger = new StandardMessenger();
    @Getter
    private final SimplePluginManager pluginManager = new SimplePluginManager(this, commandMap); // PandaSpigot
    protected final MinecraftServer console;
    protected final DedicatedPlayerList playerList;
    private final Map<String, World> worlds = new Object2ObjectLinkedOpenHashMap<String, World>();
    private final Yaml yaml = new Yaml(new SafeConstructor());
    private final Map<UUID, OfflinePlayer> offlinePlayers = new MapMaker().softValues().makeMap();
    private final EntityMetadataStore entityMetadata = new EntityMetadataStore();
    private final PlayerMetadataStore playerMetadata = new PlayerMetadataStore();
    private final WorldMetadataStore worldMetadata = new WorldMetadataStore();
    public int chunkGCPeriod = -1;
    public int chunkGCLoadThresh = 0;
    private File container;
    @Getter
    private WarningState warningState = WarningState.DEFAULT;
    private final BooleanWrapper online = new BooleanWrapper();
    @Getter
    public CraftScoreboardManager scoreboardManager;
    public boolean playerCommandState;
    private boolean printSaveWarning;
    private CraftIconCache icon;
    private final List<CraftPlayer> playerView;
    public int reloadCount;

    private final class BooleanWrapper {
        private boolean value = true;
    }

    static {
        ConfigurationSerialization.registerClass(CraftOfflinePlayer.class);
        CraftItemFactory.instance();
    }

    public CraftServer(MinecraftServer console, PlayerList playerList) {
        this.console = console;
        this.playerList = (DedicatedPlayerList) playerList;
        this.playerView = Collections
                .unmodifiableList(Lists.transform(playerList.players, new Function<EntityPlayer, CraftPlayer>() {
                    @Override
                    public CraftPlayer apply(EntityPlayer player) {
                        return player.getBukkitEntity();
                    }
                }));
        this.serverVersion = CraftServer.class.getPackage().getImplementationVersion();
        online.value = GlobalConfig.getInstance().isOnlineMode();

        Bukkit.setServer(this);

        // Register all the Enchantments and PotionTypes now so we can stop new
        // registration immediately after
        Enchantment.DAMAGE_ALL.getClass();
        org.bukkit.enchantments.Enchantment.stopAcceptingRegistrations();

        Potion.setPotionBrewer(new CraftPotionBrewer());
        MobEffectList.BLINDNESS.getClass();
        PotionEffectType.stopAcceptingRegistrations();
        // Ugly hack :(

        if (!Main.useConsole)
            getLogger().info("Console input is disabled due to --noconsole command argument");

        console.autosavePeriod = GlobalConfig.getInstance().getTicksPerAutosave();
        warningState = GlobalConfig.getInstance().getWarningState();
        chunkGCPeriod = GlobalConfig.getInstance().getChunkGCPeriod();
        chunkGCLoadThresh = GlobalConfig.getInstance().getChunkGCLoadThresh();
        loadIcon();

        GlobalConfig.getInstance().registerCommands(this);

        // Spigot Start - Moved to old location of new DedicatedPlayerList in
        // DedicatedServer
        // loadPlugins();
        // enablePlugins(PluginLoadOrder.STARTUP);
        // Spigot End
    }

    public boolean getCommandBlockOverride(String command) {
        boolean includesOverride = false;

        for (String override : GlobalConfig.getInstance().getCommandBlockOverrides()) {
            if (override.equalsIgnoreCase(command)) {
                includesOverride = true;
                break;
            }
        }
        return GlobalConfig.getInstance().isOverrideAllCommandBlockCommands()
                || includesOverride;
    }

    public void loadPlugins() {
        pluginManager.registerInterface(JavaPluginLoader.class);

        // PandaSpigot start - extra jars
        File pluginFolder = this.getPluginsFolder();

        if (!pluginFolder.exists())
            pluginFolder.mkdirs();

        Plugin[] plugins = this.pluginManager.loadPlugins(pluginFolder, this.extraPluginJars());
        // PandaSpigot end
        for (Plugin plugin : plugins) {
            try {
                String message = String.format("Loading %s", plugin.getDescription().getFullName());
                plugin.getLogger().info(message);
                plugin.onLoad();
            } catch (Throwable ex) {
                Logger.getLogger(CraftServer.class.getName()).log(Level.SEVERE, ex.getMessage() + " initializing "
                        + plugin.getDescription().getFullName() + " (Is it up to date?)", ex);
            }
        }
    }

    // PandaSpigot start
    @Override
    public File getPluginsFolder() {
        return (File) this.console.options.valueOf("plugins");
    }

    private List<File> extraPluginJars() {
        @SuppressWarnings("unchecked")
        final List<File> jars = (List<File>) this.console.options.valuesOf("add-plugin");
        final List<File> list = new ArrayList<>();
        for (final File file : jars) {
            if (!file.exists()) {
                net.minecraft.server.MinecraftServer.LOGGER.warn(
                        "File '{}' specified through 'add-plugin' argument does not exist, cannot load a plugin from it!",
                        file.getAbsolutePath());
                continue;
            }
            if (!file.isFile()) {
                net.minecraft.server.MinecraftServer.LOGGER.warn(
                        "File '{}' specified through 'add-plugin' argument is not a file, cannot load a plugin from it!",
                        file.getAbsolutePath());
                continue;
            }
            if (!file.getName().endsWith(".jar")) {
                net.minecraft.server.MinecraftServer.LOGGER.warn(
                        "File '{}' specified through 'add-plugin' argument is not a jar file, cannot load a plugin from it!",
                        file.getAbsolutePath());
                continue;
            }
            list.add(file);
        }
        return list;
    }
    // PandaSpigot end

    public void enablePlugins(PluginLoadOrder type) {
        if (type == PluginLoadOrder.STARTUP) {
            helpMap.clear();
            helpMap.initializeGeneralTopics();
        }

        Plugin[] plugins = pluginManager.getPlugins();

        for (Plugin plugin : plugins)
            if ((!plugin.isEnabled()) && (plugin.getDescription().getLoad() == type))
                loadPlugin(plugin);

        if (type == PluginLoadOrder.POSTWORLD) {
            // Spigot start - Allow vanilla commands to be forced to be the main command
            setVanillaCommands(true);
            commandMap.setFallbackCommands();
            setVanillaCommands(false);
            // Spigot end
            commandMap.registerServerAliases();
            loadCustomPermissions();
            DefaultPermissions.registerCorePermissions();
            CraftDefaultPermissions.registerCorePermissions();
            helpMap.initializeCommands();
        }
    }

    public void disablePlugins() {
        pluginManager.disablePlugins();
    }

    private void setVanillaCommands(boolean first) { // Spigot
        Map<String, ICommand> commands = new CommandDispatcher().getCommands();
        for (ICommand cmd : commands.values()) {
            // Spigot start
            VanillaCommandWrapper wrapper = new VanillaCommandWrapper((CommandAbstract) cmd,
                    LocaleI18n.get(cmd.getUsage(null)));

            boolean containsStr = false;

            for (String s : GlobalConfig.getInstance().getReplaceCommands()) {
                if (s.equalsIgnoreCase(wrapper.getName())) {
                    containsStr = true;
                    break;
                }
            }

            if (containsStr) {
                if (first)
                    commandMap.register("minecraft", wrapper);
            } else if (!first)
                commandMap.register("minecraft", wrapper);

            // Spigot end
        }
    }

    private void loadPlugin(Plugin plugin) {
        try {
            pluginManager.enablePlugin(plugin);

            List<Permission> perms = plugin.getDescription().getPermissions();

            for (Permission perm : perms) {
                try {
                    pluginManager.addPermission(perm);
                } catch (IllegalArgumentException ex) {
                    getLogger().log(Level.WARNING, "Plugin " + plugin.getDescription().getFullName()
                            + " tried to register permission '" + perm.getName() + "' but it's already registered", ex);
                }
            }
        } catch (Throwable ex) {
            Logger.getLogger(CraftServer.class.getName()).log(Level.SEVERE,
                    ex.getMessage() + " loading " + plugin.getDescription().getFullName() + " (Is it up to date?)", ex);
        }
    }

    @Override
    public String getName() {
        return serverName;
    }

    @Override
    public String getVersion() {
        return serverVersion + " (MC: " + console.getVersion() + ")";
    }

    @Override
    @Deprecated
    @SuppressWarnings("unchecked")
    public Player[] _INVALID_getOnlinePlayers() {
        return getOnlinePlayers().toArray(EMPTY_PLAYER_ARRAY);
    }

    @Override
    public List<CraftPlayer> getOnlinePlayers() {
        return this.playerView;
    }

    @Override
    @Deprecated
    public Player getPlayer(final String name) {
        Validate.notNull(name, "Name cannot be null");

        Player found = getPlayerExact(name);
        // Try for an exact match first.
        if (found != null) {
            return found;
        }

        String lowerName = name.toLowerCase();
        int delta = Integer.MAX_VALUE;
        for (Player player : getOnlinePlayers()) {
            if (player.getName().toLowerCase().startsWith(lowerName)) {
                int curDelta = Math.abs(player.getName().length() - lowerName.length());
                if (curDelta < delta) {
                    found = player;
                    delta = curDelta;
                }
                if (curDelta == 0)
                    break;
            }
        }
        return found;
    }

    @Override
    @Deprecated
    public Player getPlayerExact(String name) {
        Validate.notNull(name, "Name cannot be null");

        EntityPlayer player = playerList.getPlayer(name);
        return (player != null) ? player.getBukkitEntity() : null;
    }

    @Override
    public Player getPlayer(UUID id) {
        EntityPlayer player = playerList.a(id);

        if (player != null) {
            return player.getBukkitEntity();
        }

        return null;
    }

    @Override
    public int broadcastMessage(String message) {
        return broadcast(message, BROADCAST_CHANNEL_USERS);
    }

    public Player getPlayer(final EntityPlayer entity) {
        return entity.getBukkitEntity();
    }

    @Override
    @Deprecated
    public List<Player> matchPlayer(String partialName) {
        Validate.notNull(partialName, "PartialName cannot be null");

        List<Player> matchedPlayers = new ArrayList<Player>();

        for (Player iterPlayer : this.getOnlinePlayers()) {
            String iterPlayerName = iterPlayer.getName();

            if (partialName.equalsIgnoreCase(iterPlayerName)) {
                // Exact match
                matchedPlayers.clear();
                matchedPlayers.add(iterPlayer);
                break;
            }
            if (iterPlayerName.toLowerCase().contains(partialName.toLowerCase())) {
                // Partial match
                matchedPlayers.add(iterPlayer);
            }
        }

        return matchedPlayers;
    }

    @Override
    public int getMaxPlayers() {
        return playerList.getMaxPlayers();
    }

    // NOTE: These are dependent on the corrisponding call in MinecraftServer
    // so if that changes this will need to as well
    @Override
    public int getPort() {
        return GlobalConfig.getInstance().getPort();
    }

    @Override
    public int getViewDistance() {
        return GlobalConfig.getInstance().getViewDistance();
    }

    @Override
    public String getIp() {
        return GlobalConfig.getInstance().getServerIp();
    }

    @Override
    public String getServerName() {
        return GlobalConfig.getInstance().getServerName();
    }

    @Override
    public String getServerId() {
        return GlobalConfig.getInstance().getServerId();
    }

    @Override
    public String getWorldType() {
        return GlobalConfig.getInstance().getLevelType();
    }

    @Override
    public boolean getGenerateStructures() {
        return GlobalConfig.getInstance().isGenerateStructures();
    }

    @Override
    public boolean getAllowEnd() {
        return GlobalConfig.getInstance().isAllowEnd();
    }

    @Override
    public boolean getAllowNether() {
        return GlobalConfig.getInstance().isAllowNether();
    }

    public boolean getWarnOnOverload() {
        return GlobalConfig.getInstance().isWarnOnOverload();
    }

    public boolean getQueryPlugins() {
        return GlobalConfig.getInstance().isQueryPlugins();
    }

    @Override
    public boolean hasWhitelist() {
        return GlobalConfig.getInstance().isWhitelist();
    }

    @Override
    public String getUpdateFolder() {
        return GlobalConfig.getInstance().getUpdateFolder();
    }

    @Override
    public File getUpdateFolderFile() {
        return new File((File) console.options.valueOf("plugins"),
                GlobalConfig.getInstance().getUpdateFolder());
    }

    @Override
    public long getConnectionThrottle() {
        // Spigot Start - Automatically set connection throttle for bungee
        // configurations
        if (GlobalConfig.getInstance().isBungee()) {
            return -1;
        } else {
            return GlobalConfig.getInstance().getConnectionThrottle();
        }
        // Spigot End
    }

    @Override
    public int getTicksPerAnimalSpawns() {
        return GlobalConfig.getInstance().getTicksPerAnimalSpawns();
    }

    @Override
    public int getTicksPerMonsterSpawns() {
        return GlobalConfig.getInstance().getTicksPerMonsterSpawns();
    }

    @Override
    public List<World> getWorlds() {
        return new ArrayList<World>(worlds.values());
    }

    public DedicatedPlayerList getHandle() {
        return playerList;
    }

    // NOTE: Should only be called from DedicatedServer.ah()
    public boolean dispatchServerCommand(CommandSender sender, ServerCommand serverCommand) {
        if (sender instanceof Conversable) {
            Conversable conversable = (Conversable) sender;

            if (conversable.isConversing()) {
                conversable.acceptConversationInput(serverCommand.command);
                return true;
            }
        }
        try {
            this.playerCommandState = true;
            return dispatchCommand(sender, serverCommand.command);
        } catch (Exception ex) {
            getLogger().log(Level.WARNING,
                    "Unexpected exception while parsing console command \"" + serverCommand.command + '"', ex);
            return false;
        } finally {
            this.playerCommandState = false;
        }
    }

    @Override
    public boolean dispatchCommand(CommandSender sender, String commandLine) {
        Validate.notNull(sender, "Sender cannot be null");
        Validate.notNull(commandLine, "CommandLine cannot be null");

        // PaperSpigot Start
        if (!Bukkit.isPrimaryThread()) {
            final CommandSender fSender = sender;
            final String fCommandLine = commandLine;
            Bukkit.getLogger().log(Level.SEVERE, "Command Dispatched Async: " + commandLine);
            Bukkit.getLogger().log(Level.SEVERE,
                    "Please notify author of plugin causing this execution to fix this bug! see: http://bit.ly/1oSiM6C",
                    new Throwable());
            org.bukkit.craftbukkit.util.Waitable<Boolean> wait = new org.bukkit.craftbukkit.util.Waitable<Boolean>() {
                @Override
                protected Boolean evaluate() {
                    return dispatchCommand(fSender, fCommandLine);
                }
            };
            net.minecraft.server.MinecraftServer.getServer().processQueue.add(wait);
            try {
                return wait.get();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // This is proper habit for java. If we aren't handling it, pass it
                                                    // on!
            } catch (Exception e) {
                throw new RuntimeException("Exception processing dispatch command", e.getCause());
            }
        }
        // PaperSpigot End

        if (commandMap.dispatch(sender, commandLine)) {
            return true;
        }

        sender.sendMessage(GlobalConfig.getInstance().getUnknownCommandMessage());

        return false;
    }

    @Override
    public void reload() {
        reloadCount++;

        boolean animals = GlobalConfig.getInstance().isSpawnAnimals();
        boolean monsters = GlobalConfig.getInstance().isSpawnMonsters();
        EnumDifficulty difficulty = EnumDifficulty
                .getById(GlobalConfig.getInstance().getDifficulty());

        online.value = GlobalConfig.getInstance().isOnlineMode();
        console.setSpawnAnimals(GlobalConfig.getInstance().isSpawnAnimals());
        console.setPVP(GlobalConfig.getInstance().isPvp());
        console.setAllowFlight(GlobalConfig.getInstance().isAllowFlight());
        console.setMotd(GlobalConfig.getInstance().getMotd());
        warningState = GlobalConfig.getInstance().getWarningState();
        printSaveWarning = false;
        console.autosavePeriod = GlobalConfig.getInstance().getTicksPerAutosave();
        chunkGCPeriod = GlobalConfig.getInstance().getChunkGCPeriod();
        chunkGCLoadThresh = GlobalConfig.getInstance().getChunkGCLoadThresh();
        loadIcon();

        try {
            playerList.getIPBans().load();
        } catch (IOException ex) {
            logger.log(Level.WARNING, "Failed to load banned-ips.json, " + ex.getMessage());
        }
        try {
            playerList.getProfileBans().load();
        } catch (IOException ex) {
            logger.log(Level.WARNING, "Failed to load banned-players.json, " + ex.getMessage());
        }

        for (WorldServer world : console.worlds) {
            world.worldData.setDifficulty(difficulty);
            world.setSpawnFlags(monsters, animals);
            if (this.getTicksPerAnimalSpawns() < 0) {
                world.ticksPerAnimalSpawns = 400;
            } else {
                world.ticksPerAnimalSpawns = this.getTicksPerAnimalSpawns();
            }

            if (this.getTicksPerMonsterSpawns() < 0) {
                world.ticksPerMonsterSpawns = 1;
            } else {
                world.ticksPerMonsterSpawns = this.getTicksPerMonsterSpawns();
            }
        }

        GlobalConfig.getInstance().reload();

        pluginManager.clearPlugins();
        commandMap.clearCommands();
        resetRecipes();
        GlobalConfig.getInstance().registerCommands(this);

        int pollCount = 0;

        // Wait for at most 2.5 seconds for plugins to close their threads
        while (pollCount < 50 && getScheduler().getActiveWorkers().size() > 0) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
            }
            pollCount++;
        }

        List<BukkitWorker> overdueWorkers = getScheduler().getActiveWorkers();
        for (BukkitWorker worker : overdueWorkers) {
            Plugin plugin = worker.getOwner();
            String author = "<NoAuthorGiven>";
            if (plugin.getDescription().getAuthors().size() > 0) {
                author = plugin.getDescription().getAuthors().get(0);
            }
            getLogger().log(Level.SEVERE, String.format(
                    "Nag author: '%s' of '%s' about the following: %s",
                    author,
                    plugin.getDescription().getName(),
                    "This plugin is not properly shutting down its async tasks when it is being reloaded.  This may cause conflicts with the newly loaded version of the plugin"));
        }
        loadPlugins();
        enablePlugins(PluginLoadOrder.STARTUP);
        enablePlugins(PluginLoadOrder.POSTWORLD);
    }

    private void loadIcon() {
        icon = new CraftIconCache(null);
        try {
            final File file = new File(new File("."), "server-icon.png");
            if (file.isFile()) {
                icon = loadServerIcon0(file);
            }
        } catch (Exception ex) {
            getLogger().log(Level.WARNING, "Couldn't load server icon", ex);
        }
    }

    @SuppressWarnings({ "unchecked", "finally" })
    private void loadCustomPermissions() {
        File file = new File(GlobalConfig.getInstance().getPermissionsFile());
        FileInputStream stream;

        try {
            stream = new FileInputStream(file);
        } catch (FileNotFoundException ex) {
            try {
                file.createNewFile();
            } finally {
                return;
            }
        }

        Map<String, Map<String, Object>> perms;

        try {
            perms = (Map<String, Map<String, Object>>) yaml.load(stream);
        } catch (MarkedYAMLException ex) {
            getLogger().log(Level.WARNING, "Server permissions file " + file + " is not valid YAML: " + ex.toString());
            return;
        } catch (Throwable ex) {
            getLogger().log(Level.WARNING, "Server permissions file " + file + " is not valid YAML.", ex);
            return;
        } finally {
            try {
                stream.close();
            } catch (IOException ex) {
            }
        }

        if (perms == null) {
            getLogger().log(Level.INFO, "Server permissions file " + file + " is empty, ignoring it");
            return;
        }

        List<Permission> permsList = Permission.loadPermissions(perms,
                "Permission node '%s' in " + file + " is invalid", Permission.DEFAULT_PERMISSION);

        for (Permission perm : permsList) {
            try {
                pluginManager.addPermission(perm);
            } catch (IllegalArgumentException ex) {
                getLogger().log(Level.SEVERE, "Permission in " + file + " was already defined", ex);
            }
        }
    }

    @Override
    public String toString() {
        return "CraftServer{" + "serverName=" + serverName + ",serverVersion=" + serverVersion + ",minecraftVersion="
                + console.getVersion() + '}';
    }

    public World createWorld(String name, World.Environment environment) {
        return WorldCreator.name(name).environment(environment).createWorld();
    }

    public World createWorld(String name, World.Environment environment, long seed) {
        return WorldCreator.name(name).environment(environment).seed(seed).createWorld();
    }

    public World createWorld(String name, Environment environment, ChunkGenerator generator) {
        return WorldCreator.name(name).environment(environment).generator(generator).createWorld();
    }

    public World createWorld(String name, Environment environment, long seed, ChunkGenerator generator) {
        return WorldCreator.name(name).environment(environment).seed(seed).generator(generator).createWorld();
    }

    @Override
    public World createWorld(WorldCreator creator) {
        Validate.notNull(creator, "Creator may not be null");

        String name = creator.name();
        ChunkGenerator generator = creator.generator();
        File folder = new File(getWorldContainer(), name);
        World world = getWorld(name);
        WorldType type = WorldType.getType(creator.type().getName());
        boolean generateStructures = creator.generateStructures();

        if (world != null) {
            return world;
        }

        if ((folder.exists()) && (!folder.isDirectory())) {
            throw new IllegalArgumentException("File exists with the name '" + name + "' and isn't a folder");
        }

        if (generator == null) {
            generator = getGenerator(name);
        }

        Convertable converter = new WorldLoaderServer(getWorldContainer());
        if (converter.isConvertable(name)) {
            getLogger().info("Converting world '" + name + "'");
            converter.convert(name, new IProgressUpdate() {
                private long b = System.currentTimeMillis();

                public void a(String s) {
                }

                public void a(int i) {
                    if (System.currentTimeMillis() - this.b >= 1000L) {
                        this.b = System.currentTimeMillis();
                        MinecraftServer.LOGGER.info("Converting... " + i + "%");
                    }

                }

                public void c(String s) {
                }
            });
        }

        int dimension = CraftWorld.CUSTOM_DIMENSION_OFFSET + console.worlds.size();
        boolean used = false;
        do {
            for (WorldServer server : console.worlds) {
                used = server.dimension == dimension;
                if (used) {
                    dimension++;
                    break;
                }
            }
        } while (used);
        boolean hardcore = false;

        IDataManager sdm = new ServerNBTManager(getWorldContainer(), name, true);
        WorldData worlddata = sdm.getWorldData();
        if (worlddata == null) {
            WorldSettings worldSettings = new WorldSettings(creator.seed(),
                    WorldSettings.EnumGamemode.getById(getDefaultGameMode().getValue()), generateStructures, hardcore,
                    type);
            worldSettings.setGeneratorSettings(creator.generatorSettings());
            worlddata = new WorldData(worldSettings, name);
        }
        worlddata.checkName(name); // CraftBukkit - Migration did not rewrite the level.dat; This forces 1.8 to
                                   // take the last loaded world as respawn (in this case the end)
        WorldServer internal = (WorldServer) new WorldServer(console, sdm, worlddata, dimension, console.methodProfiler,
                creator.environment(), generator).b();

        if (!(worlds.containsKey(name.toLowerCase()))) {
            return null;
        }

        internal.scoreboard = getScoreboardManager().getMainScoreboard().getHandle();

        internal.tracker = new EntityTracker(internal);
        internal.addIWorldAccess(new WorldManager(console, internal));
        internal.worldData.setDifficulty(EnumDifficulty.EASY);
        internal.setSpawnFlags(true, true);
        console.worlds.add(internal);

        if (generator != null) {
            internal.getWorld().getPopulators().addAll(generator.getDefaultPopulators(internal.getWorld()));
        }

        pluginManager.callEvent(new WorldInitEvent(internal.getWorld()));
        System.out.print("Preparing start region for level " + (console.worlds.size() - 1) + " (Seed: "
                + internal.getSeed() + ")");

        if (internal.getWorld().getKeepSpawnInMemory()) {
            short short1 = 196;
            long i = System.currentTimeMillis();
            for (int j = -short1; j <= short1; j += 16) {
                for (int k = -short1; k <= short1; k += 16) {
                    long l = System.currentTimeMillis();

                    if (l < i) {
                        i = l;
                    }

                    if (l > i + 1000L) {
                        int i1 = (short1 * 2 + 1) * (short1 * 2 + 1);
                        int j1 = (j + short1) * (short1 * 2 + 1) + k + 1;

                        System.out.println("Preparing spawn area for " + name + ", " + (j1 * 100 / i1) + "%");
                        i = l;
                    }

                    BlockPosition chunkcoordinates = internal.getSpawn();
                    internal.chunkProviderServer.getChunkAt(chunkcoordinates.getX() + j >> 4,
                            chunkcoordinates.getZ() + k >> 4);
                }
            }
        }
        pluginManager.callEvent(new WorldLoadEvent(internal.getWorld()));
        return internal.getWorld();
    }

    @Override
    public boolean unloadWorld(String name, boolean save) {
        return unloadWorld(getWorld(name), save);
    }

    @Override
    public boolean unloadWorld(World world, boolean save) {
        if (world == null) {
            return false;
        }

        WorldServer handle = ((CraftWorld) world).getHandle();

        if (!(console.worlds.contains(handle))) {
            return false;
        }

        if (!(handle.dimension > 1)) {
            return false;
        }

        if (handle.players.size() > 0) {
            return false;
        }

        WorldUnloadEvent e = new WorldUnloadEvent(handle.getWorld());
        pluginManager.callEvent(e);

        if (e.isCancelled()) {
            return false;
        }

        if (save) {
            try {
                handle.save(true, null);
                handle.saveLevel();
            } catch (ExceptionWorldConflict ex) {
                getLogger().log(Level.SEVERE, null, ex);
            }
        } else { // FlamePaper - Fix chunk memory leak
            ChunkProviderServer chunkProviderServer = handle.chunkProviderServer;
            ChunkRegionLoader regionLoader = (ChunkRegionLoader) chunkProviderServer.chunkLoader;

            regionLoader.b.clear();
            regionLoader.c.clear();

            chunkProviderServer.chunkLoader = null;
            chunkProviderServer.chunkProvider = null;
            chunkProviderServer.chunks.clear();
        }

        worlds.remove(world.getName().toLowerCase());
        console.worlds.remove(console.worlds.indexOf(handle));

        return true;
    }

    public MinecraftServer getServer() {
        return console;
    }

    @Override
    public World getWorld(String name) {
        Validate.notNull(name, "Name cannot be null");

        return worlds.get(name.toLowerCase());
    }

    @Override
    public World getWorld(UUID uid) {
        for (World world : worlds.values()) {
            if (world.getUID().equals(uid)) {
                return world;
            }
        }
        return null;
    }

    public void addWorld(World world) {
        // Check if a World already exists with the UID.
        if (getWorld(world.getUID()) != null) {
            System.out.println("World " + world.getName()
                    + " is a duplicate of another world and has been prevented from loading. Please delete the uid.dat file from "
                    + world.getName() + "'s world directory if you want to be able to load the duplicate world.");
            return;
        }
        worlds.put(world.getName().toLowerCase(), world);
    }

    @Override
    public Logger getLogger() {
        return logger;
    }

    /*
     * // PandaSpigot - jline update
     * public ConsoleReader getReader() {
     * return console.reader;
     * }
     */ // PandaSpigot

    @Override
    public PluginCommand getPluginCommand(String name) {
        Command command = commandMap.getCommand(name);

        if (command instanceof PluginCommand)
            return (PluginCommand) command;

        return null;
    }

    @Override
    public void savePlayers() {
        checkSaveState();
        playerList.savePlayers();
    }

    @Override
    public boolean addRecipe(Recipe recipe) {
        CraftRecipe toAdd;
        if (recipe instanceof CraftRecipe) {
            toAdd = (CraftRecipe) recipe;
        } else {
            if (recipe instanceof ShapedRecipe) {
                toAdd = CraftShapedRecipe.fromBukkitRecipe((ShapedRecipe) recipe);
            } else if (recipe instanceof ShapelessRecipe) {
                toAdd = CraftShapelessRecipe.fromBukkitRecipe((ShapelessRecipe) recipe);
            } else if (recipe instanceof FurnaceRecipe) {
                toAdd = CraftFurnaceRecipe.fromBukkitRecipe((FurnaceRecipe) recipe);
            } else {
                return false;
            }
        }
        toAdd.addToCraftingManager();
        CraftingManager.getInstance().sort();
        return true;
    }

    @Override
    public List<Recipe> getRecipesFor(ItemStack result) {
        Validate.notNull(result, "Result cannot be null");

        List<Recipe> results = new ArrayList<Recipe>();
        Iterator<Recipe> iter = recipeIterator();
        while (iter.hasNext()) {
            Recipe recipe = iter.next();
            ItemStack stack = recipe.getResult();
            if (stack.getType() != result.getType()) {
                continue;
            }
            if (result.getDurability() == -1 || result.getDurability() == stack.getDurability()) {
                results.add(recipe);
            }
        }
        return results;
    }

    @Override
    public Iterator<Recipe> recipeIterator() {
        return new RecipeIterator();
    }

    @Override
    public void clearRecipes() {
        CraftingManager.getInstance().recipes.clear();
        RecipesFurnace.getInstance().recipes.clear();
        RecipesFurnace.getInstance().customRecipes.clear();
    }

    @Override
    public void resetRecipes() {
        CraftingManager.getInstance().recipes = new CraftingManager().recipes;
        RecipesFurnace.getInstance().recipes = new RecipesFurnace().recipes;
        RecipesFurnace.getInstance().customRecipes.clear();
    }

    @Override
    public Map<String, String[]> getCommandAliases() {
        return GlobalConfig.getInstance().getCommandAliases();
    }

    @Override
    public boolean getOnlineMode() {
        return online.value;
    }

    @Override
    public boolean getAllowFlight() {
        return console.getAllowFlight();
    }

    @Override
    public boolean isHardcore() {
        return console.isHardcore();
    }

    @Override
    public boolean useExactLoginLocation() {
        return GlobalConfig.getInstance().isUseExactLoginLocation();
    }

    public ChunkGenerator getGenerator(String world) {
        return GlobalConfig.getInstance().getGenerator();
    }

    @Override
    @Deprecated
    public CraftMapView getMap(short id) {
        PersistentCollection collection = console.worlds.get(0).worldMaps;
        WorldMap worldmap = (WorldMap) collection.get(WorldMap.class, "map_" + id);
        if (worldmap == null) {
            return null;
        }
        return worldmap.mapView;
    }

    @Override
    public CraftMapView createMap(World world) {
        Validate.notNull(world, "World cannot be null");

        net.minecraft.server.ItemStack stack = new net.minecraft.server.ItemStack(Items.MAP, 1, -1);
        WorldMap worldmap = Items.FILLED_MAP.getSavedMap(stack, ((CraftWorld) world).getHandle());
        return worldmap.mapView;
    }

    @Override
    public void shutdown() {
        console.safeShutdown();
    }

    @Override
    public int broadcast(String message, String permission) {
        int count = 0;
        Set<Permissible> permissibles = getPluginManager().getPermissionSubscriptions(permission);

        for (Permissible permissible : permissibles) {
            if (permissible instanceof CommandSender && permissible.hasPermission(permission)) {
                CommandSender user = (CommandSender) permissible;
                user.sendMessage(message);
                count++;
            }
        }

        return count;
    }

    // Paper start
    @Override
    public void broadcast(BaseComponent component) {
        for (Player player : getOnlinePlayers()) {
            player.sendMessage(component);
        }
    }

    @Override
    public void broadcast(BaseComponent... components) {
        for (Player player : getOnlinePlayers()) {
            player.sendMessage(components);
        }
    }
    // Paper end

    @Override
    @Deprecated
    public OfflinePlayer getOfflinePlayer(String name) {
        Validate.notNull(name, "Name cannot be null");
        com.google.common.base.Preconditions.checkArgument(!org.apache.commons.lang.StringUtils.isBlank(name),
                "Name cannot be blank"); // Spigot

        OfflinePlayer result = getPlayerExact(name);
        if (result == null) {
            // Spigot Start
            GameProfile profile = null;
            // Only fetch an online UUID in online mode
            if (MinecraftServer.getServer().getOnlineMode()
                    || GlobalConfig.getInstance().isProxyOnlineMode()) // PandaSpigot - Handle
                                                                       // via setting
            {
                profile = MinecraftServer.getServer().getUserCache().getProfile(name);
            }
            // Spigot end
            if (profile == null) {
                // Make an OfflinePlayer using an offline mode UUID since the name has no
                // profile
                result = getOfflinePlayer(new GameProfile(
                        UUID.nameUUIDFromBytes(("OfflinePlayer:" + name).getBytes(Charsets.UTF_8)), name));
            } else {
                // Use the GameProfile even when we get a UUID so we ensure we still have a name
                result = getOfflinePlayer(profile);
            }
        } else {
            offlinePlayers.remove(result.getUniqueId());
        }

        return result;
    }

    @Override
    public OfflinePlayer getOfflinePlayer(UUID id) {
        Validate.notNull(id, "UUID cannot be null");

        OfflinePlayer result = getPlayer(id);
        if (result == null) {
            result = offlinePlayers.get(id);
            if (result == null) {
                result = new CraftOfflinePlayer(this, new GameProfile(id, null));
                offlinePlayers.put(id, result);
            }
        } else {
            offlinePlayers.remove(id);
        }

        return result;
    }

    public OfflinePlayer getOfflinePlayer(GameProfile profile) {
        OfflinePlayer player = new CraftOfflinePlayer(this, profile);
        offlinePlayers.put(profile.getId(), player);
        return player;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Set<String> getIPBans() {
        return new HashSet<String>(Arrays.asList(playerList.getIPBans().getEntries()));
    }

    @Override
    public void banIP(String address) {
        Validate.notNull(address, "Address cannot be null.");

        this.getBanList(org.bukkit.BanList.Type.IP).addBan(address, null, null, null);
    }

    @Override
    public void unbanIP(String address) {
        Validate.notNull(address, "Address cannot be null.");

        this.getBanList(org.bukkit.BanList.Type.IP).pardon(address);
    }

    @Override
    public Set<OfflinePlayer> getBannedPlayers() {
        Set<OfflinePlayer> result = new HashSet<OfflinePlayer>();

        for (JsonListEntry entry : playerList.getProfileBans().getValues()) {
            result.add(getOfflinePlayer((GameProfile) entry.getKey()));
        }

        return result;
    }

    @Override
    public BanList getBanList(BanList.Type type) {
        Validate.notNull(type, "Type cannot be null");

        switch (type) {
            case IP:
                return new CraftIpBanList(playerList.getIPBans());
            case NAME:
            default:
                return new CraftProfileBanList(playerList.getProfileBans());
        }
    }

    @Override
    public void setWhitelist(boolean value) {
        playerList.setHasWhitelist(value);
    }

    @Override
    public Set<OfflinePlayer> getWhitelistedPlayers() {
        Set<OfflinePlayer> result = new LinkedHashSet<OfflinePlayer>();

        for (JsonListEntry entry : playerList.getWhitelist().getValues()) {
            result.add(getOfflinePlayer((GameProfile) entry.getKey()));
        }

        return result;
    }

    @Override
    public Set<OfflinePlayer> getOperators() {
        Set<OfflinePlayer> result = new HashSet<OfflinePlayer>();

        for (JsonListEntry entry : playerList.getOPs().getValues()) {
            result.add(getOfflinePlayer((GameProfile) entry.getKey()));
        }

        return result;
    }

    @Override
    public void reloadWhitelist() {
        playerList.reloadWhitelist();
    }

    @Override
    public GameMode getDefaultGameMode() {
        return GameMode.getByValue(console.worlds.get(0).getWorldData().getGameType().getId());
    }

    @Override
    public void setDefaultGameMode(GameMode mode) {
        Validate.notNull(mode, "Mode cannot be null");

        for (World world : getWorlds()) {
            ((CraftWorld) world).getHandle().worldData.setGameType(WorldSettings.EnumGamemode.getById(mode.getValue()));
        }
    }

    @Override
    public ConsoleCommandSender getConsoleSender() {
        return console.console;
    }

    public EntityMetadataStore getEntityMetadata() {
        return entityMetadata;
    }

    public PlayerMetadataStore getPlayerMetadata() {
        return playerMetadata;
    }

    public WorldMetadataStore getWorldMetadata() {
        return worldMetadata;
    }

    @Override
    public File getWorldContainer() {
        if (this.getServer().universe != null)
            return this.getServer().universe;

        if (container == null)
            container = new File(GlobalConfig.getInstance().getWorldsFolder());

        return container;
    }

    @Override
    public OfflinePlayer[] getOfflinePlayers() {
        WorldNBTStorage storage = (WorldNBTStorage) console.worlds.get(0).getDataManager();
        String[] files = storage.getPlayerDir().list(new DatFileFilter());
        Set<OfflinePlayer> players = new HashSet<OfflinePlayer>();

        for (String file : files) {
            try {
                players.add(getOfflinePlayer(UUID.fromString(file.substring(0, file.length() - 4))));
            } catch (IllegalArgumentException ex) {
                // Who knows what is in this directory, just ignore invalid files
            }
        }

        players.addAll(getOnlinePlayers());

        return players.toArray(new OfflinePlayer[players.size()]);
    }

    @Override
    public Messenger getMessenger() {
        return messenger;
    }

    @Override
    public void sendPluginMessage(Plugin source, String channel, byte[] message) {
        StandardMessenger.validatePluginMessage(getMessenger(), source, channel, message);

        for (Player player : getOnlinePlayers()) {
            player.sendPluginMessage(source, channel, message);
        }
    }

    @Override
    public Set<String> getListeningPluginChannels() {
        Set<String> result = new ObjectOpenHashSet<String>();

        for (Player player : getOnlinePlayers()) {
            result.addAll(player.getListeningPluginChannels());
        }

        return result;
    }

    @Override
    public Inventory createInventory(InventoryHolder owner, InventoryType type) {
        // TODO: Create the appropriate type, rather than Custom?
        return new CraftInventoryCustom(owner, type);
    }

    @Override
    public Inventory createInventory(InventoryHolder owner, InventoryType type, String title) {
        return new CraftInventoryCustom(owner, type, title);
    }

    @Override
    public Inventory createInventory(InventoryHolder owner, int size) throws IllegalArgumentException {
        Validate.isTrue(size % 9 == 0, "Chests must have a size that is a multiple of 9!");
        return new CraftInventoryCustom(owner, size);
    }

    @Override
    public Inventory createInventory(InventoryHolder owner, int size, String title) throws IllegalArgumentException {
        Validate.isTrue(size % 9 == 0, "Chests must have a size that is a multiple of 9!");
        return new CraftInventoryCustom(owner, size, title);
    }

    @Override
    public int getMonsterSpawnLimit() {
        return GlobalConfig.getInstance().getMonsterSpawnLimit();
    }

    @Override
    public int getAnimalSpawnLimit() {
        return GlobalConfig.getInstance().getAnimalSpawnLimit();
    }

    @Override
    public int getWaterAnimalSpawnLimit() {
        return GlobalConfig.getInstance().getWaterAnimalSpawnLimit();
    }

    @Override
    public int getAmbientSpawnLimit() {
        return GlobalConfig.getInstance().getAmbientSpawnLimit();
    }

    @Override
    public boolean isPrimaryThread() {
        return Thread.currentThread().equals(console.primaryThread);
    }

    @Override
    public String getMotd() {
        return console.getMotd();
    }

    public List<String> tabComplete(net.minecraft.server.ICommandListener sender, String message) {
        return tabComplete(sender, message, null); // PaperSpigot - location tab-completes. Original code here moved
                                                   // below
    }

    // PaperSpigot start - add BlockPosition support
    /*
     * this code is copied, except for the noted change, from the original
     * tabComplete(net.minecraft.server.ICommandListener sender, String message)
     * method
     */
    public List<String> tabComplete(net.minecraft.server.ICommandListener sender, String message,
            BlockPosition blockPosition) {
        if (!(sender instanceof EntityPlayer)) {
            return ImmutableList.of();
        }

        Player player = ((EntityPlayer) sender).getBukkitEntity();
        // PandaSpigot start - TabCompleteEvent
        List<String> offers;
        if (message.startsWith("/")) {
            offers = tabCompleteCommand(player, message, blockPosition);
        } else {
            offers = tabCompleteChat(player, message);
        }
        org.bukkit.event.server.TabCompleteEvent tabEvent = new org.bukkit.event.server.TabCompleteEvent(player,
                message, offers, message.startsWith("/"),
                blockPosition != null
                        ? new Location(player.getWorld(), blockPosition.getX(), blockPosition.getY(),
                                blockPosition.getZ())
                        : null);
        getPluginManager().callEvent(tabEvent);
        return tabEvent.isCancelled() ? Collections.emptyList() : tabEvent.getCompletions();
        // PandaSpigot end
    }
    // PaperSpigot end

    public List<String> tabCompleteCommand(Player player, String message) {
        return tabCompleteCommand(player, message, null); // PaperSpigot - location tab-completes. Original code here
                                                          // moved below
    }

    // PaperSpigot start - add BlockPosition support
    /*
     * this code is copied, except for the noted change, from the original
     * tabCompleteCommand(Player player, String message) method
     */
    public List<String> tabCompleteCommand(Player player, String message, BlockPosition blockPosition) {
        // Spigot Start
        if ((GlobalConfig.getInstance().getTabComplete() < 0
                || message.length() <= GlobalConfig.getInstance().getTabComplete())
                && !message.contains(" ")) {
            return ImmutableList.of();
        }
        // Spigot End

        List<String> completions = null;
        try {
            // send location info if present
            // completions = getCommandMap().tabComplete(player, message.substring(1));
            if (blockPosition == null || !GlobalConfig.getInstance().isAllowBlockLocationTabCompletion()) {
                completions = getCommandMap().tabComplete(player, message.substring(1));
            } else {
                completions = getCommandMap().tabComplete(player, message.substring(1), new Location(player.getWorld(),
                        blockPosition.getX(), blockPosition.getY(), blockPosition.getZ()));
            }
        } catch (CommandException ex) {
            player.sendMessage(
                    ChatColor.RED + "An internal error occurred while attempting to tab-complete this command");
            getLogger().log(Level.SEVERE,
                    "Exception when " + player.getName() + " attempted to tab complete " + message, ex);
        }

        return completions == null ? ImmutableList.<String>of() : completions;
    }
    // PaperSpigot end

    public List<String> tabCompleteChat(Player player, String message) {
        List<String> completions = new ArrayList<String>();
        PlayerChatTabCompleteEvent event = new PlayerChatTabCompleteEvent(player, message, completions);
        String token = event.getLastToken();
        for (Player p : getOnlinePlayers())
            if (player.canSeeOnTab(p) && StringUtil.startsWithIgnoreCase(p.getName(), token))
                completions.add(p.getName());

        pluginManager.callEvent(event);

        Iterator<?> it = completions.iterator();
        while (it.hasNext()) {
            Object current = it.next();
            if (!(current instanceof String)) {
                // Sanity
                it.remove();
            }
        }
        Collections.sort(completions, String.CASE_INSENSITIVE_ORDER);
        return completions;
    }

    @Override
    public CraftItemFactory getItemFactory() {
        return CraftItemFactory.instance();
    }

    public void checkSaveState() {
        if (this.playerCommandState || this.printSaveWarning || this.console.autosavePeriod <= 0) {
            return;
        }
        this.printSaveWarning = true;
        getLogger().log(Level.WARNING,
                "A manual (plugin-induced) save has been detected while server is configured to auto-save. This may affect performance.",
                warningState == WarningState.ON ? new Throwable() : null);
    }

    @Override
    public CraftIconCache getServerIcon() {
        return icon;
    }

    @Override
    public CraftIconCache loadServerIcon(File file) throws Exception {
        Validate.notNull(file, "File cannot be null");
        if (!file.isFile()) {
            throw new IllegalArgumentException(file + " is not a file");
        }
        return loadServerIcon0(file);
    }

    static CraftIconCache loadServerIcon0(File file) throws Exception {
        return loadServerIcon0(ImageIO.read(file));
    }

    @Override
    public CraftIconCache loadServerIcon(BufferedImage image) throws Exception {
        Validate.notNull(image, "Image cannot be null");
        return loadServerIcon0(image);
    }

    static CraftIconCache loadServerIcon0(BufferedImage image) throws Exception {
        ByteBuf bytebuf = Unpooled.buffer();

        Validate.isTrue(image.getWidth() == 64, "Must be 64 pixels wide");
        Validate.isTrue(image.getHeight() == 64, "Must be 64 pixels high");
        ImageIO.write(image, "PNG", new ByteBufOutputStream(bytebuf));
        ByteBuf bytebuf1 = Base64.encode(bytebuf);

        return new CraftIconCache("data:image/png;base64," + bytebuf1.toString(Charsets.UTF_8));
    }

    @Override
    public void setIdleTimeout(int threshold) {
        console.setIdleTimeout(threshold);
    }

    @Override
    public int getIdleTimeout() {
        return console.getIdleTimeout();
    }

    @Override
    public ChunkGenerator.ChunkData createChunkData(World world) {
        return new CraftChunkData(world);
    }

    @Deprecated
    @Override
    public UnsafeValues getUnsafe() {
        return CraftMagicNumbers.INSTANCE;
    }

    // PandaSpigot start - PlayerProfile API
    @Override
    public com.destroystokyo.paper.profile.PlayerProfile createProfile(UUID uuid) {
        return createProfile(uuid, null);
    }

    @Override
    public com.destroystokyo.paper.profile.PlayerProfile createProfile(String name) {
        return createProfile(null, name);
    }

    @Override
    public com.destroystokyo.paper.profile.PlayerProfile createProfile(UUID uuid, String name) {
        Player player = uuid != null ? Bukkit.getPlayer(uuid) : (name != null ? Bukkit.getPlayerExact(name) : null);
        if (player != null) {
            return new com.destroystokyo.paper.profile.CraftPlayerProfile((CraftPlayer) player);
        }
        return new com.destroystokyo.paper.profile.CraftPlayerProfile(uuid, name);
    }
    // PandaSpigot end

    private final Spigot spigot = new Spigot() {

        // PaperSpigot start - Add getTPS (Further improve tick loop)
        @Override
        public double[] getTPS() {
            return new double[] {
                    MinecraftServer.getServer().tps1.getAverage(),
                    MinecraftServer.getServer().tps5.getAverage(),
                    MinecraftServer.getServer().tps15.getAverage()
            };
        }

        @Override
        public void restart() {
            org.spigotmc.RestartCommand.restart();
        }

        @Override
        public void broadcast(BaseComponent component) {
            for (Player player : getOnlinePlayers())
                player.spigot().sendMessage(component);
        }

        @Override
        public void broadcast(BaseComponent... components) {
            for (Player player : getOnlinePlayers())
                player.spigot().sendMessage(components);
        }
    };

    public Spigot spigot() {
        return spigot;
    }

    @Override
    public String getShutdownMessage() {
        return GlobalConfig.getInstance().getShutdownMessage();
    }
}
