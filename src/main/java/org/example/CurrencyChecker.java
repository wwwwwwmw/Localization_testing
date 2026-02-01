package org.example;

import org.openqa.selenium.*;
import java.util.*;
import java.util.regex.*;

/**
 * Kiem tra dinh dang tien te
 * Luu y: PrestaShop demo mac dinh dung EUR (Euro) cho tat ca ngon ngu
 * 
 * Da tach cac ham static de ho tro Unit Test khong can Selenium Driver
 */
public class CurrencyChecker {

    private WebDriver driver;

    // Cac ky hieu tien te hop le (PrestaShop demo dung EUR mac dinh)
    private static final String[] VALID_CURRENCY_SYMBOLS = { "€", "$", "£", "¥", "₩", "₫", "฿", "₹", "zł", "Kč", "kr",
            "лв", "₽", "грн", "₺", "Ft", "lei", "L", "R$", "Rp", "KM", "NT$", "₴", "₪", "﷼", "৳", "د.إ", "ر.س" };

    // Pattern de nhan dien gia tien
    private static final Pattern PRICE_PATTERN = Pattern.compile(
            "([€$£¥₩₫฿₹₽₴₪﷼৳₺])?\\s*([\\d,. ]+(?:[.,]\\d{1,2})?)\\s*([€$£¥₩₫฿₹₽₴₪﷼৳₺]|zł|Kč|kr|лв|грн|Ft|lei|L|R\\$|Rp|KM|NT\\$|د\\.إ|ر\\.س)?");

    public CurrencyChecker(WebDriver driver) {
        this.driver = driver;
    }

    // ==================== STATIC UTILITY METHODS (cho Unit Test)
    // ====================

    /**
     * Nhan dien ky hieu tien te tu chuoi gia
     * 
     * @param priceText Chuoi chua gia tien (vd: "10,00 €", "$9.99")
     * @return Ky hieu tien te tim duoc hoac null
     */
    public static String detectCurrencySymbol(String priceText) {
        if (priceText == null || priceText.isEmpty())
            return null;

        for (String symbol : VALID_CURRENCY_SYMBOLS) {
            if (priceText.contains(symbol)) {
                return symbol;
            }
        }
        return null;
    }

    /**
     * Kiem tra xem chuoi co chua gia tien hop le khong
     * 
     * @param priceText Chuoi can kiem tra
     * @return true neu co dinh dang gia tien hop le
     */
    public static boolean isValidPriceFormat(String priceText) {
        if (priceText == null || priceText.isEmpty())
            return false;

        String symbol = detectCurrencySymbol(priceText);
        boolean hasNumber = priceText.matches(".*\\d+.*");

        return symbol != null && hasNumber;
    }

    /**
     * Trich xuat gia tri so tu chuoi gia tien
     * 
     * @param priceText        Chuoi chua gia tien
     * @param decimalSeparator Dau phan cach thap phan ("." hoac ",")
     * @return Gia tri so hoac -1 neu khong hop le
     */
    public static double extractNumericValue(String priceText, String decimalSeparator) {
        if (priceText == null || priceText.isEmpty())
            return -1;

        // Loai bo ky hieu tien te va khoang trang
        String cleaned = priceText.replaceAll("[^0-9,. ]", "").trim();

        if (cleaned.isEmpty())
            return -1;

        try {
            // Chuan hoa dinh dang
            if (",".equals(decimalSeparator)) {
                // Format chau Au: 1.234,56 -> 1234.56
                cleaned = cleaned.replace(".", "").replace(",", ".").replace(" ", "");
            } else {
                // Format My: 1,234.56 -> 1234.56
                cleaned = cleaned.replace(",", "").replace(" ", "");
            }

            return Double.parseDouble(cleaned);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    /**
     * Kiem tra dinh dang tien te co khop voi cau hinh ngon ngu
     * 
     * @param priceText Chuoi gia tien
     * @param config    Cau hinh ngon ngu
     * @return CurrencyCheckResult chua ket qua kiem tra
     */
    public static CurrencyCheckResult validateCurrency(String priceText, LanguageConfig config) {
        CurrencyCheckResult result = new CurrencyCheckResult();
        result.originalText = priceText;
        result.isValid = false;

        if (priceText == null || priceText.isEmpty()) {
            result.errorMessage = "Chuoi gia tien rong";
            return result;
        }

        // Kiem tra ky hieu tien te
        result.detectedSymbol = detectCurrencySymbol(priceText);
        if (result.detectedSymbol == null) {
            result.errorMessage = "Khong tim thay ky hieu tien te";
            return result;
        }

        // Kiem tra so
        if (!priceText.matches(".*\\d+.*")) {
            result.errorMessage = "Khong tim thay gia tri so";
            return result;
        }

        // Kiem tra dau phan cach thap phan
        result.numericValue = extractNumericValue(priceText, config.decimalSeparator);
        if (result.numericValue < 0) {
            result.errorMessage = "Khong the phan tich gia tri so";
            return result;
        }

        // Kiem tra ky hieu co dung voi ngon ngu khong
        boolean matchesPrimary = config.primaryCurrency != null && result.detectedSymbol.equals(config.primaryCurrency);
        boolean matchesSecondary = config.secondaryCurrency != null
                && result.detectedSymbol.equals(config.secondaryCurrency);
        boolean matchesDefault = config.defaultCurrency != null && result.detectedSymbol.equals(config.defaultCurrency);
        boolean isEuro = result.detectedSymbol.equals("€"); // PrestaShop default

        if (!matchesPrimary && !matchesSecondary && !matchesDefault && !isEuro) {
            result.warningMessage = "Ky hieu tien te '" + result.detectedSymbol + "' khong khop voi cau hinh ngon ngu";
        }

        result.isValid = true;
        return result;
    }

    /**
     * Kiem tra dinh dang so theo DecimalSeparatorType
     * 
     * @param numberText Chuoi so
     * @param type       Loai dau phan cach
     * @return true neu dinh dang dung
     */
    public static boolean validateNumberFormat(String numberText, LanguageConfig.DecimalSeparatorType type) {
        if (numberText == null || numberText.isEmpty())
            return false;

        // Loai bo ky hieu tien te
        String cleaned = numberText.replaceAll("[^0-9,. ]", "").trim();

        if (type == LanguageConfig.DecimalSeparatorType.DOT) {
            // My: 1,234.56
            return cleaned.matches("[\\d,]+(\\.\\d{1,2})?");
        } else {
            // Chau Au: 1.234,56 hoac 1 234,56
            return cleaned.matches("[\\d. ]+([,]\\d{1,2})?");
        }
    }

    /**
     * Chuyen doi gia tien giua cac dinh dang
     * 
     * @param priceText   Chuoi gia goc
     * @param fromDecimal Dau thap phan goc
     * @param toDecimal   Dau thap phan dich
     * @return Chuoi gia da chuyen doi
     */
    public static String convertPriceFormat(String priceText, String fromDecimal, String toDecimal) {
        if (priceText == null)
            return null;
        if (fromDecimal.equals(toDecimal))
            return priceText;

        String result = priceText;
        if (",".equals(fromDecimal) && ".".equals(toDecimal)) {
            // 1.234,56 -> 1,234.56
            result = result.replace(".", "TEMP").replace(",", ".").replace("TEMP", ",");
        } else if (".".equals(fromDecimal) && ",".equals(toDecimal)) {
            // 1,234.56 -> 1.234,56
            result = result.replace(",", "TEMP").replace(".", ",").replace("TEMP", ".");
        }
        return result;
    }

    // ==================== CLASS KET QUA ====================

    /**
     * Class chua ket qua kiem tra tien te
     */
    public static class CurrencyCheckResult {
        public String originalText;
        public String detectedSymbol;
        public double numericValue;
        public boolean isValid;
        public String errorMessage;
        public String warningMessage;

        @Override
        public String toString() {
            if (isValid) {
                String msg = "OK: " + originalText + " (symbol: " + detectedSymbol + ", value: " + numericValue + ")";
                if (warningMessage != null)
                    msg += " [Warning: " + warningMessage + "]";
                return msg;
            } else {
                return "ERROR: " + originalText + " - " + errorMessage;
            }
        }
    }

    // ==================== SELENIUM-DEPENDENT METHODS ====================

    // Retry configuration for stale element handling
    private static final int MAX_RETRY_ATTEMPTS = 3;
    private static final long RETRY_DELAY_MS = 500;

    /**
     * Kiem tra dinh dang tien te tren trang
     */
    public void check(LanguageConfig config) {
        System.out.println("\n[KIEM TRA TIEN TE]");
        System.out.println("   Tien te mong doi: EUR (Euro) - mac dinh cua PrestaShop demo");

        try {
            Set<String> checkedPrices = new HashSet<>();
            int validCount = 0;
            int errorCount = 0;

            // Use retry mechanism to handle stale elements
            List<String> priceTexts = extractPricesWithRetry();

            if (priceTexts.isEmpty()) {
                System.out.println("   [CANH BAO] Khong tim thay gia tien tren trang nay.");
                return;
            }

            for (String priceText : priceTexts) {
                if (priceText.isEmpty() || checkedPrices.contains(priceText) || priceText.length() > 50)
                    continue;
                checkedPrices.add(priceText);

                // Su dung ham static de kiem tra
                CurrencyCheckResult result = validateCurrency(priceText, config);

                if (result.isValid) {
                    String displayPrice = toAsciiSafe(priceText);
                    System.out
                            .println("   [OK] Gia: " + displayPrice + " (ky hieu: " + result.detectedSymbol + ")");
                    if (result.warningMessage != null) {
                        System.out.println("        [CANH BAO] " + result.warningMessage);
                    }
                    validCount++;
                } else {
                    String displayPrice = toAsciiSafe(priceText);
                    System.out.println("   [LOI] Gia: " + displayPrice + " - " + result.errorMessage);
                    errorCount++;
                }
            }

            System.out.println("   >> Tong ket: " + validCount + " gia dung, " + errorCount + " gia loi");

        } catch (Exception e) {
            System.out.println("   [LOI] Loi kiem tra tien te: " + e.getMessage());
        }
    }

    /**
     * Lay danh sach gia tien tu trang web voi co che retry
     * 
     * @return Danh sach chuoi gia tien
     */
    private List<String> extractPricesWithRetry() {
        List<String> prices = new ArrayList<>();

        for (int attempt = 1; attempt <= MAX_RETRY_ATTEMPTS; attempt++) {
            try {
                prices.clear();
                List<WebElement> priceElements = driver.findElements(By.cssSelector(
                        ".price, .product-price, .current-price, [class*='price'], .regular-price"));

                for (WebElement element : priceElements) {
                    String priceText = getTextWithRetry(element);
                    if (priceText != null && !priceText.isEmpty() && priceText.length() <= 50) {
                        prices.add(priceText);
                    }
                }

                // If we got prices successfully, return
                if (!prices.isEmpty()) {
                    return prices;
                }

            } catch (Exception e) {
                System.out.println(
                        "   [RETRY " + attempt + "/" + MAX_RETRY_ATTEMPTS + "] Loi lay gia tien: " + e.getMessage());
                if (attempt < MAX_RETRY_ATTEMPTS) {
                    try {
                        Thread.sleep(RETRY_DELAY_MS);
                    } catch (InterruptedException ignored) {
                    }
                }
            }
        }

        return prices;
    }

    /**
     * Lay text tu element voi co che retry
     * 
     * @param element WebElement can lay text
     * @return Text hoac null neu that bai sau tat ca lan retry
     */
    private String getTextWithRetry(WebElement element) {
        for (int attempt = 1; attempt <= MAX_RETRY_ATTEMPTS; attempt++) {
            try {
                return element.getText().trim();
            } catch (StaleElementReferenceException e) {
                if (attempt < MAX_RETRY_ATTEMPTS) {
                    try {
                        Thread.sleep(RETRY_DELAY_MS);
                    } catch (InterruptedException ignored) {
                    }
                }
            }
        }
        return null;
    }

    /**
     * Lay danh sach gia tien tu trang web
     * 
     * @return Danh sach chuoi gia tien
     */
    public List<String> extractPricesFromPage() {
        return extractPricesWithRetry();
    }

    /**
     * Chuyen ky tu Unicode thanh ASCII an toan cho console
     */
    private String toAsciiSafe(String text) {
        if (text == null)
            return "";
        return text
                .replace("€", "EUR")
                .replace("£", "GBP")
                .replace("¥", "JPY")
                .replace("₩", "KRW")
                .replace("₫", "VND")
                .replace("฿", "THB")
                .replace("₹", "INR")
                .replace("₽", "RUB");
    }
}
