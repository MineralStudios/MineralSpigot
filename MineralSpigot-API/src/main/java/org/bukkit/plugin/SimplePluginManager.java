package org.bukkit.plugin;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.Validate;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.PluginCommandYamlParser;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.permissions.Permissible;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.util.FileUtil;
import org.github.paperspigot.event.ServerExceptionEvent;
import org.github.paperspigot.exception.ServerEventException;
import org.github.paperspigot.exception.ServerPluginEnableDisableException;

import com.google.common.collect.ImmutableSet;

import it.unimi.dsi.fastutil.bytes.Byte2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

/**
 * Handles all plugin management from the Server
 */
public final class SimplePluginManager implements PluginManager {
    private final Server server;
    private final Map<Pattern, PluginLoader> fileAssociations = new Object2ObjectOpenHashMap<Pattern, PluginLoader>();
    private final List<Plugin> plugins = new ArrayList<Plugin>();
    private final Map<String, Plugin> lookupNames = new Object2ObjectOpenHashMap<String, Plugin>();
    private static File updateDirectory = null;
    private final SimpleCommandMap commandMap;
    private final Map<String, Permission> permissions = new Object2ObjectOpenHashMap<String, Permission>();
    private final Byte2ObjectLinkedOpenHashMap<Set<Permission>> defaultPerms = new Byte2ObjectLinkedOpenHashMap<Set<Permission>>();
    private final Map<String, Map<Permissible, Boolean>> permSubs = new Object2ObjectOpenHashMap<String, Map<Permissible, Boolean>>();
    private final Byte2ObjectLinkedOpenHashMap<Map<Permissible, Boolean>> defSubs = new Byte2ObjectLinkedOpenHashMap<Map<Permissible, Boolean>>();

    public SimplePluginManager(Server instance, SimpleCommandMap commandMap) {
        server = instance;
        this.commandMap = commandMap;

        defaultPerms.put((byte) 1, new ObjectOpenHashSet<Permission>());
        defaultPerms.put((byte) 0, new ObjectOpenHashSet<Permission>());
    }

    /**
     * Registers the specified plugin loader
     *
     * @param loader Class name of the PluginLoader to register
     * @throws IllegalArgumentException Thrown when the given Class is not a
     *                                  valid PluginLoader
     */
    public void registerInterface(Class<? extends PluginLoader> loader) throws IllegalArgumentException {
        PluginLoader instance;

        if (PluginLoader.class.isAssignableFrom(loader)) {
            Constructor<? extends PluginLoader> constructor;

            try {
                constructor = loader.getConstructor(Server.class);
                instance = constructor.newInstance(server);
            } catch (NoSuchMethodException ex) {
                String className = loader.getName();

                throw new IllegalArgumentException(
                        String.format("Class %s does not have a public %s(Server) constructor", className, className),
                        ex);
            } catch (Exception ex) {
                throw new IllegalArgumentException(
                        String.format("Unexpected exception %s while attempting to construct a new instance of %s",
                                ex.getClass().getName(), loader.getName()),
                        ex);
            }
        } else {
            throw new IllegalArgumentException(
                    String.format("Class %s does not implement interface PluginLoader", loader.getName()));
        }

        Pattern[] patterns = instance.getPluginFileFilters();

        synchronized (this) {
            for (Pattern pattern : patterns) {
                fileAssociations.put(pattern, instance);
            }
        }
    }

    /**
     * Loads the plugins contained within the specified directory
     *
     * @param directory Directory to check for plugins
     * @return A list of all plugins loaded
     */
    public Plugin[] loadPlugins(File directory) {
        // PandaSpigot start - extra jars
        return this.loadPlugins(directory, java.util.Collections.emptyList());
    }

    public Plugin[] loadPlugins(File directory, List<File> extraPluginJars) {
        // PandaSpigot end
        Validate.notNull(directory, "Directory cannot be null");
        Validate.isTrue(directory.isDirectory(), "Directory must be a directory");

        List<Plugin> result = new ArrayList<Plugin>();
        Set<Pattern> filters = fileAssociations.keySet();

        if (!(server.getUpdateFolder().equals(""))) {
            updateDirectory = new File(directory, server.getUpdateFolder());
        }

        Map<String, File> plugins = new Object2ObjectOpenHashMap<String, File>();
        Set<String> loadedPlugins = new ObjectOpenHashSet<String>();
        Map<String, Collection<String>> dependencies = new Object2ObjectOpenHashMap<String, Collection<String>>();
        Map<String, Collection<String>> softDependencies = new Object2ObjectOpenHashMap<String, Collection<String>>();

        // This is where it figures out all possible plugins
        // PandaSpigot start - extra jars
        List<File> pluginJars = new ArrayList<>(java.util.Arrays.asList(directory.listFiles()));
        pluginJars.addAll(extraPluginJars);
        for (File file : pluginJars) {
            // PandaSpigot end
            PluginLoader loader = null;
            for (Pattern filter : filters) {
                Matcher match = filter.matcher(file.getName());
                if (match.find()) {
                    loader = fileAssociations.get(filter);
                }
            }

            if (loader == null)
                continue;

            PluginDescriptionFile description = null;
            try {
                description = loader.getPluginDescription(file);
                String name = description.getName();
                if (name.equalsIgnoreCase("bukkit") || name.equalsIgnoreCase("minecraft")
                        || name.equalsIgnoreCase("mojang")) {
                    server.getLogger().log(Level.SEVERE, "Could not load '" + file.getPath() + "' in folder '"
                            + file.getParentFile().getPath() + "': Restricted Name"); // PandaSpigot
                    continue;
                } else if (description.rawName.indexOf(' ') != -1) {
                    server.getLogger().warning(String.format(
                            "Plugin `%s' uses the space-character (0x20) in its name `%s' - this is discouraged",
                            description.getFullName(),
                            description.rawName));
                }
            } catch (InvalidDescriptionException ex) {
                server.getLogger().log(Level.SEVERE,
                        "Could not load '" + file.getPath() + "' in folder '" + file.getParentFile().getPath() + "'",
                        ex); // PandaSpigot
                continue;
            }

            File replacedFile = plugins.put(description.getName(), file);
            if (replacedFile != null) {
                server.getLogger().severe(String.format(
                        "Ambiguous plugin name `%s' for files `%s' and `%s' in `%s'",
                        description.getName(),
                        file.getPath(),
                        replacedFile.getPath(),
                        file.getParentFile().getPath() // PandaSpigot
                ));
            }

            Collection<String> softDependencySet = description.getSoftDepend();
            if (softDependencySet != null && !softDependencySet.isEmpty()) {
                if (softDependencies.containsKey(description.getName())) {
                    // Duplicates do not matter, they will be removed together if applicable
                    softDependencies.get(description.getName()).addAll(softDependencySet);
                } else {
                    softDependencies.put(description.getName(), new LinkedList<String>(softDependencySet));
                }
            }

            Collection<String> dependencySet = description.getDepend();
            if (dependencySet != null && !dependencySet.isEmpty()) {
                dependencies.put(description.getName(), new LinkedList<String>(dependencySet));
            }

            Collection<String> loadBeforeSet = description.getLoadBefore();
            if (loadBeforeSet != null && !loadBeforeSet.isEmpty()) {
                for (String loadBeforeTarget : loadBeforeSet) {
                    if (softDependencies.containsKey(loadBeforeTarget)) {
                        softDependencies.get(loadBeforeTarget).add(description.getName());
                    } else {
                        // softDependencies is never iterated, so 'ghost' plugins aren't an issue
                        Collection<String> shortSoftDependency = new LinkedList<String>();
                        shortSoftDependency.add(description.getName());
                        softDependencies.put(loadBeforeTarget, shortSoftDependency);
                    }
                }
            }
        }

        while (!plugins.isEmpty()) {
            boolean missingDependency = true;
            Iterator<String> pluginIterator = plugins.keySet().iterator();

            while (pluginIterator.hasNext()) {
                String plugin = pluginIterator.next();

                if (dependencies.containsKey(plugin)) {
                    Iterator<String> dependencyIterator = dependencies.get(plugin).iterator();

                    while (dependencyIterator.hasNext()) {
                        String dependency = dependencyIterator.next();

                        // Dependency loaded
                        if (loadedPlugins.contains(dependency)) {
                            dependencyIterator.remove();

                            // We have a dependency not found
                        } else if (!plugins.containsKey(dependency)) {
                            missingDependency = false;
                            File file = plugins.get(plugin);
                            pluginIterator.remove();
                            softDependencies.remove(plugin);
                            dependencies.remove(plugin);

                            server.getLogger().log(
                                    Level.SEVERE,
                                    "Could not load '" + file.getPath() + "' in folder '"
                                            + file.getParentFile().getPath() + "'", // PandaSpigot
                                    new UnknownDependencyException(dependency));
                            break;
                        }
                    }

                    if (dependencies.containsKey(plugin) && dependencies.get(plugin).isEmpty()) {
                        dependencies.remove(plugin);
                    }
                }
                if (softDependencies.containsKey(plugin)) {
                    Iterator<String> softDependencyIterator = softDependencies.get(plugin).iterator();

                    while (softDependencyIterator.hasNext()) {
                        String softDependency = softDependencyIterator.next();

                        // Soft depend is no longer around
                        if (!plugins.containsKey(softDependency)) {
                            softDependencyIterator.remove();
                        }
                    }

                    if (softDependencies.get(plugin).isEmpty()) {
                        softDependencies.remove(plugin);
                    }
                }
                if (!(dependencies.containsKey(plugin) || softDependencies.containsKey(plugin))
                        && plugins.containsKey(plugin)) {
                    // We're clear to load, no more soft or hard dependencies left
                    File file = plugins.get(plugin);
                    pluginIterator.remove();
                    missingDependency = false;

                    try {
                        result.add(loadPlugin(file));
                        loadedPlugins.add(plugin);
                        continue;
                    } catch (InvalidPluginException ex) {
                        server.getLogger().log(Level.SEVERE, "Could not load '" + file.getPath() + "' in folder '"
                                + file.getParentFile().getPath() + "'", ex); // PandaSpigot
                    }
                }
            }

            if (missingDependency) {
                // We now iterate over plugins until something loads
                // This loop will ignore soft dependencies
                pluginIterator = plugins.keySet().iterator();

                while (pluginIterator.hasNext()) {
                    String plugin = pluginIterator.next();

                    if (!dependencies.containsKey(plugin)) {
                        softDependencies.remove(plugin);
                        missingDependency = false;
                        File file = plugins.get(plugin);
                        pluginIterator.remove();

                        try {
                            result.add(loadPlugin(file));
                            loadedPlugins.add(plugin);
                            break;
                        } catch (InvalidPluginException ex) {
                            server.getLogger().log(Level.SEVERE, "Could not load '" + file.getPath() + "' in folder '"
                                    + file.getParentFile().getPath() + "'", ex); // PandaSpigot
                        }
                    }
                }
                // We have no plugins left without a depend
                if (missingDependency) {
                    softDependencies.clear();
                    dependencies.clear();
                    Iterator<File> failedPluginIterator = plugins.values().iterator();

                    while (failedPluginIterator.hasNext()) {
                        File file = failedPluginIterator.next();
                        failedPluginIterator.remove();
                        server.getLogger().log(Level.SEVERE, "Could not load '" + file.getPath() + "' in folder '"
                                + file.getParentFile().getPath() + "': circular dependency detected"); // PandaSpigot
                    }
                }
            }
        }

        return result.toArray(new Plugin[result.size()]);
    }

    /**
     * Loads the plugin in the specified file
     * <p>
     * File must be valid according to the current enabled Plugin interfaces
     *
     * @param file File containing the plugin to load
     * @return The Plugin loaded, or null if it was invalid
     * @throws InvalidPluginException     Thrown when the specified file is not a
     *                                    valid plugin
     * @throws UnknownDependencyException If a required dependency could not
     *                                    be found
     */
    public synchronized Plugin loadPlugin(File file) throws InvalidPluginException, UnknownDependencyException {
        Validate.notNull(file, "File cannot be null");

        file = checkUpdate(file); // PandaSpigot - update the reference in case checkUpdate renamed it

        Set<Pattern> filters = fileAssociations.keySet();
        Plugin result = null;

        for (Pattern filter : filters) {
            String name = file.getName();
            Matcher match = filter.matcher(name);

            if (match.find()) {
                PluginLoader loader = fileAssociations.get(filter);

                result = loader.loadPlugin(file);
            }
        }

        if (result != null) {
            plugins.add(result);
            lookupNames.put(result.getDescription().getName().toLowerCase(), result); // Spigot
        }

        return result;
    }

    // PandaSpigot start - Update Folder Uses Plugin Name to replace
    /**
     * Replaces a plugin with a plugin of the same plugin name in the update folder.
     * 
     * @param file
     * @throws InvalidPluginException
     */
    private File checkUpdate(File file) throws InvalidPluginException {
        if (updateDirectory == null || !updateDirectory.isDirectory()) {
            return file;
        }
        PluginLoader pluginLoader = getPluginLoader(file);
        try {
            String pluginName = pluginLoader.getPluginDescription(file).getName();
            for (File updateFile : updateDirectory.listFiles()) {
                if (!updateFile.isFile())
                    continue;
                PluginLoader updatePluginLoader = getPluginLoader(updateFile);
                if (updatePluginLoader == null)
                    continue;
                String updatePluginName;
                try {
                    updatePluginName = updatePluginLoader.getPluginDescription(updateFile).getName();
                    // We failed to load this data for some reason, so, we'll skip over this
                } catch (InvalidDescriptionException ex) {
                    continue;
                }
                if (!pluginName.equals(updatePluginName))
                    continue;
                if (!FileUtil.copy(updateFile, file))
                    continue;
                File newName = new File(file.getParentFile(), updateFile.getName());
                file.renameTo(newName);
                updateFile.delete();
                return newName;
            }
        } catch (InvalidDescriptionException e) {
            throw new InvalidPluginException(e);
        }
        return file;
    }

    private PluginLoader getPluginLoader(File file) {
        Set<Pattern> filters = fileAssociations.keySet();
        for (Pattern filter : filters) {
            Matcher match = filter.matcher(file.getName());
            if (match.find()) {
                return fileAssociations.get(filter);
            }
        }
        return null;
    }
    // PandaSpigot end

    /**
     * Checks if the given plugin is loaded and returns it when applicable
     * <p>
     * Please note that the name of the plugin is case-sensitive
     *
     * @param name Name of the plugin to check
     * @return Plugin if it exists, otherwise null
     */
    public synchronized Plugin getPlugin(String name) {
        return lookupNames.get(name.replace(' ', '_').toLowerCase()); // Spigot
    }

    public synchronized Plugin[] getPlugins() {
        return plugins.toArray(new Plugin[0]);
    }

    /**
     * Checks if the given plugin is enabled or not
     * <p>
     * Please note that the name of the plugin is case-sensitive.
     *
     * @param name Name of the plugin to check
     * @return true if the plugin is enabled, otherwise false
     */
    public boolean isPluginEnabled(String name) {
        Plugin plugin = getPlugin(name);

        return isPluginEnabled(plugin);
    }

    /**
     * Checks if the given plugin is enabled or not
     *
     * @param plugin Plugin to check
     * @return true if the plugin is enabled, otherwise false
     */
    public boolean isPluginEnabled(Plugin plugin) {
        if ((plugin != null) && (plugins.contains(plugin))) {
            return plugin.isEnabled();
        } else {
            return false;
        }
    }

    public void enablePlugin(final Plugin plugin) {
        if (!plugin.isEnabled()) {
            List<Command> pluginCommands = PluginCommandYamlParser.parse(plugin);

            if (!pluginCommands.isEmpty()) {
                commandMap.registerAll(plugin.getDescription().getName(), pluginCommands);
            }

            try {
                plugin.getPluginLoader().enablePlugin(plugin);
            } catch (Throwable ex) {
                handlePluginException("Error occurred (in the plugin loader) while enabling "
                        + plugin.getDescription().getFullName() + " (Is it up to date?)", ex, plugin);
            }

            HandlerList.bakeAll();
        }
    }

    public void disablePlugins() {
        Plugin[] plugins = getPlugins();
        for (int i = plugins.length - 1; i >= 0; i--) {
            disablePlugin(plugins[i]);
        }
    }

    public void disablePlugin(final Plugin plugin) {
        if (plugin.isEnabled()) {
            try {
                plugin.getPluginLoader().disablePlugin(plugin);
            } catch (Throwable ex) {
                handlePluginException("Error occurred (in the plugin loader) while disabling "
                        + plugin.getDescription().getFullName() + " (Is it up to date?)", ex, plugin); // Paper
            }

            try {
                server.getScheduler().cancelTasks(plugin);
            } catch (Throwable ex) {
                handlePluginException("Error occurred (in the plugin loader) while cancelling tasks for "
                        + plugin.getDescription().getFullName() + " (Is it up to date?)", ex, plugin); // Paper
            }

            try {
                server.getServicesManager().unregisterAll(plugin);
            } catch (Throwable ex) {
                handlePluginException("Error occurred (in the plugin loader) while unregistering services for "
                        + plugin.getDescription().getFullName() + " (Is it up to date?)", ex, plugin); // Paper
            }

            try {
                HandlerList.unregisterAll(plugin);
            } catch (Throwable ex) {
                handlePluginException("Error occurred (in the plugin loader) while unregistering events for "
                        + plugin.getDescription().getFullName() + " (Is it up to date?)", ex, plugin); // Paper
            }

            try {
                server.getMessenger().unregisterIncomingPluginChannel(plugin);
                server.getMessenger().unregisterOutgoingPluginChannel(plugin);
            } catch (Throwable ex) {
                handlePluginException("Error occurred (in the plugin loader) while unregistering plugin channels for "
                        + plugin.getDescription().getFullName() + " (Is it up to date?)", ex, plugin); // Paper
            }
        }
    }

    // Paper start
    private void handlePluginException(String msg, Throwable ex, Plugin plugin) {
        server.getLogger().log(Level.SEVERE, msg, ex);
        callEvent(new ServerExceptionEvent(new ServerPluginEnableDisableException(msg, ex, plugin)));
    }
    // Paper end

    public void clearPlugins() {
        synchronized (this) {
            disablePlugins();
            plugins.clear();
            lookupNames.clear();
            HandlerList.unregisterAll();
            fileAssociations.clear();
            permissions.clear();
            defaultPerms.get((byte) 1).clear();
            defaultPerms.get((byte) 0).clear();
        }
    }

    /**
     * Calls an event with the given details.
     * <p>
     * This method only synchronizes when the event is not asynchronous.
     *
     * @param event Event details
     */
    public void callEvent(Event event) {
        if (event.isAsynchronous()) {
            if (Thread.holdsLock(this)) {
                throw new IllegalStateException(
                        event.getEventName() + " cannot be triggered asynchronously from inside synchronized code.");
            }
            if (server.isPrimaryThread()) {
                throw new IllegalStateException(
                        event.getEventName() + " cannot be triggered asynchronously from primary server thread.");
            }
            fireEvent(event);
        } else {
            synchronized (this) {
                fireEvent(event);
            }
        }
    }

    private void fireEvent(Event event) {
        HandlerList handlers = event.getHandlers();
        RegisteredListener[] listeners = handlers.getRegisteredListeners();

        listeners: for (RegisteredListener registration : listeners) {

            // Fix LPX error with bots
            if (event instanceof PlayerEvent) {
                PlayerEvent playerEvent = (PlayerEvent) event;
                Player player = playerEvent.getPlayer();

                if (player.isFakePlayer()) {
                    for (String pl : player.getBotIncompatiblePlugins()) {
                        if (registration.getPlugin().getName().equals(pl)) {
                            continue listeners;
                        }
                    }
                }
            }

            if (!registration.getPlugin().isEnabled()) {
                continue;
            }

            try {
                registration.callEvent(event);
            } catch (AuthorNagException ex) {
                Plugin plugin = registration.getPlugin();

                if (plugin.isNaggable()) {
                    plugin.setNaggable(false);

                    server.getLogger().log(Level.SEVERE, String.format(
                            "Nag author(s): '%s' of '%s' about the following: %s",
                            plugin.getDescription().getAuthors(),
                            plugin.getDescription().getFullName(),
                            ex.getMessage()));
                }
            } catch (Throwable ex) {
                // Paper start - error reporting
                String msg = "Could not pass event " + event.getEventName() + " to "
                        + registration.getPlugin().getDescription().getFullName();
                server.getLogger().log(Level.SEVERE, msg, ex);
                if (!(event instanceof ServerExceptionEvent)) { // We don't want to cause an endless event loop
                    callEvent(new ServerExceptionEvent(new ServerEventException(msg, ex, registration.getPlugin(),
                            registration.getListener(), event)));
                }
                // Paper end
            }
        }
    }

    public void registerEvents(Listener listener, Plugin plugin) {
        if (!plugin.isEnabled()) {
            throw new IllegalPluginAccessException("Plugin attempted to register " + listener + " while not enabled");
        }

        for (Map.Entry<Class<? extends Event>, Set<RegisteredListener>> entry : plugin.getPluginLoader()
                .createRegisteredListeners(listener, plugin).entrySet()) {
            getEventListeners(getRegistrationClass(entry.getKey())).registerAll(entry.getValue());
        }

    }

    public void registerEvent(Class<? extends Event> event, Listener listener, EventPriority priority,
            EventExecutor executor, Plugin plugin) {
        registerEvent(event, listener, priority, executor, plugin, false);
    }

    /**
     * Registers the given event to the specified listener using a directly
     * passed EventExecutor
     *
     * @param event           Event class to register
     * @param listener        PlayerListener to register
     * @param priority        Priority of this event
     * @param executor        EventExecutor to register
     * @param plugin          Plugin to register
     * @param ignoreCancelled Do not call executor if event was already
     *                        cancelled
     */
    public void registerEvent(Class<? extends Event> event, Listener listener, EventPriority priority,
            EventExecutor executor, Plugin plugin, boolean ignoreCancelled) {
        Validate.notNull(listener, "Listener cannot be null");
        Validate.notNull(priority, "Priority cannot be null");
        Validate.notNull(executor, "Executor cannot be null");
        Validate.notNull(plugin, "Plugin cannot be null");

        if (!plugin.isEnabled()) {
            throw new IllegalPluginAccessException("Plugin attempted to register " + event + " while not enabled");
        }

        getEventListeners(event)
                .register(new RegisteredListener(listener, executor, priority, plugin, ignoreCancelled));
    }

    private HandlerList getEventListeners(Class<? extends Event> type) {
        try {
            Method method = getRegistrationClass(type).getDeclaredMethod("getHandlerList");
            method.setAccessible(true);
            return (HandlerList) method.invoke(null);
        } catch (Exception e) {
            throw new IllegalPluginAccessException(e.toString());
        }
    }

    private Class<? extends Event> getRegistrationClass(Class<? extends Event> clazz) {
        try {
            clazz.getDeclaredMethod("getHandlerList");
            return clazz;
        } catch (NoSuchMethodException e) {
            if (clazz.getSuperclass() != null
                    && !clazz.getSuperclass().equals(Event.class)
                    && Event.class.isAssignableFrom(clazz.getSuperclass())) {
                return getRegistrationClass(clazz.getSuperclass().asSubclass(Event.class));
            } else {
                throw new IllegalPluginAccessException("Unable to find handler list for event " + clazz.getName()
                        + ". Static getHandlerList method required!");
            }
        }
    }

    public Permission getPermission(String name) {
        return permissions.get(name.toLowerCase());
    }

    public void addPermission(Permission perm) {
        String name = perm.getName().toLowerCase();

        if (permissions.containsKey(name)) {
            throw new IllegalArgumentException("The permission " + name + " is already defined!");
        }

        permissions.put(name, perm);
        calculatePermissionDefault(perm);
    }

    public Set<Permission> getDefaultPermissions(boolean op) {
        return ImmutableSet.copyOf(defaultPerms.get(op ? (byte) 1 : (byte) 0));
    }

    public void removePermission(Permission perm) {
        removePermission(perm.getName());
    }

    public void removePermission(String name) {
        permissions.remove(name.toLowerCase());
    }

    public void recalculatePermissionDefaults(Permission perm) {
        if (perm != null && permissions.containsKey(perm.getName().toLowerCase())) {
            defaultPerms.get((byte) 1).remove(perm);
            defaultPerms.get((byte) 0).remove(perm);

            calculatePermissionDefault(perm);
        }
    }

    private void calculatePermissionDefault(Permission perm) {
        if ((perm.getDefault() == PermissionDefault.OP) || (perm.getDefault() == PermissionDefault.TRUE)) {
            defaultPerms.get((byte) 1).add(perm);
            dirtyPermissibles(true);
        }
        if ((perm.getDefault() == PermissionDefault.NOT_OP) || (perm.getDefault() == PermissionDefault.TRUE)) {
            defaultPerms.get((byte) 0).add(perm);
            dirtyPermissibles(false);
        }
    }

    private void dirtyPermissibles(boolean op) {
        Set<Permissible> permissibles = getDefaultPermSubscriptions(op);

        for (Permissible p : permissibles) {
            p.recalculatePermissions();
        }
    }

    public void subscribeToPermission(String permission, Permissible permissible) {
        String name = permission.toLowerCase();
        Map<Permissible, Boolean> map = permSubs.get(name);

        if (map == null) {
            map = new WeakHashMap<Permissible, Boolean>();
            permSubs.put(name, map);
        }

        map.put(permissible, true);
    }

    public void unsubscribeFromPermission(String permission, Permissible permissible) {
        String name = permission.toLowerCase();
        Map<Permissible, Boolean> map = permSubs.get(name);

        if (map != null) {
            map.remove(permissible);

            if (map.isEmpty()) {
                permSubs.remove(name);
            }
        }
    }

    public Set<Permissible> getPermissionSubscriptions(String permission) {
        String name = permission.toLowerCase();
        Map<Permissible, Boolean> map = permSubs.get(name);

        if (map == null) {
            return ImmutableSet.of();
        } else {
            return ImmutableSet.copyOf(map.keySet());
        }
    }

    public void subscribeToDefaultPerms(boolean op, Permissible permissible) {
        Map<Permissible, Boolean> map = defSubs.get(op ? (byte) 1 : (byte) 0);

        if (map == null) {
            map = new WeakHashMap<Permissible, Boolean>();
            defSubs.put(op ? (byte) 1 : (byte) 0, map);
        }

        map.put(permissible, true);
    }

    public void unsubscribeFromDefaultPerms(boolean op, Permissible permissible) {
        Map<Permissible, Boolean> map = defSubs.get(op ? (byte) 1 : (byte) 0);

        if (map != null) {
            map.remove(permissible);

            if (map.isEmpty()) {
                defSubs.remove(op ? (byte) 1 : (byte) 0);
            }
        }
    }

    public Set<Permissible> getDefaultPermSubscriptions(boolean op) {
        Map<Permissible, Boolean> map = defSubs.get(op ? (byte) 1 : (byte) 0);

        if (map == null) {
            return ImmutableSet.of();
        } else {
            return ImmutableSet.copyOf(map.keySet());
        }
    }

    public Set<Permission> getPermissions() {
        return new ObjectOpenHashSet<Permission>(permissions.values());
    }
}
