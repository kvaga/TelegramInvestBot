package telegrambot;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.telegram.telegrambots.TelegramBotsApi;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;
import org.telegram.telegrambots.exceptions.TelegramApiRequestException;

// https://habr.com/ru/post/476306/

public class _Bot extends TelegramLongPollingBot {
    private static final Logger log = Logger.getLogger(_Bot.class);

    final int RECONNECT_PAUSE =10000;

   
    String userName;
    String token;
    
    public _Bot(String userName, String token) {
    	this.userName=userName;
    	this.token=token;
    }
    
    public void setBotUsername(String userName) {
    	this.userName=userName;
    }
    
    public String getBotUsername() {
    	return userName;
    }
    
    public void setBotToken(String token) {
    	this.token=token;
    }
    
    public String getBotToken() {
    	return token;
    }
    
    

    public void onUpdateReceived(Update update) {
    	log.debug("Receive new Update. updateID: " + update.getUpdateId());

//        Long chatId = update.getMessage().getChatId();
    	
    	 if(update.hasMessage()){
             if(update.getMessage().hasText()){
                 if(update.getMessage().getText().equals("Hello")){
                     try {
                         execute(sendInlineKeyBoardMessage(update.getMessage().getChatId()));
                     } catch (TelegramApiException e) {
                         e.printStackTrace();
                     }
                 }
             }
         }else if(update.hasCallbackQuery()){
             try {
                 execute(new SendMessage().setText(
                         update.getCallbackQuery().getData())
                         .setChatId(update.getCallbackQuery().getMessage().getChatId()));
             } catch (TelegramApiException e) {
                 e.printStackTrace();
             }
         }else if(update.hasChannelPost()) {

    	 
//        String inputText = update.getMessage().getText();
    	 Long chatId=update.getChannelPost().getChatId();

    	 String inputText = update.getChannelPost().getText();


        System.out.println("Text: " + update.getChannelPost().getText());
        if (inputText.startsWith("/start")) {
            SendMessage message = new SendMessage();
            message.setChatId(chatId);
            message.setText("Hello. This is start message ");
            
            try {
                execute(message);
                execute(sendInlineKeyBoardMessage(chatId));
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
         }
    }

    public static SendMessage sendInlineKeyBoardMessage(long chatId) {
    	// https://habr.com/ru/post/418905/
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        InlineKeyboardButton inlineKeyboardButton1 = new InlineKeyboardButton();
        InlineKeyboardButton inlineKeyboardButton2 = new InlineKeyboardButton();
        inlineKeyboardButton1.setText("Тык");
        inlineKeyboardButton1.setCallbackData("Button \"Тык\" has been pressed");
        inlineKeyboardButton2.setText("Тык2");
        inlineKeyboardButton2.setCallbackData("Button \"Тык2\" has been pressed");
        List<InlineKeyboardButton> keyboardButtonsRow1 = new ArrayList<InlineKeyboardButton>();
        List<InlineKeyboardButton> keyboardButtonsRow2 = new ArrayList<InlineKeyboardButton>();
        keyboardButtonsRow1.add(inlineKeyboardButton1);
       keyboardButtonsRow1.add(new InlineKeyboardButton().setText("Fi4a").setCallbackData("CallFi4a"));
        keyboardButtonsRow2.add(inlineKeyboardButton2);
        List<List<InlineKeyboardButton>> rowList = new ArrayList<List<InlineKeyboardButton>>();
        rowList.add(keyboardButtonsRow1);
        rowList.add(keyboardButtonsRow2);
        inlineKeyboardMarkup.setKeyboard(rowList);
        return new SendMessage().setChatId(chatId).setText("Пример").setReplyMarkup(inlineKeyboardMarkup);
       }
    public void botConnect() {
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi();
        try {
            telegramBotsApi.registerBot(this);
            log.info("TelegramAPI started. Look for messages");
        } catch (TelegramApiRequestException e) {
            log.error("Cant Connect. Pause " + RECONNECT_PAUSE / 1000 + "sec and try again. Error: " + e.getMessage());
            try {
                Thread.sleep(RECONNECT_PAUSE);
            } catch (InterruptedException e1) {
            	log.error("Exception during reconnect pause of the bot", e);
            	return;
            }
            botConnect();
        }
    }

	
}