package ru.whitecity.bot;

public final class BotConfig {
    public final String botToken;
    public final String botUsername;
    public final String dbFile;

    private BotConfig(String botToken, String botUsername, String dbFile) {
        this.botToken = botToken;
        this.botUsername = botUsername;
        this.dbFile = dbFile;
    }

    public static BotConfig fromEnv() {
        String token = getenvRequired("BOT_TOKEN");
        String username = getenvRequired("BOT_USERNAME");
        String dbFile = System.getenv().getOrDefault("DB_FILE", "/data/bot.db");
        return new BotConfig(token, username, dbFile);
    }

    private static String getenvRequired(String key) {
        String v = System.getenv(key);
        if (v == null || v.isBlank()) {
            throw new IllegalStateException("Missing required env var: " + key);
        }
        return v.trim();
    }
}