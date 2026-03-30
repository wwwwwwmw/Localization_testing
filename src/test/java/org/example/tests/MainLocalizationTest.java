package org.example.tests;

import org.example.core.L10nError;
import org.example.strategies.ILocaleStrategy;
import org.example.strategies.LocaleStrategyProvider.SupportedLocale;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * MainLocalizationTest — 6 chức năng kiểm tra L10n chính.
 *
 * <ol>
 * <li>💰 Currency Format — kiểm tra tiền tệ cho từng ngôn ngữ</li>
 * <li>📝 Untranslated Text — phát hiện đầy đủ text tiếng Anh chưa dịch</li>
 * <li>📅 Date Format — kiểm tra định dạng ngày tháng</li>
 * <li>↔️ Layout Direction — kiểm tra RTL/LTR</li>
 * <li>📦 Text Overflow — kiểm tra tràn text</li>
 * <li>🔤 Charset — kiểm tra charset/encoding và ký tự locale</li>
 * </ol>
 *
 * Mỗi test được parameterize qua 7 ngôn ngữ: EN, FR, VI, DE, JA, RU, AR.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("🌍 Localization Test Suite — Shop Under Test")
public class MainLocalizationTest extends BaseTest {

    /** Theo dõi ngôn ngữ hiện tại để tránh switch lại không cần thiết */
    private String currentLanguage = null;

    /**
     * Switch ngôn ngữ nếu cần.
     * Nếu KHÔNG switch được (và không phải EN) thì SKIP test.
     */
    private void ensureLanguage(ILocaleStrategy strategy) {
        String code = strategy.getLanguageCode();
        String actualUiCode = page.getCurrentLanguageCode();
        boolean needSwitch = !code.equals(currentLanguage) || !code.equals(actualUiCode);
        if (needSwitch) {
            boolean success = page.switchLanguage(code);
            assertTrue(success,
                    "Không thể chuyển sang ngôn ngữ " + strategy.getLanguageName() + " (" + code + ")");
            String verifiedUiCode = page.getCurrentLanguageCode();
            assertTrue(code.equals(verifiedUiCode),
                    "UI đang ở ngôn ngữ " + verifiedUiCode + " thay vì " + code);
            currentLanguage = code;
        }
    }

    // ==================== TEST 1: CURRENCY FORMAT ====================

    @Order(1)
    @DisplayName("💰 Currency Format")
    @ParameterizedTest(name = "[{0}] Currency Format")
    @EnumSource(SupportedLocale.class)
    void test01_CurrencyFormat(SupportedLocale locale) {
        ILocaleStrategy strategy = locale.getStrategy();
        ensureLanguage(strategy);

        List<L10nError> errors = validator.validateCurrency(page, strategy);

        // Phải tìm thấy giá + tất cả giá phải đúng format
        assertNoErrors(errors, "Currency Format — " + strategy.getLanguageName());
    }

    // ==================== TEST 2: UNTRANSLATED TEXT ====================

    @Order(2)
    @DisplayName("📝 Untranslated Text")
    @ParameterizedTest(name = "[{0}] Untranslated Text")
    @EnumSource(SupportedLocale.class)
    void test02_UntranslatedText(SupportedLocale locale) {
        ILocaleStrategy strategy = locale.getStrategy();

        // Tiếng Anh là ngôn ngữ gốc → bỏ qua
        Assumptions.assumeFalse("en".equals(strategy.getLanguageCode()),
                "Bỏ qua kiểm tra dịch sót cho tiếng Anh (ngôn ngữ gốc)");

        ensureLanguage(strategy);

        List<L10nError> errors = validator.validateUntranslatedText(page, strategy);

        if (!errors.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            sb.append("Phát hiện text tiếng Anh chưa dịch cho ")
                    .append(strategy.getLanguageName()).append(":\n");
            for (L10nError e : errors) {
                sb.append("  • ").append(e.getMessage()).append("\n");
            }
            fail(sb.toString());
        }
    }

    // ==================== TEST 3: DATE FORMAT ====================

    @Order(3)
    @DisplayName("📅 Date Format")
    @ParameterizedTest(name = "[{0}] Date Format")
    @EnumSource(SupportedLocale.class)
    void test03_DateFormat(SupportedLocale locale) {
        ILocaleStrategy strategy = locale.getStrategy();
        ensureLanguage(strategy);

        List<L10nError> errors = validator.validateDateFormat(page, strategy);

        // null = không tìm thấy dữ liệu ngày tháng trên trang → SKIP (ignore)
        Assumptions.assumeTrue(errors != null,
                "Không tìm thấy ngày tháng trên trang — bỏ qua kiểm tra");

        assertNoErrors(errors, "Date Format — " + strategy.getLanguageName());
    }

    // ==================== TEST 4: LAYOUT DIRECTION ====================

    @Order(4)
    @DisplayName("↔️ Layout Direction")
    @ParameterizedTest(name = "[{0}] Layout Direction")
    @EnumSource(SupportedLocale.class)
    void test04_LayoutDirection(SupportedLocale locale) {
        ILocaleStrategy strategy = locale.getStrategy();
        ensureLanguage(strategy);

        List<L10nError> errors = validator.validateLayoutDirection(page, strategy);

        assertNoErrors(errors, "Layout Direction — " + strategy.getLanguageName());
    }

    // ==================== TEST 5: TEXT OVERFLOW ====================

    @Order(5)
    @DisplayName("📦 Text Overflow")
    @ParameterizedTest(name = "[{0}] Text Overflow")
    @EnumSource(SupportedLocale.class)
    void test05_TextOverflow(SupportedLocale locale) {
        ILocaleStrategy strategy = locale.getStrategy();
        ensureLanguage(strategy);

        List<L10nError> errors = validator.validateTextOverflow(page, strategy);

        assertNoErrors(errors, "Text Overflow — " + strategy.getLanguageName());
    }

    // ==================== TEST 6: CHARSET ====================

    @Order(6)
    @DisplayName("🔤 Charset")
    @ParameterizedTest(name = "[{0}] Charset")
    @EnumSource(SupportedLocale.class)
    void test06_Charset(SupportedLocale locale) {
        ILocaleStrategy strategy = locale.getStrategy();
        ensureLanguage(strategy);

        List<L10nError> errors = validator.validateCharset(page, strategy);

        assertNoErrors(errors, "Charset — " + strategy.getLanguageName());
    }

    // ==================== TEST 7: NUMBER & MEASUREMENT ====================

    @Order(7)
    @DisplayName("🔢 Number & Measurement Format")
    @ParameterizedTest(name = "[{0}] Number & Measurement")
    @EnumSource(SupportedLocale.class)
    void test07_NumberAndMeasurementFormat(SupportedLocale locale) {
        ILocaleStrategy strategy = locale.getStrategy();
        ensureLanguage(strategy);

        List<L10nError> errors = validator.validateNumberAndMeasurementFormat(page, strategy);
        assertNoErrors(errors, "Number & Measurement — " + strategy.getLanguageName());
    }

    // ==================== TEST 8: MEDIA LOCALIZATION ====================

    @Order(8)
    @DisplayName("🖼 Media & Alt Text Localization")
    @ParameterizedTest(name = "[{0}] Media & Alt Text")
    @EnumSource(SupportedLocale.class)
    void test08_MediaAndAltText(SupportedLocale locale) {
        ILocaleStrategy strategy = locale.getStrategy();
        ensureLanguage(strategy);

        List<L10nError> errors = validator.validateMediaLocalization(page, strategy);
        assertNoErrors(errors, "Media & Alt Text — " + strategy.getLanguageName());
    }

    // ==================== TEST 9: URL LOCALIZATION ====================

    @Order(9)
    @DisplayName("🔗 URL & Routing Localization")
    @ParameterizedTest(name = "[{0}] URL Routing")
    @EnumSource(SupportedLocale.class)
    void test09_UrlRoutingLocalization(SupportedLocale locale) {
        ILocaleStrategy strategy = locale.getStrategy();
        ensureLanguage(strategy);

        List<L10nError> errors = validator.validateLocalizedUrls(page, strategy);
        assertNoErrors(errors, "URL & Routing — " + strategy.getLanguageName());
    }
}
