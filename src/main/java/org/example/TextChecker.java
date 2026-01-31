package org.example;

import org.openqa.selenium.*;
import java.util.*;

/**
 * Kiem tra van ban / dich thuat
 */
public class TextChecker {

    private WebDriver driver;
    private String currentLanguage;
    private List<L10nError> errors;

    public TextChecker(WebDriver driver, String language, List<L10nError> errors) {
        this.driver = driver;
        this.currentLanguage = language;
        this.errors = errors;
    }

    /**
     * Kiem tra van ban dich thuat tren trang
     */
    public void check(LanguageConfig config) {
        System.out.println("\n[KIEM TRA VAN BAN / DICH THUAT]");

        try {
            String pageText = driver.findElement(By.tagName("body")).getText();

            // Kiem tra cac tu khoa mong doi
            int foundCount = 0;
            for (String keyword : config.expectedKeywords) {
                if (pageText.toLowerCase().contains(keyword.toLowerCase())) {
                    foundCount++;
                    System.out.println("   [OK] Tim thay: " + keyword);
                }
            }

            // Kiem tra van ban tieng Anh khong mong muon (neu khong phai EN)
            if (!currentLanguage.equals("en")) {
                String[] englishOnlyWords = {
                        "Add to cart", "Sign in", "My account", "Search our catalog",
                        "PRODUCTS", "OUR COMPANY", "YOUR ACCOUNT", "STORE INFORMATION",
                        "Promotions", "New products", "Best sales", "Delivery", "Legal Notice",
                        "Terms and conditions", "About us", "Secure payment", "Contact us",
                        "Sitemap", "Stores", "Order tracking", "Create account", "My alerts"
                };

                for (String englishWord : englishOnlyWords) {
                    if (pageText.contains(englishWord)) {
                        String errorMsg = "Tim thay van ban tieng Anh: '" + englishWord + "' trong trang "
                                + config.languageName;
                        System.out.println("   [LOI] " + errorMsg);
                        errors.add(new L10nError("UNTRANSLATED_TEXT", "Van ban chua dich", errorMsg,
                                driver.getCurrentUrl()));
                    }
                }
            }

            // Ket qua
            System.out.println(
                    "   >> Tim thay " + foundCount + "/" + config.expectedKeywords.length + " tu khoa mong doi");

        } catch (Exception e) {
            System.out.println("   [LOI] Loi kiem tra van ban: " + e.getMessage());
        }
    }
}
