package org.heronika.heronikabedwarschat.compatibility;

import org.bukkit.entity.Player;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class ArenaContext {

    private final boolean spectator;
    private final boolean gameSpectator;
    private final String state;
    private final List<Player> players;
    private final List<Player> spectators;
    private final List<Player> teamMembers;
    private final String teamPlaceholder;

    public ArenaContext(boolean spectator, boolean gameSpectator, String state, List<Player> players,
                        List<Player> spectators, List<Player> teamMembers,
                        String teamPlaceholder) {
        this.spectator = spectator;
        this.gameSpectator = gameSpectator;
        this.state = state == null ? "" : state;
        this.players = immutableCopy(players);
        this.spectators = immutableCopy(spectators);
        this.teamMembers = immutableCopy(teamMembers);
        this.teamPlaceholder = teamPlaceholder == null ? "" : teamPlaceholder;
    }

    private List<Player> immutableCopy(List<Player> source) {
        if (source == null || source.isEmpty()) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(new ArrayList<Player>(source));
    }

    public boolean isSpectator() {
        return spectator;
    }

    public boolean isGameSpectator() {
        return gameSpectator;
    }

    public boolean isWaiting() {
        return state.equalsIgnoreCase("waiting") || state.equalsIgnoreCase("starting");
    }

    public List<Player> getPlayers() {
        return players;
    }

    public List<Player> getSpectators() {
        return spectators;
    }

    public List<Player> getTeamMembers() {
        return teamMembers;
    }

    public String getTeamPlaceholder() {
        return teamPlaceholder;
    }
}
