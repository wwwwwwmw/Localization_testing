package org.example.core;

import org.example.pages.PrestaShopPage;
import org.example.strategies.ILocaleStrategy;
import org.openqa.selenium.WebElement;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * L10nValidator - Logic kiểm tra Localization cho PrestaShop Demo.
 *
 * 5 tiêu chí kiểm tra:
 * 1. Currency Format — ký hiệu tiền tệ, vị trí (prefix/suffix), dấu phân cách
 * 2. Untranslated Text — phát hiện TOÀN BỘ text tiếng Anh chưa dịch
 * 3. Date Format — định dạng ngày tháng, tên tháng tiếng Anh sót
 * 4. Layout Direction — RTL (Arabic) / LTR (các ngôn ngữ khác)
 * 5. Text Overflow — UI bị tràn text
 */
public class L10nValidator {

    private final DriverManager driverManager;

    // ==================== ENGLISH UI PHRASES ====================
    // Cụm từ tiếng Anh phổ biến trên trang e-commerce,
    // dùng để phát hiện text chưa dịch (multi-word → ít false positive).
    private static final List<String> ENGLISH_UI_PHRASES = List.of(
            // Navigation & Header
            "Add to cart", "Quick view", "Sign in", "Log in",
            "Sign out", "Log out", "My account", "Shopping cart",
            "Search our catalog", "Create account",
            // Footer & Info
            "Contact us", "About us", "Our stores", "Your account",
            "Personal info", "Credit slips", "Legal Notice",
            "Secure payment", "Terms and Conditions",
            "Forgot your password", "No account",
            "Already have an account",
            // Product listing
            "New products", "Best sellers", "Prices drop",
            "On sale", "All products", "Popular Products",
            "Add to wishlist", "Free shipping",
            "Regular price", "In stock", "Out of stock",
            "Sort by",
            // Cart & Checkout
            "Proceed to checkout", "Continue shopping",
            "View my cart",
            // Product detail
            "Product Details", "Product detail",
            "Data sheet", "Specific References");

    // Từ tiếng Anh đơn lẻ đặc trưng cho UI e-commerce
    // (dùng word boundary matching để tránh false positive).
    private static final List<String> ENGLISH_UI_WORDS = List.of(
            "Clothes", "Accessories", "Home",
            "Search", "Delivery", "Sitemap",
            "Returns", "Brands", "Suppliers",
            "Discount", "Shipping", "Description",
            "Reviews", "Categories", "Availability",
            "Checkout", "Wishlist", "Newsletter",
            "Subscribe", "Orders", "Addresses",
            "Cart", "Price", "Quantity");

    // Tên tháng tiếng Anh (phát hiện trong ngày tháng chưa dịch)
    private static final String[] ENGLISH_MONTHS = {
            "January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December",
            "Jan", "Feb", "Mar", "Apr", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
    };

    // Ký hiệu tiền tệ phổ biến (dùng để detect symbol trong price text)
    private static final String[] CURRENCY_SYMBOLS = {
            "€", "$", "£", "¥", "₩", "₫", "฿", "₹", "₽", "₴", "₪", "﷼",
            "د.إ", "ر.س"
    };

    // Pattern nhận dạng ngày tháng dạng số (dd/MM/yyyy, MM-dd-yyyy, ...)
    private static final Pattern DATE_NUMERIC = Pattern.compile(
            "(\\d{1,4})[/\\.-](\\d{1,2})[/\\.-](\\d{1,4})");

    public L10nValidator(DriverManager driverManager) {
        this.driverManager = driverManager;
    }

    // ====================================================================
    // 1. CURRENCY FORMAT
    // ====================================================================

    /**
     * Kiểm tra định dạng tiền tệ trên trang.
     * <ul>
     * <li>Phải tìm thấy ít nhất 1 giá trên trang</li>
     * <li>Giá phải chứa ký hiệu tiền tệ nằm trong danh sách chấp nhận</li>
     * <li>Vị trí ký hiệu (prefix/suffix) phải đúng theo locale</li>
     * </ul>
     *
     * @return danh sách lỗi tìm thấy (rỗng = PASS)
     */
    public List<L10nError> validateCurrency(PrestaShopPage page, ILocaleStrategy strategy) {
        List<L10nError> errors = new ArrayList<>();
        String langCode = strategy.getLanguageCode();
        String pageUrl = page.getCurrentUrl();

        System.out.println("\n  [CURRENCY] Kiểm tra tiền tệ — " + strategy.getLanguageName());

        List<String> prices = page.getAllPriceTexts();
        if (prices.isEmpty()) {
            System.out.println("    ⚠ Không tìm thấy giá trên trang");
            errors.add(createError("CURRENCY_FORMAT",
                    "Không tìm thấy giá nào trên trang", langCode, pageUrl));
            return errors;
        }

        Set<String> checked = new HashSet<>();
        List<String> accepted = strategy.getAcceptedCurrencySymbols();
        int validCount = 0;
        int totalChecked = 0;

        for (String raw : prices) {
            String priceText = raw.trim();
            if (priceText.isEmpty() || checked.contains(priceText) || priceText.length() > 50)
                continue;
            checked.add(priceText);

            // Bỏ qua text không chứa số (không phải giá)
            if (!priceText.matches(".*\\d+.*"))
                continue;

            totalChecked++;
            String symbol = detectCurrencySymbol(priceText);

            // ── Kiểm tra 1: phải có ký hiệu tiền tệ ──
            if (symbol == null) {
                System.out.println("    ✗ \"" + priceText + "\" — Không có ký hiệu tiền tệ");
                errors.add(createError("CURRENCY_FORMAT",
                        "Giá không có ký hiệu tiền tệ: " + priceText, langCode, pageUrl));
                continue;
            }

            // ── Kiểm tra 2: symbol có trong danh sách chấp nhận ──
            if (!accepted.contains(symbol)) {
                System.out.println("    ✗ \"" + priceText + "\" — Symbol '" + symbol
                        + "' không thuộc " + accepted);
                errors.add(createError("CURRENCY_FORMAT",
                        "Symbol '" + symbol + "' không thuộc " + accepted
                                + " | Giá: " + priceText,
                        langCode, pageUrl));
                continue;
            }

            // ── Kiểm tra 3: vị trí symbol (prefix / suffix) ──
            boolean positionOk = checkSymbolPosition(priceText, symbol,
                    strategy.isCurrencySymbolPrefix());
            if (!positionOk) {
                String expected = strategy.isCurrencySymbolPrefix()
                        ? "PREFIX (trước số)"
                        : "SUFFIX (sau số)";
                System.out.println("    ✗ \"" + priceText + "\" — Vị trí symbol sai, expected " + expected);
                errors.add(createError("CURRENCY_FORMAT",
                        "Vị trí symbol sai (expected " + expected + ") | Giá: " + priceText,
                        langCode, pageUrl));
                continue;
            }

            System.out.println("    ✓ \"" + priceText + "\"");
            validCount++;
        }

        System.out.println("    >> Kết quả: " + validCount + "/" + totalChecked + " giá hợp lệ");
        return errors;
    }

    // ====================================================================
    // 2. UNTRANSLATED TEXT
    // ====================================================================

    /**
     * Phát hiện TOÀN BỘ text tiếng Anh chưa được dịch trên trang.
     * Sử dụng danh sách cụm từ + từ đơn tiếng Anh phổ biến trên UI e-commerce.
     * Bỏ qua kiểm tra cho tiếng Anh (ngôn ngữ gốc).
     *
     * @return danh sách lỗi (rỗng = PASS)
     */
    public List<L10nError> validateUntranslatedText(PrestaShopPage page, ILocaleStrategy strategy) {
        List<L10nError> errors = new ArrayList<>();
        String langCode = strategy.getLanguageCode();
        String pageUrl = page.getCurrentUrl();

        System.out.println("\n  [UNTRANSLATED] Kiểm tra text chưa dịch — " + strategy.getLanguageName());

        if ("en".equals(langCode)) {
            System.out.println("    — Bỏ qua (ngôn ngữ gốc tiếng Anh)");
            return errors;
        }

        String bodyText = page.getBodyText();
        if (bodyText == null || bodyText.isEmpty()) {
            System.out.println("    ⚠ Không lấy được nội dung trang");
            return errors;
        }

        List<String> allFound = new ArrayList<>();

        // ── 1. Tìm cụm từ tiếng Anh (case-insensitive) ──
        for (String phrase : ENGLISH_UI_PHRASES) {
            if (containsIgnoreCase(bodyText, phrase)) {
                allFound.add(phrase);
            }
        }

        // ── 2. Tìm từ forbidden đặc thù từ strategy ──
        for (String word : strategy.getForbiddenWords()) {
            if (bodyText.contains(word) && !allFound.contains(word)) {
                allFound.add(word);
            }
        }

        // ── 3. Tìm từ đơn tiếng Anh (word boundary) ──
        for (String word : ENGLISH_UI_WORDS) {
            String trimmed = word.trim();
            if (allFound.contains(trimmed))
                continue;
            Pattern p = Pattern.compile("\\b" + Pattern.quote(trimmed) + "\\b",
                    Pattern.CASE_INSENSITIVE);
            if (p.matcher(bodyText).find()) {
                allFound.add(trimmed);
            }
        }

        // Loại trùng
        allFound = allFound.stream().distinct().collect(Collectors.toList());

        if (allFound.isEmpty()) {
            System.out.println("    ✓ Không phát hiện text tiếng Anh chưa dịch");
        } else {
            System.out.println("    ✗ Phát hiện " + allFound.size() + " text tiếng Anh chưa dịch:");
            for (String item : allFound) {
                System.out.println("      — \"" + item + "\"");
            }
            errors.add(createError("UNTRANSLATED_TEXT",
                    "Phát hiện " + allFound.size() + " text chưa dịch: " + allFound,
                    langCode, pageUrl));
        }

        return errors;
    }

    // ====================================================================
    // 3. DATE FORMAT
    // ====================================================================

    /**
     * Kiểm tra định dạng ngày tháng trên trang.
     * <ul>
     * <li>Tên tháng tiếng Anh không được xuất hiện trên trang không phải EN</li>
     * <li>Tìm ngày dạng số và in kết quả</li>
     * </ul>
     *
     * @return danh sách lỗi (rỗng = PASS), hoặc {@code null} nếu không có dữ liệu
     *         ngày tháng để kiểm tra (SKIP)
     */
    public List<L10nError> validateDateFormat(PrestaShopPage page, ILocaleStrategy strategy) {
        List<L10nError> errors = new ArrayList<>();
        String langCode = strategy.getLanguageCode();
        String pageUrl = page.getCurrentUrl();

        System.out.println("\n  [DATE] Kiểm tra ngày tháng — " + strategy.getLanguageName());

        String bodyText = page.getBodyText();
        boolean hasDateData = false;

        // ── 1. Phát hiện tên tháng tiếng Anh trên trang không phải EN ──
        if (!"en".equals(langCode)) {
            List<String> foundMonths = new ArrayList<>();
            for (String month : ENGLISH_MONTHS) {
                Pattern p = Pattern.compile("\\b" + Pattern.quote(month) + "\\b");
                if (p.matcher(bodyText).find()) {
                    foundMonths.add(month);
                }
            }
            if (!foundMonths.isEmpty()) {
                hasDateData = true;
                System.out.println("    ✗ Tên tháng tiếng Anh chưa dịch: " + foundMonths);
                errors.add(createError("DATE_FORMAT",
                        "Tên tháng tiếng Anh chưa dịch: " + foundMonths, langCode, pageUrl));
            }
        }

        // ── 2. Tìm ngày dạng số ──
        List<String> dates = extractDates(bodyText);
        if (!dates.isEmpty()) {
            hasDateData = true;
            System.out.println("    ℹ Tìm thấy " + dates.size() + " ngày tháng:");
            for (String d : dates) {
                System.out.println("      — " + d);
            }
        }

        // Không tìm thấy dữ liệu ngày tháng nào → trả về null để test SKIP
        if (!hasDateData) {
            System.out.println("    — Không tìm thấy ngày tháng trên trang → SKIP");
            return null;
        }

        if (errors.isEmpty()) {
            System.out.println("    ✓ Định dạng ngày tháng hợp lệ");
        }

        return errors;
    }

    // ====================================================================
    // 4. LAYOUT DIRECTION (RTL / LTR)
    // ====================================================================

    /**
     * Kiểm tra hướng layout của trang.
     * <ul>
     * <li>Arabic (RTL) phải có dir="rtl"</li>
     * <li>Các ngôn ngữ khác (LTR) KHÔNG được có dir="rtl"</li>
     * </ul>
     *
     * @return danh sách lỗi (rỗng = PASS)
     */
    public List<L10nError> validateLayoutDirection(PrestaShopPage page, ILocaleStrategy strategy) {
        List<L10nError> errors = new ArrayList<>();
        String langCode = strategy.getLanguageCode();
        String pageUrl = page.getCurrentUrl();

        System.out.println("\n  [LAYOUT] Kiểm tra direction — " + strategy.getLanguageName());

        String expectedDir = strategy.isRTL() ? "rtl" : "ltr";
        String actualDir = page.getPageDirection();

        System.out.println("    Expected: " + expectedDir + " | Actual: "
                + (actualDir != null ? actualDir : "NOT SET"));

        if (strategy.isRTL()) {
            if (actualDir == null || !actualDir.trim().equalsIgnoreCase("rtl")) {
                System.out.println("    ✗ Ngôn ngữ RTL nhưng trang không có dir=\"rtl\"");
                errors.add(createError("RTL_LAYOUT",
                        "Ngôn ngữ RTL (" + strategy.getLanguageName()
                                + ") nhưng dir=\"" + actualDir + "\" (expected: rtl)",
                        langCode, pageUrl));
            } else {
                System.out.println("    ✓ RTL layout đúng");
            }
        } else {
            if (actualDir != null && actualDir.trim().equalsIgnoreCase("rtl")) {
                System.out.println("    ✗ Ngôn ngữ LTR nhưng trang có dir=\"rtl\"");
                errors.add(createError("RTL_LAYOUT",
                        "Ngôn ngữ LTR (" + strategy.getLanguageName() + ") nhưng dir=\"rtl\"",
                        langCode, pageUrl));
            } else {
                System.out.println("    ✓ LTR layout đúng");
            }
        }

        return errors;
    }

    // ====================================================================
    // 5. TEXT OVERFLOW
    // ====================================================================

    /**
     * Kiểm tra text overflow trên các phần tử UI (buttons, nav links...).
     * Phát hiện khi scrollWidth vượt quá clientWidth (text tràn container).
     *
     * @return danh sách lỗi (rỗng = PASS)
     */
    public List<L10nError> validateTextOverflow(PrestaShopPage page, ILocaleStrategy strategy) {
        List<L10nError> errors = new ArrayList<>();
        String langCode = strategy.getLanguageCode();
        String pageUrl = page.getCurrentUrl();

        System.out.println("\n  [OVERFLOW] Kiểm tra text overflow — " + strategy.getLanguageName());

        List<WebElement> buttons = page.getAllButtons();
        List<String> overflowedTexts = new ArrayList<>();

        for (WebElement btn : buttons) {
            try {
                if (page.isElementOverflowing(btn)) {
                    String text = btn.getText().trim();
                    if (!text.isEmpty()) {
                        overflowedTexts.add(text);
                    }
                }
            } catch (Exception e) {
                // Skip stale elements
            }
        }

        if (overflowedTexts.isEmpty()) {
            System.out.println("    ✓ Không phát hiện text overflow");
        } else {
            System.out.println("    ✗ " + overflowedTexts.size() + " phần tử bị tràn text:");
            for (String txt : overflowedTexts) {
                System.out.println("      — \"" + txt + "\"");
            }
            errors.add(createError("TEXT_OVERFLOW",
                    overflowedTexts.size() + " phần tử UI bị tràn text: " + overflowedTexts,
                    langCode, pageUrl));
        }

        return errors;
    }

    // ====================================================================
    // UTILITY METHODS
    // ====================================================================

    private String detectCurrencySymbol(String text) {
        if (text == null)
            return null;
        for (String symbol : CURRENCY_SYMBOLS) {
            if (text.contains(symbol))
                return symbol;
        }
        return null;
    }

    private boolean checkSymbolPosition(String priceText, String symbol, boolean expectedPrefix) {
        if (symbol == null || priceText == null)
            return true;
        String trimmed = priceText.trim();

        int firstDigit = -1;
        for (int i = 0; i < trimmed.length(); i++) {
            if (Character.isDigit(trimmed.charAt(i))) {
                firstDigit = i;
                break;
            }
        }
        if (firstDigit < 0)
            return true;

        int symbolIdx = trimmed.indexOf(symbol);
        if (symbolIdx < 0)
            return true;

        return expectedPrefix ? symbolIdx < firstDigit : symbolIdx > firstDigit;
    }

    private List<String> extractDates(String text) {
        List<String> dates = new ArrayList<>();
        if (text == null)
            return dates;
        Matcher m = DATE_NUMERIC.matcher(text);
        while (m.find()) {
            dates.add(m.group());
        }
        return dates;
    }

    private boolean containsIgnoreCase(String text, String search) {
        return text.toLowerCase().contains(search.toLowerCase());
    }

    private L10nError createError(String type, String message, String langCode, String pageUrl) {
        return L10nError.createWithScreenshot(type, message, langCode, pageUrl, driverManager);
    }
}
