package org.heronika.heronikabedwarschat.core;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.heronika.heronikabedwarschat.compatibility.ArenaContext;
import org.heronika.heronikabedwarschat.compatibility.BedWarsCompatibility;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public final class AutoMessageManager {

    private final HeronikaBedwarsChat plugin;
    private final List<BukkitTask> tasks = new ArrayList<BukkitTask>();

    public AutoMessageManager(HeronikaBedwarsChat plugin) {
        this.plugin = plugin;
    }

    public void reload() {
        stop();
        if (plugin.getCompatibility() == null) {
            return;
        }

        for (String key : plugin.getConfig().getConfigurationSection("auto_messages") == null
                ? new ArrayList<String>()
                : plugin.getConfig().getConfigurationSection("auto_messages").getKeys(false)) {
            String path = "auto_messages." + key;
            if (!plugin.getConfig().getBoolean(path + ".enabled", false)) {
                continue;
            }

            String message = plugin.getConfig().getString(path + ".message", "");
            if (message == null || message.trim().isEmpty()) {
                plugin.getLogger().warning("Auto message '" + key + "' has no message; skipping it.");
                continue;
            }

            long interval = parseInterval(plugin.getConfig().getString(path + ".send-interval", "1m"));
            if (interval <= 0L) {
                plugin.getLogger().warning("Invalid send-interval for auto message '" + key + "'; skipping it.");
                continue;
            }

            final String configuredMessage = message;
            final String hoverMessage = plugin.getConfig().getString(path + ".hover-message", "");
            final String clickMessage = plugin.getConfig().getString(path + ".click-message", "");
            long ticks = Math.max(1L, interval / 50L);

            BukkitTask task = Bukkit.getScheduler().runTaskTimer(plugin, new Runnable() {
                @Override
                public void run() {
                    sendToActiveArenas(configuredMessage, hoverMessage, clickMessage);
                }
            }, ticks, ticks);
            tasks.add(task);
        }
    }

    public void stop() {
        for (BukkitTask task : tasks) {
            task.cancel();
        }
        tasks.clear();
    }

    private void sendToActiveArenas(String message, String hoverMessage, String clickMessage) {
        BedWarsCompatibility compatibility = plugin.getCompatibility();
        if (compatibility == null || !compatibility.isReady()) {
            return;
        }

        Set<Player> recipients = new LinkedHashSet<Player>();
        for (Player player : Bukkit.getOnlinePlayers()) {
            ArenaContext arena = compatibility.getArenaContext(player);
            // No arena means lobby/outside a game. Waiting and starting arenas are excluded.
            if (arena == null || arena.isWaiting() || arena.getState().trim().isEmpty()) {
                continue;
            }
            recipients.add(player);
        }

        if (recipients.isEmpty()) {
            return;
        }

        String formatted = plugin.getConfigManager().formatMessage(message);
        TextComponent component = new TextComponent(TextComponent.fromLegacyText(formatted));

        if (hoverMessage != null && !hoverMessage.trim().isEmpty()) {
            String hover = plugin.getConfigManager().formatMessage(hoverMessage);
            component.setHoverEvent(new HoverEvent(
                    HoverEvent.Action.SHOW_TEXT,
                    TextComponent.fromLegacyText(hover)
            ));
        }

        if (clickMessage != null && !clickMessage.trim().isEmpty()) {
            String click = plugin.getConfigManager().formatMessage(clickMessage);
            String clickTarget = ChatColor.stripColor(click).trim();
            ClickEvent.Action action;
            if (clickTarget.startsWith("https://") || clickTarget.startsWith("http://")) {
                action = ClickEvent.Action.OPEN_URL;
            } else if (clickTarget.startsWith("/")) {
                action = ClickEvent.Action.RUN_COMMAND;
            } else {
                action = ClickEvent.Action.SUGGEST_COMMAND;
            }
            component.setClickEvent(new ClickEvent(action, clickTarget));
        }

        BaseComponent[] components = new BaseComponent[] { component };
        for (Player recipient : recipients) {
            if (recipient.isOnline()) {
                recipient.spigot().sendMessage(components);
            }
        }
    }

    private long parseInterval(String configured) {
        if (configured == null || configured.trim().isEmpty()) {
            return -1L;
        }
        String value = configured.trim().toLowerCase();
        long multiplier;
        if (value.endsWith("ms")) {
            multiplier = 1L;
            value = value.substring(0, value.length() - 2);
        } else if (value.endsWith("s")) {
            multiplier = 1000L;
            value = value.substring(0, value.length() - 1);
        } else if (value.endsWith("m")) {
            multiplier = 60000L;
            value = value.substring(0, value.length() - 1);
        } else if (value.endsWith("h")) {
            multiplier = 3600000L;
            value = value.substring(0, value.length() - 1);
        } else {
            return -1L;
        }
        try {
            long amount = Long.parseLong(value.trim());
            return amount <= 0L ? -1L : Math.multiplyExact(amount, multiplier);
        } catch (NumberFormatException | ArithmeticException exception) {
            return -1L;
        }
    }
}
