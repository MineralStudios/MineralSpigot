package gg.mineral.server.combat;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.stream.Stream;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import lombok.Getter;

public class KnockbackProfileList {
    @Getter
    private static Map<String, KnockbackProfile> profiles = new Object2ObjectOpenHashMap<>();

    static {
        // Check if knockback folder exists
        File knockbackFolder = new File("knockback");
        if (!knockbackFolder.exists())
            knockbackFolder.mkdir();
        // Load all Groovy files in the "knockback" folder
        getAllGroovyFiles("knockback")
                .forEach(file -> {
                    KnockbackProfile profile = new KnockbackProfile(file.getPath(), getFileNameWithoutExtension(file));
                    profile.loadConfig();
                    profiles.put(file.getPath(),
                            profile);
                });

        if (profiles.isEmpty() || profiles.values().stream()
                .noneMatch(profile -> profile.getName().equals("default_kb"))) {
            // export default from resources (knockback/default_kb.groovy)
            // and load it

            java.io.InputStream inputStream = KnockbackProfile.class
                    .getResourceAsStream("/knockback/default_kb.groovy");
            try {
                Files.copy(inputStream, Paths.get("knockback/default_kb.groovy"));
                KnockbackProfile profile = new KnockbackProfile("knockback/default_kb.groovy", "default_kb");
                profile.loadConfig();
                profiles.put("knockback/default_kb.groovy", profile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        System.out.println("[Mineral] Loaded " + profiles.size() + " knockback profiles.");
    }

    public static KnockbackProfile getDefaultKnockbackProfile() {

        for (KnockbackProfile profile : profiles.values()) {
            if (profile.getName().equals("default_kb")) {
                return profile;
            }
        }

        try {
            return KnockbackProfile.createNew("default_kb");
        } catch (KBProfileAlreadyExistsException | IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String getFileNameWithoutExtension(File file) {
        String fileName = file.getName();
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex != -1) {
            return fileName.substring(0, dotIndex);
        }
        return fileName;
    }

    public static KnockbackProfile getComboKnockbackProfile() {
        for (KnockbackProfile profile : profiles.values()) {
            if (profile.getName().equals("combo_kb")) {
                return profile;
            }
        }
        return getDefaultKnockbackProfile();
    }

    public static KnockbackProfile getKnockbackProfileByName(String name) {
        for (KnockbackProfile profile : profiles.values()) {
            if (profile.getName().equals(name)) {
                return profile;
            }
        }
        return null;
    }

    public static Stream<File> getAllGroovyFiles(String folderPath) {
        try {
            return Files.list(Paths.get(folderPath))
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".groovy"))
                    .map(path -> path.toFile());
        } catch (IOException e) {
            e.printStackTrace();
            return Stream.empty();
        }
    }
}
