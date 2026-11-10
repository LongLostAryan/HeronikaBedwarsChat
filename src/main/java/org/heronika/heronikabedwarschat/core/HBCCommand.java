package org.heronika.heronikabedwarschat.core;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class HBCCommand implements CommandExecutor, TabCompleter {

    private final HeronikaBedwarsChat plugin;

    public HBCCommand(HeronikaBedwarsChat plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        if (args.length != 1) {
            sendUsage(sender);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        if (subCommand.equals("help")) {
            sendHelp(sender);
            return true;
        }

        if (subCommand.equals("clearchat")) {
            if (!hasPermission(sender, "heronikachat.bw.clearchat")) {
                sendMessage(sender, plugin.getConfigManager().getNoPermissionMessage());
                return true;
            }
            for (CommandSender online : Bukkit.getOnlinePlayers()) {
                for (int i = 0; i < 100; i++) {
                    online.sendMessage(" ");
                }
            }
            String message = plugin.getConfigManager().getClearChatMessage().replace("{player}", sender.getName());
            Bukkit.broadcastMessage(plugin.getConfigManager().formatMessage(message));
            return true;
        }

        if (subCommand.equals("reload")) {
            if (!hasPermission(sender, "heronikachat.bw.reload")) {
                sendMessage(sender, plugin.getConfigManager().getNoPermissionMessage());
                return true;
            }
            plugin.getConfigManager().reload();
            sendMessage(sender, plugin.getConfigManager().getReloadMessage());
            return true;
        }

        if (subCommand.equals("stopchat")) {
            if (!hasPermission(sender, "heronikachat.bw.stopchat")) {
                sendMessage(sender, plugin.getConfigManager().getNoPermissionMessage());
                return true;
            }
            boolean enabled = plugin.toggleChat();
            String configured = enabled
                    ? plugin.getConfigManager().getChatEnabledMessage()
                    : plugin.getConfigManager().getChatDisabledMessage();
            Bukkit.broadcastMessage(plugin.getConfigManager().formatMessage(
                    configured.replace("{player}", sender.getName())
            ));
            return true;
        }

        sendHelp(sender);
        return true;
    }

    private boolean hasPermission(CommandSender sender, String permission) {
        return sender.hasPermission("heronikachat.bw.admin") || sender.hasPermission(permission);
    }

    private void sendHelp(CommandSender sender) {
        String[] lines = plugin.getConfigManager().getCommandHelpMessage().split("\n", -1);
        for (String line : lines) {
            if (!line.isEmpty()) {
                sendMessage(sender, line);
            }
        }
    }

    private void sendUsage(CommandSender sender) {
        sendMessage(sender, plugin.getConfigManager().getCommandUsageMessage());
    }

    private void sendMessage(CommandSender sender, String message) {
        sender.sendMessage(plugin.getConfigManager().formatMessage(message));
    }
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length != 1) {
            return Collections.emptyList();
        }

        String input = args[0].toLowerCase();
        List<String> suggestions = new ArrayList<String>();

        if ("help".startsWith(input)) {
            suggestions.add("help");
        }
        if (hasPermission(sender, "heronikachat.bw.clearchat") && "clearchat".startsWith(input)) {
            suggestions.add("clearchat");
        }
        if (hasPermission(sender, "heronikachat.bw.reload") && "reload".startsWith(input)) {
            suggestions.add("reload");
        }
        if (hasPermission(sender, "heronikachat.bw.stopchat") && "stopchat".startsWith(input)) {
            suggestions.add("stopchat");
        }

        return suggestions;
    }

}
