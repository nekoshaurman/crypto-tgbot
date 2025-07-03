package neko.crypto.scrapper.model;

import lombok.Data;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import java.io.Serializable;

@Entity
@Data
@IdClass(Watchlist.WatchlistId.class)
public class Watchlist {

    @Id
    private Long chatId;

    @Id
    private String ticker;

    public static class WatchlistId implements Serializable {
        private Long chatId;
        private String ticker;

        // Required for Hibernate
        public WatchlistId() {}

        public WatchlistId(Long chatId, String ticker) {
            this.chatId = chatId;
            this.ticker = ticker;
        }

        // Equals and hashCode for composite key
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            WatchlistId that = (WatchlistId) o;
            return chatId.equals(that.chatId) && ticker.equals(that.ticker);
        }

        @Override
        public int hashCode() {
            return 31 * chatId.hashCode() + ticker.hashCode();
        }
    }
}