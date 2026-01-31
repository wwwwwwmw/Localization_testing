package org.example;

import org.openqa.selenium.*;
import java.util.*;

/**
 * Kiem tra dinh dang tien te
 * Luu y: PrestaShop demo mac dinh dung EUR (Euro) cho tat ca ngon ngu
 */
public class CurrencyChecker {

    private WebDriver driver;

    // Cac ky hieu tien te hop le (PrestaShop demo dung EUR mac dinh)
    private static final String[] VALID_CURRENCY_SYMBOLS = { "€", "$", "£", "¥", "₩", "₫", "฿", "₹", "zł", "Kč", "kr",
            "лв", "₽", "грн" };

    public CurrencyChecker(WebDriver driver) {
        this.driver = driver;
    }

    /**
     * Kiem tra dinh dang tien te tren trang
     */
    public void check(LanguageConfig config) {
        System.out.println("\n[KIEM TRA TIEN TE]");
        System.out.println("   Tien te mong doi: EUR (Euro) - mac dinh cua PrestaShop demo");

        try {
            List<WebElement> priceElements = driver.findElements(By.cssSelector(
                    ".price, .product-price, .current-price, [class*='price'], .regular-price"));

            if (priceElements.isEmpty()) {
                System.out.println("   [CANH BAO] Khong tim thay gia tien tren trang nay.");
                return;
            }

            Set<String> checkedPrices = new HashSet<>();
            int validCount = 0;
            int errorCount = 0;

            for (WebElement element : priceElements) {
                try {
                    String priceText = element.getText().trim();
                    if (priceText.isEmpty() || checkedPrices.contains(priceText) || priceText.length() > 50)
                        continue;
                    checkedPrices.add(priceText);

                    // Kiem tra xem co ky hieu tien te hop le khong
                    boolean hasValidCurrency = false;
                    String foundSymbol = "";
                    for (String symbol : VALID_CURRENCY_SYMBOLS) {
                        if (priceText.contains(symbol)) {
                            hasValidCurrency = true;
                            foundSymbol = symbol;
                            break;
                        }
                    }

                    // Kiem tra so tien co dung format khong (co chua so)
                    boolean hasNumber = priceText.matches(".*\\d+.*");

                    // Log ket qua
                    if (hasValidCurrency && hasNumber) {
                        String displayPrice = toAsciiSafe(priceText);
                        System.out.println("   [OK] Gia: " + displayPrice + " (ky hieu: " + foundSymbol + ")");
                        validCount++;
                    } else if (hasNumber && !hasValidCurrency) {
                        // Co so nhung khong co ky hieu tien te
                        String displayPrice = toAsciiSafe(priceText);
                        System.out.println("   [CANH BAO] Gia khong co ky hieu tien te: " + displayPrice);
                    }

                } catch (StaleElementReferenceException e) {
                    // Element da thay doi, bo qua
                }
            }

            System.out.println("   >> Tong ket: " + validCount + " gia dung, " + errorCount + " gia loi");

        } catch (Exception e) {
            System.out.println("   [LOI] Loi kiem tra tien te: " + e.getMessage());
        }
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
