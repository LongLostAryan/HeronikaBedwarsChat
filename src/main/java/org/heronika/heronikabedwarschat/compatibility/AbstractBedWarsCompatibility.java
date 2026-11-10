package org.heronika.heronikabedwarschat.compatibility;

import com.andrei1058.bedwars.BedWars;
import com.andrei1058.bedwars.api.arena.IArena;
import com.andrei1058.bedwars.api.arena.team.ITeam;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class AbstractBedWarsCompatibility implements BedWarsCompatibility {

    private final String version;

    protected AbstractBedWarsCompatibility(String version) {
        this.version = version;
    }

    @Override
    public boolean isReady() {
        return BedWars.getAPI() != null;
    }

    @Override
    public String getVersion() {
        return version;
    }

    @Override
    public ArenaContext getArenaContext(Player player) {
        if (player == null || BedWars.getAPI() == null || BedWars.getAPI().getArenaUtil() == null) {
            return null;
        }

        IArena arena = BedWars.getAPI().getArenaUtil().getArenaByPlayer(player);
        if (arena == null) {
            return null;
        }

        boolean spectator = arena.isSpectator(player);
        ITeam team = spectator ? arena.getExTeam(player.getUniqueId()) : arena.getTeam(player);
        boolean gameSpectator = spectator && team != null;
        List<Player> teamMembers = new ArrayList<Player>();
        if (team != null) {
            List<Player> cachedMembers = team.getMembersCache();
            List<Player> sourceMembers = cachedMembers == null || cachedMembers.isEmpty()
                    ? team.getMembers()
                    : cachedMembers;
            if (sourceMembers != null) {
                for (Player member : sourceMembers) {
                    if (member != null && member.isOnline() && !teamMembers.contains(member)) {
                        teamMembers.add(member);
                    }
                }
            }
        }
        if (player.isOnline() && !teamMembers.contains(player)) {
            teamMembers.add(player);
        }

        String teamPlaceholder = "";
        if (team != null) {
            ChatColor teamColor = team.getColor() == null ? null : team.getColor().chat();
            String teamName = team.getDisplayName(BedWars.getAPI().getPlayerLanguage(player));
            if (teamName == null || teamName.isEmpty()) {
                teamName = team.getName();
            }
            teamPlaceholder = (teamColor == null ? "" : teamColor.toString())
                    + "[" + teamName + "]";
        }

        String state = arena.getStatus() == null ? "" : arena.getStatus().name();
        return new ArenaContext(
                spectator && !gameSpectator,
                gameSpectator,
                state,
                arena.getPlayers(),
                arena.getSpectators(),
                teamMembers,
                teamPlaceholder
        );
    }

    @Override
    public String getLevel(Player player) {
        if (player == null || BedWars.getLevelSupport() == null) {
            return "";
        }
        String level = BedWars.getLevelSupport().getLevel(player);
        return level == null ? "" : level;
    }

    @Override
    public String getPrefix(Player player) {
        if (player == null || BedWars.getChatSupport() == null) {
            return "";
        }
        String prefix = BedWars.getChatSupport().getPrefix(player);
        return prefix == null ? "" : prefix;
    }

    @Override
    public String getSuffix(Player player) {
        if (player == null || BedWars.getChatSupport() == null) {
            return "";
        }
        String suffix = BedWars.getChatSupport().getSuffix(player);
        return suffix == null ? "" : suffix;
    }

    @Override
    public boolean isMultiArena() {
        return BedWars.getServerType() != null
                && BedWars.getServerType().name().equalsIgnoreCase("MULTIARENA");
    }

    @Override
    public String getLobbyWorld() {
        String world = BedWars.getLobbyWorld();
        return world == null ? "" : world;
    }
}
