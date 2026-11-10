package org.heronika.heronikabedwarschat.core;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import com.andrei1058.bedwars.BedWars;
import com.andrei1058.bedwars.api.arena.team.ITeam;
import com.andrei1058.bedwars.api.events.gameplay.GameEndEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.heronika.heronikabedwarschat.compatibility.ArenaContext;
import org.heronika.heronikabedwarschat.compatibility.BedWarsCompatibility;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;

public final class ChatListener implements Listener {

    private final HeronikaBedwarsChat plugin;
    private final Map<AsyncPlayerChatEvent, String> originalMessages =
            Collections.synchronizedMap(new WeakHashMap<AsyncPlayerChatEvent, String>());
    private final Map<UUID, Long> shoutCooldowns = new HashMap<UUID, Long>();

    public ChatListener(HeronikaBedwarsChat plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void captureOriginalMessage(AsyncPlayerChatEvent event) {
        originalMessages.put(event, event.getMessage());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        final Player player = event.getPlayer();

        if (!plugin.isChatEnabled() && !player.hasPermission("heronikachat.bw.admin")) {
            event.setCancelled(true);
            final String notice = plugin.getConfigManager().formatMessage(
                    plugin.getConfigManager().getChatDisabledNotice()
            );
            if (event.isAsynchronous()) {
                Bukkit.getScheduler().runTask(plugin, new Runnable() {
                    @Override
                    public void run() {
                        if (player.isOnline()) {
                            player.sendMessage(notice);
                        }
                    }
                });
            } else {
                player.sendMessage(notice);
            }
            return;
        }

        BedWarsCompatibility compatibility = plugin.getCompatibility();
        if (compatibility == null) {
            return;
        }

        String originalMessage = originalMessages.remove(event);
        if (originalMessage == null) {
            originalMessage = event.getMessage();
        }

        ArenaContext arena = compatibility.getArenaContext(player);
        ChatFormats format;
        String message = event.getMessage();

        if (arena == null) {
            format = ChatFormats.LOBBY;
            if (compatibility.isMultiArena()
                    && player.getWorld().getName().equalsIgnoreCase(compatibility.getLobbyWorld())) {
                event.getRecipients().clear();
                event.getRecipients().addAll(player.getWorld().getPlayers());
            }
        } else if (arena.isSpectator()) {
            format = ChatFormats.SPECTATOR;
            event.getRecipients().clear();
            event.getRecipients().addAll(arena.getSpectators());
        } else if (arena.isGameSpectator() && originalMessage.startsWith("!")) {
            message = originalMessage.substring(1);
            if (message.trim().isEmpty()) {
                event.setCancelled(true);
                return;
            }

            boolean bypassCooldown = player.hasPermission("heronikachat.bw.shoutbypass")
                    || player.hasPermission("heronikachat.bw.admin");
            long cooldown = bypassCooldown ? 0L : plugin.getConfigManager().getShoutCooldownMillis();
            if (cooldown > 0L) {
                long remaining = tryUseShoutCooldown(player.getUniqueId(), cooldown);
                if (remaining > 0L) {
                    event.setCancelled(true);
                    long seconds = (remaining + 999L) / 1000L;
                    String cooldownMessage = plugin.getConfigManager().getShoutCooldownMessage()
                            .replace("{time}", String.valueOf(seconds));
                    final String finalCooldownMessage = plugin.getConfigManager().formatMessage(cooldownMessage);
                    Bukkit.getScheduler().runTask(plugin, new Runnable() {
                        @Override
                        public void run() {
                            if (player.isOnline()) {
                                player.sendMessage(finalCooldownMessage);
                            }
                        }
                    });
                    return;
                }
            }

            format = ChatFormats.GAME_GLOBAL_SPECTATOR;
            event.getRecipients().clear();
            event.getRecipients().addAll(arena.getPlayers());
            event.getRecipients().addAll(arena.getSpectators());
        } else if (arena.isGameSpectator()) {
            format = ChatFormats.GAME_SPECTATOR;
            event.getRecipients().clear();
            event.getRecipients().addAll(arena.getTeamMembers());
        } else if (arena.isWaiting()) {
            format = ChatFormats.WAITING;
            event.getRecipients().clear();
            event.getRecipients().addAll(arena.getPlayers());
        } else if (originalMessage.startsWith("!")) {
            message = originalMessage.substring(1);
            if (message.trim().isEmpty()) {
                event.setCancelled(true);
                return;
            }

            boolean bypassCooldown = player.hasPermission("heronikachat.bw.shoutbypass")
                    || player.hasPermission("heronikachat.bw.admin");
            long cooldown = bypassCooldown ? 0L : plugin.getConfigManager().getShoutCooldownMillis();
            if (cooldown > 0L) {
                long remaining = tryUseShoutCooldown(player.getUniqueId(), cooldown);
                if (remaining > 0L) {
                    event.setCancelled(true);
                    long seconds = (remaining + 999L) / 1000L;
                    String cooldownMessage = plugin.getConfigManager().getShoutCooldownMessage()
                            .replace("{time}", String.valueOf(seconds));
                    final String finalCooldownMessage = plugin.getConfigManager().formatMessage(cooldownMessage);
                    Bukkit.getScheduler().runTask(plugin, new Runnable() {
                        @Override
                        public void run() {
                            if (player.isOnline()) {
                                player.sendMessage(finalCooldownMessage);
                            }
                        }
                    });
                    return;
                }
            }

            format = ChatFormats.GLOBAL;
            event.getRecipients().clear();
            event.getRecipients().addAll(arena.getPlayers());
            event.getRecipients().addAll(arena.getSpectators());
        } else {
            format = ChatFormats.TEAM;
            event.getRecipients().clear();
            event.getRecipients().addAll(arena.getTeamMembers());
        }

        event.setMessage(message);
        Map<String, String> placeholders = createPlaceholders(player, compatibility, arena);
        String formatted = plugin.getChatFormatManager().format(format, placeholders, message);
        event.setFormat(formatted.replace("%", "%%"));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onGameEnd(GameEndEvent event) {
        if (event == null || event.getArena() == null) {
            return;
        }

        final com.andrei1058.bedwars.api.arena.IArena arena = event.getArena();
        Set<UUID> speakers = new LinkedHashSet<UUID>();

        if (event.getAliveWinners() != null) {
            speakers.addAll(event.getAliveWinners());
        }

        if (event.getLosers() != null && !event.getLosers().isEmpty()) {
            speakers.add(event.getLosers().get(event.getLosers().size() - 1));
        }

        Set<UUID> arenaMemberIds = new LinkedHashSet<UUID>();
        for (Player member : arena.getPlayers()) {
            if (member != null && member.isOnline()) {
                arenaMemberIds.add(member.getUniqueId());
            }
        }
        for (Player spectator : arena.getSpectators()) {
            if (spectator != null && spectator.isOnline()) {
                arenaMemberIds.add(spectator.getUniqueId());
            }
        }

        Set<UUID> leavingIds = new LinkedHashSet<UUID>();
        if (arena.getLeavingPlayers() != null) {
            for (Player leaving : arena.getLeavingPlayers()) {
                if (leaving != null) {
                    leavingIds.add(leaving.getUniqueId());
                }
            }
        }

        Set<Player> recipients = new LinkedHashSet<Player>();
        for (Player member : arena.getPlayers()) {
            if (member != null && member.isOnline()) {
                recipients.add(member);
            }
        }
        for (Player spectator : arena.getSpectators()) {
            if (spectator != null && spectator.isOnline()) {
                recipients.add(spectator);
            }
        }

        for (UUID speakerId : speakers) {
            if (!arenaMemberIds.contains(speakerId) || leavingIds.contains(speakerId)) {
                continue;
            }

            Player speaker = Bukkit.getPlayer(speakerId);
            if (speaker == null || !speaker.isOnline()) {
                continue;
            }

            Map<String, String> placeholders = createAutoGgPlaceholders(speaker, event);
            String formatted = plugin.getChatFormatManager().format(
                    ChatFormats.AUTO_GG,
                    placeholders,
                    "GG."
            );
            for (Player recipient : recipients) {
                recipient.sendMessage(formatted);
            }
        }
    }

    private Map<String, String> createAutoGgPlaceholders(Player player, GameEndEvent event) {
        Map<String, String> placeholders = new HashMap<String, String>();
        placeholders.put("level", plugin.getCompatibility().getLevel(player));
        placeholders.put("name", player.getName());
        placeholders.put("player", player.getName());
        placeholders.put("vPrefix", plugin.getCompatibility().getPrefix(player));
        placeholders.put("vSuffix", plugin.getCompatibility().getSuffix(player));

        ITeam team = event.getArena().getExTeam(player.getUniqueId());
        if (team == null) {
            team = event.getArena().getTeam(player);
        }

        String teamPlaceholder = "";
        if (team != null) {
            ChatColor teamColor = team.getColor() == null ? null : team.getColor().chat();
            String teamName = BedWars.getAPI().getPlayerLanguage(player) == null
                    ? team.getName()
                    : team.getDisplayName(BedWars.getAPI().getPlayerLanguage(player));
            if (teamName == null || teamName.isEmpty()) {
                teamName = team.getName();
            }
            teamPlaceholder = (teamColor == null ? "" : teamColor.toString())
                    + "[" + teamName + "]";
        }
        placeholders.put("team", teamPlaceholder);
        return placeholders;
    }

    private synchronized long tryUseShoutCooldown(UUID playerId, long cooldown) {
        long now = System.currentTimeMillis();
        Long lastShout = shoutCooldowns.get(playerId);
        if (lastShout != null) {
            long remaining = cooldown - (now - lastShout);
            if (remaining > 0L) {
                return remaining;
            }
        }
        shoutCooldowns.put(playerId, now);
        return 0L;
    }

    private Map<String, String> createPlaceholders(Player player,
                                                   BedWarsCompatibility compatibility,
                                                   ArenaContext arena) {
        Map<String, String> placeholders = new HashMap<String, String>();
        placeholders.put("level", compatibility.getLevel(player));
        placeholders.put("name", player.getName());
        placeholders.put("player", player.getName());
        placeholders.put("vPrefix", compatibility.getPrefix(player));
        placeholders.put("vSuffix", compatibility.getSuffix(player));
        placeholders.put("team", arena == null ? "" : arena.getTeamPlaceholder());
        return placeholders;
    }
}
