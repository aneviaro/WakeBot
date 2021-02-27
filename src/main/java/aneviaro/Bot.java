package aneviaro;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;

import javax.annotation.Nullable;
import javax.annotation.PostConstruct;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Component
public class Bot extends TelegramLongPollingBot {
    public static final String TOKEN = System.getenv("BOT_TOKEN");
    public static final String USERNAME = "wake_me_bot";
    public static final String WAKEUP = "Wake up";
    public static final String GOTOSLEEP = "Go to sleep";

    public Bot(DefaultBotOptions options) {
        super(options);
    }

    public Bot() {
    }

    private static InlineKeyboardMarkup makeClarificationButtons() {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        InlineKeyboardButton wakeUpButton = new InlineKeyboardButton();
        wakeUpButton.setText(WAKEUP);
        wakeUpButton.setCallbackData(WAKEUP);
        InlineKeyboardButton goToSleepButton = new InlineKeyboardButton();
        goToSleepButton.setText(GOTOSLEEP);
        goToSleepButton.setCallbackData(GOTOSLEEP);
        List<InlineKeyboardButton> row = new ArrayList<>();
        row.add(wakeUpButton);
        row.add(goToSleepButton);
        List<List<InlineKeyboardButton>> rowList = new ArrayList<>();
        rowList.add(row);
        inlineKeyboardMarkup.setKeyboard(rowList);
        return inlineKeyboardMarkup;
    }

    private static List<Calendar> calculateTime(Date date, Integer coef) {
        List<Calendar> times = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            calendar.add(Calendar.MINUTE, coef * 90 * (i + 1));
            times.add(calendar);
        }
        return times;
    }

    public void onUpdateReceived(Update update) {
        if (update.hasMessage()) {
            processMessage(update.getMessage());
        } else if (update.hasCallbackQuery()) {
            processCallback(update.getCallbackQuery());
        }
    }

    private void processCallback(CallbackQuery query) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm");
        AnswerCallbackQuery answerCallbackQuery = new AnswerCallbackQuery();
        answerCallbackQuery.setCallbackQueryId(query.getId());
        Date date = null;
        try {
            date = simpleDateFormat.parse(query.getMessage().getReplyToMessage().getText());
        } catch (ParseException e) {
            send(query.getMessage(), Errors.NotValidTimeFormat.getMessage(), null);
        }
        if (query.getData().equals(GOTOSLEEP)) {
            send(query.getMessage(), formatTimes(Messages.BestTimeToWakeUp.getMessage(), calculateTime(date, 1)), null);
        } else if (query.getData().equals(WAKEUP)) {
            send(query.getMessage(), formatTimes(Messages.BestTimeToGoToSleep.getMessage(), calculateTime(date, -1)),
                    null);
        } else {
            send(query.getMessage(), Errors.NotValidTimeFormat.getMessage(), null);
        }
    }

    private void processMessage(Message message) {
        if (isFirst(message.getText())) {
            send(message, Messages.Greetings.getMessage(), null);
        } else if (message.getText().equals("/now")) {
            send(message, Errors.FeatureIsNotSupported.getMessage(), null);
        } else {
            try {
                sendClarification(message);
            } catch (ParseException e) {
                send(message, Errors.NotValidTimeFormat.getMessage(), null);
            }
        }
    }

    private void sendClarification(Message message) throws ParseException {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm");
        simpleDateFormat.parse(message.getText());
        send(message, Messages.ClarificationQuestion.getMessage(), makeClarificationButtons());
    }

    private void send(Message message, String text, @Nullable InlineKeyboardMarkup keyboardMarkup) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setText(text);
        sendMessage.enableMarkdown(true);
        sendMessage.setChatId(message.getChatId());
        sendMessage.setReplyToMessageId(message.getMessageId());
        if (keyboardMarkup != null) {
            sendMessage.setReplyMarkup(keyboardMarkup);
        }
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            Logger.getLogger(Bot.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    private String formatTimes(String text, List<Calendar> times) {
        StringBuilder answer = new StringBuilder();
        answer.append(text);
        SimpleDateFormat output = new SimpleDateFormat("HH:mm");
        answer.append("*");
        times.forEach((time) -> {
            answer.append(output.format(time.getTime()));
            answer.append('\n');
        });
        answer.append("*");
        return answer.toString();
    }

    private boolean isFirst(String text) {
        return text.equals("/start") || text.equals("/restart");
    }

    public String getBotUsername() {
        return USERNAME;
    }

    public String getBotToken() {
        return TOKEN;
    }

    @PostConstruct
    public void addBot() {
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi();
        try {
            telegramBotsApi.registerBot(this);
        } catch (TelegramApiRequestException e) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, e);
        }
    }
}
