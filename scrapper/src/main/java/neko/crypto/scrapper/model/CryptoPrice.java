package neko.crypto.scrapper.model;

import lombok.Data;

@Data
public class CryptoPrice {
    private String ticker;
    private Double price;
}