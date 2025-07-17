package gg.mineral.server.command;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import gg.mineral.server.combat.KnockbackProfile;
import gg.mineral.server.combat.KnockbackProfileList;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import lombok.val;

public class KnockbackCommand extends Command {

    public KnockbackCommand(String name) {
        super(name);
        this.description = "Manage knockback profiles";
        this.usageMessage = "/kb reload";
        this.setPermission("mineral.command.knockback");
    }

    @Override
    public boolean execute(CommandSender sender, String currentAlias, String[] args) {
        if (!testPermission(sender)) {
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + "Usage: /kb reload");
            return true;
        }

        String subCommand = args[0].toLowerCase();
        
        if (subCommand.equals("reload")) {
            reloadKnockbackProfiles(sender);
            return true;
        }

        sender.sendMessage(ChatColor.RED + "Unknown subcommand. Usage: /kb reload");
        return true;
    }

    private void reloadKnockbackProfiles(CommandSender sender) {
        try {
            sender.sendMessage(ChatColor.YELLOW + "Reloading knockback profiles...");
            
            // Clear existing profiles
            Map<String, KnockbackProfile> profiles = KnockbackProfileList.getProfiles();
            profiles.clear();
            
            // Check if knockback folder exists
            val knockbackFolder = new File("knockback");
            if (!knockbackFolder.exists()) {
                knockbackFolder.mkdir();
            }
            
            // Load all Groovy files in the "knockback" folder
            KnockbackProfileList.iterateAllGroovyFiles("knockback", file -> {
                val profile = new KnockbackProfile(file.getPath(), KnockbackProfileList.getFileNameWithoutExtension(file));
                profile.loadConfig();
                profiles.put(file.getPath(), profile);
            });
            
            // Ensure default profile exists
            if (profiles.isEmpty() || profiles.values().stream()
                    .noneMatch(profile -> profile.getName().equals("default_kb"))) {
                
                try (val inputStream = KnockbackProfile.class
                        .getResourceAsStream("/knockback/default_kb.groovy")) {
                    Files.copy(inputStream, Paths.get("knockback/default_kb.groovy"));
                    val profile = new KnockbackProfile("knockback/default_kb.groovy", "default_kb");
                    profile.loadConfig();
                    profiles.put("knockback/default_kb.groovy", profile);
                } catch (Exception e) {
                    sender.sendMessage(ChatColor.RED + "Failed to create default knockback profile: " + e.getMessage());
                }
            }
            
            sender.sendMessage(ChatColor.GREEN + "Successfully reloaded " + profiles.size() + " knockback profiles.");
            
        } catch (Exception e) {
            sender.sendMessage(ChatColor.RED + "Failed to reload knockback profiles: " + e.getMessage());
            e.printStackTrace();
        }
    }
}