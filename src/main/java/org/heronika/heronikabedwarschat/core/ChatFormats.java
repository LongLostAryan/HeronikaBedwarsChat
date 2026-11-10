package org.heronika.heronikabedwarschat.core;

public enum ChatFormats {
    LOBBY("format-chat-lobby"),
    WAITING("format-chat-waiting"),
    GLOBAL("format-chat-global"),
    TEAM("format-chat-team"),
    SPECTATOR("format-chat-spectator"),
    GAME_SPECTATOR("format-game-spectator"),
    GAME_GLOBAL_SPECTATOR("format-game-global-spectator"),
    AUTO_GG("format-auto-gg");

    private final String configPath;

    ChatFormats(String configPath) {
        this.configPath = configPath;
    }

    public String getConfigPath() {
        return configPath;
    }
}
