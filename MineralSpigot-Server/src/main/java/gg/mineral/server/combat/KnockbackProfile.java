package gg.mineral.server.combat;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import gg.mineral.api.knockback.Knockback;
import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import groovy.lang.Script;
import lombok.Getter;
import net.minecraft.server.EntityLiving;

@Getter
public class KnockbackProfile extends Knockback {
    private Script script;
    private Map<String, Object> configValues;
    private final String scriptFilePath, name;
    private Binding binding;

    public static KnockbackProfile createNew(String name) throws KBProfileAlreadyExistsException, IOException {
        File knockbackFolder = new File("knockback");
        if (!knockbackFolder.exists())
            knockbackFolder.mkdir();

        String path = "knockback/" + name + ".groovy";

        // check if the file already exists
        if (KnockbackProfileList.getProfiles().values().stream().anyMatch(profile -> profile.getName().equals(name)))
            throw new KBProfileAlreadyExistsException("A knockback profile with the name " + name + " already exists.");

        java.io.InputStream inputStream = KnockbackProfile.class
                .getResourceAsStream("/knockback/default_kb.groovy");

        Files.copy(inputStream, Paths.get(path));

        KnockbackProfile newProfile = new KnockbackProfile(path, name);
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
        Binding binding = new Binding();
        GroovyShell shell = new GroovyShell(binding);

        try {
            // Load and parse the Groovy script
            script = shell.parse(new File(scriptFilePath));
            script.run();

            // Retrieve all variables from the script
            configValues = new HashMap<>(binding.getVariables());

            System.out.println("Successfully loaded the Groovy script.");
            System.out.println("Config values: " + configValues);
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Failed to load the Groovy script.");
        }
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> callFirstStage(EntityLiving attacker, EntityLiving victim) {
        return (Map<String, Object>) script.invokeMethod("firstStage",
                new Object[] { attacker, victim });
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> callSecondStage(EntityLiving attacker, EntityLiving victim,
            int knockbackEnchantLevel) {
        return (Map<String, Object>) script.invokeMethod("secondStage",
                new Object[] { attacker, victim,
                        knockbackEnchantLevel });
    }

    public void setConfigValues(Map<String, Object> newConfigValues) {
        // Update the binding with new values
        for (Map.Entry<String, Object> entry : newConfigValues.entrySet())
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
