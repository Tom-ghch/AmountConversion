package com.study.utils.Amount;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

public class AmountToWordsConverterUtil {

    private static final Map<Integer, String> NUMBER_WORDS = new HashMap<>();
    private static final Map<String, String> CURRENCY_MAP = new HashMap<>();

    static {
        // 初始化数字词汇映射
        NUMBER_WORDS.put(0, "ZERO");
        NUMBER_WORDS.put(1, "ONE");
        NUMBER_WORDS.put(2, "TWO");
        NUMBER_WORDS.put(3, "THREE");
        NUMBER_WORDS.put(4, "FOUR");
        NUMBER_WORDS.put(5, "FIVE");
        NUMBER_WORDS.put(6, "SIX");
        NUMBER_WORDS.put(7, "SEVEN");
        NUMBER_WORDS.put(8, "EIGHT");
        NUMBER_WORDS.put(9, "NINE");
        NUMBER_WORDS.put(10, "TEN");
        NUMBER_WORDS.put(11, "ELEVEN");
        NUMBER_WORDS.put(12, "TWELVE");
        NUMBER_WORDS.put(13, "THIRTEEN");
        NUMBER_WORDS.put(14, "FOURTEEN");
        NUMBER_WORDS.put(15, "FIFTEEN");
        NUMBER_WORDS.put(16, "SIXTEEN");
        NUMBER_WORDS.put(17, "SEVENTEEN");
        NUMBER_WORDS.put(18, "EIGHTEEN");
        NUMBER_WORDS.put(19, "NINETEEN");
        NUMBER_WORDS.put(20, "TWENTY");
        NUMBER_WORDS.put(30, "THIRTY");
        NUMBER_WORDS.put(40, "FORTY");
        NUMBER_WORDS.put(50, "FIFTY");
        NUMBER_WORDS.put(60, "SIXTY");
        NUMBER_WORDS.put(70, "SEVENTY");
        NUMBER_WORDS.put(80, "EIGHTY");
        NUMBER_WORDS.put(90, "NINETY");

        // 初始化币种映射
        CURRENCY_MAP.put("USD", "US DOLLARS");
        CURRENCY_MAP.put("CNY", "CHINESE YUAN");
        CURRENCY_MAP.put("GBP", "POUNDS STERLING");
        CURRENCY_MAP.put("EUR", "EURO");
        CURRENCY_MAP.put("JPY", "JAPANESE YEN");
        CURRENCY_MAP.put("HKD", "HONG KONG DOLLARS");
    }

    public static String convertToWords(String oldAmount, String currencyCode, String decimalMode) {
        BigDecimal processedAmount = getBigDecimal(oldAmount, currencyCode, decimalMode);
        long integerPart = processedAmount.setScale(0, RoundingMode.DOWN).longValueExact();
        int cents = processedAmount.subtract(new BigDecimal(integerPart))
                .multiply(new BigDecimal(100))
                .setScale(0, RoundingMode.HALF_UP)
                .intValueExact();

        String integerWords = convertIntegerPart(integerPart);
        String centsWords = convertCents(cents, decimalMode);
        String currency = CURRENCY_MAP.getOrDefault(currencyCode, "UNKNOWN CURRENCY");

        StringBuilder result = new StringBuilder("SAY ");
        result.append(currency).append(" ").append(integerWords);

        if (cents > 0) {
            result.append(" AND ").append(centsWords);
        }

        result.append(" ONLY");

        return result.toString().trim();
    }

    private static BigDecimal getBigDecimal(String oldAmount, String currencyCode, String decimalMode) {
        if (oldAmount == null || oldAmount.isEmpty()) {
            throw new IllegalArgumentException("Amount cannot be empty.");
        }
        if (oldAmount.contains(",")) {
            oldAmount = oldAmount.replace(",", "");
        }
        if (!oldAmount.matches("^\\d+(\\.\\d{1,2})?$")) {
            throw new IllegalArgumentException("Invalid amount format.");
        }
        if (decimalMode == null || !decimalMode.equalsIgnoreCase("CENTS") && !decimalMode.equalsIgnoreCase("POINT") && !decimalMode.equalsIgnoreCase("FRACTION")) {
            throw new IllegalArgumentException("Invalid decimal mode.");
        }
        if (currencyCode == null || !CURRENCY_MAP.containsKey(currencyCode)) {
            throw new IllegalArgumentException("Invalid currency code.");
        }

        BigDecimal amount = new BigDecimal(oldAmount);
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Amount cannot be negative.");
        }

        // 新增：去除金额字符串中的逗号
        String amountStr = amount.toPlainString().replace(",", "");
        return new BigDecimal(amountStr);
    }

    private static String convertIntegerPart(long number) {
        if (number == 0) {
            return "ZERO";
        }

        StringBuilder result = new StringBuilder();

        // 处理百万级
        if (number >= 1_000_000L) {
            long millions = number / 1_000_000L;
            number %= 1_000_000L;
            result.append(convertSegment((int) millions)).append(" MILLION ");
        }

        // 处理千级
        if (number >= 1_000L) {
            long thousands = number / 1_000L;
            number %= 1_000L;
            result.append(convertSegment((int) thousands)).append(" THOUSAND ");
        }

        // 处理百级
        if (number > 0) {
            result.append(convertSegment((int) number));
        }

        return result.toString().trim();
    }

    private static String convertSegment(int number) {
        if (number == 0) {
            return "";
        }

        StringBuilder result = new StringBuilder();
        int hundreds = number / 100;
        int remainder = number % 100;

        if (hundreds > 0) {
            result.append(NUMBER_WORDS.get(hundreds)).append(" HUNDRED");
            if (remainder > 0) {
                result.append(" AND ");
            }
        }

        if (remainder > 0) {
            result.append(convertLessThanOneHundred(remainder));
        }

        return result.toString().trim();
    }

    private static String convertLessThanOneHundred(int number) {
        if (number < 20) {
            return NUMBER_WORDS.get(number);
        } else {
            int tens = (number / 10) * 10;
            int units = number % 10;
            return units == 0
                    ? NUMBER_WORDS.get(tens)
                    : NUMBER_WORDS.get(tens) + "-" + NUMBER_WORDS.get(units);
        }
    }

    private static String convertCents(int cents, String mode) {
        if (cents == 0) {
            return "";
        }

        String centsInWords = convertLessThanOneHundred(cents);
        return switch (mode.toUpperCase()) {
            case "CENTS" -> "CENTS " + centsInWords;
            case "POINT" -> "POINT " + centsInWords;
            case "FRACTION" -> centsInWords + " " + String.format("%02d/100", cents);
            default -> throw new IllegalArgumentException("Invalid decimal mode: " + mode);
        };
    }

    public static void main(String[] args) {
        // 示例测试
        String result = convertToWords("112,222,111.01", "USD", "CENTS");
        System.out.println(result);
        // 输出：SAY US DOLLARS TWO MILLION TWO HUNDRED TWENTY-TWO THOUSAND ONE HUNDRED AND ELEVEN AND CENTS ELEVEN ONLY

        result = convertToWords("888.88", "USD", "CENTS");
        System.out.println(result);
        // 输出：SAY US DOLLARS EIGHT HUNDRED AND EIGHTY-EIGHT AND CENTS EIGHTY-EIGHT ONLY
    }
}
