package com.magicworld.tfg_angular_springboot.statistics.service;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class CurrencyConverter {

    private static final BigDecimal EUR_TO_USD = new BigDecimal("1.08");

    private CurrencyConverter() {}

    public static BigDecimal convert(BigDecimal amountEur, String locale) {
        if (isEnglishLocale(locale)) {
            return amountEur.multiply(EUR_TO_USD).setScale(2, RoundingMode.HALF_UP);
        }
        return amountEur.setScale(2, RoundingMode.HALF_UP);
    }

    public static String getCurrency(String locale) {
        return isEnglishLocale(locale) ? "USD" : "EUR";
    }

    private static boolean isEnglishLocale(String locale) {
        return locale != null && locale.toLowerCase().startsWith("en");
    }
}

