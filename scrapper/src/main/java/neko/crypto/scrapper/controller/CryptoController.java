package neko.crypto.scrapper.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import neko.crypto.scrapper.service.CryptoService;
import neko.crypto.scrapper.service.WatchlistService;
import org.springframework.context.MessageSource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

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
        log.debug("Received GET /api/crypto/price/{} with refresh={}", ticker, refresh);
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
    public ResponseEntity<String> addToWatchlist(@PathVariable Long chatId, @PathVariable String ticker) {
        log.debug("Received POST /api/crypto/watchlist/{}/add/{}", chatId, ticker);
        try {
            watchlistService.addToWatchlist(chatId, ticker.toLowerCase());
            log.info("Added ticker {} to watchlist for chatId: {}", ticker, chatId);
            return ResponseEntity.ok("");
        } catch (IllegalArgumentException e) {
            log.error("Error adding ticker {} to watchlist for chatId {}: {}", ticker, chatId, e.getMessage());
            return ResponseEntity.badRequest().body(messageSource.getMessage("api.watchlist.add.error", new Object[]{ticker, e.getMessage()}, Locale.getDefault()));
        }
    }

    @DeleteMapping("/watchlist/{chatId}/remove/{ticker}")
    public ResponseEntity<Boolean> removeFromWatchlist(@PathVariable Long chatId, @PathVariable String ticker) {
        log.debug("Received DELETE /api/crypto/watchlist/{}/remove/{}", chatId, ticker);
        boolean removed = watchlistService.removeFromWatchlist(chatId, ticker.toLowerCase());
        log.info("Removed ticker {} from watchlist for chatId: {}, success: {}", ticker, chatId, removed);
        return ResponseEntity.ok(removed);
    }

    @GetMapping("/watchlist/{chatId}")
    public ResponseEntity<String> getWatchlist(@PathVariable Long chatId) {
        log.debug("Received GET /api/crypto/watchlist/{}", chatId);
        Set<String> watchlist = watchlistService.getWatchlist(chatId);
        String response = watchlist.isEmpty() ? "" : String.join(", ", watchlist);
        log.info("Fetched watchlist for chatId: {}, watchlist: {}", chatId, response);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/watchlist/chatIds")
    public ResponseEntity<String> getAllChatIds() {
        Set<Long> chatIds = watchlistService.getAllChatIds();
        String result = chatIds.stream().map(String::valueOf).collect(Collectors.joining(","));
        return ResponseEntity.ok(result);
    }

    @GetMapping("/pairs")
    public ResponseEntity<String> getUsdtPairs(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        List<String> pairs = watchlistService.getUsdtPairs(page, size);
        String result = String.join(",", pairs);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/pairs/count")
    public ResponseEntity<Integer> getUsdtPairsCount() {
        int count = watchlistService.getUsdtPairsCount();
        return ResponseEntity.ok(count);
    }
}