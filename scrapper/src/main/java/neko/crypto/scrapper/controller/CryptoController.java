package neko.crypto.scrapper.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class CryptoController {
    private final CryptoService cryptoService;
    private final WatchlistService watchlistService;
    private final MessageSource messageSource;

    @GetMapping("/price/{ticker}")
    public ResponseEntity<String> getPrice(@PathVariable String ticker, @RequestParam(defaultValue = "false") boolean refresh) {
        log.info("Received GET /api/crypto/price/{} with refresh={}", ticker, refresh);
        try {
            String price = cryptoService.getPrice(ticker);
            log.info("Successfully fetched price for ticker {}: ${}", ticker, price);
            return ResponseEntity.ok(price);
        } catch (RuntimeException e) {
            log.error("Error fetching price for ticker {}: {}", ticker, e.getMessage());
            return ResponseEntity.badRequest().body(messageSource.getMessage("api.price.error", new Object[]{ticker, e.getMessage()}, Locale.getDefault()));
        }
    }

    @PostMapping("/watchlist/{chatId}/add/{ticker}")
    public ResponseEntity<Void> addToWatchlist(@PathVariable Long chatId, @PathVariable String ticker) {
        log.info("Received POST /api/crypto/watchlist/{}/add/{}", chatId, ticker);
        watchlistService.addToWatchlist(chatId, ticker.toLowerCase());
        log.info("Added ticker {} to watchlist for chatId: {}", ticker, chatId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/watchlist/{chatId}/remove/{ticker}")
    public ResponseEntity<Boolean> removeFromWatchlist(@PathVariable Long chatId, @PathVariable String ticker) {
        log.info("Received DELETE /api/crypto/watchlist/{}/remove/{}", chatId, ticker);
        boolean removed = watchlistService.removeFromWatchlist(chatId, ticker.toLowerCase());
        log.info("Removed ticker {} from watchlist for chatId: {}, success: {}", ticker, chatId, removed);
        return ResponseEntity.ok(removed);
    }

    @GetMapping("/watchlist/{chatId}")
    public ResponseEntity<String> getWatchlist(@PathVariable Long chatId) {
        log.info("Received GET /api/crypto/watchlist/{}", chatId);
        Set<String> watchlist = watchlistService.getWatchlist(chatId);
        String response = watchlist.isEmpty() ? "" : String.join(", ", watchlist);
        log.info("Fetched watchlist for chatId: {}, watchlist: {}", chatId, response);
        return ResponseEntity.ok(response);
    }
}