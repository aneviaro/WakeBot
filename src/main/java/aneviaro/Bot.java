package aneviaro;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
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
    public static final String TOKEN = "1499583354:AAELEOBlZyUl2M8RcqwmJjUxzhraVl_UlfM";
    public static final String USERNAME = "wake_me_bot";

    public Bot(DefaultBotOptions options) {
        super(options);
    }

    public Bot() {
    }

    private static InlineKeyboardMarkup getClarificationButtons() {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        InlineKeyboardButton wakeUpButton = new InlineKeyboardButton();
        wakeUpButton.setText("Wake up");
        wakeUpButton.setCallbackData("Wake up");
        InlineKeyboardButton goToSleepButton = new InlineKeyboardButton();
        goToSleepButton.setText("Go to sleep");
        goToSleepButton.setCallbackData("Go to sleep");
        List<InlineKeyboardButton> row = new ArrayList<>();
        row.add(wakeUpButton);
        row.add(goToSleepButton);
        List<List<InlineKeyboardButton>> rowList = new ArrayList<>();
        rowList.add(row);
        inlineKeyboardMarkup.setKeyboard(rowList);
        return inlineKeyboardMarkup;
    }

    private static List<Calendar> calculateTime(Date date, Integer coef) {
        List<Calendar> times = new ArrayList<Calendar>();
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
        Date date = null;
        try {
            date = simpleDateFormat.parse(query.getMessage().getReplyToMessage().getText());
        } catch (ParseException e) {
            send(query.getMessage(), "Not a valid time format, please try 22:22", null);
        }
        if (query.getData().equals("Go to sleep")) {
            send(query.getMessage(), formatTimes("You can wake up at:\n", calculateTime(date, 1)), null);
        } else if (query.getData().equals("Wake up")) {
            send(query.getMessage(),formatTimes("You can go to sleep at:\n", calculateTime(date, -1)) , null);
        } else {
            send(query.getMessage(), "Not a valid time format, please try 22:22", null);
        }
    }

    private void processMessage(Message message) {
        if (isFirst(message.getText())) {
            send(message, "Greeting, please type in the time you want to go to sleep at: I.e. 22:15", null);
        } else if (message.getText().equals("/now")){
//            send(message, formatTimes("You can wake up at:\n", calculateTime(new Date(message), 1)), null);
            send(message, "Sorry, we don't support this feature right now", null);
        }
        else {
            try {
                sendClarification(message);
            } catch (ParseException e) {
                send(message, "Not a valid time format, please try 22:22", null);
            }
        }
    }

    private void sendClarification(Message message) throws ParseException {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm");
        Date date = simpleDateFormat.parse(message.getText());
        String text = "Is it wake up time or go to sleep time?";
        send(message, text, getClarificationButtons());
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
        times.forEach((time) -> {
            answer.append(output.format(time.getTime()));
            answer.append('\n');
        });
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
