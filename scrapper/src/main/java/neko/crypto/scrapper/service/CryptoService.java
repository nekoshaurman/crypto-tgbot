package neko.crypto.scrapper.service;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class CryptoService {
    private final OkHttpClient client;

    @Cacheable(value = "cryptoPrices", key = "#ticker")
    public String getPrice(String ticker) {
        String url = "https://api.coingecko.com/api/v3/simple/price?ids=" + ticker + "&vs_currencies=usd";
        Request request = new Request.Builder().url(url).build();
        try {
            Response response = client.newCall(request).execute();
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected response code: " + response.code());
            }
            JSONObject json = new JSONObject(response.body().string());
            if (json.has(ticker)) {
                return json.getJSONObject(ticker).getDouble("usd") + "";
            } else {
                throw new IOException("Ticker not found: " + ticker);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to fetch price for " + ticker + ": " + e.getMessage(), e);
        }
    }
}