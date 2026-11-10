package org.heronika.heronikabedwarschat.core;

import org.bukkit.ChatColor;
import java.util.Map;

public final class ChatFormatManager {

    private final ConfigManager configManager;

    public ChatFormatManager(ConfigManager configManager) {
        this.configManager = configManager;
    }

    public String format(ChatFormats format, Map<String, String> placeholders, String message) {
        String result = configManager.getFormat(format).replace("{prefix}", configManager.getPrefix());
        String safeMessage = message == null ? "" : message;
        result = result.replace("{message}", "&f" + safeMessage);

        if (placeholders != null) {
            for (Map.Entry<String, String> entry : placeholders.entrySet()) {
                String key = "{" + entry.getKey() + "}";
                String value = entry.getValue() == null ? "" : entry.getValue();
                result = result.replace(key, value);
            }
        }

        String name = placeholders == null ? null : placeholders.get("name");
        if (name == null && placeholders != null) {
            name = placeholders.get("player");
        }
        if (name != null) {
            result = result.replace("{name}", name).replace("{player}", name);
        }
        return ChatColor.translateAlternateColorCodes('&', result);
    }
}
