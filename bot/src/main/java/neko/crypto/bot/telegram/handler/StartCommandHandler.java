package neko.crypto.bot.telegram.handler;

import com.pengrad.telegrambot.model.Update;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class StartCommandHandler implements CommandHandler {
    @Override
    public boolean canHandle(String command) {
        return command.startsWith("/start");
    }

    @Override
    public String handle(Update update, MessageSource messageSource) {
        return messageSource.getMessage("start.message", null, Locale.getDefault());
    }
}