package org.heronika.heronikabedwarschat.core;

import org.bukkit.plugin.java.JavaPlugin;
import org.heronika.heronikabedwarschat.compatibility.BedWarsCompatibility;
import org.heronika.heronikabedwarschat.compatibility.CompatibilityManager;

public final class HeronikaBedwarsChat extends JavaPlugin {

    private static HeronikaBedwarsChat instance;
    private ConfigManager configManager;
    private ChatFormatManager chatFormatManager;
    private CompatibilityManager compatibilityManager;
    private BedWarsCompatibility compatibility;
    private AutoMessageManager autoMessageManager;
    private volatile boolean chatEnabled = true;

    @Override
    public void onEnable() {
        instance = this;
        configManager = new ConfigManager(this);
        chatFormatManager = new ChatFormatManager(configManager);
        compatibilityManager = new CompatibilityManager(this);
        compatibility = compatibilityManager.load();
        autoMessageManager = new AutoMessageManager(this);
        autoMessageManager.reload();

        if (compatibility != null) {
            getServer().getPluginManager().registerEvents(new ChatListener(this), this);
            getLogger().info("BedWars1058 " + compatibility.getVersion() + " compatibility loaded.");
        } else {
            getLogger().severe("A supported BedWars1058 version was not detected. Chat integration is disabled.");
        }

        if (getCommand("hbc") != null) {
            HBCCommand commandHandler = new HBCCommand(this);
            getCommand("hbc").setExecutor(commandHandler);
            getCommand("hbc").setTabCompleter(commandHandler);
        } else {
            getLogger().severe("Command 'hbc' is missing from plugin.yml.");
        }

        getLogger().info("HeronikaBedwarsChat has been enabled!");
    }

    @Override
    public void onDisable() {
        if (autoMessageManager != null) {
            autoMessageManager.stop();
        }
        getLogger().info("HeronikaBedwarsChat has been disabled!");
        instance = null;
    }

    public static HeronikaBedwarsChat getInstance() {
        return instance;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public ChatFormatManager getChatFormatManager() {
        return chatFormatManager;
    }

    public void reloadAutoMessages() {
        if (autoMessageManager != null) {
            autoMessageManager.reload();
        }
    }

    public BedWarsCompatibility getCompatibility() {
        return compatibility;
    }

    public boolean isChatEnabled() {
        return chatEnabled;
    }

    public boolean toggleChat() {
        chatEnabled = !chatEnabled;
        return chatEnabled;
    }
}
