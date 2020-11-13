package aneviaro;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;

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

    public void onUpdateReceived(Update update) {
        if (update.hasMessage()) {
            processMessage(update.getMessage());
        }
    }

    private void processMessage(Message message) {
        if (isFirst(message.getText())) {
            send(message, "Greeting, please type in the time you want to go to sleep at: I.e. 22:15");
        } else {
            try {
                sendTimes(message, calculateTime(message.getText()));
            } catch (ParseException e) {
                send(message, "Not a valid time format, please try 22:22");
            }
        }
    }

    private List<Calendar> calculateTime(String text) throws ParseException {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm");
        Date date = simpleDateFormat.parse(text);
        List<Calendar> times = new ArrayList<Calendar>();
        for (int i = 0; i < 6; i++) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            calendar.add(Calendar.MINUTE, 90 * (i + 1));
            times.add(calendar);
        }
        return times;
    }

    private void send(Message message, String text) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setText(text);
        sendMessage.enableMarkdown(true);
        sendMessage.setChatId(message.getChatId());
        sendMessage.setReplyToMessageId(message.getMessageId());

        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            Logger.getLogger(Bot.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    private void sendTimes(Message message, List<Calendar> times) {
        StringBuilder answer = new StringBuilder();
        answer.append("You can wake up at:\n");
        SimpleDateFormat output = new SimpleDateFormat("HH:mm");
        times.forEach((time) -> {
            answer.append(output.format(time.getTime()));
            answer.append('\n');
        });
        send(message, answer.toString());
    }

//    private static ReplyKeyboardMarkup getSettingsKeyboard(String language) {
//        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
//        replyKeyboardMarkup.setSelective(true);
//        replyKeyboardMarkup.setResizeKeyboard(true);
//        replyKeyboardMarkup.setOneTimeKeyboard(false);
//
//        List<KeyboardRow> keyboard = new ArrayList<>();
//        KeyboardRow keyboardFirstRow = new KeyboardRow();
//        keyboardFirstRow.add(new KeyboardButton(""));
//        keyboardFirstRow.add(getUnitsCommand(language));
//        KeyboardRow keyboardSecondRow = new KeyboardRow();
//        keyboardSecondRow.add(getAlertsCommand(language));
//        keyboardSecondRow.add(getBackCommand(language));
//        keyboard.add(keyboardFirstRow);
//        keyboard.add(keyboardSecondRow);
//        replyKeyboardMarkup.setKeyboard(keyboard);
//
//        return replyKeyboardMarkup;
//    }

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
