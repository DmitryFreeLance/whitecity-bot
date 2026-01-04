package ru.whitecity.bot;

import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

public final class BotApp {
    public static void main(String[] args) throws Exception {
        BotConfig cfg = BotConfig.fromEnv();
        Db db = new Db(cfg.dbFile);

        TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
        botsApi.registerBot(new WhiteCityBot(cfg, db));

        System.out.println("✅ WhiteCity bot started as @" + cfg.botUsername);
    }
}