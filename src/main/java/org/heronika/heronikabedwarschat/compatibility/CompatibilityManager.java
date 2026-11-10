package org.heronika.heronikabedwarschat.compatibility;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.heronika.heronikabedwarschat.core.HeronikaBedwarsChat;

public final class CompatibilityManager {

    private final HeronikaBedwarsChat plugin;

    public CompatibilityManager(HeronikaBedwarsChat plugin) {
        this.plugin = plugin;
    }

    public BedWarsCompatibility load() {
        Plugin bedWarsPlugin = Bukkit.getPluginManager().getPlugin("BedWars1058");
        if (bedWarsPlugin == null || !bedWarsPlugin.isEnabled()) {
            plugin.getLogger().severe("BedWars1058 was not found or is not enabled.");
            return null;
        }

        String version = bedWarsPlugin.getDescription().getVersion();
        if (version == null) {
            plugin.getLogger().severe("Could not read the BedWars1058 version.");
            return null;
        }

        String normalized = version.trim();
        BedWarsCompatibility compatibility;
        if (normalized.startsWith("22.3.4")) {
            compatibility = new BedWars2234Compatibility();
        } else if (normalized.startsWith("23.2.1")) {
            compatibility = new BedWars2321Compatibility();
        } else if (normalized.startsWith("23.3")) {
            compatibility = new BedWars233Compatibility();
        } else if (normalized.startsWith("23.12.2")) {
            compatibility = new BedWars23122Compatibility();
        } else if (normalized.startsWith("24.9")) {
            compatibility = new BedWars249Compatibility();
        } else if (normalized.startsWith("25.2")) {
            compatibility = new BedWars252Compatibility();
        } else {
            plugin.getLogger().severe("BedWars1058 version " + normalized + " is not supported. Supported versions: 22.3.4, 23.2.1, 23.3, 23.12.2, 24.9, 25.2.");
            return null;
        }

        if (!compatibility.isReady()) {
            plugin.getLogger().severe("BedWars1058 " + normalized + " was detected, but its API is not ready.");
            return null;
        }

        return compatibility;
    }
}
