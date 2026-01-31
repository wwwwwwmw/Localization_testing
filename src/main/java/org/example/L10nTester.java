package org.example;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * L10n Tester - Kiem tra Localization cho PrestaShop
 * Bao cao CHI TIET ve van ban da dich va chua dich
 */
public class L10nTester {

    private static final String PRESTASHOP_URL = "https://demo.prestashop.com/";

    private WebDriver driver;
    private WebDriverWait wait;
    private JavascriptExecutor js;
    private List<TextCheckResult> textResults;
    private List<TestResult> testResults;
    private PrintWriter logWriter;
    private String reportDir;
    private String currentLanguage;
    private Scanner scanner;

    // Danh sach cac tu/cum tu tieng Anh can kiem tra
    private static final String[] ENGLISH_TEXTS = {
            // Navigation & Menu
            "Home", "Clothes", "Men", "Women", "Accessories", "Art",
            // Buttons
            "Add to cart", "Buy now", "Quick view", "Read more", "Learn more", "View details",
            // User actions
            "Sign in", "Sign up", "Log in", "Register", "My account", "Log out",
            // Shopping
            "Cart", "Shopping cart", "Checkout", "Proceed to checkout",
            // Product info
            "In stock", "Out of stock", "Available", "Unavailable",
            "New", "Sale", "Best seller", "Popular",
            // Filters
            "Filter", "Sort by", "Price", "Size", "Color", "Brand",
            // Footer
            "About us", "Contact us", "Terms and conditions", "Privacy policy",
            "Our company", "Your account", "Store information",
            // Search
            "Search", "Search our catalog", "No results",
            // Messages
            "Free shipping", "Discount", "Save", "Tax included",
            // Categories
            "Categories", "Products", "All products",
            // Other common
            "Description", "Details", "Reviews", "Quantity"
    };

    public static void main(String[] args) {
        L10nTester tester = new L10nTester();
        tester.run();
    }

    public void run() {
        scanner = new Scanner(System.in);

        printHeader();

        while (true) {
            try {
                String langCode = askForLanguageCode();
                if (langCode == null) {
                    System.out.println("\n[THOAT] Cam on ban da su dung L10N Tester!");
                    break;
                }

                setup();
                openWebsiteAndSwitchLanguage(langCode);

                int testMode = askForTestMode();

                if (testMode == 1) {
                    testCurrentPageDetailed();
                } else if (testMode == 2) {
                    testAllPagesDetailed();
                }

                showDetailedResults();

                if (!askContinue()) {
                    System.out.println("\n[THOAT] Cam on ban da su dung L10N Tester!");
                    break;
                }

            } catch (Exception e) {
                System.err.println("[LOI] " + e.getMessage());
                e.printStackTrace();
            } finally {
                cleanup();
            }
        }

        scanner.close();
    }

    private void printHeader() {
        System.out.println("+============================================================+");
        System.out.println("|           L10N TESTER - PRESTASHOP DEMO                    |");
        System.out.println("|  Kiem tra chi tiet: Van ban da dich / chua dich            |");
        System.out.println("+============================================================+");
        System.out.println();
        printSupportedLanguages();
    }

    private void printSupportedLanguages() {
        System.out.println("Cac ngon ngu duoc ho tro:");
        System.out.println("-------------------------------------------------------------");
        String[] codes = LanguageConfig.getSupportedLanguages();
        Arrays.sort(codes);
        StringBuilder sb = new StringBuilder();
        int count = 0;
        for (String code : codes) {
            sb.append(String.format("%-5s", code));
            count++;
            if (count % 12 == 0) {
                System.out.println(sb.toString());
                sb = new StringBuilder();
            }
        }
        if (sb.length() > 0) {
            System.out.println(sb.toString());
        }
        System.out.println("-------------------------------------------------------------");
    }

    private String askForLanguageCode() {
        System.out.println();
        System.out.println("+-------------------------------------------------------------+");
        System.out.println("| NHAP MA NGON NGU CAN KIEM TRA (vd: en, vi, ja, ko, fr...)   |");
        System.out.println("| Nhap 'exit' de thoat                                        |");
        System.out.println("+-------------------------------------------------------------+");
        System.out.print(">> Ma ngon ngu: ");

        String input = scanner.nextLine().trim().toLowerCase();

        if (input.equals("exit") || input.equals("quit") || input.equals("q")) {
            return null;
        }

        if (!LanguageConfig.isSupported(input)) {
            System.out.println("[LOI] Ma ngon ngu '" + input + "' khong duoc ho tro!");
            return askForLanguageCode();
        }

        currentLanguage = input;
        return input;
    }

    private int askForTestMode() {
        System.out.println();
        System.out.println("+-------------------------------------------------------------+");
        System.out.println("| CHON CHE DO KIEM TRA                                        |");
        System.out.println("| 1. Kiem tra TRANG HIEN TAI                                  |");
        System.out.println("| 2. Kiem tra TU DONG TOAN BO (Homepage + Product + Cart)     |");
        System.out.println("+-------------------------------------------------------------+");
        System.out.print(">> Lua chon (1 hoac 2): ");

        String input = scanner.nextLine().trim();

        if (input.equals("1"))
            return 1;
        if (input.equals("2"))
            return 2;

        System.out.println("[LOI] Vui long nhap 1 hoac 2!");
        return askForTestMode();
    }

    private boolean askContinue() {
        System.out.println();
        System.out.println("+-------------------------------------------------------------+");
        System.out.println("| TIEP TUC KIEM TRA NGON NGU KHAC?                            |");
        System.out.println("| 1. Co      2. Khong (Thoat)                                 |");
        System.out.println("+-------------------------------------------------------------+");
        System.out.print(">> Lua chon: ");

        String input = scanner.nextLine().trim();
        return input.equals("1") || input.equalsIgnoreCase("y");
    }

    private void setup() throws Exception {
        reportDir = "report/" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        Files.createDirectories(Paths.get(reportDir));

        String logFile = reportDir + "/l10n_report.txt";
        logWriter = new PrintWriter(new OutputStreamWriter(new FileOutputStream(logFile), StandardCharsets.UTF_8),
                true);

        textResults = new ArrayList<>();
        testResults = new ArrayList<>();

        System.out.println("\n[SETUP] Dang khoi tao Chrome...");
        WebDriverManager.chromedriver().setup();

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--start-maximized");
        options.addArguments("--disable-notifications");
        options.addArguments("--remote-allow-origins=*");

        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        js = (JavascriptExecutor) driver;

        log("============================================================");
        log("L10N TEST REPORT");
        log("Thoi gian: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        log("URL: " + PRESTASHOP_URL);
        log("============================================================");
    }

    private void openWebsiteAndSwitchLanguage(String langCode) {
        LanguageConfig config = LanguageConfig.get(langCode);

        System.out.println("[DANG TAI] " + PRESTASHOP_URL);
        driver.get(PRESTASHOP_URL);
        sleep(5000);

        if (!switchToIframe()) {
            System.err.println("[LOI] Khong the tim thay iframe!");
            return;
        }

        System.out.println("[CHUYEN NGON NGU] " + toAscii(config.languageName) + " (" + langCode + ")");
        log("\n============================================================");
        log("NGON NGU KIEM TRA: " + config.languageName + " (" + langCode + ")");
        log("============================================================");

        switchLanguage(langCode);
        sleep(3000);

        String actualLang = detectCurrentLanguage();
        if (actualLang != null && actualLang.equals(langCode)) {
            System.out.println("[OK] Da chuyen thanh cong sang: " + langCode);
        } else {
            System.out.println("[CANH BAO] Ngon ngu hien tai: " + (actualLang != null ? actualLang : "N/A"));
        }
    }

    private String detectCurrentLanguage() {
        try {
            String currentUrl = driver.getCurrentUrl();
            if (currentUrl.contains("/#/")) {
                String[] parts = currentUrl.split("/#/");
                if (parts.length > 1) {
                    return parts[1].split("/")[0];
                }
            }
        } catch (Exception e) {
        }
        return null;
    }

    // ==================== KIEM TRA CHI TIET ====================

    private void testCurrentPageDetailed() {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("KIEM TRA CHI TIET TRANG HIEN TAI");
        System.out.println("=".repeat(60));

        log("\n------------------------------------------------------------");
        log("KIEM TRA TRANG: Homepage");
        log("------------------------------------------------------------");

        LanguageConfig config = LanguageConfig.get(currentLanguage);

        // Kiem tra URL va HTML lang
        checkLanguageSettings(config, "Homepage");

        // Kiem tra Page Title
        checkPageTitle(config, "Homepage");

        // Kiem tra tien te
        checkCurrency(config, "Homepage");

        // Scroll va kiem tra van ban chi tiet
        System.out.println("\n[KIEM TRA VAN BAN CHI TIET]");
        log("\n[VAN BAN CHI TIET]");

        scrollToTop();
        checkSectionText("HEADER", "#header, .header, #_desktop_top_menu", config, "Homepage");

        scrollBy(300);
        checkSectionText("MENU", "nav, .top-menu, .menu", config, "Homepage");

        scrollBy(300);
        checkSectionText("MAIN CONTENT", "#content, .main-content, #wrapper", config, "Homepage");

        scrollBy(300);
        checkSectionText("SIDEBAR", ".sidebar, #left-column, #right-column", config, "Homepage");

        scrollToBottom();
        checkSectionText("FOOTER", "#footer, .footer, footer", config, "Homepage");
    }

    private void testAllPagesDetailed() {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("KIEM TRA CHI TIET TAT CA TRANG");
        System.out.println("=".repeat(60));

        LanguageConfig config = LanguageConfig.get(currentLanguage);

        // === HOMEPAGE ===
        System.out.println("\n>>> TRANG 1: HOMEPAGE");
        log("\n------------------------------------------------------------");
        log("KIEM TRA TRANG: Homepage");
        log("------------------------------------------------------------");

        checkLanguageSettings(config, "Homepage");
        checkPageTitle(config, "Homepage");
        checkCurrency(config, "Homepage");

        scrollToTop();
        checkSectionText("HEADER", "#header, .header", config, "Homepage");
        checkSectionText("NAVIGATION", "nav, .top-menu", config, "Homepage");

        scrollBy(400);
        checkSectionText("PRODUCTS SECTION", ".products, .product-miniature", config, "Homepage");

        scrollToBottom();
        checkSectionText("FOOTER", "#footer, .footer", config, "Homepage");

        // === PRODUCT PAGE ===
        System.out.println("\n>>> TRANG 2: PRODUCT DETAIL");
        log("\n------------------------------------------------------------");
        log("KIEM TRA TRANG: Product Detail");
        log("------------------------------------------------------------");

        scrollToTop();
        if (navigateToProductPage()) {
            sleep(2000);

            checkLanguageSettings(config, "Product");
            checkPageTitle(config, "Product");
            checkCurrency(config, "Product");

            checkSectionText("BREADCRUMB", ".breadcrumb", config, "Product");
            checkSectionText("PRODUCT INFO", ".product-information, .product-details", config, "Product");
            checkSectionText("ADD TO CART AREA", ".product-actions, .add-to-cart", config, "Product");
            checkSectionText("PRODUCT DESCRIPTION", ".product-description, #description", config, "Product");

            scrollToBottom();
            checkSectionText("PRODUCT FOOTER", ".product-additional-info", config, "Product");
        }

        // === CATEGORY PAGE ===
        System.out.println("\n>>> TRANG 3: CATEGORY PAGE");
        log("\n------------------------------------------------------------");
        log("KIEM TRA TRANG: Category");
        log("------------------------------------------------------------");

        if (navigateToCategoryPage()) {
            sleep(2000);

            checkLanguageSettings(config, "Category");
            checkPageTitle(config, "Category");

            checkSectionText("CATEGORY HEADER", ".category-header, #category-description", config, "Category");
            checkSectionText("FILTERS", ".facets, #search_filters", config, "Category");
            checkSectionText("PRODUCT LISTING", ".products, .product-list", config, "Category");
            checkSectionText("PAGINATION", ".pagination", config, "Category");
        }

        // === CART ===
        System.out.println("\n>>> TRANG 4: CART");
        log("\n------------------------------------------------------------");
        log("KIEM TRA TRANG: Cart");
        log("------------------------------------------------------------");

        if (navigateToCart()) {
            sleep(2000);

            checkLanguageSettings(config, "Cart");
            checkPageTitle(config, "Cart");
            checkCurrency(config, "Cart");

            checkSectionText("CART CONTENT", ".cart-container, #cart", config, "Cart");
            checkSectionText("CART SUMMARY", ".cart-summary", config, "Cart");
        }
    }

    /**
     * Kiem tra van ban trong mot section cu the
     */
    private void checkSectionText(String sectionName, String cssSelector, LanguageConfig config, String pageName) {
        System.out.println("\n  [" + sectionName + "]");
        log("\n  [" + sectionName + "]");

        try {
            List<WebElement> elements = driver.findElements(By.cssSelector(cssSelector));

            if (elements.isEmpty()) {
                System.out.println("    (Khong tim thay section nay)");
                return;
            }

            StringBuilder sectionText = new StringBuilder();
            for (WebElement el : elements) {
                try {
                    sectionText.append(el.getText()).append(" ");
                } catch (Exception ignored) {
                }
            }

            String text = sectionText.toString();

            // Neu la tieng Anh thi khong can kiem tra
            if (currentLanguage.equals("en")) {
                System.out.println("    (Ngon ngu EN - khong can kiem tra dich)");
                return;
            }

            // Kiem tra tung tu/cum tu tieng Anh
            List<String> foundEnglish = new ArrayList<>();
            List<String> notFound = new ArrayList<>();

            for (String engText : ENGLISH_TEXTS) {
                if (text.contains(engText)) {
                    foundEnglish.add(engText);
                }
            }

            if (foundEnglish.isEmpty()) {
                System.out.println("    [PASS] Khong tim thay text tieng Anh chua dich");
                log("    PASS - Khong co text tieng Anh");
            } else {
                System.out.println("    [FAIL] Tim thay " + foundEnglish.size() + " text tieng Anh CHUA DICH:");
                log("    FAIL - Tim thay " + foundEnglish.size() + " text chua dich:");

                for (String eng : foundEnglish) {
                    System.out.println("      - \"" + eng + "\"");
                    log("      - \"" + eng + "\"");

                    textResults.add(new TextCheckResult(
                            pageName,
                            sectionName,
                            eng,
                            "CHUA DICH - Van la tieng Anh",
                            false));

                    testResults.add(new TestResult("TEXT_" + sectionName, pageName,
                            "Text '" + eng + "' trong " + sectionName,
                            "Phai dich sang " + toAscii(config.languageName),
                            "Van la tieng Anh: \"" + eng + "\"",
                            false));
                }
            }

        } catch (Exception e) {
            System.out.println("    [LOI] " + e.getMessage());
        }
    }

    /**
     * Kiem tra URL va HTML lang
     */
    private void checkLanguageSettings(LanguageConfig config, String pageName) {
        System.out.println("\n[KIEM TRA CAI DAT NGON NGU] " + pageName);
        log("\n[CAI DAT NGON NGU - " + pageName + "]");

        String expectedLang = currentLanguage;

        // URL
        String actualLang = detectCurrentLanguage();
        boolean urlPassed = actualLang != null && actualLang.equals(expectedLang);

        System.out.println("  URL Language:");
        System.out.println("    Mong doi: " + expectedLang);
        System.out.println("    Thuc te : " + (actualLang != null ? actualLang : "N/A"));
        System.out.println("    Ket qua : " + (urlPassed ? "PASS" : "FAIL"));

        log("  URL: Mong doi=" + expectedLang + ", Thuc te=" + actualLang + " -> " + (urlPassed ? "PASS" : "FAIL"));

        testResults.add(new TestResult("LANGUAGE_URL", pageName,
                "Ma ngon ngu trong URL",
                expectedLang,
                actualLang != null ? actualLang : "N/A",
                urlPassed));

        // HTML lang
        try {
            WebElement html = driver.findElement(By.tagName("html"));
            String htmlLang = html.getAttribute("lang");

            if (htmlLang != null && !htmlLang.isEmpty()) {
                boolean htmlPassed = htmlLang.toLowerCase().startsWith(expectedLang.toLowerCase());

                System.out.println("  HTML lang:");
                System.out.println("    Mong doi: " + expectedLang);
                System.out.println("    Thuc te : " + htmlLang);
                System.out.println("    Ket qua : " + (htmlPassed ? "PASS" : "FAIL"));

                log("  HTML lang: Mong doi=" + expectedLang + ", Thuc te=" + htmlLang + " -> "
                        + (htmlPassed ? "PASS" : "FAIL"));

                testResults.add(new TestResult("LANGUAGE_HTML", pageName,
                        "HTML lang attribute",
                        expectedLang,
                        htmlLang,
                        htmlPassed));
            }
        } catch (Exception e) {
        }
    }

    /**
     * Kiem tra Page Title
     */
    private void checkPageTitle(LanguageConfig config, String pageName) {
        System.out.println("\n[KIEM TRA PAGE TITLE] " + pageName);
        log("\n[PAGE TITLE - " + pageName + "]");

        try {
            driver.switchTo().defaultContent();
            switchToIframe();

            String pageTitle = driver.getTitle();
            String displayTitle = toAscii(pageTitle);

            boolean hasEnglishTitle = false;
            if (!currentLanguage.equals("en") && pageTitle != null) {
                String[] englishTitles = { "Home", "Product", "Cart", "Shopping Cart", "My Store", "Category" };
                for (String eng : englishTitles) {
                    if (pageTitle.toLowerCase().contains(eng.toLowerCase())) {
                        hasEnglishTitle = true;
                        break;
                    }
                }
            }

            boolean passed = !hasEnglishTitle;

            System.out.println("  Title: " + displayTitle);
            System.out.println("  Ket qua: " + (passed ? "PASS" : "FAIL - Title chua dich"));

            log("  Title: " + pageTitle + " -> " + (passed ? "PASS" : "FAIL"));

            testResults.add(new TestResult("PAGE_TITLE", pageName,
                    "Kiem tra Page Title",
                    "Title phai dich sang " + toAscii(config.languageName),
                    displayTitle,
                    passed));

        } catch (Exception e) {
            System.out.println("  [LOI] " + e.getMessage());
        }
    }

    /**
     * Kiem tra tien te
     */
    private void checkCurrency(LanguageConfig config, String pageName) {
        System.out.println("\n[KIEM TRA TIEN TE] " + pageName);
        log("\n[TIEN TE - " + pageName + "]");

        try {
            List<WebElement> priceElements = driver.findElements(By.cssSelector(
                    ".price, .product-price, .current-price, [class*='price']"));

            if (priceElements.isEmpty()) {
                System.out.println("  (Khong tim thay gia tien)");
                return;
            }

            Set<String> checkedPrices = new HashSet<>();
            String expectedCurrency = config.primaryCurrency;
            int passCount = 0;
            int failCount = 0;

            for (WebElement element : priceElements) {
                try {
                    String priceText = element.getText().trim();
                    if (priceText.isEmpty() || checkedPrices.contains(priceText) || priceText.length() > 50)
                        continue;
                    checkedPrices.add(priceText);

                    boolean hasExpectedCurrency = priceText.contains(expectedCurrency);
                    String actualCurrency = detectCurrency(priceText);
                    boolean passed = hasExpectedCurrency;
                    String displayPrice = toAscii(priceText);

                    if (passed) {
                        passCount++;
                    } else {
                        failCount++;
                        System.out.println("  [FAIL] " + displayPrice + " - Mong doi: " + expectedCurrency
                                + ", Thuc te: " + actualCurrency);
                        log("  FAIL: " + priceText + " - Mong doi: " + expectedCurrency + ", Thuc te: "
                                + actualCurrency);
                    }

                    testResults.add(new TestResult("CURRENCY", pageName,
                            "Tien te: " + displayPrice,
                            expectedCurrency,
                            actualCurrency,
                            passed));

                } catch (StaleElementReferenceException ignored) {
                }
            }

            System.out.println("  Tong ket: " + passCount + " PASS, " + failCount + " FAIL");
            log("  Tong ket: " + passCount + " PASS, " + failCount + " FAIL");

        } catch (Exception e) {
            System.out.println("  [LOI] " + e.getMessage());
        }
    }

    private String detectCurrency(String priceText) {
        if (priceText.contains("€"))
            return "EUR (€)";
        if (priceText.contains("$"))
            return "USD ($)";
        if (priceText.contains("£"))
            return "GBP (£)";
        if (priceText.contains("¥"))
            return "JPY/CNY (¥)";
        if (priceText.contains("₩"))
            return "KRW (₩)";
        if (priceText.contains("₫"))
            return "VND (₫)";
        if (priceText.contains("฿"))
            return "THB (฿)";
        return "N/A";
    }

    // ==================== KET QUA CHI TIET ====================

    private void showDetailedResults() {
        System.out.println("\n\n");
        System.out.println("╔════════════════════════════════════════════════════════════╗");
        System.out.println("║              BAO CAO KET QUA KIEM TRA L10N                 ║");
        System.out.println("╚════════════════════════════════════════════════════════════╝");

        log("\n\n============================================================");
        log("BAO CAO KET QUA KIEM TRA L10N");
        log("============================================================");

        // Thong ke tong hop
        int totalPass = 0;
        int totalFail = 0;
        Map<String, int[]> statsByType = new LinkedHashMap<>();

        for (TestResult result : testResults) {
            if (result.passed)
                totalPass++;
            else
                totalFail++;

            String type = result.type.split("_")[0]; // Lay phan dau cua type
            statsByType.computeIfAbsent(type, k -> new int[] { 0, 0 });
            if (result.passed)
                statsByType.get(type)[0]++;
            else
                statsByType.get(type)[1]++;
        }

        System.out.println("\n┌─────────────────────────────────────────────────────────────┐");
        System.out.println("│ THONG KE TONG HOP                                           │");
        System.out.println("├─────────────────────────────────────────────────────────────┤");
        System.out.println(String.format("│ Tong so kiem tra: %-40d │", testResults.size()));
        System.out.println(String.format("│ PASS: %-52d │", totalPass));
        System.out.println(String.format("│ FAIL: %-52d │", totalFail));
        System.out.println("└─────────────────────────────────────────────────────────────┘");

        log("\nTONG SO KIEM TRA: " + testResults.size());
        log("PASS: " + totalPass);
        log("FAIL: " + totalFail);

        // Thong ke theo loai
        System.out.println("\n┌─────────────────────────────────────────────────────────────┐");
        System.out.println("│ THONG KE THEO LOAI                                          │");
        System.out.println("├─────────────────────────────────────────────────────────────┤");

        log("\nTHONG KE THEO LOAI:");

        for (Map.Entry<String, int[]> entry : statsByType.entrySet()) {
            String line = String.format("│ %-15s: %3d PASS, %3d FAIL",
                    entry.getKey(), entry.getValue()[0], entry.getValue()[1]);
            System.out.println(line + " ".repeat(Math.max(0, 60 - line.length())) + "│");
            log("  " + entry.getKey() + ": " + entry.getValue()[0] + " PASS, " + entry.getValue()[1] + " FAIL");
        }
        System.out.println("└─────────────────────────────────────────────────────────────┘");

        // Chi tiet text chua dich
        if (!textResults.isEmpty()) {
            System.out.println("\n┌─────────────────────────────────────────────────────────────┐");
            System.out.println("│ CHI TIET VAN BAN CHUA DICH                                  │");
            System.out.println("└─────────────────────────────────────────────────────────────┘");

            log("\nCHI TIET VAN BAN CHUA DICH:");

            // Group by page
            Map<String, List<TextCheckResult>> byPage = new LinkedHashMap<>();
            for (TextCheckResult r : textResults) {
                byPage.computeIfAbsent(r.page, k -> new ArrayList<>()).add(r);
            }

            for (Map.Entry<String, List<TextCheckResult>> entry : byPage.entrySet()) {
                System.out.println("\n  [TRANG: " + entry.getKey() + "]");
                log("\n  [TRANG: " + entry.getKey() + "]");

                // Group by section
                Map<String, List<String>> bySection = new LinkedHashMap<>();
                for (TextCheckResult r : entry.getValue()) {
                    bySection.computeIfAbsent(r.section, k -> new ArrayList<>()).add(r.englishText);
                }

                for (Map.Entry<String, List<String>> secEntry : bySection.entrySet()) {
                    System.out.println("    " + secEntry.getKey() + ":");
                    log("    " + secEntry.getKey() + ":");

                    for (String text : secEntry.getValue()) {
                        System.out.println("      - \"" + text + "\" -> CHUA DICH (van la tieng Anh)");
                        log("      - \"" + text + "\" -> CHUA DICH");
                    }
                }
            }
        }

        // Chi tiet tat ca FAIL
        List<TestResult> failedTests = new ArrayList<>();
        for (TestResult r : testResults) {
            if (!r.passed)
                failedTests.add(r);
        }

        if (!failedTests.isEmpty()) {
            System.out.println("\n┌─────────────────────────────────────────────────────────────┐");
            System.out.println("│ DANH SACH TAT CA TEST FAIL                                  │");
            System.out.println("└─────────────────────────────────────────────────────────────┘");

            log("\nDANH SACH TAT CA TEST FAIL:");

            int i = 1;
            for (TestResult result : failedTests) {
                System.out.println(String.format("\n%d. [%s] %s", i, result.type, result.testName));
                System.out.println("   Trang    : " + result.page);
                System.out.println("   Mong doi : " + result.expected);
                System.out.println("   Thuc te  : " + result.actual);

                log(String.format("\n%d. [%s] %s", i, result.type, result.testName));
                log("   Trang: " + result.page);
                log("   Mong doi: " + result.expected);
                log("   Thuc te: " + result.actual);

                i++;
            }
        }

        // Ket luan
        System.out.println("\n" + "═".repeat(60));
        if (totalFail == 0) {
            System.out.println("✓ TAT CA TEST PASS - KHONG TIM THAY LOI LOCALIZATION!");
        } else {
            System.out.println("✗ TIM THAY " + totalFail + " LOI LOCALIZATION CAN SUA!");
        }
        System.out.println("═".repeat(60));

        log("\n" + "=".repeat(60));
        log(totalFail == 0 ? "KET LUAN: PASS" : "KET LUAN: FAIL - " + totalFail + " loi");
        log("=".repeat(60));

        System.out.println("\n[BAO CAO] Da luu tai: " + reportDir + "/l10n_report.txt");
    }

    // ==================== SCROLL & NAVIGATION ====================

    private void scrollToTop() {
        js.executeScript("window.scrollTo(0, 0);");
    }

    private void scrollToBottom() {
        js.executeScript("window.scrollTo(0, document.body.scrollHeight);");
    }

    private void scrollBy(int pixels) {
        js.executeScript("window.scrollBy(0, " + pixels + ");");
        sleep(300);
    }

    private boolean navigateToProductPage() {
        try {
            scrollToTop();
            WebElement product = wait.until(ExpectedConditions.elementToBeClickable(
                    By.cssSelector(
                            ".product-miniature a.thumbnail, .product-miniature .product-thumbnail, .product-miniature a")));
            product.click();
            sleep(3000);
            return true;
        } catch (Exception e) {
            System.out.println("  [CANH BAO] Khong the vao trang san pham");
            return false;
        }
    }

    private boolean navigateToCategoryPage() {
        try {
            scrollToTop();
            // Click vao menu Clothes hoac bat ky category nao
            WebElement categoryLink = wait.until(ExpectedConditions.elementToBeClickable(
                    By.cssSelector(".top-menu a[href*='category'], .category-top-menu a, nav a[href*='clothes']")));
            categoryLink.click();
            sleep(3000);
            return true;
        } catch (Exception e) {
            System.out.println("  [CANH BAO] Khong the vao trang category");
            return false;
        }
    }

    private boolean navigateToCart() {
        try {
            try {
                WebElement addToCart = driver.findElement(By.cssSelector(
                        ".add-to-cart, button[data-button-action='add-to-cart']"));
                addToCart.click();
                sleep(2000);
            } catch (Exception ignored) {
            }

            WebElement cartLink = wait.until(ExpectedConditions.elementToBeClickable(
                    By.cssSelector(".cart-preview, a[href*='cart'], .blockcart")));
            cartLink.click();
            sleep(3000);
            return true;
        } catch (Exception e) {
            System.out.println("  [CANH BAO] Khong the vao trang gio hang");
            return false;
        }
    }

    // ==================== UTILITIES ====================

    // Map tu ISO code sang PrestaShop code (neu khac nhau)
    private static final Map<String, String> LANG_CODE_MAP = new HashMap<>();
    static {
        LANG_CODE_MAP.put("vi", "vn"); // Tieng Viet: ISO=vi, PrestaShop=vn
        LANG_CODE_MAP.put("sl", "si"); // Slovenian: ISO=sl, PrestaShop=si
        // Them cac mapping khac neu can
    }

    private String getPrestaShopLangCode(String isoCode) {
        return LANG_CODE_MAP.getOrDefault(isoCode, isoCode);
    }

    private boolean switchLanguage(String langCode) {
        System.out.println("[DEBUG] Dang chuyen ngon ngu sang: " + langCode);

        // Map sang PrestaShop code neu can
        String psLangCode = getPrestaShopLangCode(langCode);
        if (!psLangCode.equals(langCode)) {
            System.out.println("[DEBUG] Map '" + langCode + "' -> '" + psLangCode + "' (PrestaShop code)");
        }

        // Chi su dung phuong phap click dropdown
        try {
            // Tim nut dropdown ngon ngu
            WebElement langButton = null;
            String[] buttonSelectors = {
                    ".language-selector button",
                    ".language-selector-wrapper button",
                    "#_desktop_language_selector button",
                    ".language-selector .expand-more",
                    "button.dropdown-toggle",
                    ".language-selector > div > button"
            };

            for (String selector : buttonSelectors) {
                try {
                    langButton = new WebDriverWait(driver, Duration.ofSeconds(5))
                            .until(ExpectedConditions.elementToBeClickable(By.cssSelector(selector)));
                    if (langButton != null && langButton.isDisplayed()) {
                        System.out.println("[DEBUG] Tim thay nut ngon ngu: " + selector);
                        break;
                    }
                } catch (Exception ignored) {
                }
            }

            if (langButton == null) {
                System.out.println("[DEBUG] Khong tim thay nut chuyen ngon ngu! Thu tim bang text...");
                // Thu tim bang text "English" hoac ten ngon ngu hien tai
                List<WebElement> buttons = driver.findElements(By.cssSelector("button"));
                for (WebElement btn : buttons) {
                    String text = btn.getText().toLowerCase();
                    if (text.contains("english") || text.contains("français") || text.contains("deutsch") ||
                            text.contains("español") || text.contains("tiếng việt") || text.contains("日本語") ||
                            text.contains("한국어") || text.contains("中文")) {
                        langButton = btn;
                        System.out.println("[DEBUG] Tim thay nut ngon ngu bang text: " + text);
                        break;
                    }
                }
            }

            if (langButton == null) {
                System.out.println("[LOI] Khong the tim thay nut chuyen ngon ngu!");
                return false;
            }

            // Scroll to element va click mo dropdown
            js.executeScript("arguments[0].scrollIntoView({block: 'center'});", langButton);
            sleep(500);

            System.out.println("[DEBUG] Click mo dropdown...");
            try {
                langButton.click();
            } catch (Exception e) {
                js.executeScript("arguments[0].click();", langButton);
            }
            sleep(1500);

            // Tim option ngon ngu trong dropdown
            List<WebElement> langOptions = driver.findElements(By.cssSelector(
                    ".language-selector .dropdown-menu a, " +
                            ".language-selector ul a, " +
                            ".dropdown-item, " +
                            ".language-selector li a, " +
                            ".language-selector .dropdown-menu li a"));

            System.out.println("[DEBUG] Tim thay " + langOptions.size() + " options ngon ngu");

            // In ra tat ca options de debug
            for (WebElement option : langOptions) {
                try {
                    String href = option.getAttribute("href");
                    String text = option.getText().trim();
                    String dataLang = option.getAttribute("data-iso-code");
                    System.out.println("[DEBUG] Option: text='" + text + "', href=" + href + ", data-iso=" + dataLang);
                } catch (Exception ignored) {
                }
            }

            // Tim option phu hop voi langCode
            LanguageConfig config = LanguageConfig.get(langCode);
            String targetLangName = config != null ? config.languageName : langCode;

            for (WebElement option : langOptions) {
                try {
                    String href = option.getAttribute("href");
                    String text = option.getText().trim().toLowerCase();
                    String dataLang = option.getAttribute("data-iso-code");

                    // Kiem tra nhieu dieu kien - su dung ca ISO code va PrestaShop code
                    boolean matchByHref = href != null && (href.contains("/" + langCode + "/") ||
                            href.endsWith("/" + langCode) ||
                            href.contains("/" + psLangCode + "/") ||
                            href.endsWith("/" + psLangCode));
                    boolean matchByDataIso = dataLang != null && (dataLang.equalsIgnoreCase(langCode) ||
                            dataLang.equalsIgnoreCase(psLangCode));
                    boolean matchByText = text.contains(targetLangName.toLowerCase()) ||
                            text.equalsIgnoreCase(langCode);

                    if (matchByHref || matchByDataIso || matchByText) {
                        System.out.println("[DEBUG] Click vao option: " + text + " (match: href=" + matchByHref
                                + ", data-iso=" + matchByDataIso + ", text=" + matchByText + ")");

                        // Thu click bang nhieu cach
                        try {
                            option.click();
                        } catch (Exception e) {
                            js.executeScript("arguments[0].click();", option);
                        }

                        sleep(4000);
                        switchToIframe();

                        // Verify
                        String newUrl = driver.getCurrentUrl();
                        System.out.println("[DEBUG] URL sau khi click: " + newUrl);

                        return true;
                    }
                } catch (Exception e) {
                    System.out.println("[DEBUG] Loi khi xu ly option: " + e.getMessage());
                }
            }

            System.out.println("[LOI] Khong tim thay option cho ngon ngu: " + langCode);

            // Dong dropdown neu mo
            try {
                langButton.click();
            } catch (Exception ignored) {
            }

            return false;

        } catch (Exception e) {
            System.out.println("[LOI] Loi khi chuyen ngon ngu: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private boolean switchToIframe() {
        try {
            driver.switchTo().defaultContent();
            sleep(500);

            WebDriverWait iframeWait = new WebDriverWait(driver, Duration.ofSeconds(10));
            String[] iframeSelectors = { "#framelive", "iframe[name='framelive']", "iframe" };

            for (String selector : iframeSelectors) {
                try {
                    WebElement iframe = iframeWait.until(
                            ExpectedConditions.presenceOfElementLocated(By.cssSelector(selector)));
                    iframeWait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(iframe));
                    System.out.println("[OK] Da chuyen vao iframe");
                    sleep(500);
                    return true;
                } catch (Exception ignored) {
                }
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    private String toAscii(String text) {
        if (text == null)
            return "";
        return text
                .replace("€", "EUR").replace("£", "GBP").replace("¥", "JPY")
                .replace("₩", "KRW").replace("₫", "VND").replace("฿", "THB")
                .replace("₹", "INR").replace("₽", "RUB")
                .replaceAll("[^\\x00-\\x7F]", "?");
    }

    private void log(String message) {
        if (logWriter != null)
            logWriter.println(message);
    }

    private void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ignored) {
        }
    }

    private void cleanup() {
        if (logWriter != null) {
            logWriter.close();
            logWriter = null;
        }
        if (driver != null) {
            driver.quit();
            driver = null;
        }
    }

    // ==================== INNER CLASSES ====================

    static class TestResult {
        String type, page, testName, expected, actual;
        boolean passed;

        TestResult(String type, String page, String testName, String expected, String actual, boolean passed) {
            this.type = type;
            this.page = page;
            this.testName = testName;
            this.expected = expected;
            this.actual = actual;
            this.passed = passed;
        }
    }

    static class TextCheckResult {
        String page;
        String section;
        String englishText;
        String status;
        boolean passed;

        TextCheckResult(String page, String section, String englishText, String status, boolean passed) {
            this.page = page;
            this.section = section;
            this.englishText = englishText;
            this.status = status;
            this.passed = passed;
        }
    }
}
