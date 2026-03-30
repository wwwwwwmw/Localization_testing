package org.example.strategies;

import java.util.List;
import java.util.stream.Stream;

/**
 * Factory / Provider cung cấp ILocaleStrategy cho JUnit 5 Data Provider.
 *
 * Sử dụng:
 * @MethodSource("org.example.strategies.LocaleStrategyProvider#allStrategies")
 * @EnumSource(LocaleStrategyProvider.SupportedLocale.class)
 */
public final class LocaleStrategyProvider {

    private LocaleStrategyProvider() {
    }

    /**
     * Enum liệt kê các locale được hỗ trợ.
     * Dùng với @EnumSource trong JUnit 5.
     */
    public enum SupportedLocale {
        ENGLISH("en", new EnglishStrategy()),
        FRENCH("fr", new FrenchStrategy()),
        VIETNAMESE("vi", new VietnameseStrategy()),
        GERMAN("de", new GermanStrategy()),
        JAPANESE("ja", new JapaneseStrategy()),
        RUSSIAN("ru", new RussianStrategy()),
        ARABIC("ar", new ArabicStrategy());

        private final String code;
        private final ILocaleStrategy strategy;

        SupportedLocale(String code, ILocaleStrategy strategy) {
            this.code = code;
            this.strategy = strategy;
        }

        public String getCode() {
            return code;
        }

        public ILocaleStrategy getStrategy() {
            return strategy;
        }

        @Override
        public String toString() {
            return strategy.getLanguageName() + " (" + code + ")";
        }
    }

    // ==================== STREAM PROVIDERS ====================

    /** Stream tất cả strategy - dùng cho @MethodSource */
    public static Stream<ILocaleStrategy> allStrategies() {
        return Stream.of(
                new EnglishStrategy(),
                new FrenchStrategy(),
                new VietnameseStrategy(),
                new GermanStrategy(),
                new JapaneseStrategy(),
                new RussianStrategy(),
                new ArabicStrategy());
    }

    /** Stream tất cả SupportedLocale enum */
    public static Stream<SupportedLocale> allLocales() {
        return Stream.of(SupportedLocale.values());
    }

    // ==================== LOOKUP ====================

    /** Lấy strategy theo mã ngôn ngữ */
    public static ILocaleStrategy getStrategy(String languageCode) {
        for (SupportedLocale locale : SupportedLocale.values()) {
            if (locale.getCode().equals(languageCode)) {
                return locale.getStrategy();
            }
        }
        throw new IllegalArgumentException("Unsupported language code: " + languageCode);
    }

    /** Kiểm tra ngôn ngữ có được hỗ trợ không */
    public static boolean isSupported(String languageCode) {
        for (SupportedLocale locale : SupportedLocale.values()) {
            if (locale.getCode().equals(languageCode))
                return true;
        }
        return false;
    }

    /** Lấy danh sách tất cả strategy */
    public static List<ILocaleStrategy> getAll() {
        return List.of(
                new EnglishStrategy(),
                new FrenchStrategy(),
                new VietnameseStrategy(),
                new GermanStrategy(),
                new JapaneseStrategy(),
                new RussianStrategy(),
                new ArabicStrategy());
    }
}
