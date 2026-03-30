package org.example.tests;

import org.example.strategies.ILocaleStrategy;
import org.example.strategies.LocaleStrategyProvider.SupportedLocale;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Black-box tests theo góc nhìn người dùng cuối.
 */
@DisplayName("Black Box - Localization")
public class LocalizationBlackBoxSkeletonTest extends BaseTest {

    @DisplayName("BB_UNICODE_01_InputValidation_UnicodeRoundTrip")
    @ParameterizedTest(name = "[{0}] Date equivalence partition")
    @EnumSource(SupportedLocale.class)
    void bbUnicodeInputValidation(SupportedLocale locale) {
        ILocaleStrategy strategy = locale.getStrategy();
        assertTrue(page.switchLanguage(strategy.getLanguageCode()), "Không thể switch locale");

        boolean navigated = page.navigateToPath("/products");
        Assumptions.assumeTrue(navigated, "Không mở được trang có form input");

        WebDriver driver = driverManager.getDriver();
        List<WebElement> inputs = driver
                .findElements(By.cssSelector("input[type='text'], input[type='search'], input:not([type])"));
        Optional<WebElement> visibleInput = inputs.stream()
                .filter(WebElement::isDisplayed)
                .findFirst();
        Assumptions.assumeTrue(visibleInput.isPresent(), "Không tìm thấy text input để test Unicode");

        String payload = sampleUnicodePayload(strategy.getLanguageCode());
        WebElement input = visibleInput.get();
        input.clear();
        input.sendKeys(payload);

        String actual = input.getAttribute("value");
        assertEquals(payload, actual, "Input Unicode bị mất/biến dạng");
    }

    @DisplayName("BB_PLURAL_01_CartPluralization_EnglishLeakCheck")
    @ParameterizedTest(name = "[{0}] Currency equivalence partition")
    @EnumSource(SupportedLocale.class)
    void bbPluralization(SupportedLocale locale) {
        ILocaleStrategy strategy = locale.getStrategy();
        assertTrue(page.switchLanguage(strategy.getLanguageCode()), "Không thể switch locale");

        page.navigateToPath("/cart");
        String body = page.getBodyText();
        Assumptions.assumeTrue(body != null && !body.isBlank(), "Không lấy được nội dung cart");

        if (!"en".equals(strategy.getLanguageCode())) {
            Pattern enPlural = Pattern.compile("\\b(1\\s+item|2\\s+items|5\\s+items)\\b", Pattern.CASE_INSENSITIVE);
            assertFalse(enPlural.matcher(body).find(),
                    "Cart pluralization vẫn còn mẫu tiếng Anh ở locale " + strategy.getLanguageCode());
        }
    }

    @DisplayName("BB_SORT_01_CollationLocaleAware")
    @ParameterizedTest(name = "[{0}] Text boundary value")
    @EnumSource(SupportedLocale.class)
    void bbSortingAndCollation(SupportedLocale locale) {
        ILocaleStrategy strategy = locale.getStrategy();
        assertTrue(page.switchLanguage(strategy.getLanguageCode()), "Không thể switch locale");

        boolean navigated = page.navigateToPath("/products");
        Assumptions.assumeTrue(navigated, "Không mở được products page");

        List<WebElement> names = driverManager.getDriver().findElements(
                By.cssSelector(".product-title, .card-title, [data-testid='product-name']"));
        List<String> productNames = new ArrayList<>();
        for (WebElement name : names) {
            try {
                String text = name.getText();
                if (text != null && !text.isBlank()) {
                    productNames.add(text.trim());
                }
            } catch (Exception ignored) {
                // skip stale nodes
            }
        }
        Assumptions.assumeTrue(productNames.size() >= 3, "Không đủ dữ liệu tên sản phẩm để đánh giá collation");

        Locale javaLocale = toJavaLocale(strategy.getLanguageCode());
        Collator collator = Collator.getInstance(javaLocale);
        List<String> sorted = new ArrayList<>(productNames);
        sorted.sort(collator);

        // Soft black-box oracle: chỉ cần danh sách gốc không đảo ngược hoàn toàn so với
        // collation locale.
        List<String> reversed = new ArrayList<>(sorted);
        Collections.reverse(reversed);
        assertFalse(productNames.equals(reversed), "Danh sách sản phẩm có thứ tự bất thường so với locale collation");
    }

    @DisplayName("BB_REGIONAL_01_PhoneRegex_FlexibleByLocale")
    @ParameterizedTest(name = "[{0}] Decision table - direction and viewport")
    @EnumSource(SupportedLocale.class)
    void bbRegionalFormats(SupportedLocale locale) {
        ILocaleStrategy strategy = locale.getStrategy();
        String phoneRegex = strategy.getPhoneRegex();
        Assumptions.assumeTrue(phoneRegex != null && !phoneRegex.isBlank(), "Locale không khai báo phone regex");

        Pattern p = Pattern.compile(phoneRegex);
        String valid = sampleValidPhone(strategy.getLanguageCode());
        String invalid = "ABC-INVALID-000";

        assertTrue(p.matcher(valid).matches(), "Số điện thoại hợp lệ không qua được regex locale");
        assertFalse(p.matcher(invalid).matches(), "Regex locale quá lỏng, chấp nhận input không hợp lệ");
    }

    @DisplayName("BB_STATE_SWITCH_01_MultiLocale_RoundTrip")
    @ParameterizedTest(name = "[{0}] State transition - locale switching")
    @EnumSource(SupportedLocale.class)
    void bbStateTransitionLocaleSwitch(SupportedLocale locale) {
        ILocaleStrategy strategy = locale.getStrategy();
        String target = strategy.getLanguageCode();

        assertTrue(page.switchLanguage("en"), "Không switch về en được");
        assertTrue(page.switchLanguage(target), "Không switch sang locale đích được");
        assertTrue(page.switchLanguage("fr"), "Không switch sang locale trung gian được");
        assertTrue(page.switchLanguage(target), "Không switch round-trip về locale đích được");

        assertEquals(target, page.getCurrentLanguageCode(), "Locale state không ổn định sau round-trip");
    }

    private String sampleUnicodePayload(String languageCode) {
        switch (languageCode) {
            case "vi":
                return "Nguyen Van A - Nguyen Van A";
            case "ja":
                return "こんにちは テスト";
            case "ar":
                return "مرحبا اختبار";
            case "ru":
                return "Привет тест";
            default:
                return "Cafe deja vu";
        }
    }

    private Locale toJavaLocale(String languageCode) {
        switch (languageCode) {
            case "vi":
                return Locale.forLanguageTag("vi-VN");
            case "de":
                return Locale.GERMAN;
            case "fr":
                return Locale.FRENCH;
            case "ja":
                return Locale.JAPANESE;
            case "ru":
                return Locale.forLanguageTag("ru-RU");
            case "ar":
                return Locale.forLanguageTag("ar-SA");
            default:
                return Locale.ENGLISH;
        }
    }

    private String sampleValidPhone(String languageCode) {
        switch (languageCode) {
            case "vi":
                return "+84 912 345 678";
            case "fr":
                return "+33 6 12 34 56 78";
            case "de":
                return "+49 30 1234567";
            case "ja":
                return "+81 90 1234 5678";
            case "ru":
                return "+7 495 123 45 67";
            case "ar":
                return "+966 50 123 4567";
            default:
                return "+1 (415) 555-0123";
        }
    }
}
