package org.example.strategy;

import java.util.Collections;
import java.util.List;

/**
 * Strategy cho ngôn ngữ tiếng Anh (English - US)
 * 
 * Đặc điểm:
 * - Currency: $1,234.56 (prefix, dot decimal, comma grouping)
 * - Date: MM/dd/yyyy
 * - Direction: LTR
 * - Script: Latin
 */
public class EnglishStrategy implements ILocaleStrategy {

    @Override
    public String getLanguageCode() {
        return "en";
    }

    @Override
    public String getLanguageName() {
        return "English";
    }

    // ==================== CURRENCY FORMAT ====================

    @Override
    public String getCurrencySymbol() {
        return "$";
    }

    @Override
    public boolean isCurrencySymbolPrefix() {
        return true; // $100
    }

    @Override
    public String getDecimalSeparator() {
        return "."; // 1.99
    }

    @Override
    public String getGroupingSeparator() {
        return ","; // 1,000
    }

    // ==================== DATE FORMAT ====================

    @Override
    public String getDatePattern() {
        return "MM/dd/yyyy"; // US format
    }

    @Override
    public List<String> getAcceptedDatePatterns() {
        return List.of("MM/dd/yyyy", "M/d/yyyy", "yyyy-MM-dd");
    }

    // ==================== LAYOUT & DIRECTION ====================

    @Override
    public boolean isRTL() {
        return false;
    }

    // ==================== UNTRANSLATED CONTENT ====================

    @Override
    public List<String> getForbiddenWords() {
        // Tiếng Anh là ngôn ngữ gốc, không có từ cấm
        return Collections.emptyList();
    }

    @Override
    public List<String> getExpectedKeywords() {
        return List.of(
                "Add to cart",
                "Sign in",
                "Search",
                "Home",
                "Clothes",
                "Accessories",
                "Art",
                "Price",
                "Contact us",
                "Cart");
    }

    // ==================== ENCODING & FONTS ====================

    @Override
    public ScriptGroup getScriptGroup() {
        return ScriptGroup.LATIN;
    }

    @Override
    public String getCharacterSample() {
        return "ABCDEFGabcdefg";
    }

    @Override
    public String getCharacterValidationPattern() {
        return "[a-zA-Z]+";
    }
}
