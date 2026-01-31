package org.example;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.*;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.*;

/**
 * L10nBaseTest - Base test class su dung @Parameterized (JUnit 4)
 * 
 * Ap dung phuong phap PHAN VUNG TUONG DUONG (Equivalence Partitioning):
 * - Nhom 1: Latin/Dot (en, mx) - Dau thap phan la dau cham
 * - Nhom 2: Latin/Comma (fr, vi, de) - Dau thap phan la dau phay
 * - Nhom 3: Double-byte (ja, zh, ko) - Ky tu kep CJK
 * - Nhom 4: RTL (ar, he, fa) - Doc tu phai sang trai
 * - Nhom 5: Cyrillic (ru, uk, bg) - Chu Cyrillic
 * - Nhom 6: Indic (hi, bn) - Chu An Do
 * 
 * Moi test se chay cho cac ngon ngu dai dien cua moi nhom
 */
@RunWith(Parameterized.class)
public class L10nBaseTest {

    protected static final String PRESTASHOP_URL = "https://demo.prestashop.com/";
    protected static final int TIMEOUT_SECONDS = 30;

    // Thoi gian cho de quan sat (milliseconds) - Dat = 0 de chay nhanh
    protected static final int OBSERVATION_DELAY = 2000;

    // Bat/tat headless mode - false = hien thi trinh duyet
    protected static final boolean HEADLESS_MODE = false;

    protected WebDriver driver;
    protected WebDriverWait wait;
    protected List<L10nError> errors;
    protected StringBuilder testReport;
    protected boolean languageSwitchSucceeded = true; // Track if language switch was successful

    // Parameter cho @Parameterized
    @Parameterized.Parameter(0)
    public String languageCode;

    @Parameterized.Parameter(1)
    public LanguageConfig.TestGroup testGroup;

    @Parameterized.Parameter(2)
    public LanguageConfig config;

    // ==================== PARAMETERIZED DATA ====================

    /**
     * Tra ve danh sach cac ngon ngu dai dien cho moi Equivalence Partition
     */
    @Parameterized.Parameters(name = "{index}: Language={0}, Group={1}")
    public static Collection<Object[]> getTestParameters() {
        List<Object[]> parameters = new ArrayList<>();

        // Lay ngon ngu dai dien cho moi nhom
        Map<LanguageConfig.TestGroup, LanguageConfig> representatives = LanguageConfig.getRepresentativeLanguages();

        for (Map.Entry<LanguageConfig.TestGroup, LanguageConfig> entry : representatives.entrySet()) {
            if (entry.getValue() != null) {
                parameters.add(new Object[] {
                        entry.getValue().code,
                        entry.getKey(),
                        entry.getValue()
                });
            }
        }

        return parameters;
    }

    /**
     * Tra ve danh sach chi cac ngon ngu Latin de test nhanh
     */
    public static Collection<Object[]> getLatinLanguagesOnly() {
        List<Object[]> parameters = new ArrayList<>();

        // Chi test English va French
        LanguageConfig enConfig = LanguageConfig.get("en");
        LanguageConfig frConfig = LanguageConfig.get("fr");

        if (enConfig != null) {
            parameters.add(new Object[] { "en", LanguageConfig.TestGroup.LATIN_DOT, enConfig });
        }
        if (frConfig != null) {
            parameters.add(new Object[] { "fr", LanguageConfig.TestGroup.LATIN_COMMA, frConfig });
        }

        return parameters;
    }

    // ==================== SETUP & TEARDOWN ====================

    @BeforeClass
    public static void setUpClass() {
        System.out.println("============================================================");
        System.out.println("L10N BASE TEST - EQUIVALENCE PARTITIONING");
        System.out.println("============================================================");
        WebDriverManager.chromedriver().setup();
    }

    @Before
    public void setUp() {
        System.out.println("\n------------------------------------------------------------");
        System.out.println("Testing Language: " + languageCode + " (Group: " + testGroup + ")");
        System.out.println("------------------------------------------------------------");

        errors = new ArrayList<>();
        testReport = new StringBuilder();
        languageSwitchSucceeded = true; // Reset flag

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--start-maximized");
        options.addArguments("--disable-notifications");
        options.addArguments("--remote-allow-origins=*");
        options.addArguments("--disable-blink-features=AutomationControlled");

        if (HEADLESS_MODE) {
            options.addArguments("--headless=new");
        }

        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(TIMEOUT_SECONDS));
    }

    @After
    public void tearDown() {
        // In test report
        if (testReport.length() > 0) {
            System.out.println("\n--- TEST REPORT ---");
            System.out.println(testReport.toString());
        }

        // In danh sach loi (khong chup screenshot)
        if (!errors.isEmpty()) {
            System.out.println("\n[!] Found " + errors.size() + " ERRORS:");
            for (L10nError error : errors) {
                System.out.println("  - " + error.type + ": " + error.title);
                System.out.println("    Details: " + error.description);
            }
        }

        // Cho de quan sat truoc khi dong trinh duyet
        if (OBSERVATION_DELAY > 0 && !HEADLESS_MODE) {
            System.out.println("\n[INFO] Waiting " + (OBSERVATION_DELAY / 1000) + " seconds for observation...");
            try {
                Thread.sleep(OBSERVATION_DELAY);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        if (driver != null) {
            driver.quit();
        }

        // In tong ket
        System.out.println("Test completed for " + languageCode + ". Errors: " + errors.size());

        // Fail test neu co loi
        if (!errors.isEmpty()) {
            StringBuilder errorMsg = new StringBuilder("Found " + errors.size() + " L10n errors:\n");
            for (L10nError error : errors) {
                errorMsg.append("  - ").append(error.type).append(": ").append(error.title).append("\n");
            }
            Assert.fail(errorMsg.toString());
        }
    }

    @AfterClass
    public static void tearDownClass() {
        System.out.println("\n============================================================");
        System.out.println("ALL TESTS COMPLETED");
        System.out.println("============================================================");
    }

    // ==================== HELPER METHODS ====================

    /**
     * Mo trang web va chuyen sang ngon ngu can test
     */
    protected void openAndSwitchLanguage() {
        System.out.println("  [1] Opening URL: " + PRESTASHOP_URL);
        driver.get(PRESTASHOP_URL);

        System.out.println("  [2] Waiting for page load...");
        waitForPageLoad();

        System.out.println("  [3] Switching to iframe...");
        boolean inIframe = switchToIframe();
        System.out.println("  [3] In iframe: " + inIframe);

        // Chi chuyen ngon ngu neu khong phai English (default)
        if (!"en".equals(languageCode)) {
            System.out.println("  [4] Switching to language: " + languageCode);
            switchLanguage(languageCode);
        } else {
            System.out.println("  [4] Already in English, skipping language switch");
        }

        // Navigate den trang san pham de thay gia va noi dung day du
        System.out.println("  [5] Navigating to product page...");
        navigateToProductPage();

        System.out.println("  [6] Page ready for testing");
    }

    /**
     * Navigate den trang san pham de kiem tra gia va noi dung
     */
    protected void navigateToProductPage() {
        try {
            // Click vao san pham dau tien tren trang chu
            WebElement firstProduct = wait.until(ExpectedConditions.elementToBeClickable(
                    By.cssSelector(
                            ".product-thumbnail, .product-miniature a, .product-image, article.product-miniature")));
            firstProduct.click();

            waitForPageLoad();
            Thread.sleep(1000); // Cho trang san pham load hoan toan
        } catch (Exception e) {
            System.out.println("  [WARNING] Could not navigate to product page: " + e.getMessage());
        }
    }

    /**
     * Cho trang load xong
     */
    protected void waitForPageLoad() {
        wait.until(driver -> ((org.openqa.selenium.JavascriptExecutor) driver)
                .executeScript("return document.readyState").equals("complete"));
    }

    /**
     * Switch vao iframe cua PrestaShop demo
     */
    protected boolean switchToIframe() {
        try {
            // Su dung ExpectedConditions.frameToBeAvailableAndSwitchToIt
            wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(By.id("framelive")));
            return true;
        } catch (Exception e) {
            try {
                // Fallback
                wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(By.cssSelector("iframe")));
                return true;
            } catch (Exception e2) {
                return false;
            }
        }
    }

    /**
     * Chuyen ngon ngu
     */
    protected void switchLanguage(String langCode) {
        try {
            // Click vao language dropdown
            WebElement langDropdown = wait.until(ExpectedConditions.elementToBeClickable(
                    By.cssSelector(".language-selector, #_desktop_language_selector, [class*='language']")));
            langDropdown.click();

            // Cho dropdown hien thi
            Thread.sleep(500);

            // Click vao ngon ngu can chon
            String prestashopCode = getPrestaShopLangCode(langCode);
            WebElement langOption = wait.until(ExpectedConditions.elementToBeClickable(
                    By.cssSelector("a[href*='/" + prestashopCode + "/'], " +
                            "a[data-iso-code='" + langCode + "'], " +
                            "[class*='language'] a[href*='" + prestashopCode + "']")));
            langOption.click();

            // Cho trang load lai
            waitForPageLoad();
            switchToIframe();
            languageSwitchSucceeded = true;

        } catch (Exception e) {
            System.out.println("[WARNING] Could not switch language: " + e.getMessage());
            languageSwitchSucceeded = false; // Mark as failed
        }
    }

    /**
     * Map ISO code sang PrestaShop code neu khac
     */
    protected String getPrestaShopLangCode(String isoCode) {
        Map<String, String> codeMap = new HashMap<>();
        codeMap.put("vi", "vn");
        codeMap.put("sl", "si");
        return codeMap.getOrDefault(isoCode, isoCode);
    }

    /**
     * Record error voi screenshot
     */
    protected void recordError(String type, String title, String description) {
        L10nError error = new L10nError(type, title, description, driver.getCurrentUrl(), languageCode);
        errors.add(error);
    }

    // ==================== REAL WEB TESTS ====================

    @Test
    public void testLanguageConfiguration() {
        System.out.println("[TEST] Language Configuration for " + languageCode);

        // Kiem tra config truoc
        Assert.assertNotNull("Config should not be null", config);
        Assert.assertEquals("Language code should match", languageCode, config.code);

        testReport.append("=== Language Config ===\n");
        testReport.append("  Code: ").append(config.code).append("\n");
        testReport.append("  Name: ").append(config.languageName).append("\n");
        testReport.append("  RTL: ").append(config.isRTL).append("\n");
        testReport.append("  Decimal: ").append(config.decimalSeparator).append("\n");

        // MO WEB VA KIEM TRA THUC TE
        openAndSwitchLanguage();

        // Kiem tra trang da load dung ngon ngu chua
        String pageSource = driver.getPageSource();
        String currentUrl = driver.getCurrentUrl();

        testReport.append("\n=== Page Info ===\n");
        testReport.append("  URL: ").append(currentUrl).append("\n");

        // Neu khong chuyen duoc ngon ngu -> bao loi ngay
        if (!languageSwitchSucceeded && !"en".equals(languageCode)) {
            recordError("LANGUAGE_SWITCH_FAILED",
                    "Could not switch to language: " + languageCode,
                    "Language switch failed, cannot test language configuration for " + config.languageName);
            testReport.append("  [FAIL] Cannot test - language switch failed\n");
            return;
        }

        // KIEM TRA NGON NGU THUC TE TREN TRANG
        // Phan tich tat ca text tren trang va xac dinh ngon ngu
        // Su dung JavaScript de lay text tranh StaleElementReferenceException
        int totalTextElements = 0;
        int correctLanguageElements = 0;
        int englishElements = 0;
        List<String> englishSamples = new ArrayList<>();

        try {
            // Lay text bang JavaScript de tranh stale element
            @SuppressWarnings("unchecked")
            List<String> texts = (List<String>) ((org.openqa.selenium.JavascriptExecutor) driver).executeScript(
                    "var elements = document.querySelectorAll('h1, h2, h3, p, span.product-title, a.product-name, button, label, .product-title, .product-description');"
                            +
                            "var texts = [];" +
                            "for (var i = 0; i < Math.min(elements.length, 100); i++) {" +
                            "  var text = elements[i].innerText || elements[i].textContent;" +
                            "  if (text && text.trim().length > 2) {" +
                            "    texts.push(text.trim());" +
                            "  }" +
                            "}" +
                            "return texts;");

            for (String text : texts) {
                if (text.isEmpty() || text.length() < 3)
                    continue;

                totalTextElements++;

                // Kiem tra xem text co phai tieng Anh khong
                if (isEnglishText(text)) {
                    englishElements++;
                    if (englishSamples.size() < 10 && !isAllowedEnglishText(text)) {
                        englishSamples.add(text);
                    }
                } else if (isTextInExpectedLanguage(text, languageCode)) {
                    correctLanguageElements++;
                }
            }
        } catch (Exception e) {
            testReport.append("  [ERROR] Could not analyze text: ").append(e.getMessage()).append("\n");
        }

        testReport.append("  Total text elements: ").append(totalTextElements).append("\n");
        testReport.append("  Correct language: ").append(correctLanguageElements).append("\n");
        testReport.append("  English text: ").append(englishElements).append("\n");

        // Neu khong phai English ma co qua nhieu text tieng Anh -> LOI
        if (!languageCode.equals("en") && englishElements > 0) {
            double englishRate = totalTextElements > 0 ? (double) englishElements / totalTextElements : 0;
            testReport.append("  English rate: ").append(String.format("%.1f%%", englishRate * 100)).append("\n");

            if (!englishSamples.isEmpty()) {
                testReport.append("  [!] English samples: ").append(englishSamples).append("\n");
            }

            // Bao loi neu hon 20% la tieng Anh
            if (englishRate > 0.2 || englishSamples.size() > 5) {
                recordError("UNTRANSLATED",
                        "Too much English text for " + languageCode + " (" + String.format("%.1f%%", englishRate * 100)
                                + ")",
                        "Found " + englishElements + "/" + totalTextElements + " English elements. Samples: "
                                + englishSamples);
            }
        }
    }

    /**
     * Kiem tra xem text co phai tieng Anh khong
     */
    private boolean isEnglishText(String text) {
        // Danh sach cac tu tieng Anh pho bien tren web thuong mai
        String[] commonEnglishWords = {
                "add to cart", "cart", "home", "search", "sign in", "login", "register",
                "contact", "about", "delivery", "shipping", "payment", "terms", "conditions",
                "privacy", "policy", "legal", "notice", "new products", "best sellers",
                "on sale", "price", "quantity", "description", "details", "reviews",
                "category", "categories", "clothes", "accessories", "art", "view", "more"
        };

        String lowerText = text.toLowerCase();
        for (String word : commonEnglishWords) {
            if (lowerText.contains(word)) {
                return true;
            }
        }

        // Kiem tra regex cho cau tieng Anh
        // Neu text chi chua chu cai Latin va co tu tieng Anh pho bien
        if (text.matches("^[A-Za-z0-9\\s.,!?'\"()-]+$")) {
            // Chi xem la tieng Anh neu co tu dai hon 4 ky tu
            String[] words = text.split("\\s+");
            for (String word : words) {
                if (word.length() > 4 && word.matches("[A-Za-z]+")) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Kiem tra xem text tieng Anh co duoc phep khong (brand names, etc.)
     */
    private boolean isAllowedEnglishText(String text) {
        String[] allowedTexts = {
                "PrestaShop", "PayPal", "Facebook", "Twitter", "Instagram", "YouTube",
                "Google", "Email", "Newsletter", "RSS", "Blog", "USD", "EUR", "GBP",
                "Visa", "MasterCard", "American Express", "Discover"
        };

        for (String allowed : allowedTexts) {
            if (text.toLowerCase().contains(allowed.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Kiem tra xem text co dung ngon ngu mong doi khong
     */
    private boolean isTextInExpectedLanguage(String text, String langCode) {
        switch (langCode) {
            case "ar": // Arabic
                return text.matches(".*[\\u0600-\\u06FF]+.*");
            case "he": // Hebrew
                return text.matches(".*[\\u0590-\\u05FF]+.*");
            case "ja": // Japanese
                return text.matches(".*[\\u3040-\\u309F\\u30A0-\\u30FF\\u4E00-\\u9FFF]+.*");
            case "zh": // Chinese
                return text.matches(".*[\\u4E00-\\u9FFF]+.*");
            case "ko": // Korean
                return text.matches(".*[\\uAC00-\\uD7AF]+.*");
            case "ru": // Russian (Cyrillic)
                return text.matches(".*[\\u0400-\\u04FF]+.*");
            case "hi": // Hindi (Devanagari)
                return text.matches(".*[\\u0900-\\u097F]+.*");
            case "th": // Thai
                return text.matches(".*[\\u0E00-\\u0E7F]+.*");
            case "fr": // French - co dau
                return text.matches(".*[àâäéèêëïîôùûüÿçœæÀÂÄÉÈÊËÏÎÔÙÛÜŸÇŒÆ]+.*");
            case "de": // German - co dau
                return text.matches(".*[äöüßÄÖÜ]+.*");
            case "es": // Spanish - co dau
                return text.matches(".*[áéíóúüñÁÉÍÓÚÜÑ¿¡]+.*");
            case "vi": // Vietnamese - co dau
                return text.matches(".*[àáảãạăằắẳẵặâầấẩẫậèéẻẽẹêềếểễệìíỉĩịòóỏõọôồốổỗộơờớởỡợùúủũụưừứửữựỳýỷỹỵđ]+.*");
            default:
                return false;
        }
    }

    @Test
    public void testCurrencyFormat() {
        System.out.println("[TEST] Currency Format for " + languageCode);

        openAndSwitchLanguage();

        // Neu khong chuyen duoc ngon ngu -> bao loi ngay
        if (!languageSwitchSucceeded && !"en".equals(languageCode)) {
            recordError("LANGUAGE_SWITCH_FAILED",
                    "Could not switch to language: " + languageCode,
                    "Language switch failed, cannot test currency for " + config.languageName);
            testReport.append("=== Currency Test ===\n");
            testReport.append("  [FAIL] Cannot test - language switch failed\n");
            return;
        }

        testReport.append("=== Currency Test ===\n");
        testReport.append("  Expected currency: ").append(config.primaryCurrency).append("\n");

        // Tim cac gia tien tren trang san pham
        List<WebElement> priceElements = new ArrayList<>();
        try {
            priceElements.addAll(driver.findElements(By.cssSelector(
                    ".price, .product-price, .current-price, .current-price-value, " +
                            "[itemprop='price'], .product-price-and-shipping .price")));
        } catch (Exception e) {
            testReport.append("  Could not find price elements\n");
        }

        testReport.append("  Found ").append(priceElements.size()).append(" price elements\n");

        if (priceElements.isEmpty()) {
            recordError("CURRENCY",
                    "No price elements found for " + languageCode,
                    "Could not find any price elements on the page");
            return;
        }

        // Thu thap tat ca gia tien - re-find elements to avoid stale reference
        List<String> allPrices = new ArrayList<>();
        Map<String, Integer> currencyCount = new HashMap<>();

        // Re-find price elements to avoid StaleElementReferenceException
        try {
            List<WebElement> freshPriceElements = driver.findElements(By.cssSelector(
                    ".price, .product-price, .current-price, .current-price-value, " +
                            "[itemprop='price'], .product-price-and-shipping .price"));

            for (WebElement priceEl : freshPriceElements) {
                try {
                    String priceText = priceEl.getText().trim();
                    if (priceText.isEmpty() || !priceText.matches(".*\\d+.*"))
                        continue;

                    allPrices.add(priceText);

                    // Phat hien currency symbol
                    String detectedSymbol = CurrencyChecker.detectCurrencySymbol(priceText);
                    if (detectedSymbol != null) {
                        currencyCount.put(detectedSymbol, currencyCount.getOrDefault(detectedSymbol, 0) + 1);
                    }
                } catch (org.openqa.selenium.StaleElementReferenceException e) {
                    // Element became stale, skip it
                    continue;
                }
            }
        } catch (Exception e) {
            testReport.append("  Error collecting prices: ").append(e.getMessage()).append("\n");
        }

        testReport.append("  Prices found: ").append(allPrices.size()).append("\n");
        if (!allPrices.isEmpty()) {
            testReport.append("  Sample prices: ").append(allPrices.subList(0, Math.min(5, allPrices.size())))
                    .append("\n");
        }
        testReport.append("  Currency symbols detected: ").append(currencyCount).append("\n");

        // KIEM TRA CURRENCY SYMBOL CO DUNG KHONG
        // Tim currency pho bien nhat tren trang
        String dominantCurrency = null;
        int maxCount = 0;
        for (Map.Entry<String, Integer> entry : currencyCount.entrySet()) {
            if (entry.getValue() > maxCount) {
                maxCount = entry.getValue();
                dominantCurrency = entry.getKey();
            }
        }

        testReport.append("  Dominant currency: ").append(dominantCurrency).append("\n");

        // So sanh voi expected currency - LUON bao loi neu khac
        String expectedCurrency = config.primaryCurrency;
        if (dominantCurrency != null && !dominantCurrency.equals(expectedCurrency)) {
            // BAO LOI khi currency khong khop voi expected
            testReport.append("  [FAIL] Currency mismatch!\n");
            testReport.append("    Expected: ").append(expectedCurrency).append("\n");
            testReport.append("    Found: ").append(dominantCurrency).append("\n");

            recordError("CURRENCY_MISMATCH",
                    "Currency mismatch for " + languageCode + ": Expected " + expectedCurrency + " but found "
                            + dominantCurrency,
                    "Expected currency '" + expectedCurrency + "' but page shows '" + dominantCurrency + "'. " +
                            "Sample prices: " + allPrices.subList(0, Math.min(3, allPrices.size())));
        } else if (dominantCurrency != null) {
            testReport.append("  [OK] Currency is correct: ").append(dominantCurrency).append("\n");
        }

        // Kiem tra dinh dang gia tien
        int validFormat = 0;
        int invalidFormat = 0;
        List<String> invalidSamples = new ArrayList<>();

        for (String price : allPrices) {
            if (CurrencyChecker.isValidPriceFormat(price)) {
                validFormat++;
            } else {
                invalidFormat++;
                if (invalidSamples.size() < 3) {
                    invalidSamples.add(price);
                }
            }
        }

        testReport.append("  Valid format: ").append(validFormat).append("/").append(allPrices.size()).append("\n");

        if (invalidFormat > 0) {
            testReport.append("  [!] Invalid format samples: ").append(invalidSamples).append("\n");
        }
    }

    @Test
    public void testRTLLayout() {
        System.out.println("[TEST] RTL Layout for " + languageCode);

        openAndSwitchLanguage();

        testReport.append("=== RTL Test ===\n");
        testReport.append("  Expected RTL: ").append(config.isRTL).append("\n");

        // Neu khong chuyen duoc ngon ngu va can RTL -> bao loi
        if (!languageSwitchSucceeded && !"en".equals(languageCode) && config.isRTL) {
            recordError("LANGUAGE_SWITCH_FAILED",
                    "Could not switch to RTL language: " + languageCode,
                    "Language switch failed, cannot test RTL layout for " + config.languageName);
            testReport.append("  [FAIL] Cannot test RTL - language switch failed\n");
            return;
        }

        // Kiem tra dir attribute
        try {
            WebElement html = driver.findElement(By.tagName("html"));
            String dirAttr = html.getAttribute("dir");
            WebElement body = driver.findElement(By.tagName("body"));
            String bodyDir = body.getAttribute("dir");

            testReport.append("  HTML dir: ").append(dirAttr).append("\n");
            testReport.append("  Body dir: ").append(bodyDir).append("\n");

            boolean pageIsRTL = "rtl".equalsIgnoreCase(dirAttr) || "rtl".equalsIgnoreCase(bodyDir);

            if (config.isRTL && !pageIsRTL) {
                recordError("RTL",
                        "RTL language " + languageCode + " not displayed as RTL",
                        "Page should have dir='rtl' but found: html=" + dirAttr + ", body=" + bodyDir);
            } else if (!config.isRTL && pageIsRTL) {
                recordError("RTL",
                        "LTR language " + languageCode + " displayed as RTL",
                        "Page should NOT have dir='rtl' but found: html=" + dirAttr + ", body=" + bodyDir);
            } else {
                testReport.append("  [OK] RTL setting is correct\n");
            }

            // Kiem tra text-align cho RTL
            if (config.isRTL) {
                String textAlign = body.getCssValue("text-align");
                String direction = body.getCssValue("direction");
                testReport.append("  Text-align: ").append(textAlign).append("\n");
                testReport.append("  Direction: ").append(direction).append("\n");
            }

        } catch (Exception e) {
            testReport.append("  [ERROR] Could not check RTL: ").append(e.getMessage()).append("\n");
        }
    }

    @Test
    public void testDecimalSeparatorByGroup() {
        System.out.println("[TEST] Decimal Separator for group " + testGroup);

        testReport.append("=== Decimal Separator Test ===\n");
        testReport.append("  Test group: ").append(testGroup).append("\n");
        testReport.append("  Expected separator: ").append(config.decimalSeparator).append("\n");

        // MO WEB VA KIEM TRA GIA TIEN THUC TE
        openAndSwitchLanguage();

        // Neu khong chuyen duoc ngon ngu -> bao loi ngay
        if (!languageSwitchSucceeded && !"en".equals(languageCode)) {
            recordError("LANGUAGE_SWITCH_FAILED",
                    "Could not switch to language: " + languageCode,
                    "Language switch failed, cannot test decimal separator for " + config.languageName);
            testReport.append("  [FAIL] Cannot test - language switch failed\n");
            return;
        }

        List<WebElement> priceElements = driver.findElements(By.cssSelector(
                ".price, .product-price, .current-price, [itemprop='price']"));
        List<String> pricesWithDecimals = new ArrayList<>();

        for (WebElement el : priceElements) {
            String text = el.getText().trim();
            // Tim gia co phan thap phan
            if (text.matches(".*\\d+[.,]\\d{2}.*")) {
                pricesWithDecimals.add(text);
            }
        }

        testReport.append("  Found ").append(pricesWithDecimals.size()).append(" prices with decimals\n");

        if (pricesWithDecimals.isEmpty()) {
            testReport.append("  [WARN] No prices with decimals found\n");
            return;
        }

        // Phan tich separator thuc te
        int dotCount = 0;
        int commaCount = 0;

        for (String price : pricesWithDecimals) {
            // Tim dau phan cach thap phan (so cuoi cung truoc 2 chu so)
            if (price.matches(".*\\d+\\.\\d{2}.*")) {
                dotCount++;
            }
            if (price.matches(".*\\d+,\\d{2}.*")) {
                commaCount++;
            }
        }

        String actualSeparator = dotCount > commaCount ? "." : ",";

        testReport.append("  Prices with dot (.): ").append(dotCount).append("\n");
        testReport.append("  Prices with comma (,): ").append(commaCount).append("\n");
        testReport.append("  Actual separator used: ").append(actualSeparator).append("\n");
        testReport.append("  Sample prices: ")
                .append(pricesWithDecimals.subList(0, Math.min(3, pricesWithDecimals.size()))).append("\n");

        // So sanh voi expected
        if (!actualSeparator.equals(config.decimalSeparator)) {
            recordError("DECIMAL_SEPARATOR",
                    "Wrong decimal separator for " + languageCode,
                    "Expected '" + config.decimalSeparator + "' but found '" + actualSeparator + "'. " +
                            "Prices: " + pricesWithDecimals.subList(0, Math.min(3, pricesWithDecimals.size())));
        } else {
            testReport.append("  [OK] Decimal separator is correct\n");
        }
    }

    @Test
    public void testRTLFlag() {
        System.out.println("[TEST] RTL Flag for " + languageCode);

        if (testGroup == LanguageConfig.TestGroup.RTL) {
            Assert.assertTrue("RTL group should have isRTL = true", config.isRTL);
        } else {
            Assert.assertFalse("Non-RTL group should have isRTL = false", config.isRTL);
        }
    }

    // ==================== UTILITY: GET LANGUAGES BY GROUP ====================

    /**
     * Lay tat ca ngon ngu trong mot nhom de test chi tiet
     */
    public static List<LanguageConfig> getLanguagesForDetailedTest(LanguageConfig.TestGroup group) {
        return LanguageConfig.getByTestGroup(group);
    }

    /**
     * Kiem tra xem ngon ngu co trong nhom mong doi khong
     */
    public static boolean isLanguageInGroup(String langCode, LanguageConfig.TestGroup expectedGroup) {
        LanguageConfig config = LanguageConfig.get(langCode);
        return config != null && config.testGroup == expectedGroup;
    }
}
