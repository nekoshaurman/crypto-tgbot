package neko.crypto.scrapper.controller;

import lombok.RequiredArgsConstructor;
import neko.crypto.scrapper.service.CryptoService;
import neko.crypto.scrapper.service.WatchlistService;
import org.springframework.context.MessageSource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Locale;
import java.util.Set;

@RestController
@RequestMapping("/api/crypto")
@RequiredArgsConstructor
public class CryptoController {
    private final CryptoService cryptoService;
    private final WatchlistService watchlistService;
    private final MessageSource messageSource;

    @GetMapping("/price/{ticker}")
    public ResponseEntity<String> getPrice(@PathVariable String ticker) {
        try {
            String price = cryptoService.getPrice(ticker.toLowerCase());
            return ResponseEntity.ok(price);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(messageSource.getMessage("api.price.error", new Object[]{e.getMessage()}, Locale.getDefault()));
        }
    }

    @PostMapping("/watchlist/{chatId}/add/{ticker}")
    public ResponseEntity<Void> addToWatchlist(@PathVariable Long chatId, @PathVariable String ticker) {
        watchlistService.addToWatchlist(chatId, ticker.toLowerCase());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/watchlist/{chatId}/remove/{ticker}")
    public ResponseEntity<Boolean> removeFromWatchlist(@PathVariable Long chatId, @PathVariable String ticker) {
        boolean removed = watchlistService.removeFromWatchlist(chatId, ticker.toLowerCase());
        return ResponseEntity.ok(removed);
    }

    @GetMapping("/watchlist/{chatId}")
    public ResponseEntity<String> getWatchlist(@PathVariable Long chatId) {
        Set<String> watchlist = watchlistService.getWatchlist(chatId);
        String response = watchlist.isEmpty() ? "" : String.join(", ", watchlist);
        return ResponseEntity.ok(response);
    }
}