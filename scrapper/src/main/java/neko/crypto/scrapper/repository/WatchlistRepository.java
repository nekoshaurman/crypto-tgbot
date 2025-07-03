package neko.crypto.scrapper.repository;

import neko.crypto.scrapper.model.Watchlist;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Set;

public interface WatchlistRepository extends JpaRepository<Watchlist, Watchlist.WatchlistId> {
    Set<Watchlist> findByChatId(Long chatId);
    boolean existsByChatIdAndTicker(Long chatId, String ticker);
    void deleteByChatIdAndTicker(Long chatId, String ticker);
}