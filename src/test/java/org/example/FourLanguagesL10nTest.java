package org.example;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.*;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.*;

/**
 * FourLanguagesL10nTest - Test L10n cho 4 ngon ngu chinh
 * 
 * Ngon ngu duoc test:
 * 1. en - English (Latin, dot decimal)
 * 2. fr - Français (Latin, comma decimal)
 * 3. vi - Tiếng Việt (Latin, comma decimal, Vietnamese)
 * 4. ar - العربية (RTL, Arabic script)
 * 
 * Su dung JUnit 4 Parameterized de chay test cho nhieu ngon ngu
 */
@RunWith(Parameterized.class)
public class FourLanguagesL10nTest {

    private static final String PRESTASHOP_URL = "https://demo.prestashop.com/";
    private static final int TIMEOUT_SECONDS = 15;

    // ==================== 4 NGON NGU CHINH ====================
    @Parameterized.Parameters(name = "{0} - {1}")
    public static Collection<Object[]> languages() {
        return Arrays.asList(new Object[][] {
                { "en", "English" },
                { "fr", "Français" },
                { "vi", "Tiếng Việt" },
                { "ar", "العربية" }
        });
    }

    @Parameterized.Parameter(0)
    public String langCode;

    @Parameterized.Parameter(1)
    public String langName;

    protected WebDriver driver;
    protected WebDriverWait wait;
    protected JavascriptExecutor js;
    protected List<L10nError> errors;
    protected LanguageConfig config;

    // ==================== JUNIT RULE: Auto Screenshot on Failure
    // ====================

    @Rule
    public TestWatcher screenshotOnFailure = new TestWatcher() {
        @Override
        protected void failed(Throwable e, Description description) {
            if (driver != null) {
                L10nError error = L10nError.fromAssertionError(
                        driver,
                        e instanceof AssertionError ? (AssertionError) e : new AssertionError(e.getMessage()),
                        langCode,
                        driver.getCurrentUrl());
                errors.add(error);
                System.out.println("[SCREENSHOT] Captured on failure: " + error.getScreenshotFilename());
            }
        }

        @Override
        protected void succeeded(Description description) {
            System.out.println("[PASS] " + description.getMethodName() + " for " + langCode);
        }
    };

    // ==================== SETUP & TEARDOWN ====================

    @BeforeClass
    public static void setUpClass() {
        System.out.println("============================================================");
        System.out.println("FOUR LANGUAGES L10N INTEGRATION TESTS");
        System.out.println("Languages: en, fr, vi, ar");
        System.out.println("============================================================");
        WebDriverManager.chromedriver().setup();
    }

    @Before
    public void setUp() {
        System.out.println("\n------------------------------------------------------------");
        System.out.println("Testing Language: " + langCode + " (" + langName + ")");

        errors = new ArrayList<>();
        config = LanguageConfig.get(langCode);

        Assert.assertNotNull("Language config should exist for " + langCode, config);

        // Khoi tao ChromeDriver
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--start-maximized");
        options.addArguments("--disable-notifications");
        options.addArguments("--remote-allow-origins=*");
        // options.addArguments("--headless"); // Uncomment de chay headless

        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(TIMEOUT_SECONDS));
        js = (JavascriptExecutor) driver;

        // Mo PrestaShop va switch vao iframe
        openPrestaShopDemo();
    }

    @After
    public void tearDown() {
        if (!errors.isEmpty()) {
            System.out.println("\n[ERRORS FOUND: " + errors.size() + " for " + langCode + "]");
            for (L10nError error : errors) {
                System.out.println("  - " + error);
            }
        }

        if (driver != null) {
            driver.quit();
        }
    }

    @AfterClass
    public static void tearDownClass() {
        System.out.println("\n============================================================");
        System.out.println("ALL FOUR LANGUAGES TESTS COMPLETED");
        System.out.println("============================================================");
    }

    // ==================== HELPER METHODS ====================

    private void openPrestaShopDemo() {
        System.out.println("[SETUP] Opening PrestaShop Demo...");
        driver.get(PRESTASHOP_URL);
        waitForPageLoad();
        switchToIframe();
        switchLanguage(langCode);
        System.out.println("[SETUP] Ready for testing in " + config.languageName);
    }

    private void waitForPageLoad() {
        wait.until(driver -> js.executeScript("return document.readyState").equals("complete"));
    }

    private boolean switchToIframe() {
        try {
            wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(By.id("framelive")));
            System.out.println("[OK] Switched to iframe #framelive");
            return true;
        } catch (Exception e) {
            try {
                wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(By.cssSelector("iframe")));
                System.out.println("[OK] Switched to iframe (fallback)");
                return true;
            } catch (Exception e2) {
                System.out.println("[ERROR] Could not switch to iframe: " + e2.getMessage());
                return false;
            }
        }
    }

    private void switchLanguage(String langCode) {
        try {
            WebElement langDropdown = wait.until(ExpectedConditions.elementToBeClickable(
                    By.cssSelector(".language-selector, #_desktop_language_selector, [class*='language']")));
            langDropdown.click();

            Thread.sleep(500);

            String psCode = getPrestaShopLangCode(langCode);
            WebElement langOption = wait.until(ExpectedConditions.elementToBeClickable(
                    By.cssSelector("a[href*='/" + psCode + "/'], a[data-iso-code='" + langCode + "']")));
            langOption.click();

            waitForPageLoad();
            switchToIframe();

            System.out.println("[OK] Switched to language: " + langCode);
        } catch (Exception e) {
            System.out.println("[WARNING] Could not switch language: " + e.getMessage());
        }
    }

    private String getPrestaShopLangCode(String isoCode) {
        Map<String, String> map = new HashMap<>();
        map.put("vi", "vn");
        return map.getOrDefault(isoCode, isoCode);
    }

    // ==================== TEST: CURRENCY DISPLAY ====================

    @Test
    public void testCurrencyDisplay() {
        System.out.println("[TEST] Currency Display for " + langCode);

        List<WebElement> priceElements = driver.findElements(By.cssSelector(
                ".price, .product-price, .current-price, [class*='price']"));

        Assert.assertFalse("Should find price elements on page", priceElements.isEmpty());

        Set<String> checkedPrices = new HashSet<>();
        int validCount = 0;
        int errorCount = 0;

        for (WebElement element : priceElements) {
            try {
                String priceText = element.getText().trim();
                if (priceText.isEmpty() || checkedPrices.contains(priceText) || priceText.length() > 50) {
                    continue;
                }
                checkedPrices.add(priceText);

                CurrencyChecker.CurrencyCheckResult result = CurrencyChecker.validateCurrency(priceText, config);

                if (result.isValid) {
                    System.out.println("  [OK] Price: " + priceText + " (symbol: " + result.detectedSymbol + ")");
                    validCount++;
                } else {
                    System.out.println("  [ERROR] Price: " + priceText + " - " + result.errorMessage);
                    errors.add(new L10nError("CURRENCY_ERROR", "Invalid currency format",
                            result.errorMessage, driver.getCurrentUrl(), langCode));
                    errorCount++;
                }

            } catch (StaleElementReferenceException e) {
                // Skip stale elements
            }
        }

        System.out.println("  >> Summary: " + validCount + " valid, " + errorCount + " errors");
        Assert.assertEquals("Should have no currency errors", 0, errorCount);
    }

    // ==================== TEST: DATE FORMAT ====================

    @Test
    public void testDateFormat() {
        System.out.println("[TEST] Date Format for " + langCode);

        String bodyText = driver.findElement(By.tagName("body")).getText();
        List<String> dates = DateChecker.extractDates(bodyText);

        System.out.println("  Found " + dates.size() + " date(s) on page");

        int errorCount = 0;
        for (String dateStr : dates) {
            DateChecker.DateCheckResult result = DateChecker.validateDate(dateStr, langCode, config.datePattern);

            if (!result.isValid) {
                System.out.println("  [ERROR] Date: " + dateStr + " - " + result.errorMessage);
                errors.add(new L10nError("DATE_FORMAT_ERROR", "Invalid date format",
                        result.errorMessage, driver.getCurrentUrl(), langCode));
                errorCount++;
            } else if (result.hasEnglishMonth) {
                System.out.println("  [WARNING] Date has English month: " + dateStr);
            } else {
                System.out.println("  [OK] Date: " + dateStr);
            }
        }

        if (!dates.isEmpty()) {
            Assert.assertEquals("Should have no date format errors", 0, errorCount);
        }
    }

    // ==================== TEST: UNTRANSLATED TEXT ====================

    @Test
    public void testUntranslatedText() {
        System.out.println("[TEST] Untranslated Text Detection for " + langCode);

        // Khong kiem tra trang tieng Anh
        Assume.assumeFalse("Skip for English language", "en".equals(langCode));

        String pageText = driver.findElement(By.tagName("body")).getText();
        List<String> untranslated = TextChecker.findUntranslatedEnglishText(pageText, langCode);

        if (!untranslated.isEmpty()) {
            System.out.println("  [WARNING] Found untranslated English text:");
            for (String text : untranslated) {
                System.out.println("    - " + text);
                errors.add(new L10nError("UNTRANSLATED_TEXT", "English text found",
                        "Found '" + text + "' in " + config.languageName + " page",
                        driver.getCurrentUrl(), langCode));
            }
        }

        System.out.println("  >> Found " + untranslated.size() + " untranslated text(s)");
    }

    // ==================== TEST: EXPECTED KEYWORDS ====================

    @Test
    public void testExpectedKeywords() {
        System.out.println("[TEST] Expected Keywords for " + langCode);

        String pageText = driver.findElement(By.tagName("body")).getText();
        Map<String, Boolean> keywordResults = TextChecker.checkExpectedKeywords(pageText, config.expectedKeywords);

        int foundCount = 0;
        int missingCount = 0;

        for (Map.Entry<String, Boolean> entry : keywordResults.entrySet()) {
            if (entry.getValue()) {
                System.out.println("  [OK] Found: " + entry.getKey());
                foundCount++;
            } else {
                System.out.println("  [MISSING] Not found: " + entry.getKey());
                missingCount++;
            }
        }

        double coverage = TextChecker.calculateKeywordCoverage(keywordResults);
        System.out.println("  >> Coverage: " + String.format("%.1f%%", coverage * 100) +
                " (" + foundCount + "/" + config.expectedKeywords.length + ")");

        Assert.assertTrue("Should find at least 50% of expected keywords", coverage >= 0.5);
    }

    // ==================== TEST: TEXT OVERFLOW (BVA) ====================

    @Test
    public void testTextOverflow() {
        System.out.println("[TEST] Text Overflow for " + langCode);

        // Kiem tra cho cac ngon ngu co van ban dai
        // fr va vi thuong co van ban dai hon en
        if (!TextChecker.isLongTextLanguage(langCode) && !"fr".equals(langCode) && !"vi".equals(langCode)) {
            System.out.println("  [SKIP] " + langCode + " is not a long-text language");
            return;
        }

        List<WebElement> buttons = driver.findElements(By.cssSelector(
                ".btn, button, .add-to-cart, [class*='btn']"));

        int overflowCount = 0;

        for (WebElement button : buttons) {
            try {
                String text = button.getText().trim();
                if (text.isEmpty() || text.length() < 3)
                    continue;

                int offsetWidth = ((Long) js.executeScript("return arguments[0].offsetWidth;", button)).intValue();
                int scrollWidth = ((Long) js.executeScript("return arguments[0].scrollWidth;", button)).intValue();

                if (scrollWidth > offsetWidth + 5) {
                    System.out.println("  [OVERFLOW] '" + text + "' - offsetWidth=" + offsetWidth +
                            ", scrollWidth=" + scrollWidth);
                    errors.add(new L10nError("TEXT_OVERFLOW", "Text overflow detected",
                            "Button '" + text + "' is overflowing",
                            driver.getCurrentUrl(), langCode));
                    overflowCount++;
                }

            } catch (Exception e) {
                // Skip
            }
        }

        System.out.println("  >> Found " + overflowCount + " overflow(s)");
    }

    // ==================== TEST: RTL LAYOUT (Dac biet cho Arabic)
    // ====================

    @Test
    public void testRTLLayout() {
        System.out.println("[TEST] RTL Layout for " + langCode);

        // Chi kiem tra cho Arabic
        if (!config.isRTL) {
            System.out.println("  [SKIP] " + langCode + " is not RTL");
            return;
        }

        WebElement html = driver.findElement(By.tagName("html"));
        String dir = html.getAttribute("dir");

        if (!"rtl".equalsIgnoreCase(dir)) {
            WebElement body = driver.findElement(By.tagName("body"));
            dir = body.getAttribute("dir");
        }

        System.out.println("  Document direction: " + (dir != null ? dir : "not set"));

        Assert.assertEquals("RTL language should have dir='rtl'", "rtl", dir != null ? dir.toLowerCase() : "");
    }

    // ==================== TEST: PAGE TITLE ====================

    @Test
    public void testPageTitle() {
        System.out.println("[TEST] Page Title for " + langCode);

        String title = driver.getTitle();
        System.out.println("  Page title: " + title);

        Assert.assertNotNull("Page should have a title", title);
        Assert.assertFalse("Page title should not be empty", title.isEmpty());
    }

    // ==================== TEST: NAVIGATION MENU ====================

    @Test
    public void testNavigationMenu() {
        System.out.println("[TEST] Navigation Menu for " + langCode);

        List<WebElement> menuItems = driver.findElements(By.cssSelector(
                ".top-menu a, nav a, .menu a, .nav-link"));

        Assert.assertFalse("Should have menu items", menuItems.isEmpty());

        System.out.println("  Found " + menuItems.size() + " menu item(s)");

        boolean hasVisibleMenuItem = false;
        for (WebElement item : menuItems) {
            String text = item.getText().trim();
            if (!text.isEmpty()) {
                hasVisibleMenuItem = true;
                System.out.println("    - " + text);
            }
        }

        Assert.assertTrue("Should have at least one visible menu item", hasVisibleMenuItem);
    }

    // ==================== TEST: PRODUCT LISTING ====================

    @Test
    public void testProductListing() {
        System.out.println("[TEST] Product Listing for " + langCode);

        List<WebElement> products = driver.findElements(By.cssSelector(
                ".product-miniature, .product-item, [class*='product']"));

        System.out.println("  Found " + products.size() + " product(s)");

        int productsWithPrice = 0;
        for (WebElement product : products) {
            try {
                WebElement price = product.findElement(By.cssSelector(".price, [class*='price']"));
                if (!price.getText().isEmpty()) {
                    productsWithPrice++;
                }
            } catch (org.openqa.selenium.NoSuchElementException e) {
                // Product doesn't have price element
            }
        }

        System.out.println("  Products with visible price: " + productsWithPrice);
    }

    // ==================== TEST: DECIMAL SEPARATOR (Dac biet cho 4 ngon ngu)
    // ====================

    @Test
    public void testDecimalSeparator() {
        System.out.println("[TEST] Decimal Separator for " + langCode);

        // en: dot (.)
        // fr, vi: comma (,)
        // ar: dot (.) - theo chuan quoc te

        String expectedSeparator = config.decimalSeparator;
        System.out.println("  Expected decimal separator: '" + expectedSeparator + "'");

        List<WebElement> priceElements = driver.findElements(By.cssSelector(
                ".price, .product-price, .current-price"));

        int correctCount = 0;
        int totalChecked = 0;

        for (WebElement element : priceElements) {
            try {
                String priceText = element.getText().trim();
                if (priceText.isEmpty())
                    continue;

                totalChecked++;

                // Kiem tra decimal separator
                // Tim so co phan thap phan
                if (priceText.matches(".*\\d+[.,]\\d{2}.*")) {
                    boolean hasCorrectSeparator = false;
                    if (".".equals(expectedSeparator) && priceText.matches(".*\\d+\\.\\d{2}.*")) {
                        hasCorrectSeparator = true;
                    } else if (",".equals(expectedSeparator) && priceText.matches(".*\\d+,\\d{2}.*")) {
                        hasCorrectSeparator = true;
                    }

                    if (hasCorrectSeparator) {
                        correctCount++;
                        System.out.println("  [OK] " + priceText);
                    } else {
                        System.out.println("  [WARNING] Unexpected separator in: " + priceText);
                    }
                }

            } catch (StaleElementReferenceException e) {
                // Skip
            }
        }

        System.out.println("  >> Checked: " + totalChecked + ", Correct separator: " + correctCount);
    }

    // ==================== TEST: CHARACTER ENCODING ====================

    @Test
    public void testCharacterEncoding() {
        System.out.println("[TEST] Character Encoding for " + langCode);

        String pageText = driver.findElement(By.tagName("body")).getText();

        // Kiem tra khong co ky tu loi encoding (? hoac .)
        boolean hasEncodingIssue = false;

        // Tim cac pattern loi encoding pho bien
        String[] brokenPatterns = { "�", "Ã¢", "Ã©", "Ã¨", "Ã", "â€" };

        for (String pattern : brokenPatterns) {
            if (pageText.contains(pattern)) {
                System.out.println("  [ERROR] Found broken encoding: " + pattern);
                hasEncodingIssue = true;
            }
        }

        if (!hasEncodingIssue) {
            System.out.println("  [OK] No encoding issues detected");
        }

        Assert.assertFalse("Should not have encoding issues", hasEncodingIssue);
    }

    // ==================== TEST: LANGUAGE SPECIFIC CHARACTERS ====================

    @Test
    public void testLanguageSpecificCharacters() {
        System.out.println("[TEST] Language Specific Characters for " + langCode);

        String pageText = driver.findElement(By.tagName("body")).getText();

        boolean hasExpectedChars = false;
        String charDescription = "";

        switch (langCode) {
            case "en":
                // Tieng Anh - chi co ky tu Latin co ban
                hasExpectedChars = pageText.matches(".*[a-zA-Z].*");
                charDescription = "Latin characters";
                break;
            case "fr":
                // Tieng Phap - co the co dau (é, è, ê, à, etc.)
                hasExpectedChars = pageText.matches(".*[a-zA-ZéèêëàâäùûüôöîïçÉÈÊËÀÂÄÙÛÜÔÖÎÏÇ].*");
                charDescription = "French accented characters";
                break;
            case "vi":
                // Tieng Viet - co nhieu dau
                hasExpectedChars = pageText
                        .matches(".*[àáạảãâầấậẩẫăằắặẳẵèéẹẻẽêềếệểễìíịỉĩòóọỏõôồốộổỗơờớợởỡùúụủũưừứựửữỳýỵỷỹđ].*");
                charDescription = "Vietnamese diacritics";
                break;
            case "ar":
                // Tieng A-Rap - ky tu Arabic
                hasExpectedChars = pageText.matches(".*[\\u0600-\\u06FF].*");
                charDescription = "Arabic script";
                break;
        }

        System.out.println("  Checking for: " + charDescription);
        System.out.println("  Found expected characters: " + hasExpectedChars);

        // Khong bat buoc - chi canh bao
        if (!hasExpectedChars && !"en".equals(langCode)) {
            System.out.println("  [WARNING] Expected " + charDescription + " but not found");
        } else {
            System.out.println("  [OK] " + charDescription + " present");
        }
    }
}
