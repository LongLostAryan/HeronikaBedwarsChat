package org.heronika.heronikabedwarschat.compatibility;

import org.bukkit.entity.Player;

public interface BedWarsCompatibility {
    String getVersion();
    boolean isReady();
    ArenaContext getArenaContext(Player player);
    String getLevel(Player player);
    String getPrefix(Player player);
    String getSuffix(Player player);
    boolean isMultiArena();
    String getLobbyWorld();
}
