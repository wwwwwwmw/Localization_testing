package org.example.strategy;

import java.util.List;
import java.util.stream.Stream;

/**
 * Factory/Provider để cung cấp các LocaleStrategy cho JUnit 5 @MethodSource
 * 
 * Sử dụng:
 * - @MethodSource("org.example.strategy.LocaleStrategyProvider#getAllStrategies")
 * - @EnumSource(LocaleStrategyProvider.SupportedLocale.class)
 */
public final class LocaleStrategyProvider {

    private LocaleStrategyProvider() {
        // Utility class - không cho phép tạo instance
    }

    /**
     * Enum liệt kê các locale được hỗ trợ
     * Sử dụng với @EnumSource trong JUnit 5
     */
    public enum SupportedLocale {
        ENGLISH("en", new EnglishStrategy()),
        FRENCH("fr", new FrenchStrategy()),
        VIETNAMESE("vi", new VietnameseStrategy()),
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

    /**
     * Cung cấp stream các strategy cho @MethodSource
     */
    public static Stream<ILocaleStrategy> getAllStrategies() {
        return Stream.of(
                new EnglishStrategy(),
                new FrenchStrategy(),
                new VietnameseStrategy(),
                new ArabicStrategy());
    }

    /**
     * Cung cấp stream các enum SupportedLocale cho @MethodSource
     */
    public static Stream<SupportedLocale> getAllLocales() {
        return Stream.of(SupportedLocale.values());
    }

    /**
     * Lấy strategy theo mã ngôn ngữ
     */
    public static ILocaleStrategy getStrategy(String languageCode) {
        for (SupportedLocale locale : SupportedLocale.values()) {
            if (locale.getCode().equals(languageCode)) {
                return locale.getStrategy();
            }
        }
        throw new IllegalArgumentException("Unsupported language code: " + languageCode);
    }

    /**
     * Lấy danh sách tất cả các strategy
     */
    public static List<ILocaleStrategy> getStrategiesList() {
        return List.of(
                new EnglishStrategy(),
                new FrenchStrategy(),
                new VietnameseStrategy(),
                new ArabicStrategy());
    }

    /**
     * Kiểm tra xem ngôn ngữ có được hỗ trợ không
     */
    public static boolean isSupported(String languageCode) {
        for (SupportedLocale locale : SupportedLocale.values()) {
            if (locale.getCode().equals(languageCode)) {
                return true;
            }
        }
        return false;
    }
}
