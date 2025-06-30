package neko.crypto.scrapper.client.api;

import okhttp3.OkHttpClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CoinGeckoClient {
    @Bean
    public OkHttpClient okHttpClient() {
        return new OkHttpClient();
    }
}