package neko.crypto.bot.telegram.handler;

import com.pengrad.telegrambot.model.Update;
import org.springframework.context.MessageSource;

public interface CommandHandler {
    boolean canHandle(String command);
    String handle(Update update, MessageSource messageSource);
}