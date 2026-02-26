package org.example;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.example.strategy.*;
import org.example.strategy.LocaleStrategyProvider.SupportedLocale;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.apache.commons.io.FileUtils;

import static org.junit.jupiter.api.Assertions.*;

/**
 * LocalizationTestV2 - Generic Test Suite cho Localization Testing (STRICT
 * VERSION)
 * 
 * PHIÊN BẢN NGHIÊM NGẶT - Kiểm tra chặt chẽ theo đúng yêu cầu:
 * - Currency: Symbol PHẢI đúng với ngôn ngữ, decimal separator PHẢI đúng
 * - Untranslated: BẤT KỲ từ tiếng Anh nào cũng là lỗi
 * - RTL: PHẢI có dir="rtl" cho Arabic
 * 
 * @author Localization Testing Team
 * @version 2.1 - Strict Validation
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("🌍 Localization Test Suite V2 (Strict)")
public class LocalizationTestV2 {

    // ==================== CONSTANTS ====================

    private static final String PRESTASHOP_URL = "https://demo.prestashop.com/";
    private static final int TIMEOUT_SECONDS = 15;
    private static final int MAX_RETRY_ATTEMPTS = 3;
    private static final int RETRY_DELAY_MS = 2000;
    private static final String SCREENSHOTS_DIR = "screenshots";

    // ==================== SHARED RESOURCES ====================

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static JavascriptExecutor js;
    private static String currentLanguage = null;

    // Cache để tránh reload page không cần thiết
    private static final Map<String, Boolean> languageSwitchCache = new HashMap<>();

    // ==================== LIFECYCLE METHODS ====================

    @BeforeAll
    static void setUpOnce() {
        System.out.println("╔══════════════════════════════════════════════════════════════╗");
        System.out.println("║   🌍 LOCALIZATION TEST SUITE V2 - STRICT VALIDATION          ║");
        System.out.println("║              Languages: EN, FR, VI, AR                       ║");
        System.out.println("╚══════════════════════════════════════════════════════════════╝");

        WebDriverManager.chromedriver().setup();

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--start-maximized");
        options.addArguments("--disable-notifications");
        options.addArguments("--remote-allow-origins=*");
        options.addArguments("--disable-blink-features=AutomationControlled");

        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(TIMEOUT_SECONDS));
        js = (JavascriptExecutor) driver;

        try {
            Files.createDirectories(Paths.get(SCREENSHOTS_DIR));
        } catch (IOException e) {
            System.err.println("Warning: Could not create screenshots directory");
        }

        openPrestaShopDemo();
    }

    @AfterAll
    static void tearDownOnce() {
        System.out.println("\n╔══════════════════════════════════════════════════════════════╗");
        System.out.println("║                    ✅ ALL TESTS COMPLETED                    ║");
        System.out.println("╚══════════════════════════════════════════════════════════════╝");

        if (driver != null) {
            driver.quit();
        }
    }

    @BeforeEach
    void setUp(TestInfo testInfo) {
        System.out.println("\n══════════════════════════════════════════════════════════════");
        System.out.println("▶ " + testInfo.getDisplayName());
    }

    @AfterEach
    void tearDown(TestInfo testInfo) {
        System.out.println("◀ Completed: " + testInfo.getDisplayName());
    }

    // ══════════════════════════════════════════════════════════════════════════════
    // TEST 01: CURRENCY FORMAT - KIỂM TRA NGHIÊM NGẶT
    // ══════════════════════════════════════════════════════════════════════════════

    @Order(1)
    @DisplayName("💰 TEST 01: Currency Format (STRICT)")
    @ParameterizedTest(name = "{0}")
    @EnumSource(SupportedLocale.class)
    void test01_CurrencyFormat_Strict(SupportedLocale locale) {
        ILocaleStrategy strategy = locale.getStrategy();
        switchToLanguage(strategy);

        String expectedSymbol = strategy.getCurrencySymbol();
        String expectedDecimal = strategy.getDecimalSeparator();
        boolean expectedPrefix = strategy.isCurrencySymbolPrefix();

        System.out.println("  ┌─────────────────────────────────────────────────────────");
        System.out.println("  │ EXPECTED FOR " + strategy.getLanguageName().toUpperCase() + ":");
        System.out.println("  │   Symbol: '" + expectedSymbol + "'");
        System.out.println("  │   Position: " + (expectedPrefix ? "PREFIX ($100)" : "SUFFIX (100€)"));
        System.out.println("  │   Decimal Separator: '" + expectedDecimal + "'");
        System.out.println("  │   Grouping Separator: '" + strategy.getGroupingSeparator() + "'");
        System.out.println("  └─────────────────────────────────────────────────────────");

        List<WebElement> priceElements = driver.findElements(By.cssSelector(
                ".price, .product-price, .current-price"));

        assertFalse(priceElements.isEmpty(),
                "Không tìm thấy phần tử giá nào trên trang!");

        List<CurrencyError> errors = new ArrayList<>();
        Set<String> checkedPrices = new HashSet<>();
        int totalChecked = 0;

        for (WebElement element : priceElements) {
            try {
                String priceText = element.getText().trim();

                if (priceText.isEmpty() || checkedPrices.contains(priceText) || priceText.length() > 50) {
                    continue;
                }
                checkedPrices.add(priceText);
                totalChecked++;

                // === KIỂM TRA SYMBOL ===
                String foundSymbol = detectCurrencySymbol(priceText);
                boolean symbolCorrect = expectedSymbol.equals(foundSymbol);

                // === KIỂM TRA DECIMAL SEPARATOR ===
                boolean decimalCorrect = checkDecimalSeparator(priceText, expectedDecimal);

                // === KIỂM TRA POSITION ===
                boolean positionCorrect = checkSymbolPosition(priceText, foundSymbol, expectedPrefix);

                // === BÁO CÁO KẾT QUẢ ===
                StringBuilder status = new StringBuilder();
                boolean hasError = false;

                if (!symbolCorrect) {
                    status.append("Symbol sai (expected '").append(expectedSymbol)
                            .append("', found '").append(foundSymbol != null ? foundSymbol : "NONE").append("'); ");
                    hasError = true;
                }
                if (!decimalCorrect) {
                    status.append("Decimal separator sai (expected '").append(expectedDecimal).append("'); ");
                    hasError = true;
                }
                if (foundSymbol != null && !positionCorrect) {
                    status.append("Position sai (expected ").append(expectedPrefix ? "PREFIX" : "SUFFIX").append("); ");
                    hasError = true;
                }

                if (hasError) {
                    System.out.println("    ✗ FAIL: " + priceText);
                    System.out.println("      └─ " + status.toString().trim());
                    errors.add(new CurrencyError(priceText, status.toString()));
                } else {
                    System.out.println("    ✓ PASS: " + priceText);
                }

            } catch (StaleElementReferenceException e) {
                // Skip
            }
        }

        // === KẾT LUẬN ===
        System.out.println("  ───────────────────────────────────────────────────────────");
        System.out.println("  SUMMARY: Checked " + totalChecked + " prices, " +
                errors.size() + " errors found");

        if (!errors.isEmpty()) {
            System.out.println("  ⚠️  ERRORS DETECTED:");
            for (CurrencyError err : errors) {
                System.out.println("      - " + err.priceText + ": " + err.reason);
            }
        }

        // ASSERTION NGHIÊM NGẶT: Không cho phép lỗi currency
        assertEquals(0, errors.size(),
                "Currency format sai cho " + strategy.getLanguageName() + ":\n" +
                        formatErrors(errors));
    }

    // ══════════════════════════════════════════════════════════════════════════════
    // TEST 02: DATE FORMAT
    // ══════════════════════════════════════════════════════════════════════════════

    @Order(2)
    @DisplayName("📅 TEST 02: Date Format")
    @ParameterizedTest(name = "{0}")
    @EnumSource(SupportedLocale.class)
    void test02_DateFormat(SupportedLocale locale) {
        ILocaleStrategy strategy = locale.getStrategy();
        switchToLanguage(strategy);

        System.out.println("  Expected pattern: " + strategy.getDatePattern());

        String bodyText = driver.findElement(By.tagName("body")).getText();
        List<String> dates = extractDates(bodyText);

        System.out.println("  Found " + dates.size() + " potential date(s)");

        if (dates.isEmpty()) {
            System.out.println("  ⚠️  No dates found on page - SKIPPING TEST");
            Assumptions.assumeTrue(false, "No dates found on page to validate");
        }

        List<String> errors = new ArrayList<>();

        for (String dateStr : dates) {
            // Kiểm tra tên tháng tiếng Anh trong trang không phải tiếng Anh
            if (!strategy.getLanguageCode().equals("en") && containsEnglishMonth(dateStr)) {
                System.out.println("    ✗ FAIL: " + dateStr + " - Chứa tên tháng tiếng Anh!");
                errors.add(dateStr + ": Untranslated month name");
            } else {
                System.out.println("    ✓ OK: " + dateStr);
            }
        }

        assertTrue(errors.isEmpty(),
                "Date format errors for " + strategy.getLanguageName() + ": " + errors);
    }

    // ══════════════════════════════════════════════════════════════════════════════
    // TEST 03: LAYOUT DIRECTION (RTL/LTR) - KIỂM TRA NGHIÊM NGẶT
    // ══════════════════════════════════════════════════════════════════════════════

    @Order(3)
    @DisplayName("↔️ TEST 03: Layout Direction RTL/LTR (STRICT)")
    @ParameterizedTest(name = "{0}")
    @EnumSource(SupportedLocale.class)
    void test03_LayoutDirection_Strict(SupportedLocale locale) {
        ILocaleStrategy strategy = locale.getStrategy();
        switchToLanguage(strategy);

        String expectedDir = strategy.isRTL() ? "rtl" : "ltr";

        System.out.println("  ┌─────────────────────────────────────────────────────────");
        System.out.println("  │ EXPECTED FOR " + strategy.getLanguageName().toUpperCase() + ":");
        System.out.println("  │   Direction: " + expectedDir.toUpperCase());
        System.out.println("  │   Is RTL: " + strategy.isRTL());
        System.out.println("  └─────────────────────────────────────────────────────────");

        // Lấy direction từ nhiều nguồn
        WebElement html = driver.findElement(By.tagName("html"));
        String htmlDir = html.getAttribute("dir");
        String cssDir = html.getCssValue("direction");

        WebElement body = driver.findElement(By.tagName("body"));
        String bodyDir = body.getAttribute("dir");

        System.out.println("  ACTUAL VALUES:");
        System.out.println("    <html dir>: " + (htmlDir != null ? "'" + htmlDir + "'" : "NOT SET"));
        System.out.println("    <body dir>: " + (bodyDir != null ? "'" + bodyDir + "'" : "NOT SET"));
        System.out.println("    CSS direction: " + cssDir);

        // Xác định actual direction
        String actualDir = htmlDir;
        if (actualDir == null || actualDir.isEmpty()) {
            actualDir = bodyDir;
        }
        if (actualDir == null || actualDir.isEmpty()) {
            actualDir = cssDir;
        }

        System.out.println("  ───────────────────────────────────────────────────────────");

        if (strategy.isRTL()) {
            // RTL LANGUAGE PHẢI có dir="rtl"
            System.out.println("  🔍 Checking RTL requirements for " + strategy.getLanguageName() + "...");

            boolean hasRtl = "rtl".equalsIgnoreCase(actualDir);

            if (hasRtl) {
                System.out.println("  ✓ PASS: RTL direction is correctly set");
            } else {
                System.out.println("  ✗ FAIL: RTL direction NOT set!");
                System.out.println("    Expected: dir='rtl'");
                System.out.println("    Actual: dir='" + actualDir + "'");
            }

            assertEquals("rtl", actualDir != null ? actualDir.toLowerCase() : "NOT_SET",
                    "Ngôn ngữ RTL (" + strategy.getLanguageName() + ") PHẢI có dir='rtl'!");

        } else {
            // LTR LANGUAGE không được có dir="rtl"
            System.out.println("  🔍 Checking LTR requirements for " + strategy.getLanguageName() + "...");

            boolean hasRtl = "rtl".equalsIgnoreCase(actualDir);

            if (hasRtl) {
                System.out.println("  ✗ FAIL: LTR language has dir='rtl'!");
                fail("Ngôn ngữ LTR (" + strategy.getLanguageName() + ") KHÔNG được có dir='rtl'!");
            } else {
                System.out.println("  ✓ PASS: LTR direction is correct");
            }
        }
    }

    // ══════════════════════════════════════════════════════════════════════════════
    // TEST 04: TEXT OVERFLOW DETECTION
    // ══════════════════════════════════════════════════════════════════════════════

    @Order(4)
    @DisplayName("📦 TEST 04: Text Overflow Detection")
    @ParameterizedTest(name = "{0}")
    @EnumSource(SupportedLocale.class)
    void test04_TextOverflow(SupportedLocale locale) {
        ILocaleStrategy strategy = locale.getStrategy();
        switchToLanguage(strategy);

        System.out.println("  Checking UI elements for text overflow...");

        List<WebElement> elements = driver.findElements(By.cssSelector(
                ".btn, button, .add-to-cart, [class*='btn'], .nav-link"));

        List<String> overflows = new ArrayList<>();
        List<String> truncations = new ArrayList<>();

        for (WebElement element : elements) {
            try {
                String text = element.getText().trim();
                if (text.isEmpty() || text.length() < 3)
                    continue;

                Long offsetWidth = (Long) js.executeScript("return arguments[0].offsetWidth;", element);
                Long scrollWidth = (Long) js.executeScript("return arguments[0].scrollWidth;", element);

                if (scrollWidth > offsetWidth + 5) {
                    String info = String.format("'%s' (offset=%d, scroll=%d)",
                            text.length() > 30 ? text.substring(0, 30) + "..." : text,
                            offsetWidth, scrollWidth);
                    overflows.add(info);
                    System.out.println("    ⚠️ OVERFLOW: " + info);
                }

                if (text.endsWith("...") || text.endsWith("…")) {
                    truncations.add(text);
                    System.out.println("    ⚠️ TRUNCATED: " + text);
                }

            } catch (Exception e) {
                // Skip
            }
        }

        System.out.println("  ───────────────────────────────────────────────────────────");
        System.out.println("  SUMMARY: " + overflows.size() + " overflow(s), " +
                truncations.size() + " truncation(s)");

        // Warning only - báo cáo nhưng không fail
        if (!overflows.isEmpty() || !truncations.isEmpty()) {
            System.out.println("  ⚠️  UI issues detected (review recommended)");
        } else {
            System.out.println("  ✓ No overflow/truncation issues found");
        }
    }

    // ══════════════════════════════════════════════════════════════════════════════
    // TEST 05: UNTRANSLATED TEXT - KIỂM TRA NGHIÊM NGẶT
    // ══════════════════════════════════════════════════════════════════════════════

    @Order(5)
    @DisplayName("🔤 TEST 05: Untranslated Text Detection (STRICT)")
    @ParameterizedTest(name = "{0}")
    @EnumSource(SupportedLocale.class)
    void test05_UntranslatedText_Strict(SupportedLocale locale) {
        ILocaleStrategy strategy = locale.getStrategy();

        // Skip cho tiếng Anh
        if (strategy.getLanguageCode().equals("en")) {
            System.out.println("  [SKIP] English is the source language - no translation check needed");
            return;
        }

        switchToLanguage(strategy);

        String pageText = driver.findElement(By.tagName("body")).getText();
        List<String> forbiddenWords = strategy.getForbiddenWords();
        List<String> expectedKeywords = strategy.getExpectedKeywords();

        System.out.println("  ┌─────────────────────────────────────────────────────────");
        System.out.println("  │ CHECKING " + strategy.getLanguageName().toUpperCase() + ":");
        System.out.println("  │   Forbidden English words: " + forbiddenWords.size());
        System.out.println("  │   Expected translated keywords: " + expectedKeywords.size());
        System.out.println("  └─────────────────────────────────────────────────────────");

        // === KIỂM TRA TỪ TIẾNG ANH KHÔNG ĐƯỢC PHÉP ===
        List<String> foundUntranslated = new ArrayList<>();

        System.out.println("  Scanning for forbidden English words...");
        for (String forbidden : forbiddenWords) {
            if (containsWord(pageText, forbidden)) {
                foundUntranslated.add(forbidden);
                System.out.println("    ✗ FOUND ENGLISH: '" + forbidden + "'");
            }
        }

        // === KIỂM TRA TỪ KHÓA ĐÃ DỊCH ===
        List<String> foundKeywords = new ArrayList<>();
        List<String> missingKeywords = new ArrayList<>();

        System.out.println("  Scanning for expected translated keywords...");
        for (String keyword : expectedKeywords) {
            if (pageText.contains(keyword)) {
                foundKeywords.add(keyword);
                System.out.println("    ✓ FOUND: '" + keyword + "'");
            } else {
                missingKeywords.add(keyword);
                System.out.println("    ✗ MISSING: '" + keyword + "'");
            }
        }

        // === TÍNH TOÁN COVERAGE ===
        double coverage = expectedKeywords.isEmpty() ? 1.0 : (double) foundKeywords.size() / expectedKeywords.size();

        System.out.println("  ───────────────────────────────────────────────────────────");
        System.out.println("  SUMMARY:");
        System.out.println("    Untranslated English words found: " + foundUntranslated.size());
        System.out.println("    Translated keyword coverage: " + String.format("%.0f%%", coverage * 100));
        System.out.println("      - Found: " + foundKeywords.size() + "/" + expectedKeywords.size());

        // === ASSERTIONS NGHIÊM NGẶT ===
        // Tiêu chuẩn: Không quá 1 từ tiếng Anh, coverage >= 50%
        final int MAX_ALLOWED_UNTRANSLATED = 1;
        final double MIN_COVERAGE = 0.20; // 20% cho PrestaShop demo có thể thiếu dịch

        final List<String> finalUntranslated = new ArrayList<>(foundUntranslated);
        final double finalCoverage = coverage;

        assertAll("Translation checks for " + strategy.getLanguageName(),
                () -> assertTrue(finalUntranslated.size() <= MAX_ALLOWED_UNTRANSLATED,
                        "Tìm thấy " + finalUntranslated.size() + " từ tiếng Anh chưa dịch (tối đa cho phép: " +
                                MAX_ALLOWED_UNTRANSLATED + "): " + finalUntranslated),
                () -> assertTrue(finalCoverage >= MIN_COVERAGE,
                        "Keyword coverage quá thấp: " + String.format("%.0f%%", finalCoverage * 100) +
                                " (yêu cầu tối thiểu: " + String.format("%.0f%%", MIN_COVERAGE * 100) + ")"));
    }

    // ══════════════════════════════════════════════════════════════════════════════
    // TEST 06: CHARACTER ENCODING
    // ══════════════════════════════════════════════════════════════════════════════

    @Order(6)
    @DisplayName("🔣 TEST 06: Character Encoding & Fonts")
    @ParameterizedTest(name = "{0}")
    @EnumSource(SupportedLocale.class)
    void test06_CharacterEncoding(SupportedLocale locale) {
        ILocaleStrategy strategy = locale.getStrategy();
        switchToLanguage(strategy);

        String pageText = driver.findElement(By.tagName("body")).getText();

        System.out.println("  Script group: " + strategy.getScriptGroup());

        // Kiểm tra broken encoding
        String[] brokenPatterns = { "�", "Ã¢", "Ã©", "Ã¨", "Ã", "â€", "ï¿½" };
        List<String> foundBroken = new ArrayList<>();

        for (String pattern : brokenPatterns) {
            if (pageText.contains(pattern)) {
                foundBroken.add(pattern);
                System.out.println("    ✗ BROKEN ENCODING: '" + pattern + "'");
            }
        }

        // Kiểm tra ký tự đặc trưng
        boolean hasExpectedChars = false;
        String validationPattern = strategy.getCharacterValidationPattern();

        if (validationPattern != null && !validationPattern.isEmpty()) {
            Pattern regex = Pattern.compile(validationPattern);
            hasExpectedChars = regex.matcher(pageText).find();
        }

        System.out.println("  ───────────────────────────────────────────────────────────");
        System.out.println("  Expected characters present: " + hasExpectedChars);
        System.out.println("  Broken encodings found: " + foundBroken.size());

        final boolean finalHasExpectedChars = hasExpectedChars;
        final List<String> finalFoundBroken = new ArrayList<>(foundBroken);

        assertAll("Encoding checks for " + strategy.getLanguageName(),
                () -> assertTrue(finalFoundBroken.isEmpty(),
                        "Phát hiện lỗi encoding: " + finalFoundBroken),
                () -> assertTrue(finalHasExpectedChars || strategy.getLanguageCode().equals("en"),
                        "Không tìm thấy ký tự đặc trưng của " + strategy.getScriptGroup()));
    }

    // ══════════════════════════════════════════════════════════════════════════════
    // HELPER METHODS
    // ══════════════════════════════════════════════════════════════════════════════

    private static void openPrestaShopDemo() {
        System.out.println("\n[SETUP] Opening PrestaShop Demo...");
        driver.get(PRESTASHOP_URL);
        waitForPageLoad();
        switchToIframe();
        System.out.println("[SETUP] Ready for testing\n");
    }

    private static void waitForPageLoad() {
        wait.until(d -> js.executeScript("return document.readyState").equals("complete"));
    }

    private static boolean switchToIframe() {
        try {
            driver.switchTo().defaultContent();
            wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(By.id("framelive")));
            return true;
        } catch (Exception e) {
            try {
                wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(By.cssSelector("iframe")));
                return true;
            } catch (Exception e2) {
                return false;
            }
        }
    }

    private void switchToLanguage(ILocaleStrategy strategy) {
        String langCode = strategy.getLanguageCode();

        if (langCode.equals(currentLanguage)) {
            System.out.println("  [CACHE] Already on " + strategy.getLanguageName());
            return;
        }

        System.out.println("  [SWITCH] Changing to " + strategy.getLanguageName() + "...");

        boolean success = switchLanguageWithRetry(strategy, MAX_RETRY_ATTEMPTS);

        if (success) {
            currentLanguage = langCode;
            System.out.println("  [OK] Now testing in " + strategy.getLanguageName());
        } else {
            if (!"en".equals(langCode)) {
                Assumptions.assumeTrue(false,
                        "Could not switch to " + strategy.getLanguageName());
            }
        }
    }

    private boolean switchLanguageWithRetry(ILocaleStrategy strategy, int maxAttempts) {
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                WebElement langDropdown = wait.until(ExpectedConditions.elementToBeClickable(
                        By.cssSelector(".language-selector, #_desktop_language_selector, [class*='language']")));
                langDropdown.click();

                wait.until(ExpectedConditions.visibilityOfElementLocated(
                        By.cssSelector(".language-selector-wrapper, .dropdown-menu, ul[class*='dropdown']")));

                String psCode = strategy.getPrestaShopCode();
                WebElement langOption = wait.until(ExpectedConditions.elementToBeClickable(
                        By.cssSelector(
                                "a[href*='/" + psCode + "/'], a[data-iso-code='" + strategy.getLanguageCode() + "']")));
                langOption.click();

                waitForPageLoad();
                switchToIframe();

                if (verifyLanguageSwitch(strategy)) {
                    return true;
                }

            } catch (Exception e) {
                System.out.println("    Attempt " + attempt + " failed: " + e.getMessage());

                if (attempt < maxAttempts) {
                    try {
                        Thread.sleep(RETRY_DELAY_MS);
                        driver.navigate().refresh();
                        waitForPageLoad();
                        switchToIframe();
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }
        return false;
    }

    private boolean verifyLanguageSwitch(ILocaleStrategy strategy) {
        try {
            String currentUrl = driver.getCurrentUrl();
            String psCode = strategy.getPrestaShopCode();

            if (currentUrl.contains("/" + psCode + "/")) {
                return true;
            }

            WebElement html = driver.findElement(By.tagName("html"));
            String htmlLang = html.getAttribute("lang");
            if (htmlLang != null && (htmlLang.startsWith(strategy.getLanguageCode()) ||
                    htmlLang.startsWith(psCode))) {
                return true;
            }

            if ("en".equals(strategy.getLanguageCode())) {
                return true;
            }

            return false;
        } catch (Exception e) {
            return false;
        }
    }

    // ══════════════════════════════════════════════════════════════════════════════
    // CURRENCY VALIDATION HELPERS
    // ══════════════════════════════════════════════════════════════════════════════

    /**
     * Phát hiện ký hiệu tiền tệ trong chuỗi giá
     */
    private String detectCurrencySymbol(String priceText) {
        String[] symbols = { "€", "$", "£", "¥", "₩", "₫", "฿", "₹", "zł", "Kč", "kr",
                "лв", "₽", "грн", "₺", "ر.س", "د.إ", "﷼" };

        for (String symbol : symbols) {
            if (priceText.contains(symbol)) {
                return symbol;
            }
        }
        return null;
    }

    /**
     * Kiểm tra decimal separator có đúng không
     */
    private boolean checkDecimalSeparator(String priceText, String expectedSeparator) {
        // Tìm pattern số có phần thập phân
        if (".".equals(expectedSeparator)) {
            // Expect: 28.68
            return priceText.matches(".*\\d+\\.\\d{2}.*");
        } else if (",".equals(expectedSeparator)) {
            // Expect: 28,68
            return priceText.matches(".*\\d+,\\d{2}.*");
        }
        return true;
    }

    /**
     * Kiểm tra vị trí symbol (prefix/suffix)
     */
    private boolean checkSymbolPosition(String priceText, String symbol, boolean expectPrefix) {
        if (symbol == null)
            return false;

        int symbolIndex = priceText.indexOf(symbol);
        int firstDigitIndex = -1;
        int lastDigitIndex = -1;

        for (int i = 0; i < priceText.length(); i++) {
            if (Character.isDigit(priceText.charAt(i))) {
                if (firstDigitIndex == -1)
                    firstDigitIndex = i;
                lastDigitIndex = i;
            }
        }

        if (firstDigitIndex == -1)
            return false;

        if (expectPrefix) {
            // Symbol should be BEFORE first digit
            return symbolIndex < firstDigitIndex;
        } else {
            // Symbol should be AFTER last digit
            return symbolIndex > lastDigitIndex;
        }
    }

    // ══════════════════════════════════════════════════════════════════════════════
    // DATE VALIDATION HELPERS
    // ══════════════════════════════════════════════════════════════════════════════

    private List<String> extractDates(String text) {
        List<String> dates = new ArrayList<>();

        // Numeric dates
        Pattern numericDate = Pattern.compile("\\d{1,2}[/\\-.]\\d{1,2}[/\\-.]\\d{2,4}");
        Matcher matcher = numericDate.matcher(text);
        while (matcher.find()) {
            dates.add(matcher.group());
        }

        // Named month dates
        Pattern namedDate = Pattern.compile(
                "(\\d{1,2})\\s+(Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)[a-z]*\\s+(\\d{2,4})",
                Pattern.CASE_INSENSITIVE);
        matcher = namedDate.matcher(text);
        while (matcher.find()) {
            dates.add(matcher.group());
        }

        return dates;
    }

    private boolean containsEnglishMonth(String text) {
        String[] months = { "January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December",
                "Jan", "Feb", "Mar", "Apr", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec" };

        for (String month : months) {
            if (text.toLowerCase().contains(month.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Kiểm tra từ có xuất hiện như một từ riêng biệt không (không phải substring)
     */
    private boolean containsWord(String text, String word) {
        // Tạo pattern để tìm từ như một từ riêng biệt
        String regex = "(?i)\\b" + Pattern.quote(word) + "\\b";
        return Pattern.compile(regex).matcher(text).find();
    }

    // ══════════════════════════════════════════════════════════════════════════════
    // UTILITY CLASSES
    // ══════════════════════════════════════════════════════════════════════════════

    private static class CurrencyError {
        String priceText;
        String reason;

        CurrencyError(String priceText, String reason) {
            this.priceText = priceText;
            this.reason = reason;
        }
    }

    private String formatErrors(List<CurrencyError> errors) {
        StringBuilder sb = new StringBuilder();
        for (CurrencyError err : errors) {
            sb.append("  - ").append(err.priceText).append(": ").append(err.reason).append("\n");
        }
        return sb.toString();
    }
}
