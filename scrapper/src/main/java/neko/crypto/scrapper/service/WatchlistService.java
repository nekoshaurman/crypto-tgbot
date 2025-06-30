package neko.crypto.scrapper.service;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Service
public class WatchlistService {
    private final Map<Long, Set<String>> watchlists = new HashMap<>();

    public void addToWatchlist(Long chatId, String ticker) {
        watchlists.computeIfAbsent(chatId, k -> new HashSet<>()).add(ticker.toLowerCase());
    }

    public boolean removeFromWatchlist(Long chatId, String ticker) {
        Set<String> watchlist = watchlists.get(chatId);
        return watchlist != null && watchlist.remove(ticker.toLowerCase());
    }

    public Set<String> getWatchlist(Long chatId) {
        return watchlists.getOrDefault(chatId, new HashSet<>());
    }
}