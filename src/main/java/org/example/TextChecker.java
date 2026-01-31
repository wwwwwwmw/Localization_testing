package org.example;

import org.openqa.selenium.*;
import java.util.*;

/**
 * Kiem tra van ban / dich thuat
 * 
 * Da tach cac ham static de ho tro Unit Test khong can Selenium Driver
 * Da them Boundary Value Analysis (BVA) de kiem tra text overflow
 */
public class TextChecker {

    private WebDriver driver;
    private String currentLanguage;
    private List<L10nError> errors;
    private JavascriptExecutor js;

    // ==================== STATIC DATA ====================

    // Van ban tieng Anh khong mong muon trong cac trang khong phai EN
    private static final String[] ENGLISH_ONLY_WORDS = {
            "Add to cart", "Sign in", "My account", "Search our catalog",
            "PRODUCTS", "OUR COMPANY", "YOUR ACCOUNT", "STORE INFORMATION",
            "Promotions", "New products", "Best sales", "Delivery", "Legal Notice",
            "Terms and conditions", "About us", "Secure payment", "Contact us",
            "Sitemap", "Stores", "Order tracking", "Create account", "My alerts",
            "Free shipping", "Discount", "Quick view", "Add to wishlist"
    };

    // Nguong gia tri cho Boundary Value Analysis
    public static final int TEXT_LENGTH_WARNING_THRESHOLD = 50; // Canh bao neu text dai hon
    public static final int TEXT_LENGTH_ERROR_THRESHOLD = 80; // Loi neu text dai hon
    public static final double OVERFLOW_RATIO_THRESHOLD = 1.0; // scrollWidth/offsetWidth > 1.0 = tran

    // Cac ngon ngu co xu huong van ban dai
    private static final Set<String> LONG_TEXT_LANGUAGES = new HashSet<>(Arrays.asList(
            "de", "pl", "hu", "fi", "nl", "cs", "sk", "lt", "lv", "et", "hr", "sl"));

    public TextChecker(WebDriver driver, String language, List<L10nError> errors) {
        this.driver = driver;
        this.currentLanguage = language;
        this.errors = errors;
        this.js = (JavascriptExecutor) driver;
    }

    // ==================== STATIC UTILITY METHODS (cho Unit Test)
    // ====================

    /**
     * Kiem tra xem van ban co chua tu tieng Anh khong mong muon khong
     * 
     * @param text         Van ban can kiem tra
     * @param languageCode Ma ngon ngu
     * @return Danh sach tu tieng Anh tim thay
     */
    public static List<String> findUntranslatedEnglishText(String text, String languageCode) {
        List<String> found = new ArrayList<>();
        if (text == null || "en".equals(languageCode))
            return found;

        for (String englishWord : ENGLISH_ONLY_WORDS) {
            if (text.contains(englishWord)) {
                found.add(englishWord);
            }
        }
        return found;
    }

    /**
     * Kiem tra tu khoa mong doi co trong van ban khong
     * 
     * @param text             Van ban can kiem tra
     * @param expectedKeywords Danh sach tu khoa mong doi
     * @return Map<keyword, found>
     */
    public static Map<String, Boolean> checkExpectedKeywords(String text, String[] expectedKeywords) {
        Map<String, Boolean> result = new LinkedHashMap<>();
        if (text == null || expectedKeywords == null)
            return result;

        String lowerText = text.toLowerCase();
        for (String keyword : expectedKeywords) {
            result.put(keyword, lowerText.contains(keyword.toLowerCase()));
        }
        return result;
    }

    /**
     * Tinh ty le tu khoa tim thay
     * 
     * @param keywordResults Ket qua tu checkExpectedKeywords
     * @return Ty le (0.0 - 1.0)
     */
    public static double calculateKeywordCoverage(Map<String, Boolean> keywordResults) {
        if (keywordResults == null || keywordResults.isEmpty())
            return 0.0;

        long foundCount = keywordResults.values().stream().filter(v -> v).count();
        return (double) foundCount / keywordResults.size();
    }

    /**
     * Kiem tra do dai van ban (Boundary Value Analysis)
     * 
     * @param text         Van ban can kiem tra
     * @param languageCode Ma ngon ngu
     * @return TextLengthResult
     */
    public static TextLengthResult checkTextLength(String text, String languageCode) {
        TextLengthResult result = new TextLengthResult();
        result.originalText = text;
        result.length = text != null ? text.length() : 0;
        result.status = TextLengthStatus.OK;

        if (text == null || text.isEmpty()) {
            result.status = TextLengthStatus.EMPTY;
            return result;
        }

        // Ngon ngu co van ban dai hon binh thuong can nguong cao hon
        int warningThreshold = TEXT_LENGTH_WARNING_THRESHOLD;
        int errorThreshold = TEXT_LENGTH_ERROR_THRESHOLD;

        if (LONG_TEXT_LANGUAGES.contains(languageCode)) {
            // Tang nguong 30% cho cac ngon ngu co van ban dai
            warningThreshold = (int) (warningThreshold * 1.3);
            errorThreshold = (int) (errorThreshold * 1.3);
        }

        if (result.length > errorThreshold) {
            result.status = TextLengthStatus.TOO_LONG;
            result.message = "Van ban qua dai (" + result.length + " ky tu > " + errorThreshold + ")";
        } else if (result.length > warningThreshold) {
            result.status = TextLengthStatus.WARNING;
            result.message = "Van ban dai (" + result.length + " ky tu > " + warningThreshold + ")";
        }

        return result;
    }

    /**
     * Kiem tra cac gia tri bien (Boundary Value Analysis) cho do dai van ban
     * 
     * @param textMap      Map<elementId, text>
     * @param languageCode Ma ngon ngu
     * @return Danh sach ket qua BVA
     */
    public static List<TextLengthResult> performBVACheck(Map<String, String> textMap, String languageCode) {
        List<TextLengthResult> results = new ArrayList<>();
        if (textMap == null)
            return results;

        for (Map.Entry<String, String> entry : textMap.entrySet()) {
            TextLengthResult result = checkTextLength(entry.getValue(), languageCode);
            result.elementId = entry.getKey();
            results.add(result);
        }
        return results;
    }

    /**
     * Kiem tra cac ngon ngu co van ban dai co the gay tran UI
     * 
     * @param languageCode Ma ngon ngu
     * @return true neu ngon ngu co van ban dai
     */
    public static boolean isLongTextLanguage(String languageCode) {
        return LONG_TEXT_LANGUAGES.contains(languageCode);
    }

    /**
     * Du doan do dai van ban dich tu tieng Anh
     * 
     * @param englishText    Van ban tieng Anh
     * @param targetLanguage Ngon ngu dich
     * @return Do dai du doan
     */
    public static int predictTranslatedLength(String englishText, String targetLanguage) {
        if (englishText == null)
            return 0;
        int baseLength = englishText.length();

        // Ty le tang do dai trung binh
        double multiplier = 1.0;
        switch (targetLanguage) {
            case "de":
                multiplier = 1.35;
                break; // Tieng Duc dai hon 35%
            case "fi":
                multiplier = 1.40;
                break; // Tieng Phan Lan dai hon 40%
            case "pl":
                multiplier = 1.30;
                break; // Tieng Ba Lan dai hon 30%
            case "ru":
                multiplier = 1.25;
                break; // Tieng Nga dai hon 25%
            case "ja":
            case "zh":
            case "ko":
                multiplier = 0.60;
                break; // CJK ngan hon
            default:
                multiplier = 1.15; // Mac dinh tang 15%
        }

        return (int) (baseLength * multiplier);
    }

    // ==================== ENUMS & RESULT CLASSES ====================

    public enum TextLengthStatus {
        OK,
        EMPTY,
        WARNING,
        TOO_LONG
    }

    public static class TextLengthResult {
        public String originalText;
        public String elementId;
        public int length;
        public TextLengthStatus status;
        public String message;

        @Override
        public String toString() {
            return String.format("[%s] %s (len=%d) %s",
                    status, elementId != null ? elementId : "unknown", length,
                    message != null ? "- " + message : "");
        }
    }

    /**
     * Ket qua kiem tra text overflow tren UI
     */
    public static class TextOverflowResult {
        public String elementSelector;
        public String text;
        public int offsetWidth;
        public int scrollWidth;
        public boolean isOverflowing;
        public double overflowRatio;

        @Override
        public String toString() {
            return String.format("[%s] '%s' - offsetWidth=%d, scrollWidth=%d, overflow=%s (ratio=%.2f)",
                    isOverflowing ? "OVERFLOW" : "OK",
                    text != null && text.length() > 30 ? text.substring(0, 30) + "..." : text,
                    offsetWidth, scrollWidth, isOverflowing, overflowRatio);
        }
    }

    // ==================== SELENIUM-DEPENDENT METHODS ====================

    /**
     * Kiem tra van ban dich thuat tren trang
     */
    public void check(LanguageConfig config) {
        System.out.println("\n[KIEM TRA VAN BAN / DICH THUAT]");

        try {
            String pageText = driver.findElement(By.tagName("body")).getText();

            // Kiem tra cac tu khoa mong doi (su dung ham static)
            Map<String, Boolean> keywordResults = checkExpectedKeywords(pageText, config.expectedKeywords);
            int foundCount = 0;
            for (Map.Entry<String, Boolean> entry : keywordResults.entrySet()) {
                if (entry.getValue()) {
                    foundCount++;
                    System.out.println("   [OK] Tim thay: " + entry.getKey());
                }
            }
            double coverage = calculateKeywordCoverage(keywordResults);

            // Kiem tra van ban tieng Anh khong mong muon (su dung ham static)
            List<String> untranslatedWords = findUntranslatedEnglishText(pageText, currentLanguage);
            for (String englishWord : untranslatedWords) {
                String errorMsg = "Tim thay van ban tieng Anh: '" + englishWord + "' trong trang "
                        + config.languageName;
                System.out.println("   [LOI] " + errorMsg);
                errors.add(new L10nError("UNTRANSLATED_TEXT", "Van ban chua dich", errorMsg, driver.getCurrentUrl()));
            }

            // Ket qua
            System.out.println("   >> Tim thay " + foundCount + "/" + config.expectedKeywords.length +
                    " tu khoa mong doi (coverage: " + String.format("%.1f%%", coverage * 100) + ")");

            // BVA: Kiem tra text overflow
            if (isLongTextLanguage(currentLanguage)) {
                System.out.println("\n[BOUNDARY VALUE ANALYSIS - TEXT OVERFLOW]");
                checkTextOverflow(config);
            }

        } catch (Exception e) {
            System.out.println("   [LOI] Loi kiem tra van ban: " + e.getMessage());
        }
    }

    /**
     * Kiem tra text overflow tren UI (Boundary Value Analysis)
     * Phat hien van ban bi cat hoac tran khoi container
     */
    public List<TextOverflowResult> checkTextOverflow(LanguageConfig config) {
        List<TextOverflowResult> results = new ArrayList<>();

        // Cac selector cho cac nut/van ban quan trong can kiem tra
        String[] criticalSelectors = {
                ".btn", "button", "[class*='btn']", // Buttons
                ".add-to-cart", ".cart-button", // Add to cart
                ".nav-link", ".menu-item", ".dropdown-item", // Navigation
                ".product-title", ".product-name", // Product names
                "h1", "h2", "h3", // Headers
                ".alert", ".notification" // Alerts
        };

        System.out.println("   Kiem tra text overflow tren cac element quan trong...");

        for (String selector : criticalSelectors) {
            try {
                List<WebElement> elements = driver.findElements(By.cssSelector(selector));

                for (WebElement element : elements) {
                    try {
                        TextOverflowResult result = checkElementOverflow(element, selector);
                        if (result != null) {
                            results.add(result);

                            if (result.isOverflowing) {
                                System.out.println("   [LOI] OVERFLOW: " + result);
                                errors.add(new L10nError("TEXT_OVERFLOW", "Van ban bi tran",
                                        "Element '" + selector + "' bi overflow (ratio: " +
                                                String.format("%.2f", result.overflowRatio) + ")",
                                        driver.getCurrentUrl()));
                            }
                        }
                    } catch (StaleElementReferenceException e) {
                        // Skip stale elements
                    }
                }
            } catch (Exception e) {
                // Skip invalid selectors
            }
        }

        int overflowCount = (int) results.stream().filter(r -> r.isOverflowing).count();
        System.out.println("   >> Tim thay " + overflowCount + "/" + results.size() + " elements bi overflow");

        return results;
    }

    /**
     * Kiem tra mot element co bi text overflow khong
     * Su dung offsetWidth va scrollWidth de phat hien
     */
    public TextOverflowResult checkElementOverflow(WebElement element, String selector) {
        try {
            String text = element.getText();
            if (text == null || text.isEmpty() || text.length() < 5) {
                return null;
            }

            TextOverflowResult result = new TextOverflowResult();
            result.elementSelector = selector;
            result.text = text;

            // Lay offsetWidth va scrollWidth qua JavaScript
            result.offsetWidth = ((Long) js.executeScript(
                    "return arguments[0].offsetWidth;", element)).intValue();
            result.scrollWidth = ((Long) js.executeScript(
                    "return arguments[0].scrollWidth;", element)).intValue();

            // Tinh ty le overflow
            result.overflowRatio = result.offsetWidth > 0
                    ? (double) result.scrollWidth / result.offsetWidth
                    : 0;

            // Kiem tra overflow
            result.isOverflowing = result.overflowRatio > OVERFLOW_RATIO_THRESHOLD &&
                    result.scrollWidth > result.offsetWidth + 5; // Tolerance 5px

            return result;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Kiem tra BVA cho cac nut "Add to cart" - thuong bi tran trong tieng Duc/Ba
     * Lan
     */
    public List<TextOverflowResult> checkAddToCartButtons() {
        List<TextOverflowResult> results = new ArrayList<>();

        String[] addToCartSelectors = {
                ".add-to-cart", "[class*='add-to-cart']", ".btn-add-to-cart",
                "button[data-button-action='add-to-cart']"
        };

        System.out.println("\n[BVA - Add to Cart Buttons]");

        for (String selector : addToCartSelectors) {
            try {
                List<WebElement> buttons = driver.findElements(By.cssSelector(selector));

                for (WebElement button : buttons) {
                    TextOverflowResult result = checkElementOverflow(button, selector);
                    if (result != null) {
                        results.add(result);

                        // BVA: Kiem tra do dai text
                        TextLengthResult lengthResult = checkTextLength(result.text, currentLanguage);
                        if (lengthResult.status == TextLengthStatus.TOO_LONG ||
                                lengthResult.status == TextLengthStatus.WARNING) {
                            System.out.println("   [BVA] " + lengthResult);
                        }

                        if (result.isOverflowing) {
                            System.out.println("   [LOI] " + result);
                        }
                    }
                }
            } catch (Exception e) {
                // Skip
            }
        }

        return results;
    }
}
