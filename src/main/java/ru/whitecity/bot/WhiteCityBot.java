package ru.whitecity.bot;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public final class WhiteCityBot extends TelegramLongPollingBot {

    // callback data
    private static final String CB_AGENCY = "menu_agency";
    private static final String CB_PROPERTY_MGMT = "menu_property_mgmt"; // ✅ NEW
    private static final String CB_CATALOG = "menu_catalog";
    private static final String CB_FAQ = "menu_faq";

    private static final String CB_FAQ_PRICE = "faq_price";
    private static final String CB_FAQ_CITIES = "faq_cities";
    private static final String CB_FAQ_SELL = "faq_sell";

    private static final String CB_BACK_FAQ = "back_faq";
    private static final String CB_BACK_MENU = "back_menu";

    private static final String CB_BACK_PROPERTY_MGMT = "back_property_mgmt"; // ✅ NEW

    private final BotConfig cfg;
    private final Db db;

    public WhiteCityBot(BotConfig cfg, Db db) {
        super(cfg.botToken);
        this.cfg = cfg;
        this.db = db;
    }

    @Override
    public String getBotUsername() {
        return cfg.botUsername;
    }

    @Override
    public void onUpdateReceived(Update update) {
        try {
            if (update.hasMessage()) {
                Message msg = update.getMessage();
                if (msg.hasText() && msg.getText().startsWith("/start")) {
                    handleStart(msg);
                    return;
                }
            }

            if (update.hasCallbackQuery()) {
                handleCallback(update.getCallbackQuery());
            }
        } catch (Exception e) {
            // чтобы бот не падал из-за одной ошибки
            e.printStackTrace();
        }
    }

    private void handleStart(Message msg) throws TelegramApiException, IOException {
        User u = msg.getFrom();
        db.upsertUser(u.getId(), u.getUserName(), u.getFirstName(), u.getLastName());
        sendStart(msg.getChatId(), u);
    }

    private void handleCallback(CallbackQuery cq) throws TelegramApiException, IOException {
        String data = cq.getData();
        Long chatId = cq.getMessage().getChatId();

        switch (data) {
            case CB_AGENCY -> sendAgency(chatId);
            case CB_PROPERTY_MGMT -> sendPropertyManagement(chatId); // ✅ NEW
            case CB_CATALOG -> sendCatalog(chatId);
            case CB_FAQ -> sendFaqMenu(chatId);

            case CB_FAQ_PRICE -> sendFaqPrice(chatId);
            case CB_FAQ_CITIES -> sendFaqCities(chatId);
            case CB_FAQ_SELL -> sendFaqSell(chatId);

            case CB_BACK_FAQ -> sendFaqMenu(chatId);

            case CB_BACK_PROPERTY_MGMT -> sendStart(chatId, cq.getFrom()); // ✅ NEW (назад из NEDVIX)
            case CB_BACK_MENU -> sendStart(chatId, cq.getFrom());

            default -> sendUnknown(chatId);
        }
    }

    // ======= Start screen (strictly with photo) =======

    private void sendStart(Long chatId, User u) throws TelegramApiException, IOException {
        String username = displayName(u); // теперь это ИМЯ, а не @username

        String text = """
                👋 <b>Приветствую Вас!</b>

                🏙️ <b>Планируете купить</b> самый привлекательный и ликвидный объект недвижимости?
                <b>Вы по адресу!</b>

                💼 <b>Необходимо</b> максимально дорого и быстро <b>продать</b> квартиру, апартамент или коммерческую недвижимость?
                <b>Вы по адресу!</b>

                👤 <b>Виктор Пешехонов:</b>
                <b>Основатель Агентства</b> недвижимости <i>"Белый город"</i> — Ваш надёжный партнёр и персональный советник!

                🧭 <b>Знаем все проекты</b> и жилые комплексы, спец. предложения и рассрочки.

                💎 <b>Лучшие варианты для инвестиций</b> и сохранения семейного капитала уже ждут вас!
                """;

        SendPhoto sp = new SendPhoto();
        sp.setChatId(chatId.toString());
        sp.setCaption(text);
        sp.setParseMode(ParseMode.HTML);
        sp.setReplyMarkup(mainMenuKeyboard());
        sp.setPhoto(loadPhotoFromResources("1.jpg"));

        execute(sp);
    }

    // ======= Screens =======

    private void sendAgency(Long chatId) throws TelegramApiException {
        String text = """
                <b>АН «БЕЛЫЙ ГОРОД»</b>\s
                <b>Надёжный и годами проверенный партнёр в любых сделках с недвижимостью!</b>
                
                🤝 Наша задача — сократить расстояние между покупателем и продавцом, помочь клиентам оперативно и без рисков продать, купить, сдать и снять недвижимость.
                🧩 Мы предлагаем готовые решения и сопровождаем на каждом этапе.
                🏘Репутация дороже денег⏳️
                
                👉 <b>Сайт Агентства:</b> https://whitecity.su/
                """;

        SendMessage sm = baseHtml(chatId, text);
        sm.setReplyMarkup(oneColumnKeyboard(List.of(
                urlBtn("👤 Связаться с Руководителем", "https://t.me/viktorpeshekhonov"),
                cbBtn("\uD83C\uDFE0 Вернуться в меню", CB_BACK_MENU)
        )));
        execute(sm);
    }

    // ✅ NEW SCREEN
    private void sendPropertyManagement(Long chatId) throws TelegramApiException {
        String text = """
                <b>NEDVIX – компания по доверительному управлению коммерческой недвижимостью</b>
                Помогаем собственникам эффективно управлять арендными отношениями и получать стабильный пассивный доход от сдачи недвижимости в аренду

                <b>Доверьте аренду профессионалам</b> 👉 https://nedvix-realty.ru/?ysclid=mk2krcrhpc727403864
                """;

        SendMessage sm = baseHtml(chatId, text);
        sm.setReplyMarkup(oneColumnKeyboard(List.of(
                cbBtn("⬅️ Вернуться назад", CB_BACK_PROPERTY_MGMT),
                cbBtn("\uD83C\uDFE0 Вернуться в меню", CB_BACK_MENU)
        )));
        execute(sm);
    }

    private void sendCatalog(Long chatId) throws TelegramApiException {
        String text = """
                <b>Каталог недвижимости</b>

                🏙️ <a href="https://drive.google.com/file/d/16FWw9skGQl9Y0WN4PSLohgtO2mU-K9jJ/view?usp=drive_link">Каталог новостроек Москвы</a>
                🏢 <a href="https://drive.google.com/file/d/1bn4tNRqHE8Xyk1Fotq_GMJ1K0Hfj_w3U/view?usp=drive_link">Каталог бизнес-центров Москвы</a>
                💎 <a href="https://drive.google.com/file/d/1Yy4qK5zfwRGHGtksUE2W4FzulFl4PBC9/view?usp=drive_link">Каталог элитных новостроек Нижнего Новгорода</a>
                """;

        SendMessage sm = baseHtml(chatId, text);
        sm.setReplyMarkup(oneColumnKeyboard(List.of(
                cbBtn("\uD83C\uDFE0 Вернуться в меню", CB_BACK_MENU)
        )));
        execute(sm);
    }

    private void sendFaqMenu(Long chatId) throws TelegramApiException {
        String text = """
                ❓ <b>Частые вопросы</b>
                Выберите интересующий пункт 👇
                """;

        SendMessage sm = baseHtml(chatId, text);
        sm.setReplyMarkup(oneColumnKeyboard(List.of(
                cbBtn("💰 Стоимость услуг", CB_FAQ_PRICE),
                cbBtn("🏙️ Города присутствия", CB_FAQ_CITIES),
                cbBtn("\uD83C\uDF06 Как продаём объекты", CB_FAQ_SELL),
                cbBtn("\uD83C\uDFE0 Вернуться в меню", CB_BACK_MENU)
        )));
        execute(sm);
    }

    private void sendFaqPrice(Long chatId) throws TelegramApiException {
        String text = """
                <b>Какая стоимость ваших услуг?</b>

                ✅ Всё индивидуально, в зависимости от сложности сделки, специфики объекта, региона.
                📌 Но есть и стандартная комиссия — <b>2% от цены объекта</b> ИЛИ <b>фиксированная сумма + %</b>.

                Все наши услуги оплачивает продавец.
                """;

        SendMessage sm = baseHtml(chatId, text);
        sm.setReplyMarkup(oneColumnKeyboard(List.of(
                cbBtn("⬅️ Назад", CB_BACK_FAQ),
                cbBtn("\uD83C\uDFE0 Вернуться в меню", CB_BACK_MENU)
        )));
        execute(sm);
    }

    private void sendFaqCities(Long chatId) throws TelegramApiException {
        String text = """
                <b>А в каких городах вы работаете?</b>

                🇷🇺 Города присутствия в России: <b>Нижний Новгород, Москва, Сочи, Санкт — Петербург</b>.
                🌍 В других регионах страны и Мира — подключаем наших проверенных партнёров.

                📩 Чтобы уточнить, работаем ли мы именно по вашему региону или городу, свяжитесь с нами любым удобным способом.
                """;

        SendMessage sm = baseHtml(chatId, text);
        sm.setReplyMarkup(oneColumnKeyboard(List.of(
                cbBtn("⬅️ Назад", CB_BACK_FAQ),
                cbBtn("\uD83C\uDFE0 Вернуться в меню", CB_BACK_MENU)
        )));
        execute(sm);
    }

    private void sendFaqSell(Long chatId) throws TelegramApiException {
        String text = """
                <b>Как вы продаёте недвижимость?</b>

                Сперва — телефонный звонок: общие вопросы — ответы, затем встреча на объекте.
                Далее проводим осмотр и оценку.
                Делаем красивые фото и видео.
                И только после начинаем маркетинговую поддержку с мощной рекламной кампанией, где используем:
                 📱 Социальные сети;
                 📍 Геолокационные сервисы;
                 🎯 Контекстную рекламу;
                 🌐 Все популярные интернет-ресурсы;
                 🏦 Дополнительно банки-партнёры активно предлагают наши объекты.

                🧑‍💻 А для эксклюзивных объектов создаём сайты-одностраничники.
                """;

        SendMessage sm = baseHtml(chatId, text);
        sm.setReplyMarkup(oneColumnKeyboard(List.of(
                cbBtn("⬅️ Назад", CB_BACK_FAQ),
                cbBtn("\uD83C\uDFE0 Вернуться в меню", CB_BACK_MENU)
        )));
        execute(sm);
    }

    private void sendUnknown(Long chatId) throws TelegramApiException {
        SendMessage sm = baseHtml(chatId, "🤔 Не понял команду. Нажмите /start или выберите пункт меню.");
        execute(sm);
    }

    // ======= Keyboards =======

    private InlineKeyboardMarkup mainMenuKeyboard() {
        // ✅ теперь 5 кнопок, новая стоит сразу после "Агентство"
        return oneColumnKeyboard(List.of(
                urlBtn("📩 Связаться", "https://t.me/viktorpeshekhonov"),
                cbBtn("🏢 Агентство «БЕЛЫЙ ГОРОД»", CB_AGENCY),
                cbBtn("🏬 Управление недвижимостью", CB_PROPERTY_MGMT), // ✅ NEW
                cbBtn("📚 Каталог недвижимости", CB_CATALOG),
                cbBtn("❓ Частые вопросы", CB_FAQ)
        ));
    }

    private InlineKeyboardMarkup oneColumnKeyboard(List<InlineKeyboardButton> buttons) {
        InlineKeyboardMarkup m = new InlineKeyboardMarkup();

        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        for (InlineKeyboardButton b : buttons) {
            rows.add(List.of(b)); // каждая кнопка — отдельная строка
        }

        m.setKeyboard(rows);
        return m;
    }

    private InlineKeyboardButton cbBtn(String text, String callbackData) {
        InlineKeyboardButton b = new InlineKeyboardButton();
        b.setText(text);
        b.setCallbackData(callbackData);
        return b;
    }

    private InlineKeyboardButton urlBtn(String text, String url) {
        InlineKeyboardButton b = new InlineKeyboardButton();
        b.setText(text);
        b.setUrl(url);
        return b;
    }

    // ======= Helpers =======

    private SendMessage baseHtml(Long chatId, String text) {
        SendMessage sm = new SendMessage();
        sm.setChatId(chatId.toString());
        sm.setParseMode(ParseMode.HTML);
        sm.setDisableWebPagePreview(true);
        sm.setText(text);
        return sm;
    }

    private InputFile loadPhotoFromResources(String resourceName) throws IOException {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(resourceName)) {
            if (is == null) throw new FileNotFoundException("Resource not found: " + resourceName);
            byte[] bytes = is.readAllBytes();
            return new InputFile(new ByteArrayInputStream(bytes), resourceName);
        }
    }

    private static String displayName(User u) {
        // приоритет: Имя + Фамилия (если есть), иначе username БЕЗ @, иначе "друг"
        String first = u.getFirstName();
        String last = u.getLastName();

        boolean hasFirst = first != null && !first.isBlank();
        boolean hasLast = last != null && !last.isBlank();

        if (hasFirst) {
            return hasLast ? (first + " " + last) : first;
        }

        String username = u.getUserName();
        if (username != null && !username.isBlank()) {
            return username; // без "@"
        }

        return "друг";
    }

    private static String escape(String s) {
        // минимальное экранирование под HTML
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}