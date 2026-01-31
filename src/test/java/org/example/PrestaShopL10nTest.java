package org.example;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.*;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.*;

/**
 * PrestaShopL10nTest - Integration/UI Tests cho PrestaShop Demo
 * 
 * Su dung JUnit 4 voi:
 * - @Before: Khoi tao ChromeDriver va switch vao iframe #framelive
 * - @After: Dong trinh duyet sau khi test xong
 * - @Test: Kiem tra tung khia canh (Currency, Date, Text)
 * 
 * Flow:
 * 1. JUnit Runner khoi dong
 * 2. @Before: Mo Chrome -> Vao PrestaShop Demo -> Cho va Switch vao Iframe
 * 3. @Test: Lay du lieu tu LanguageConfig -> Goi Checker -> Assert
 * 4. @After: Dong trinh duyet, xuat bao cao
 */
public class PrestaShopL10nTest {

    private static final String PRESTASHOP_URL = "https://demo.prestashop.com/";
    private static final int TIMEOUT_SECONDS = 15;

    // Test language - co the thay doi de test ngon ngu khac
    private static final String TEST_LANGUAGE = "fr"; // French la default

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
            // Tu dong chup screenshot khi test that bai
            if (driver != null) {
                L10nError error = L10nError.fromAssertionError(
                        driver,
                        e instanceof AssertionError ? (AssertionError) e : new AssertionError(e.getMessage()),
                        TEST_LANGUAGE,
                        driver.getCurrentUrl());
                errors.add(error);
                System.out.println("[SCREENSHOT] Captured on failure: " + error.getScreenshotFilename());
            }
        }

        @Override
        protected void succeeded(Description description) {
            System.out.println("[PASS] " + description.getMethodName());
        }
    };

    // ==================== SETUP & TEARDOWN ====================

    @BeforeClass
    public static void setUpClass() {
        System.out.println("============================================================");
        System.out.println("PRESTASHOP L10N INTEGRATION TESTS");
        System.out.println("Test Language: " + TEST_LANGUAGE);
        System.out.println("============================================================");
        WebDriverManager.chromedriver().setup();
    }

    @Before
    public void setUp() {
        System.out.println("\n------------------------------------------------------------");

        errors = new ArrayList<>();
        config = LanguageConfig.get(TEST_LANGUAGE);

        Assert.assertNotNull("Language config should exist for " + TEST_LANGUAGE, config);

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
        // In cac loi da thu thap
        if (!errors.isEmpty()) {
            System.out.println("\n[ERRORS FOUND: " + errors.size() + "]");
            for (L10nError error : errors) {
                System.out.println("  - " + error);
            }
        }

        // Dong trinh duyet
        if (driver != null) {
            driver.quit();
        }
    }

    @AfterClass
    public static void tearDownClass() {
        System.out.println("\n============================================================");
        System.out.println("ALL INTEGRATION TESTS COMPLETED");
        System.out.println("============================================================");
    }

    // ==================== HELPER METHODS ====================

    /**
     * Mo PrestaShop Demo va switch vao iframe
     */
    private void openPrestaShopDemo() {
        System.out.println("[SETUP] Opening PrestaShop Demo...");
        driver.get(PRESTASHOP_URL);

        // Cho trang load
        waitForPageLoad();

        // Switch vao iframe su dung ExpectedConditions.frameToBeAvailableAndSwitchToIt
        switchToIframe();

        // Chuyen ngon ngu
        switchLanguage(TEST_LANGUAGE);

        System.out.println("[SETUP] Ready for testing in " + config.languageName);
    }

    /**
     * Cho trang load xong
     */
    private void waitForPageLoad() {
        wait.until(driver -> js.executeScript("return document.readyState").equals("complete"));
    }

    /**
     * Switch vao iframe #framelive su dung WebDriverWait
     */
    private boolean switchToIframe() {
        try {
            // Su dung ExpectedConditions.frameToBeAvailableAndSwitchToIt
            wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(By.id("framelive")));
            System.out.println("[OK] Switched to iframe #framelive");
            return true;
        } catch (Exception e) {
            try {
                // Fallback - try any iframe
                wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(By.cssSelector("iframe")));
                System.out.println("[OK] Switched to iframe (fallback)");
                return true;
            } catch (Exception e2) {
                System.out.println("[ERROR] Could not switch to iframe: " + e2.getMessage());
                return false;
            }
        }
    }

    /**
     * Chuyen ngon ngu
     */
    private void switchLanguage(String langCode) {
        try {
            // Click language selector
            WebElement langDropdown = wait.until(ExpectedConditions.elementToBeClickable(
                    By.cssSelector(".language-selector, #_desktop_language_selector, [class*='language']")));
            langDropdown.click();

            Thread.sleep(500); // Cho dropdown mo

            // Click ngon ngu can chon
            String psCode = getPrestaShopLangCode(langCode);
            WebElement langOption = wait.until(ExpectedConditions.elementToBeClickable(
                    By.cssSelector("a[href*='/" + psCode + "/'], a[data-iso-code='" + langCode + "']")));
            langOption.click();

            // Cho trang load lai va switch lai vao iframe
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
        map.put("sl", "si");
        return map.getOrDefault(isoCode, isoCode);
    }

    // ==================== TEST: CURRENCY DISPLAY ====================

    @Test
    public void testCurrencyDisplay() {
        System.out.println("[TEST] Currency Display");

        // Tim cac element chua gia
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

                // Su dung static method tu CurrencyChecker
                CurrencyChecker.CurrencyCheckResult result = CurrencyChecker.validateCurrency(priceText, config);

                if (result.isValid) {
                    System.out.println("  [OK] Price: " + priceText + " (symbol: " + result.detectedSymbol + ")");
                    validCount++;
                } else {
                    System.out.println("  [ERROR] Price: " + priceText + " - " + result.errorMessage);
                    errors.add(new L10nError("CURRENCY_ERROR", "Invalid currency format",
                            result.errorMessage, driver.getCurrentUrl(), TEST_LANGUAGE));
                    errorCount++;
                }

            } catch (StaleElementReferenceException e) {
                // Skip stale elements
            }
        }

        System.out.println("  >> Summary: " + validCount + " valid, " + errorCount + " errors");

        // Assert khong co loi nghiem trong
        Assert.assertEquals("Should have no currency errors", 0, errorCount);
    }

    // ==================== TEST: DATE FORMAT ====================

    @Test
    public void testDateFormat() {
        System.out.println("[TEST] Date Format");

        String bodyText = driver.findElement(By.tagName("body")).getText();

        // Trich xuat cac ngay tu trang
        List<String> dates = DateChecker.extractDates(bodyText);

        System.out.println("  Found " + dates.size() + " date(s) on page");

        int errorCount = 0;
        for (String dateStr : dates) {
            DateChecker.DateCheckResult result = DateChecker.validateDate(dateStr, TEST_LANGUAGE, config.datePattern);

            if (!result.isValid) {
                System.out.println("  [ERROR] Date: " + dateStr + " - " + result.errorMessage);
                errors.add(new L10nError("DATE_FORMAT_ERROR", "Invalid date format",
                        result.errorMessage, driver.getCurrentUrl(), TEST_LANGUAGE));
                errorCount++;
            } else if (result.hasEnglishMonth) {
                System.out.println("  [WARNING] Date has English month: " + dateStr);
            } else {
                System.out.println("  [OK] Date: " + dateStr);
            }
        }

        // Khong bat buoc co ngay tren trang
        if (!dates.isEmpty()) {
            Assert.assertEquals("Should have no date format errors", 0, errorCount);
        }
    }

    // ==================== TEST: UNTRANSLATED TEXT ====================

    @Test
    public void testUntranslatedText() {
        System.out.println("[TEST] Untranslated Text Detection");

        // Khong kiem tra trang tieng Anh
        Assume.assumeFalse("Skip for English language", "en".equals(TEST_LANGUAGE));

        String pageText = driver.findElement(By.tagName("body")).getText();

        // Tim van ban tieng Anh chua dich
        List<String> untranslated = TextChecker.findUntranslatedEnglishText(pageText, TEST_LANGUAGE);

        if (!untranslated.isEmpty()) {
            System.out.println("  [WARNING] Found untranslated English text:");
            for (String text : untranslated) {
                System.out.println("    - " + text);
                errors.add(new L10nError("UNTRANSLATED_TEXT", "English text found",
                        "Found '" + text + "' in " + config.languageName + " page",
                        driver.getCurrentUrl(), TEST_LANGUAGE));
            }
        }

        // Canh bao nhung khong that bai test
        System.out.println("  >> Found " + untranslated.size() + " untranslated text(s)");
    }

    // ==================== TEST: EXPECTED KEYWORDS ====================

    @Test
    public void testExpectedKeywords() {
        System.out.println("[TEST] Expected Keywords");

        String pageText = driver.findElement(By.tagName("body")).getText();

        // Kiem tra cac tu khoa mong doi
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

        // Assert it nhat 50% tu khoa duoc tim thay
        Assert.assertTrue("Should find at least 50% of expected keywords", coverage >= 0.5);
    }

    // ==================== TEST: TEXT OVERFLOW (BVA) ====================

    @Test
    public void testTextOverflow() {
        System.out.println("[TEST] Text Overflow (Boundary Value Analysis)");

        // Chi kiem tra cho cac ngon ngu co van ban dai
        if (!TextChecker.isLongTextLanguage(TEST_LANGUAGE)) {
            System.out.println("  [SKIP] " + TEST_LANGUAGE + " is not a long-text language");
            return;
        }

        // Tim cac nut quan trong
        List<WebElement> buttons = driver.findElements(By.cssSelector(
                ".btn, button, .add-to-cart, [class*='btn']"));

        int overflowCount = 0;

        for (WebElement button : buttons) {
            try {
                String text = button.getText().trim();
                if (text.isEmpty() || text.length() < 3)
                    continue;

                // Lay offsetWidth va scrollWidth
                int offsetWidth = ((Long) js.executeScript("return arguments[0].offsetWidth;", button)).intValue();
                int scrollWidth = ((Long) js.executeScript("return arguments[0].scrollWidth;", button)).intValue();

                if (scrollWidth > offsetWidth + 5) {
                    System.out.println("  [OVERFLOW] '" + text + "' - offsetWidth=" + offsetWidth +
                            ", scrollWidth=" + scrollWidth);
                    errors.add(new L10nError("TEXT_OVERFLOW", "Text overflow detected",
                            "Button '" + text + "' is overflowing",
                            driver.getCurrentUrl(), TEST_LANGUAGE));
                    overflowCount++;
                }

            } catch (Exception e) {
                // Skip
            }
        }

        System.out.println("  >> Found " + overflowCount + " overflow(s)");
    }

    // ==================== TEST: RTL LAYOUT ====================

    @Test
    public void testRTLLayout() {
        System.out.println("[TEST] RTL Layout");

        // Chi kiem tra cho cac ngon ngu RTL
        if (!config.isRTL) {
            System.out.println("  [SKIP] " + TEST_LANGUAGE + " is not RTL");
            return;
        }

        // Kiem tra dir attribute cua html hoac body
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
        System.out.println("[TEST] Page Title");

        String title = driver.getTitle();
        System.out.println("  Page title: " + title);

        Assert.assertNotNull("Page should have a title", title);
        Assert.assertFalse("Page title should not be empty", title.isEmpty());

        // Canh bao neu title la tieng Anh cho trang khong phai EN
        if (!"en".equals(TEST_LANGUAGE)) {
            // Kiem tra xem title co chua tu tieng Anh khong
            String[] englishWords = { "Demo", "Shop", "Store", "PrestaShop" };
            for (String word : englishWords) {
                if (title.contains(word)) {
                    System.out.println("  [INFO] Title contains English word: " + word);
                }
            }
        }
    }

    // ==================== TEST: NAVIGATION MENU ====================

    @Test
    public void testNavigationMenu() {
        System.out.println("[TEST] Navigation Menu");

        List<WebElement> menuItems = driver.findElements(By.cssSelector(
                ".top-menu a, nav a, .menu a, .nav-link"));

        Assert.assertFalse("Should have menu items", menuItems.isEmpty());

        System.out.println("  Found " + menuItems.size() + " menu item(s)");

        // Kiem tra it nhat mot menu item co van ban
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
        System.out.println("[TEST] Product Listing");

        List<WebElement> products = driver.findElements(By.cssSelector(
                ".product-miniature, .product-item, [class*='product']"));

        System.out.println("  Found " + products.size() + " product(s)");

        // Kiem tra moi san pham co gia
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

    // ==================== STATIC TEST HELPER: Run for specific language
    // ====================

    /**
     * Helper method de chay test cho mot ngon ngu cu the
     * Co the goi tu main() de test nhanh
     */
    public static void runTestsForLanguage(String langCode) {
        System.out.println("Running tests for: " + langCode);
        // Can tao instance va goi cac test method
        // Trong thuc te, nen su dung JUnit runner
    }
}
