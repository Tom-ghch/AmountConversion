package com.study.utils.Amount;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FinancialAmountConverterUtils {
    // 常量定义
    private static final String CLEAN_PATTERN_STR = "(?i)\\b(SAY|US|DOLLARS|ONLY)\\b|\\s+AND\\s+";
    private static final String WORD_PATTERN_STR = "(?i)\\b(?:[A-Z]+(?:-[A-Z]+)*)\\b|CENTS|CENT|POINT";
    private static final String POINT = "POINT";
    private static final String CENTS = "CENTS";
    private static final String CENT = "CENT";

    // 数字词库
    private static final Map<String, Integer> NUMBER_WORDS = new HashMap<>() {{
        put("ZERO", 0); put("ONE", 1); put("TWO", 2); put("THREE", 3);
        put("FOUR", 4); put("FIVE", 5); put("SIX", 6); put("SEVEN", 7);
        put("EIGHT", 8); put("NINE", 9); put("TEN", 10); put("ELEVEN", 11);
        put("TWELVE", 12); put("THIRTEEN", 13); put("FOURTEEN", 14);
        put("FIFTEEN", 15); put("SIXTEEN", 16); put("SEVENTEEN", 17);
        put("EIGHTEEN", 18); put("NINETEEN", 19); put("TWENTY", 20);
        put("THIRTY", 30); put("FORTY", 40); put("FIFTY", 50);
        put("SIXTY", 60); put("SEVENTY", 70); put("EIGHTY", 80);
        put("NINETY", 90);
    }};
    // 单位体系（必须按从大到小顺序）
    private static final Map<String, Long> UNIT_SCALES = new LinkedHashMap<>() {{
        put("BILLION", 1_000_000_000L);
        put("MILLION", 1_000_000L);
        put("THOUSAND", 1_000L);
        put("HUNDRED", 100L);
    }};
    // 正则表达式
    private static final Pattern WORD_PATTERN = Pattern.compile(WORD_PATTERN_STR);
    private static final Pattern CLEAN_PATTERN = Pattern.compile(CLEAN_PATTERN_STR);

    public static String convert(String amountInWords) {
        ConversionContext context = parseInput(amountInWords);
        return formatResult(context);
    }
    private static ConversionContext parseInput(String input) {
        // 清理无效词汇并分词
        String cleaned = CLEAN_PATTERN.matcher(input.toUpperCase()).replaceAll(" ");
        Matcher matcher = WORD_PATTERN.matcher(cleaned);

        ConversionContext ctx = new ConversionContext();
        while (matcher.find()) {
            String token = matcher.group().toUpperCase();

            if (UNIT_SCALES.containsKey(token)) {
                handleUnit(ctx, token);
            } else if (token.contains("-")) {
                handleCompoundNumber(ctx, token);
            } else if (NUMBER_WORDS.containsKey(token)) {
                ctx.appendNumber(NUMBER_WORDS.get(token));
            } else if (token.equals(POINT) || token.equals(CENTS) || token.equals(CENT)) {
                ctx.switchToDecimalMode(token);
            } else {
                throw new AmountFormatException("Invalid token: " + token);
            }
        }
        ctx.flush(); // 确保最后的数据被提交
        return ctx;
    }

    private static void handleUnit(ConversionContext ctx, String unit) {
        long scale = UNIT_SCALES.get(unit);
        ctx.applyUnit(scale);
    }

    private static void handleCompoundNumber(ConversionContext ctx, String word) {
        int sum = Arrays.stream(word.split("-"))
                .mapToInt(p -> NUMBER_WORDS.getOrDefault(p, 0))
                .sum();
        ctx.appendNumber(sum);
    }

    private static String formatResult(ConversionContext ctx) {
        return String.format("%,.2f", ctx.getTotalAmount());
    }

    private static class ConversionContext {
        private long totalInteger;
        private long currentGroup;
        private long currentValue;
        private BigDecimal decimalPart = BigDecimal.ZERO;
        private boolean isDecimalMode;
        private int decimalScale = 2;

        void appendNumber(int value) {
            if (isDecimalMode) {
                handleDecimalNumber(value);
            } else {
                currentValue += value;
            }
        }

        void applyUnit(long unit) {
            if (unit >= 1000) {
                currentGroup += currentValue * unit;
                currentValue = 0;
                // 遇到更大单位时提交分组
                if (shouldFlushGroup(unit)) {
                    totalInteger += currentGroup;
                    currentGroup = 0;
                }
            } else {
                currentValue *= unit;
            }
        }

        void switchToDecimalMode(String type) {
            totalInteger += currentGroup + currentValue;
            currentGroup = currentValue = 0;
            isDecimalMode = true;
            if (POINT.equals(type)) decimalScale = 1;
        }

        void flush() {
            totalInteger += currentGroup + currentValue;
            currentGroup = currentValue = 0;
        }

        BigDecimal getTotalAmount() {
            return new BigDecimal(totalInteger)
                    .add(decimalPart)
                    .setScale(2, RoundingMode.HALF_UP);
        }

        private boolean shouldFlushGroup(long currentUnit) {
            List<Long> units = new ArrayList<>(UNIT_SCALES.values());
            units.sort(Collections.reverseOrder());
            return units.indexOf(currentUnit) == 0; // 最大单位时提交
        }

        private void handleDecimalNumber(int value) {
            decimalPart = decimalPart.add(
                    BigDecimal.valueOf(value)
                            .movePointLeft(decimalScale)
            );
//            decimalScale++;
        }
    }

    public static class AmountFormatException extends RuntimeException {
        public AmountFormatException(String message) {
            super(message);
        }
    }

    public static void main(String[] args) {
        String input = "SAY US DOLLARS EIGHT MILLION SEVEN HUNDRED AND THIRTEEN THOUSAND THREE HUNDRED AND TWELVE AND CENT ONE ONLY";
        System.out.println(convert(input)); // 输出：872,392.92
    }
}
