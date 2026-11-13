package org.heronika.heronikabedwarschat.core;

import org.bukkit.configuration.file.FileConfiguration;

public final class ConfigManager {

    private final HeronikaBedwarsChat plugin;

    public ConfigManager(HeronikaBedwarsChat plugin) {
        this.plugin = plugin;
        load();
    }

    public void load() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
    }

    public void reload() {
        plugin.reloadConfig();
        plugin.reloadAutoMessages();
    }

    public FileConfiguration getConfig() {
        return plugin.getConfig();
    }

    public String getFormat(ChatFormats format) {
        String path = format.getConfigPath();
        String value = getConfig().getString(path);
        if (value == null || value.trim().isEmpty()) {
            plugin.getLogger().warning("Missing or empty chat format '" + path + "'. Using the default format.");
            return getDefaultFormat(format);
        }
        return value;
    }

    public String getPrefix() {
        return getConfig().getString("prefix", "&5&lHERONIKA &7» ");
    }

    public String formatMessage(String message) {
        if (message == null) {
            return "";
        }
        return org.bukkit.ChatColor.translateAlternateColorCodes('&', message.replace("{prefix}", getPrefix()));
    }

    public long getShoutCooldownMillis() {
        String configured = getConfig().getString("shout-cooldown", "10s");
        if (configured == null || configured.trim().isEmpty()) {
            return 10000L;
        }
        String value = configured.trim().toLowerCase();
        try {
            long multiplier = 1000L;
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
            }
            return Math.max(0L, Math.multiplyExact(Long.parseLong(value.trim()), multiplier));
        } catch (NumberFormatException | ArithmeticException exception) {
            plugin.getLogger().warning("Invalid shout-cooldown value '" + configured + "'. Using default 10s.");
            return 10000L;
        }
    }

    public String getShoutCooldownMessage() {
        return getConfig().getString("shout-cooldown-message", "{prefix}&cYou must wait {time}s before shouting again.");
    }

    public String getClearChatMessage() {
        return getConfig().getString("clearchat-message", "{prefix}&aChat has been cleared by {player}.");
    }

    public String getReloadMessage() {
        return getConfig().getString("reload-message", "{prefix}&aConfiguration reloaded.");
    }

    public String getChatEnabledMessage() {
        return getConfig().getString("chat-enabled-message", "{prefix}&aChat has been enabled by {player}.");
    }

    public String getChatDisabledMessage() {
        return getConfig().getString("chat-disabled-message", "{prefix}&cChat has been disabled by {player}.");
    }

    public String getChatDisabledNotice() {
        return getConfig().getString("chat-disabled-notice", "{prefix}&cChat is currently disabled.");
    }

    public String getNoPermissionMessage() {
        return getConfig().getString("no-permission-message", "{prefix}&cYou don't have permission to use this command.");
    }

    public String getCommandUsageMessage() {
        return getConfig().getString("command-usage-message", "{prefix}&cUsage: /hbc");
    }

    public String getCommandHelpMessage() {
        return getConfig().getString("command-help-message", "{prefix}&eAvailable commands:\n{prefix}&f/hbc help &7- Show this help message\n{prefix}&f/hbc clearchat &7- Clear chat for everyone\n{prefix}&f/hbc reload &7- Reload the configuration\n{prefix}&f/hbc stopchat &7- Toggle chat on or off");
    }

    private String getDefaultFormat(ChatFormats format) {
        switch (format) {
            case LOBBY:
            case WAITING:
                return "{level} {vPrefix}{name}&8: {message}";
            case GLOBAL:
                return "{level} {team} &6[SHOUT] {vPrefix}{name}&7: {message}";
            case TEAM:
                return "{level} {team} {vPrefix}{name}&8: {message}";
            case SPECTATOR:
                return "{level} &7[SPECTATOR] {vPrefix}{name}&8: {message}";
            case GAME_SPECTATOR:
                return "{level} {team} &7[SPECTATOR] {vPrefix}{name}&8: {message}";
            case GAME_GLOBAL_SPECTATOR:
                return "{level} {team} &6[SHOUT] &7[SPECTATOR] {vPrefix}{name}&8: {message}";
            case AUTO_GG:
                return "{level} {team} &6[SHOUT] {vPrefix}{name}&7: &6GG.";
            default:
                return "{name}&8: {message}";
        }
    }
}
