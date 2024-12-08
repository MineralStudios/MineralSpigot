package gg.mineral.server.combat;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

import gg.mineral.api.knockback.Knockback;
import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import groovy.lang.Script;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import lombok.Getter;
import lombok.val;
import net.minecraft.server.EntityLiving;

@Getter
public class KnockbackProfile extends Knockback {
    private Script script;
    private final Map<String, Object> configValues = new Object2ObjectOpenHashMap<>();
    private final String scriptFilePath, name;
    private Binding binding;
    private KnockbackProtocol protocol;
    private int attackBuffer = 0;

    public static KnockbackProfile createNew(String name) throws KBProfileAlreadyExistsException, IOException {
        val knockbackFolder = new File("knockback");
        if (!knockbackFolder.exists())
            knockbackFolder.mkdir();

        val path = "knockback/" + name + ".groovy";

        // check if the file already exists
        if (KnockbackProfileList.getProfiles().values().stream().anyMatch(profile -> profile.getName().equals(name)))
            throw new KBProfileAlreadyExistsException("A knockback profile with the name " + name + " already exists.");

        val inputStream = KnockbackProfile.class
                .getResourceAsStream("/knockback/default_kb.groovy");

        Files.copy(inputStream, Paths.get(path));

        val newProfile = new KnockbackProfile(path, name);
        KnockbackProfileList.getProfiles().put(path, newProfile);
        return newProfile;
    }

    public KnockbackProfile(String scriptFilePath, String name) {
        super(name);
        this.scriptFilePath = scriptFilePath;
        this.name = name;
    }

    @SuppressWarnings("unchecked")
    public void loadConfig() {
        val binding = new Binding();
        val shell = new GroovyShell(binding);

        try {
            // Load and parse the Groovy script
            script = shell.parse(new File(scriptFilePath));
            script.run();

            // Retrieve all variables from the script
            configValues.putAll(binding.getVariables());

            this.attackBuffer = (int) configValues.getOrDefault("attackBuffer", 0);

            protocol = (KnockbackProtocol) configValues.get("protocol");

            System.out.println("Successfully loaded the Groovy script.");
            System.out.println("Config values: " + configValues);
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Failed to load the Groovy script.");
        }
    }

    public void callFirstStage(EntityLiving attacker, EntityLiving victim) {
        protocol.firstStage(attacker, victim);
    }

    public void callSecondStage(EntityLiving attacker, EntityLiving victim,
            int knockbackEnchantLevel) {
        protocol.secondStage(attacker, victim, knockbackEnchantLevel);
    }

    public void setConfigValues(Map<String, Object> newConfigValues) {
        // Update the binding with new values
        for (val entry : newConfigValues.entrySet())
            binding.setVariable(entry.getKey(), entry.getValue());

        configValues.putAll(newConfigValues);
        saveConfig();
    }

    public void reloadConfig() {
        loadConfig();
    }

    private void saveConfig() {
        setConfigValues(configValues);
    }
}
