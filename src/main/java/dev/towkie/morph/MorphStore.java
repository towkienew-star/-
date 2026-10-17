package dev.towkie.morph;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.PlayerListEntry;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

/** Client-only data. Never sends game packets or modifies collision dimensions. */
public final class MorphStore {
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("towkiemorphs.properties");
    private static final Map<String, Scale> SCALES = new HashMap<>();
    private static final Map<String, UUID> NAME_TO_UUID = new HashMap<>();
    private static String server = "singleplayer";

    private MorphStore() {}

    public static void syncOnlinePlayers(MinecraftClient client) {
        if (client.getNetworkHandler() == null) {
            NAME_TO_UUID.clear();
            return;
        }
        server = client.getCurrentServerEntry() != null
            ? client.getCurrentServerEntry().address : "singleplayer";
        NAME_TO_UUID.clear();
        for (PlayerListEntry entry : client.getNetworkHandler().getPlayerList()) {
            NAME_TO_UUID.put(entry.getProfile().getName(), entry.getProfile().getId());
        }
    }

    private static String key(UUID uuid) { return server + "|" + uuid; }

    public static Scale edit(UUID uuid) {
        return SCALES.computeIfAbsent(key(uuid), k -> new Scale());
    }

    /** Called in renderer; name comes from actual entity render state. */
    public static Scale getForName(String name) {
        UUID id = NAME_TO_UUID.get(name);
        return id == null ? null : SCALES.get(key(id));
    }

    public static void reset(UUID uuid) {
        SCALES.remove(key(uuid));
        save();
    }

    public static void load() {
        if (!Files.isRegularFile(FILE)) return;
        Properties props = new Properties();
        try (InputStream in = Files.newInputStream(FILE)) {
            props.load(in);
            for (String compoundKey : props.stringPropertyNames()) {
                int idx = compoundKey.lastIndexOf('#');
                if (idx <= 0) continue;
                String which = compoundKey.substring(idx + 1);
                String playerKey = compoundKey.substring(0, idx);
                Scale scale = SCALES.computeIfAbsent(playerKey, k -> new Scale());
                for (int i = 0; i < Scale.KEYS.length; i++) {
                    if (which.equals(Scale.KEYS[i])) {
                        try { scale.set(i, Float.parseFloat(props.getProperty(compoundKey))); }
                        catch (NumberFormatException ignored) {}
                        break;
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("[TowkieMorphs] Cannot read config: " + e.getMessage());
        }
    }

    public static void save() {
        Properties props = new Properties();
        SCALES.forEach((playerKey, scale) -> {
            for (int i = 0; i < Scale.KEYS.length; i++) {
                props.setProperty(playerKey + "#" + Scale.KEYS[i], Float.toString(scale.get(i)));
            }
        });
        try {
            Files.createDirectories(FILE.getParent());
            try (OutputStream out = Files.newOutputStream(FILE)) {
                props.store(out, "Towkie Morphs - client-only visual sizes");
            }
        } catch (IOException e) {
            System.err.println("[TowkieMorphs] Cannot save config: " + e.getMessage());
        }
    }
}
