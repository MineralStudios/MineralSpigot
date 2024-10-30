package gg.mineral.server.combat;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.function.Consumer;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.val;

public class KnockbackProfileList {
    @Getter
    private static Map<String, KnockbackProfile> profiles = new Object2ObjectOpenHashMap<>();

    static {
        // Check if knockback folder exists
        val knockbackFolder = new File("knockback");
        if (!knockbackFolder.exists())
            knockbackFolder.mkdir();
        // Load all Groovy files in the "knockback" folder
        iterateAllGroovyFiles("knockback", file -> {
            val profile = new KnockbackProfile(file.getPath(), getFileNameWithoutExtension(file));
            profile.loadConfig();
            profiles.put(file.getPath(),
                    profile);
        });

        if (profiles.isEmpty() || profiles.values().stream()
                .noneMatch(profile -> profile.getName().equals("default_kb"))) {
            // export default from resources (knockback/default_kb.groovy)
            // and load it

            try (val inputStream = KnockbackProfile.class
                    .getResourceAsStream("/knockback/default_kb.groovy")) {
                Files.copy(inputStream, Paths.get("knockback/default_kb.groovy"));
                val profile = new KnockbackProfile("knockback/default_kb.groovy", "default_kb");
                profile.loadConfig();
                profiles.put("knockback/default_kb.groovy", profile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        System.out.println("[Mineral] Loaded " + profiles.size() + " knockback profiles.");
    }

    public static KnockbackProfile getDefaultKnockbackProfile() {

        for (val profile : profiles.values())
            if (profile.getName().equals("default_kb"))
                return profile;

        try {
            return KnockbackProfile.createNew("default_kb");
        } catch (KBProfileAlreadyExistsException | IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String getFileNameWithoutExtension(File file) {
        val fileName = file.getName();
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex != -1)
            return fileName.substring(0, dotIndex);

        return fileName;
    }

    public static KnockbackProfile getComboKnockbackProfile() {
        for (val profile : profiles.values())
            if (profile.getName().equals("combo_kb"))
                return profile;

        return getDefaultKnockbackProfile();
    }

    public static KnockbackProfile getKnockbackProfileByName(String name) {
        for (val profile : profiles.values())
            if (profile.getName().equals(name))
                return profile;

        return null;
    }

    @SneakyThrows
    public static void iterateAllGroovyFiles(String folderPath, Consumer<File> consumer) {
        Files.list(Paths.get(folderPath))
                .filter(Files::isRegularFile)
                .filter(path -> path.toString().endsWith(".groovy"))
                .map(path -> path.toFile()).forEach(consumer);
    }
}
