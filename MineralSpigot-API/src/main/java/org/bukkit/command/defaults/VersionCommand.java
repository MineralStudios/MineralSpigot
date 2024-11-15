package org.bukkit.command.defaults;

import com.google.common.base.Charsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.util.StringUtil;

import com.google.common.collect.ImmutableList;
import com.google.common.io.Resources;
import java.io.BufferedReader;
import java.io.IOException;
import java.net.URL;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

// PandaSpigot start
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
// PandaSpigot end

public class VersionCommand extends BukkitCommand {
    public VersionCommand(String name) {
        super(name);

        this.description = "Gets the version of this server including any plugins in use";
        this.usageMessage = "/version [plugin name]";
        this.setPermission("bukkit.command.version");
        this.setAliases(Arrays.asList("ver", "about"));
    }

    @Override
    public boolean execute(CommandSender sender, String currentAlias, String[] args) {
        if (!testPermission(sender))
            return true;

        if (args.length == 0) {
            sender.sendMessage(Bukkit.getVersionMessage()); // PandaSpigot - Use Bukkit.getVersionMessage
            sendVersion(sender);
        } else {
            StringBuilder name = new StringBuilder();

            for (String arg : args) {
                if (name.length() > 0) {
                    name.append(' ');
                }

                name.append(arg);
            }

            String pluginName = name.toString();
            Plugin exactPlugin = Bukkit.getPluginManager().getPlugin(pluginName);
            if (exactPlugin != null) {
                describeToSender(exactPlugin, sender);
                return true;
            }

            boolean found = false;
            pluginName = pluginName.toLowerCase();
            for (Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
                if (plugin.getName().toLowerCase().contains(pluginName)) {
                    describeToSender(plugin, sender);
                    found = true;
                }
            }

            if (!found) {
                sender.sendMessage("This server is not running any plugin by that name.");
                sender.sendMessage("Use /plugins to get a list of plugins.");
            }
        }
        return true;
    }

    private void describeToSender(Plugin plugin, CommandSender sender) {
        PluginDescriptionFile desc = plugin.getDescription();
        sender.sendMessage(
                ChatColor.GREEN + desc.getName() + ChatColor.WHITE + " version " + ChatColor.GREEN + desc.getVersion());

        if (desc.getDescription() != null) {
            sender.sendMessage(desc.getDescription());
        }

        if (desc.getWebsite() != null) {
            sender.sendMessage("Website: " + ChatColor.GREEN + desc.getWebsite());
        }

        if (!desc.getAuthors().isEmpty()) {
            if (desc.getAuthors().size() == 1) {
                sender.sendMessage("Author: " + getAuthors(desc));
            } else {
                sender.sendMessage("Authors: " + getAuthors(desc));
            }
        }
    }

    private String getAuthors(final PluginDescriptionFile desc) {
        StringBuilder result = new StringBuilder();
        List<String> authors = desc.getAuthors();

        for (int i = 0; i < authors.size(); i++) {
            if (result.length() > 0) {
                result.append(ChatColor.WHITE);

                if (i < authors.size() - 1) {
                    result.append(", ");
                } else {
                    result.append(" and ");
                }
            }

            result.append(ChatColor.GREEN);
            result.append(authors.get(i));
        }

        return result.toString();
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
        Validate.notNull(sender, "Sender cannot be null");
        Validate.notNull(args, "Arguments cannot be null");
        Validate.notNull(alias, "Alias cannot be null");

        if (args.length == 1) {
            List<String> completions = new ArrayList<String>();
            String toComplete = args[0].toLowerCase();
            for (Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
                if (StringUtil.startsWithIgnoreCase(plugin.getName(), toComplete)) {
                    completions.add(plugin.getName());
                }
            }
            return completions;
        }
        return ImmutableList.of();
    }

    private final ReentrantLock versionLock = new ReentrantLock();
    private boolean hasVersion = false;
    private String versionMessage = null;
    private final Set<CommandSender> versionWaiters = new ObjectOpenHashSet<CommandSender>();
    private boolean versionTaskStarted = false;
    private long lastCheck = 0;

    private void sendVersion(CommandSender sender) {
        if (hasVersion) {
            if (System.currentTimeMillis() - lastCheck > 21600000) {
                lastCheck = System.currentTimeMillis();
                hasVersion = false;
            } else {
                sender.sendMessage(versionMessage);
                return;
            }
        }
        versionLock.lock();
        try {
            if (hasVersion) {
                sender.sendMessage(versionMessage);
                return;
            }
            versionWaiters.add(sender);
            sender.sendMessage("Checking version, please wait...");
            if (!versionTaskStarted) {
                versionTaskStarted = true;
                new Thread(new Runnable() {

                    @Override
                    public void run() {
                        obtainVersion();
                    }
                }).start();
            }
        } finally {
            versionLock.unlock();
        }
    }

    private void obtainVersion() {
        String version = Bukkit.getVersion();
        if (version == null)
            version = "Custom";
        // PandaSpigot start
        if (version.startsWith("git-PandaSpigot-")) {
            String[] parts = version.substring("git-PandaSpigot-".length()).split("[-\\s]");
            int distance;
            try {
                distance = fetchActionsDistance("hpfxd/PandaSpigot", "build.yml", Integer.parseInt(parts[0]));
            } catch (NumberFormatException e) {
                distance = fetchGitDistance("hpfxd/PandaSpigot", "master", parts[0]);
            }

            switch (distance) {
                case -1:
                    this.setVersionMessage(ChatColor.YELLOW + "Error obtaining version information");
                    break;
                case 0:
                    this.setVersionMessage(ChatColor.GREEN + "You are running the latest version");
                    break;
                case -2:
                    this.setVersionMessage(ChatColor.YELLOW + "Unknown version");
                    break;
                default:
                    this.setVersionMessage(ChatColor.RED + "You are " + distance + " version(s) behind!\n"
                            + ChatColor.RED + "Download the new version at " + ChatColor.GOLD
                            + "https://github.com/hpfxd/PandaSpigot");
                    break;
            }
            // PaperSpigot start
        } else if (version.startsWith("git-PaperSpigot-")) {
            // PandaSpigot end
            String[] parts = version.substring("git-PaperSpigot-".length()).split("[-\\s]");
            int paperSpigotVersions = getDistance("paperspigot", parts[0]);
            if (paperSpigotVersions == -1) {
                setVersionMessage("Error obtaining version information");
            } else {
                if (paperSpigotVersions == 0) {
                    setVersionMessage("You are running the latest version");
                } else {
                    setVersionMessage("You are " + paperSpigotVersions + " version(s) behind");
                }
            }
        } else if (version.startsWith("git-Spigot-")) {
            // PaperSpigot end
            String[] parts = version.substring("git-Spigot-".length()).split("-");
            int cbVersions = getDistance("craftbukkit", parts[1].substring(0, parts[1].indexOf(' ')));
            int spigotVersions = getDistance("spigot", parts[0]);
            if (cbVersions == -1 || spigotVersions == -1) {
                setVersionMessage("Error obtaining version information");
            } else {
                if (cbVersions == 0 && spigotVersions == 0) {
                    setVersionMessage("You are running the latest version");
                } else {
                    setVersionMessage("You are " + (cbVersions + spigotVersions) + " version(s) behind");
                }
            }

        } else if (version.startsWith("git-Bukkit-")) {
            version = version.substring("git-Bukkit-".length());
            int cbVersions = getDistance("craftbukkit", version.substring(0, version.indexOf(' ')));
            if (cbVersions == -1) {
                setVersionMessage("Error obtaining version information");
            } else {
                if (cbVersions == 0) {
                    setVersionMessage("You are running the latest version");
                } else {
                    setVersionMessage("You are " + cbVersions + " version(s) behind");
                }
            }
        } else {
            setVersionMessage("Unknown version, custom build?");
        }
    }

    private void setVersionMessage(String msg) {
        lastCheck = System.currentTimeMillis();
        versionMessage = msg;
        versionLock.lock();
        try {
            hasVersion = true;
            versionTaskStarted = false;
            for (CommandSender sender : versionWaiters) {
                sender.sendMessage(versionMessage);
            }
            versionWaiters.clear();
        } finally {
            versionLock.unlock();
        }
    }

    // PandaSpigot start
    private static int fetchActionsDistance(String repo, String workflowId, int runNumber) {
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(
                    "https://api.github.com/repos/" + repo + "/actions/workflows/" + workflowId + "/runs")
                    .openConnection();
            connection.setRequestProperty("Accept", "application/vnd.github.v3+json");
            connection.setRequestProperty("User-Agent", "PandaSpigot/" + Bukkit.getVersion());
            connection.connect();
            if (connection.getResponseCode() == HttpURLConnection.HTTP_NOT_FOUND)
                return -2;
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), Charsets.UTF_8))) {
                JsonObject obj = new Gson().fromJson(reader, JsonObject.class);
                JsonArray arr = obj.getAsJsonArray("workflow_runs");
                if (arr.size() == 0)
                    return -1;
                JsonObject firstElement = arr.iterator().next().getAsJsonObject();
                int latestRunNumber = firstElement.get("run_number").getAsInt();
                return latestRunNumber - runNumber;
            } catch (JsonSyntaxException | NumberFormatException e) {
                e.printStackTrace();
                return -1;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return -1;
        }
    }

    private static int fetchGitDistance(String repo, String branch, String hash) {
        hash = hash.replace("\"", "");
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(
                    "https://api.github.com/repos/" + repo + "/compare/" + branch + "..." + hash).openConnection();
            connection.setRequestProperty("Accept", "application/vnd.github.v3+json");
            connection.setRequestProperty("User-Agent", "PandaSpigot/" + Bukkit.getVersion());
            connection.connect();
            if (connection.getResponseCode() == HttpURLConnection.HTTP_NOT_FOUND)
                return -2; // Unknown commit
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), Charsets.UTF_8))) {
                JsonObject obj = new Gson().fromJson(reader, JsonObject.class);
                String status = obj.get("status").getAsString();
                switch (status) {
                    case "identical":
                        return 0;
                    case "behind":
                        return obj.get("behind_by").getAsInt();
                    default:
                        return -1;
                }
            } catch (JsonSyntaxException | NumberFormatException e) {
                e.printStackTrace();
                return -1;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return -1;
        }
    }
    // PandaSpigot end

    private static int getDistance(String repo, String currentVerInt) { // PaperSpigot
        try {
            BufferedReader reader = Resources.asCharSource(
                    new URL("https://ci.destroystokyo.com/job/PaperSpigot/lastSuccessfulBuild/buildNumber"), // PaperSpigot
                    Charsets.UTF_8).openBufferedStream();
            try {
                // PaperSpigot start
                int newVer = Integer.decode(reader.readLine());
                int currentVer = Integer.decode(currentVerInt);
                return newVer - currentVer;
            } catch (NumberFormatException ex) {
                // ex.printStackTrace();
                // PaperSpigot end
                return -1;
            } finally {
                reader.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
            return -1;
        }
    }
}
