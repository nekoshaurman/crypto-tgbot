package neko.crypto.scrapper.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Service
@Slf4j
public class WatchlistService {
    private final Map<Long, Set<String>> watchlists = new HashMap<>();

    public void addToWatchlist(Long chatId, String ticker) {
        log.info("Adding ticker {} to watchlist for chatId: {}", ticker, chatId);
        watchlists.computeIfAbsent(chatId, k -> {
            log.info("Creating new watchlist for chatId: {}", chatId);
            return new HashSet<>();
        }).add(ticker.toLowerCase());
        log.info("Ticker {} added to watchlist for chatId: {}", ticker, chatId);
    }

    public boolean removeFromWatchlist(Long chatId, String ticker) {
        log.info("Removing ticker {} from watchlist for chatId: {}", ticker, chatId);
        Set<String> watchlist = watchlists.get(chatId);
        boolean removed = watchlist != null && watchlist.remove(ticker.toLowerCase());
        log.info("Ticker {} removal from watchlist for chatId: {}, success: {}", ticker, chatId, removed);
        return removed;
    }

    public Set<String> getWatchlist(Long chatId) {
        log.info("Fetching watchlist for chatId: {}", chatId);
        Set<String> watchlist = watchlists.getOrDefault(chatId, new HashSet<>());
        log.info("Watchlist for chatId {}: {}", chatId, watchlist);
        return watchlist;
    }
}