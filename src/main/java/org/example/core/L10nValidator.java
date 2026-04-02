package org.example.core;

import org.example.pages.PrestaShopPage;
import org.example.pages.PrestaShopPage.ImageLocalizationSample;
import org.example.strategies.ILocaleStrategy;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * L10nValidator - Logic kiem tra Localization cho shop dang duoc test.
 *
 * 9 tiêu chí kiểm tra:
 * 1. Currency Format — ký hiệu tiền tệ, vị trí (prefix/suffix), dấu phân cách
 * 2. Untranslated Text — phát hiện TOÀN BỘ text tiếng Anh chưa dịch
 * 3. Date Format — định dạng ngày tháng, tên tháng tiếng Anh sót
 * 4. Layout Direction — RTL (Arabic) / LTR (các ngôn ngữ khác)
 * 5. Text Overflow — UI bị tràn text
 * 6. Charset — khai báo UTF-8, phát hiện mojibake, kiểm tra script đặc trưng
 * 7. Number & Measurement — phân cách số, đơn vị đo, rò rỉ chuẩn tiếng Anh
 * 8. Media Localization — ảnh/asset và alt text theo locale
 * 9. URL Localization — path/query phản ánh locale đúng chuẩn
 */
public class L10nValidator {

    private static final class TextOccurrence {
        private final String location;
        private final int index;
        private final String snippet;

        private TextOccurrence(String location, int index, String snippet) {
            this.location = location;
            this.index = index;
            this.snippet = snippet;
        }
    }

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
            "Data sheet", "Specific References",
            "Seeded from PrestaShop image set");

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
            "€", "$", "£", "¥", "￥", "₩", "₫", "฿", "₹", "₽", "₴", "₪", "﷼",
            "د.إ", "ر.س"
    };

    private static final String DEFAULT_TEST_CUSTOMER_EMAIL = "customer@example.com";
    private static final String DEFAULT_TEST_CUSTOMER_PASSWORD = "123456";

    // Pattern nhận dạng ngày tháng dạng số (dd/MM/yyyy, MM-dd-yyyy, ...)
    private static final Pattern DATE_NUMERIC = Pattern.compile(
            "([0-9\\p{Nd}]{1,4})[/\\.-]([0-9\\p{Nd}]{1,2})[/\\.-]([0-9\\p{Nd}]{1,4})");

    private static final Set<String> ENGLISH_STOP_WORDS = Set.of(
            "the", "and", "with", "for", "from", "this", "that", "your", "you", "are", "is", "to", "of", "in",
            "on", "at", "by", "new", "best", "more", "about", "product", "description", "quality", "material",
            "size", "color", "designed", "comfortable", "available");

    private static final Set<String> ENGLISH_MEASUREMENT_UNITS = Set.of(
            "inch", "inches", "ft", "feet", "yard", "yards", "mile", "miles", "lb", "lbs", "pound", "pounds");

    // Các marker thường gặp khi text UTF-8 bị decode sai (mojibake).
    private static final List<String> MOJIBAKE_MARKERS = List.of(
            "Ã", "â€", "�");

    private static final int MIN_SAMPLE_HITS_LATIN_NON_EN = 2;
    private static final int MIN_SAMPLE_HITS_COMPLEX_SCRIPT = 8;
    private static final int MAX_OCCURRENCES_PER_TOKEN = 10;

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

        Map<String, List<String>> pricesByPage = page.collectPriceTextsAcrossPages(
                getTestCustomerEmail(),
                getTestCustomerPassword());
        if (pricesByPage.isEmpty()) {
            System.out.println("    ⚠ Không thể duyệt các tab/trang để lấy giá");
            errors.add(createDetailedError("CURRENCY_FORMAT", langCode, pageUrl,
                    "all-pages",
                    "Thu thập được price texts",
                    "Không thu thập được",
                    "collectPriceTextsAcrossPages trả về rỗng"));
            return errors;
        }

        Set<String> checked = new HashSet<>();
        List<String> accepted = strategy.getAcceptedCurrencySymbols();
        int validCount = 0;
        int totalChecked = 0;
        int pagesWithPrices = 0;

        for (Map.Entry<String, List<String>> pageEntry : pricesByPage.entrySet()) {
            String location = pageEntry.getKey();
            List<String> prices = pageEntry.getValue() != null ? pageEntry.getValue() : Collections.emptyList();

            if (prices.isEmpty()) {
                System.out.println("    — [" + location + "] Không có giá");
                continue;
            }
            pagesWithPrices++;
            System.out.println("    — [" + location + "] " + prices.size() + " giá");

            for (String raw : prices) {
                String priceText = raw.trim();
                String dedupeKey = location + "::" + priceText;
                if (priceText.isEmpty() || checked.contains(dedupeKey) || priceText.length() > 50)
                    continue;
                checked.add(dedupeKey);

                // Bỏ qua text không chứa chữ số Unicode (không phải giá)
                if (!priceText.matches(".*\\p{Nd}+.*"))
                    continue;

                totalChecked++;
                String symbol = detectCurrencySymbol(priceText);

                // ── Kiểm tra 1: phải có ký hiệu tiền tệ ──
                if (symbol == null) {
                    System.out.println("    ✗ [" + location + "] \"" + priceText + "\" — Không có ký hiệu tiền tệ");
                    errors.add(createDetailedError("CURRENCY_FORMAT", langCode, pageUrl,
                            location,
                            "Price chứa currency symbol thuộc " + accepted,
                            priceText,
                            "Không detect được symbol"));
                    continue;
                }

                // ── Kiểm tra 2: symbol có trong danh sách chấp nhận ──
                if (!accepted.contains(symbol)) {
                    System.out.println("    ✗ [" + location + "] \"" + priceText + "\" — Symbol '" + symbol
                            + "' không thuộc " + accepted);
                    errors.add(createDetailedError("CURRENCY_FORMAT", langCode, pageUrl,
                            location,
                            "Symbol thuộc " + accepted,
                            symbol,
                            "price=\"" + priceText + "\""));
                    continue;
                }

                // ── Kiểm tra 3: vị trí symbol (prefix / suffix) ──
                boolean positionOk = checkSymbolPosition(priceText, symbol,
                        strategy.isCurrencySymbolPrefix());
                if (!positionOk) {
                    String expected = strategy.isCurrencySymbolPrefix()
                            ? "PREFIX (trước số)"
                            : "SUFFIX (sau số)";
                    System.out.println(
                            "    ✗ [" + location + "] \"" + priceText + "\" — Vị trí symbol sai, expected "
                                    + expected);
                    errors.add(createDetailedError("CURRENCY_FORMAT", langCode, pageUrl,
                            location,
                            expected,
                            "price=\"" + priceText + "\"",
                            "symbol=\"" + symbol + "\""));
                    continue;
                }

                System.out.println("    ✓ [" + location + "] \"" + priceText + "\"");
                validCount++;
            }
        }

        if (pagesWithPrices == 0 || totalChecked == 0) {
            errors.add(createDetailedError("CURRENCY_FORMAT", langCode, pageUrl,
                    "all-pages",
                    "Có ít nhất 1 giá hợp lệ",
                    "0 giá được kiểm tra",
                    "pagesWithPrices=" + pagesWithPrices + ", totalChecked=" + totalChecked));
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

        Map<String, String> pageBodies = page.collectLocalizationBodies(
                getTestCustomerEmail(),
                getTestCustomerPassword());
        if (pageBodies.isEmpty()) {
            System.out.println("    ⚠ Không thu thập được nội dung từ các tab/trang");
            return errors;
        }

        Map<String, List<TextOccurrence>> foundOccurrences = new LinkedHashMap<>();
        for (Map.Entry<String, String> entry : pageBodies.entrySet()) {
            String location = entry.getKey();
            String bodyText = entry.getValue();
            collectUntranslatedTokens(bodyText, strategy, location, foundOccurrences);
        }

        if (foundOccurrences.isEmpty()) {
            System.out.println("    ✓ Không phát hiện text tiếng Anh chưa dịch");
        } else {
            for (Map.Entry<String, List<TextOccurrence>> entry : foundOccurrences.entrySet()) {
                String token = entry.getKey();
                List<TextOccurrence> occurrences = entry.getValue();
                String details = summarizeOccurrences(occurrences);
                System.out.println("      — token=\"" + token + "\" | hits=" + occurrences.size());
                System.out.println("         " + details);

                errors.add(createDetailedError(
                        "UNTRANSLATED_TEXT",
                        langCode,
                        pageUrl,
                        null,
                        "Token phải được dịch hoặc nằm trong allowed list",
                        "token=\"" + token + "\"",
                        details));
            }

            System.out.println("    ✗ Phát hiện " + foundOccurrences.size() + " token tiếng Anh chưa dịch");
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

        Map<String, String> dateBodies = page.collectLocalizationBodies(
                getTestCustomerEmail(),
                getTestCustomerPassword());
        if (dateBodies.isEmpty()) {
            errors.add(createDetailedError("DATE_FORMAT", langCode, page.getCurrentUrl(),
                    "all-pages",
                    "Thu thập được nội dung chứa date",
                    "Không thu thập được",
                    "collectLocalizationBodies trả về rỗng"));
            return errors;
        }

        boolean hasDateData = false;

        for (Map.Entry<String, String> entry : dateBodies.entrySet()) {
            String section = entry.getKey();
            String bodyText = entry.getValue();

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
                    System.out.println("    ✗ [" + section + "] Tên tháng tiếng Anh chưa dịch: " + foundMonths);
                    errors.add(createDetailedError("DATE_FORMAT", langCode, page.getCurrentUrl(),
                            section,
                            "Không chứa tháng tiếng Anh ở locale non-EN",
                            foundMonths.toString(),
                            "body có month tokens EN"));
                }
            }

            // ── 2. Tìm ngày dạng số ──
            List<String> dates = extractDates(bodyText);
            if (!dates.isEmpty()) {
                hasDateData = true;
                System.out.println("    ℹ [" + section + "] Tìm thấy " + dates.size() + " ngày tháng:");
                for (String d : dates) {
                    System.out.println("      — " + d);
                }
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
        Map<String, String> directionsByPage = page.collectDirectionsAcrossPages(
                getTestCustomerEmail(),
                getTestCustomerPassword());
        if (directionsByPage.isEmpty()) {
            errors.add(createDetailedError("RTL_LAYOUT", langCode, pageUrl,
                    "all-pages",
                    "Thu thập direction từ html/body",
                    "Không thu thập được",
                    "collectDirectionsAcrossPages trả về rỗng"));
            return errors;
        }

        for (Map.Entry<String, String> entry : directionsByPage.entrySet()) {
            String location = entry.getKey();
            String actualDir = entry.getValue();
            System.out.println("    [" + location + "] Expected: " + expectedDir + " | Actual: "
                    + (actualDir != null ? actualDir : "NOT SET"));

            if (strategy.isRTL()) {
                if (actualDir == null || !actualDir.trim().equalsIgnoreCase("rtl")) {
                    System.out.println("    ✗ [" + location + "] Ngôn ngữ RTL nhưng trang không có dir=\"rtl\"");
                    errors.add(createDetailedError("RTL_LAYOUT", langCode, pageUrl,
                            location,
                            "dir=rtl",
                            "dir=" + actualDir,
                            "locale=" + strategy.getLanguageName()));
                }
            } else {
                if (actualDir != null && actualDir.trim().equalsIgnoreCase("rtl")) {
                    System.out.println("    ✗ [" + location + "] Ngôn ngữ LTR nhưng trang có dir=\"rtl\"");
                    errors.add(createDetailedError("RTL_LAYOUT", langCode, pageUrl,
                            location,
                            "dir != rtl",
                            "dir=rtl",
                            "locale=" + strategy.getLanguageName()));
                }
            }
        }

        if (errors.isEmpty()) {
            System.out.println("    ✓ Direction hợp lệ trên tất cả tab/trang đã duyệt");
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

        Map<String, List<String>> overflowByPage = page.collectOverflowedTextsAcrossPages(
                getTestCustomerEmail(),
                getTestCustomerPassword());
        if (overflowByPage.isEmpty()) {
            errors.add(createDetailedError("TEXT_OVERFLOW", langCode, pageUrl,
                    "all-pages",
                    "Thu thập được candidate UI elements",
                    "Không thu thập được",
                    "collectOverflowedTextsAcrossPages trả về rỗng"));
            return errors;
        }

        List<String> overflowedTexts = new ArrayList<>();
        for (Map.Entry<String, List<String>> entry : overflowByPage.entrySet()) {
            String location = entry.getKey();
            List<String> texts = entry.getValue() != null ? entry.getValue() : Collections.emptyList();
            if (texts.isEmpty()) {
                System.out.println("    — [" + location + "] Không phát hiện overflow");
                continue;
            }
            System.out.println("    ✗ [" + location + "] " + texts.size() + " phần tử bị tràn text");
            for (String txt : texts) {
                String marker = "[" + location + "] " + txt;
                overflowedTexts.add(marker);
                System.out.println("      — \"" + txt + "\"");
            }
        }

        if (overflowedTexts.isEmpty()) {
            System.out.println("    ✓ Không phát hiện text overflow");
        } else {
            errors.add(createDetailedError("TEXT_OVERFLOW", langCode, pageUrl,
                    "multiple",
                    "Không có phần tử bị overflow",
                    overflowedTexts.size() + " phần tử overflow",
                    String.join(" | ", overflowedTexts)));
        }

        return errors;
    }

    // ====================================================================
    // 6. CHARSET / ENCODING
    // ====================================================================

    /**
     * Kiểm tra charset/encoding của trang.
     * <ul>
     * <li>Document phải khai báo UTF-8</li>
     * <li>HTML lang phải khớp locale đang test</li>
     * <li>Không được có chuỗi mojibake (Ã, â€, �...)</li>
     * <li>Phải có mật độ ký tự phù hợp với script theo locale</li>
     * </ul>
     *
     * @return danh sách lỗi (rỗng = PASS)
     */
    public List<L10nError> validateCharset(PrestaShopPage page, ILocaleStrategy strategy) {
        List<L10nError> errors = new ArrayList<>();
        String langCode = strategy.getLanguageCode();
        String pageUrl = page.getCurrentUrl();

        System.out.println("\n  [CHARSET] Kiểm tra charset/encoding — " + strategy.getLanguageName());

        Map<String, String> bodyByPage = page.collectLocalizationBodies(
                getTestCustomerEmail(),
                getTestCustomerPassword());
        Map<String, String> htmlLangByPage = page.collectHtmlLangAcrossPages(
                getTestCustomerEmail(),
                getTestCustomerPassword());
        Map<String, String> charsetByPage = page.collectDocumentCharsetsAcrossPages(
                getTestCustomerEmail(),
                getTestCustomerPassword());

        LinkedHashSet<String> allLocations = new LinkedHashSet<>();
        allLocations.addAll(bodyByPage.keySet());
        allLocations.addAll(htmlLangByPage.keySet());
        allLocations.addAll(charsetByPage.keySet());

        if (allLocations.isEmpty()) {
            errors.add(createDetailedError("CHARSET", langCode, pageUrl,
                    "all-pages",
                    "Thu thập được htmlLang/body/charset",
                    "Không thu thập được",
                    "Không có location nào từ các collector"));
            return errors;
        }

        String expectedIso = strategy.getLanguageCode();
        String expectedPresta = strategy.getPrestaShopCode();

        StringBuilder mergedBodyBuilder = new StringBuilder();
        boolean hasMojibake = false;
        for (String location : allLocations) {
            String detectedCharset = charsetByPage.get(location);
            if (detectedCharset == null || detectedCharset.isBlank()) {
                System.out.println("    ✗ [" + location + "] Không xác định được charset của trang");
                errors.add(createDetailedError("CHARSET", langCode, pageUrl,
                        location,
                        "UTF-8",
                        "UNKNOWN",
                        "Không đọc được từ meta/document.characterSet"));
            } else {
                String normalized = detectedCharset.trim().toUpperCase(Locale.ROOT)
                        .replace("_", "-");
                System.out.println("    [" + location + "] Charset phát hiện: " + detectedCharset);
                if (!(normalized.contains("UTF-8") || "UTF8".equals(normalized))) {
                    System.out.println("    ✗ [" + location + "] Charset không phải UTF-8");
                    errors.add(createDetailedError("CHARSET", langCode, pageUrl,
                            location,
                            "UTF-8",
                            detectedCharset,
                            "document/meta charset mismatch"));
                }
            }

            String htmlLang = htmlLangByPage.get(location);
            if (htmlLang == null || htmlLang.isBlank()) {
                System.out.println("    ✗ [" + location + "] Không đọc được thuộc tính html lang");
                errors.add(createDetailedError("CHARSET", langCode, pageUrl,
                        location,
                        expectedIso + " hoặc " + expectedPresta,
                        "UNKNOWN",
                        "html[lang] bị thiếu"));
            } else {
                String normalizedHtmlLang = htmlLang.trim().toLowerCase(Locale.ROOT);
                boolean langMatch = normalizedHtmlLang.startsWith(expectedIso.toLowerCase(Locale.ROOT))
                        || normalizedHtmlLang.startsWith(expectedPresta.toLowerCase(Locale.ROOT));
                System.out.println("    [" + location + "] HTML lang: " + htmlLang
                        + " | Expected: " + expectedIso + " / " + expectedPresta);
                if (!langMatch) {
                    System.out.println("    ✗ [" + location + "] html lang không khớp locale hiện tại");
                    errors.add(createDetailedError("CHARSET", langCode, pageUrl,
                            location,
                            expectedIso + " hoặc " + expectedPresta,
                            htmlLang,
                            "html lang mismatch"));
                }
            }

            String bodyText = bodyByPage.getOrDefault(location, "");
            if (!bodyText.isBlank()) {
                mergedBodyBuilder.append('\n').append(bodyText);
            }
            List<String> mojibakeTokens = detectMojibakeTokens(bodyText);
            if (!mojibakeTokens.isEmpty()) {
                hasMojibake = true;
                System.out.println("    ✗ [" + location + "] Phát hiện chuỗi lỗi encoding: " + mojibakeTokens);
                errors.add(createDetailedError("CHARSET", langCode, pageUrl,
                        location,
                        "Không có mojibake",
                        mojibakeTokens.toString(),
                        "marker tokens bị phát hiện"));
            }
        }

        String mergedBodyText = mergedBodyBuilder.toString();
        if (!hasMojibake) {
            System.out.println("    ✓ Không phát hiện dấu hiệu mojibake");
        }

        String validationPattern = strategy.getCharacterValidationPattern();
        if (mergedBodyText != null && !mergedBodyText.isBlank()
                && validationPattern != null && !validationPattern.isBlank()) {
            try {
                boolean hasExpectedScript = Pattern.compile(validationPattern).matcher(mergedBodyText).find();
                if (!hasExpectedScript) {
                    System.out.println("    ✗ Không tìm thấy script đúng theo locale");
                    errors.add(createDetailedError("CHARSET", langCode, pageUrl,
                            "merged-body",
                            "Match regex: " + validationPattern,
                            "No match",
                            "Dữ liệu text không chứa script kỳ vọng"));
                }
            } catch (Exception ex) {
                errors.add(createDetailedError("CHARSET", langCode, pageUrl,
                        "validator",
                        "Regex hợp lệ",
                        validationPattern,
                        "Regex syntax error: " + ex.getMessage()));
            }
        }

        // Locale không phải tiếng Anh phải có đủ ký tự đặc trưng để tránh false PASS
        // khi chỉ còn 1-2 nhãn locale nhưng phần còn lại chưa thực sự đổi ngôn ngữ.
        if (!"en".equals(langCode)) {
            String sample = strategy.getCharacterSample();
            int sampleHits = countSampleCharacterHits(mergedBodyText, sample);
            int minHits = getMinSampleHits(strategy);
            System.out.println("    Script sample hits: " + sampleHits + " (min " + minHits + ")");
            if (sampleHits < minHits) {
                System.out.println("    ✗ Mật độ ký tự đặc trưng thấp");
                errors.add(createDetailedError("CHARSET", langCode, pageUrl,
                        "merged-body",
                        "sampleHits >= " + minHits,
                        "sampleHits=" + sampleHits,
                        "locale=" + strategy.getLanguageName()));
            }
        }

        if (errors.isEmpty()) {
            System.out.println("    ✓ Charset/encoding hợp lệ");
        }

        return errors;
    }

    // ====================================================================
    // 7. NUMBER & MEASUREMENT FORMAT
    // ====================================================================

    /**
     * Kiểm tra định dạng số (thập phân/hàng nghìn) và đơn vị đo lường theo locale.
     */
    public List<L10nError> validateNumberAndMeasurementFormat(PrestaShopPage page, ILocaleStrategy strategy) {
        List<L10nError> errors = new ArrayList<>();
        String langCode = strategy.getLanguageCode();
        String pageUrl = page.getCurrentUrl();

        System.out.println("\n  [NUMBER] Kiểm tra số & đơn vị đo lường — " + strategy.getLanguageName());

        Map<String, String> pageBodies = page.collectLocalizationBodies(
                getTestCustomerEmail(),
                getTestCustomerPassword());

        if (pageBodies.isEmpty()) {
            errors.add(createDetailedError("NUMBER_MEASUREMENT", langCode, pageUrl,
                    "all-pages",
                    "Thu thập được body text",
                    "Không thu thập được",
                    "collectLocalizationBodies trả về rỗng"));
            return errors;
        }

        boolean foundNumericCandidate = false;
        for (Map.Entry<String, String> entry : pageBodies.entrySet()) {
            String location = entry.getKey();
            String bodyText = entry.getValue();
            if (bodyText == null || bodyText.isBlank()) {
                continue;
            }

            List<String> numericCandidates = extractNumericCandidates(bodyText);
            if (!numericCandidates.isEmpty()) {
                foundNumericCandidate = true;
            }

            for (String candidate : numericCandidates) {
                if (!matchesLocaleNumberSeparators(candidate, strategy)) {
                    errors.add(createDetailedError("NUMBER_MEASUREMENT", langCode, pageUrl,
                            location,
                            "decimal='" + strategy.getDecimalSeparator() + "', grouping='"
                                    + strategy.getGroupingSeparator() + "'",
                            candidate,
                            "Số không khớp separator locale"));
                }
            }

            if (!"en".equals(langCode)) {
                List<String> units = extractEnglishMeasurementUnits(bodyText);
                if (!units.isEmpty()) {
                    errors.add(createDetailedError("NUMBER_MEASUREMENT", langCode, pageUrl,
                            location,
                            "Không xuất hiện đơn vị EN ở locale non-EN",
                            units.toString(),
                            "Detected measurement units"));
                }
            }
        }

        if (!foundNumericCandidate) {
            System.out.println("    — Không tìm thấy mẫu số phù hợp để đánh giá");
            return errors;
        }

        if (errors.isEmpty()) {
            System.out.println("    ✓ Number/measurement hợp lệ");
        }

        return errors;
    }

    // ====================================================================
    // 8. MEDIA & ALT TEXT LOCALIZATION
    // ====================================================================

    /**
     * Kiểm tra alt/title ảnh đã localized và không dùng asset EN cố định quá mức.
     */
    public List<L10nError> validateMediaLocalization(PrestaShopPage page, ILocaleStrategy strategy) {
        List<L10nError> errors = new ArrayList<>();
        String langCode = strategy.getLanguageCode();
        String pageUrl = page.getCurrentUrl();

        System.out.println("\n  [MEDIA] Kiểm tra ảnh & alt text — " + strategy.getLanguageName());

        Map<String, List<ImageLocalizationSample>> imagesByPage = page.collectImageLocalizationAcrossPages(
                getTestCustomerEmail(),
                getTestCustomerPassword());
        if (imagesByPage.isEmpty()) {
            errors.add(createDetailedError("MEDIA_LOCALIZATION", langCode, pageUrl,
                    "all-pages",
                    "Thu thập được metadata ảnh",
                    "Không thu thập được",
                    "collectImageLocalizationAcrossPages trả về rỗng"));
            return errors;
        }

        for (Map.Entry<String, List<ImageLocalizationSample>> entry : imagesByPage.entrySet()) {
            String location = entry.getKey();
            List<ImageLocalizationSample> samples = entry.getValue() != null ? entry.getValue()
                    : Collections.emptyList();

            int checked = 0;
            for (ImageLocalizationSample sample : samples) {
                String src = safeLower(sample.getSrc());
                String alt = sample.getAlt() == null ? "" : sample.getAlt().trim();
                String title = sample.getTitle() == null ? "" : sample.getTitle().trim();
                String mergedMeta = (alt + " " + title).trim();

                if (src == null || src.isBlank()) {
                    continue;
                }
                checked++;

                boolean seemsEnglishAsset = src.contains("/en/") || src.contains("_en")
                        || src.contains("-en") || src.contains("english");
                if (!"en".equals(langCode) && seemsEnglishAsset) {
                    errors.add(createDetailedError("MEDIA_LOCALIZATION", langCode, pageUrl,
                            location,
                            "Asset locale-specific hoặc neutral",
                            sample.getSrc(),
                            "src chứa marker tiếng Anh (/en/, _en, -en, english)"));
                }

                if (mergedMeta.isBlank()) {
                    errors.add(createDetailedError("MEDIA_LOCALIZATION", langCode, pageUrl,
                            location,
                            "alt hoặc title có nội dung localized",
                            "alt/title trống",
                            "src=" + sample.getSrc()));
                    continue;
                }

                if (!"en".equals(langCode) && containsEnglishUiToken(mergedMeta)) {
                    errors.add(createDetailedError("MEDIA_LOCALIZATION", langCode, pageUrl,
                            location,
                            "Alt/title localized",
                            mergedMeta,
                            "Phát hiện token tiếng Anh trong alt/title"));
                }
            }

            System.out.println("    — [" + location + "] kiểm tra " + checked + " ảnh");
        }

        if (errors.isEmpty()) {
            System.out.println("    ✓ Media/alt text hợp lệ");
        }

        return errors;
    }

    // ====================================================================
    // 9. URL & ROUTING LOCALIZATION
    // ====================================================================

    /**
     * Kiểm tra URL phản ánh locale (path/query) để tránh SEO issue khi đa ngôn ngữ.
     */
    public List<L10nError> validateLocalizedUrls(PrestaShopPage page, ILocaleStrategy strategy) {
        List<L10nError> errors = new ArrayList<>();
        String langCode = strategy.getLanguageCode();
        String pageUrl = page.getCurrentUrl();

        System.out.println("\n  [URL] Kiểm tra URL localization — " + strategy.getLanguageName());

        Map<String, String> urlsByPage = page.collectUrlsAcrossPages(
                getTestCustomerEmail(),
                getTestCustomerPassword());
        if (urlsByPage.isEmpty()) {
            errors.add(createDetailedError("URL_LOCALIZATION", langCode, pageUrl,
                    "all-pages",
                    "Thu thập được URL theo location",
                    "Không thu thập được",
                    "collectUrlsAcrossPages trả về rỗng"));
            return errors;
        }

        String prestaCode = strategy.getPrestaShopCode().toLowerCase(Locale.ROOT);
        String isoCode = langCode.toLowerCase(Locale.ROOT);

        for (Map.Entry<String, String> entry : urlsByPage.entrySet()) {
            String location = entry.getKey();
            String url = entry.getValue();
            if (url == null || url.isBlank()) {
                continue;
            }

            if (!urlContainsLocaleHint(url, isoCode, prestaCode)) {
                errors.add(createDetailedError("URL_LOCALIZATION", langCode, url,
                        location,
                        "URL chứa locale hint (path/query) cho " + isoCode + "/" + prestaCode,
                        url,
                        "Không tìm thấy /<locale>/ hoặc lang=/locale="));
            }
        }

        if (errors.isEmpty()) {
            System.out.println("    ✓ URL routing có dấu hiệu locale hợp lệ");
        }
        return errors;
    }

    // ====================================================================
    // UTILITY METHODS
    // ====================================================================

    private List<String> detectMojibakeTokens(String text) {
        if (text == null || text.isBlank()) {
            return Collections.emptyList();
        }
        List<String> found = new ArrayList<>();
        for (String marker : MOJIBAKE_MARKERS) {
            if (text.contains(marker)) {
                found.add(marker);
            }
        }
        return found.stream().distinct().collect(Collectors.toList());
    }

    private int countSampleCharacterHits(String bodyText, String sampleChars) {
        if (bodyText == null || bodyText.isBlank() || sampleChars == null || sampleChars.isBlank()) {
            return 0;
        }

        Set<Integer> sampleSet = sampleChars.codePoints()
                .filter(cp -> !Character.isWhitespace(cp))
                .map(Character::toLowerCase)
                .boxed()
                .collect(Collectors.toSet());

        int hits = 0;
        PrimitiveIterator.OfInt iterator = bodyText.codePoints().iterator();
        while (iterator.hasNext()) {
            int cp = Character.toLowerCase(iterator.nextInt());
            if (sampleSet.contains(cp)) {
                hits++;
            }
        }
        return hits;
    }

    private int getMinSampleHits(ILocaleStrategy strategy) {
        if (strategy == null) {
            return 0;
        }
        if ("en".equals(strategy.getLanguageCode())) {
            return 0;
        }
        switch (strategy.getScriptGroup()) {
            case ARABIC:
            case CYRILLIC:
            case CJK:
            case HEBREW:
            case INDIC:
            case THAI:
                return MIN_SAMPLE_HITS_COMPLEX_SCRIPT;
            case LATIN:
                return 0;
            default:
                return 1;
        }
    }

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

    private void collectUntranslatedTokens(String bodyText,
            ILocaleStrategy strategy,
            String location,
            Map<String, List<TextOccurrence>> foundOccurrences) {
        if (bodyText == null || bodyText.isBlank()) {
            return;
        }

        Set<String> allowedWords = strategy.getAllowedEnglishWords().stream()
                .map(word -> word == null ? "" : word.trim().toLowerCase(Locale.ROOT))
                .filter(word -> !word.isBlank())
                .collect(Collectors.toSet());

        // 1) Common English UI phrases.
        for (String phrase : ENGLISH_UI_PHRASES) {
            addOccurrences(foundOccurrences, phrase, location, bodyText, false);
        }

        // 2) Locale-specific forbidden words.
        for (String word : strategy.getForbiddenWords()) {
            addOccurrences(foundOccurrences, word, location, bodyText, false);
        }

        // 3) Single English words with word-boundary matching.
        for (String word : ENGLISH_UI_WORDS) {
            String trimmed = word.trim();
            if (allowedWords.contains(trimmed.toLowerCase(Locale.ROOT))) {
                continue;
            }
            addOccurrences(foundOccurrences, trimmed, location, bodyText, true);
        }

        // 4) Heuristic for long English product-description lines.
        if (location.toLowerCase(Locale.ROOT).contains("product")) {
            for (String snippet : detectEnglishSentenceSnippets(bodyText)) {
                addOccurrences(foundOccurrences, snippet, location, bodyText, false);
            }
        }
    }

    private List<String> detectEnglishSentenceSnippets(String bodyText) {
        if (bodyText == null || bodyText.isBlank()) {
            return Collections.emptyList();
        }

        List<String> hits = new ArrayList<>();
        String[] lines = bodyText.split("\\R");
        for (String rawLine : lines) {
            String line = rawLine == null ? "" : rawLine.trim();
            if (line.length() < 40) {
                continue;
            }

            List<String> words = Pattern.compile("[A-Za-z]{3,}")
                    .matcher(line)
                    .results()
                    .map(m -> m.group().toLowerCase(Locale.ROOT))
                    .collect(Collectors.toList());

            if (words.size() < 8) {
                continue;
            }

            long stopwordHits = words.stream().filter(ENGLISH_STOP_WORDS::contains).count();
            if (stopwordHits >= 4) {
                String clipped = line.length() > 90 ? line.substring(0, 90) + "..." : line;
                hits.add("ENGLISH_SENTENCE:" + clipped);
            }
        }

        return hits.stream().distinct().collect(Collectors.toList());
    }

    private String getTestCustomerEmail() {
        String value = System.getProperty("l10n.customer.email", DEFAULT_TEST_CUSTOMER_EMAIL);
        return value == null ? DEFAULT_TEST_CUSTOMER_EMAIL : value.trim();
    }

    private String getTestCustomerPassword() {
        String value = System.getProperty("l10n.customer.password", DEFAULT_TEST_CUSTOMER_PASSWORD);
        return value == null ? DEFAULT_TEST_CUSTOMER_PASSWORD : value;
    }

    private List<String> extractNumericCandidates(String bodyText) {
        if (bodyText == null || bodyText.isBlank()) {
            return Collections.emptyList();
        }
        Pattern numericPattern = Pattern.compile("\\b\\p{Nd}{1,3}(?:[.,\\s]\\p{Nd}{3})+(?:[.,]\\p{Nd}{2})?\\b");
        Matcher matcher = numericPattern.matcher(bodyText);
        List<String> results = new ArrayList<>();
        while (matcher.find()) {
            results.add(matcher.group());
        }
        return results.stream().distinct().limit(30).collect(Collectors.toList());
    }

    private boolean matchesLocaleNumberSeparators(String candidate, ILocaleStrategy strategy) {
        String decimal = strategy.getDecimalSeparator();
        String grouping = strategy.getGroupingSeparator();
        String normalized = candidate.replace('\u00A0', ' ').trim();

        int dotIndex = normalized.lastIndexOf('.');
        int commaIndex = normalized.lastIndexOf(',');
        int decimalIndex = Math.max(dotIndex, commaIndex);

        if (decimalIndex > 0 && decimalIndex < normalized.length() - 1) {
            String usedDecimal = normalized.substring(decimalIndex, decimalIndex + 1);
            if (!usedDecimal.equals(decimal) && normalized.length() - decimalIndex - 1 <= 2) {
                return false;
            }
        }

        if (grouping == null || grouping.isBlank()) {
            return true;
        }

        String integerPart = decimalIndex > 0 ? normalized.substring(0, decimalIndex) : normalized;
        if (integerPart.contains(".") || integerPart.contains(",") || integerPart.contains(" ")) {
            if (!integerPart.contains(grouping)) {
                return false;
            }
        }
        return true;
    }

    private List<String> extractEnglishMeasurementUnits(String bodyText) {
        if (bodyText == null || bodyText.isBlank()) {
            return Collections.emptyList();
        }
        List<String> found = new ArrayList<>();
        for (String unit : ENGLISH_MEASUREMENT_UNITS) {
            Pattern p = Pattern.compile("\\b" + Pattern.quote(unit) + "\\b", Pattern.CASE_INSENSITIVE);
            if (p.matcher(bodyText).find()) {
                found.add(unit);
            }
        }
        return found;
    }

    private boolean containsEnglishUiToken(String text) {
        if (text == null || text.isBlank()) {
            return false;
        }
        String lowered = text.toLowerCase(Locale.ROOT);
        for (String phrase : ENGLISH_UI_PHRASES) {
            if (lowered.contains(phrase.toLowerCase(Locale.ROOT))) {
                return true;
            }
        }
        for (String word : ENGLISH_UI_WORDS) {
            Pattern p = Pattern.compile("\\b" + Pattern.quote(word) + "\\b", Pattern.CASE_INSENSITIVE);
            if (p.matcher(text).find()) {
                return true;
            }
        }
        return false;
    }

    private boolean urlContainsLocaleHint(String url, String isoCode, String prestaCode) {
        String lower = safeLower(url);
        if (lower == null || lower.isBlank()) {
            return false;
        }
        return lower.contains("lang=" + isoCode)
                || lower.contains("locale=" + isoCode)
                || lower.contains("lang=" + prestaCode)
                || lower.contains("locale=" + prestaCode)
                || lower.contains("/" + isoCode + "/")
                || lower.endsWith("/" + isoCode)
                || lower.contains("/" + prestaCode + "/")
                || lower.endsWith("/" + prestaCode);
    }

    private String safeLower(String value) {
        return value == null ? null : value.toLowerCase(Locale.ROOT);
    }

    private L10nError createError(String type, String message, String langCode, String pageUrl) {
        return L10nError.createWithScreenshot(type, message, langCode, pageUrl, driverManager);
    }

    private L10nError createDetailedError(String type, String langCode, String pageUrl,
            String location, String expected, String actual, String evidence) {
        StringBuilder sb = new StringBuilder();
        if (location != null && !location.isBlank()) {
            sb.append("location=").append(location).append(" | ");
        }
        if (expected != null && !expected.isBlank()) {
            sb.append("expected=").append(expected).append(" | ");
        }
        if (actual != null && !actual.isBlank()) {
            sb.append("actual=").append(actual).append(" | ");
        }
        if (evidence != null && !evidence.isBlank()) {
            sb.append("evidence=").append(evidence);
        }

        String message = sb.toString().trim();
        if (message.endsWith("|")) {
            message = message.substring(0, message.length() - 1).trim();
        }
        return createError(type, message, langCode, pageUrl);
    }

    private String safeSnippet(String bodyText, int startIndex, int tokenLength) {
        if (bodyText == null || bodyText.isBlank()) {
            return "";
        }
        int from = Math.max(0, startIndex - 25);
        int to = Math.min(bodyText.length(), startIndex + Math.max(tokenLength, 1) + 25);
        String snippet = bodyText.substring(from, to).replaceAll("\\s+", " ").trim();
        return snippet.length() > 120 ? snippet.substring(0, 120) + "..." : snippet;
    }

    private void addOccurrences(Map<String, List<TextOccurrence>> foundOccurrences,
            String token, String location, String bodyText, boolean wholeWord) {
        if (token == null || token.isBlank() || bodyText == null || bodyText.isBlank()) {
            return;
        }

        if (wholeWord) {
            Pattern p = Pattern.compile("\\b" + Pattern.quote(token) + "\\b", Pattern.CASE_INSENSITIVE);
            Matcher matcher = p.matcher(bodyText);
            while (matcher.find()) {
                List<TextOccurrence> bucket = foundOccurrences.computeIfAbsent(token, k -> new ArrayList<>());
                if (bucket.size() >= MAX_OCCURRENCES_PER_TOKEN) {
                    break;
                }
                bucket.add(new TextOccurrence(location, matcher.start(),
                        safeSnippet(bodyText, matcher.start(), token.length())));
            }
            return;
        }

        String haystack = bodyText.toLowerCase(Locale.ROOT);
        String needle = token.toLowerCase(Locale.ROOT);
        int fromIndex = 0;
        while (fromIndex < haystack.length()) {
            int idx = haystack.indexOf(needle, fromIndex);
            if (idx < 0) {
                break;
            }
            List<TextOccurrence> bucket = foundOccurrences.computeIfAbsent(token, k -> new ArrayList<>());
            if (bucket.size() >= MAX_OCCURRENCES_PER_TOKEN) {
                break;
            }
            bucket.add(new TextOccurrence(location, idx, safeSnippet(bodyText, idx, token.length())));
            fromIndex = idx + Math.max(needle.length(), 1);
        }
    }

    private String summarizeOccurrences(List<TextOccurrence> occurrences) {
        if (occurrences == null || occurrences.isEmpty()) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < occurrences.size(); i++) {
            TextOccurrence occ = occurrences.get(i);
            if (i > 0) {
                sb.append("; ");
            }
            sb.append("#").append(i + 1)
                    .append("[").append(occ.location).append("]")
                    .append(" idx=").append(occ.index)
                    .append(" snippet=\"").append(occ.snippet).append("\"");
        }
        return sb.toString();
    }
}
