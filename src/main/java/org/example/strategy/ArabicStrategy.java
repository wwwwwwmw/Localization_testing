package org.example.strategy;

import java.util.List;

/**
 * Strategy cho ngôn ngữ tiếng Ả Rập (العربية - Arabic)
 * 
 * Đặc điểm:
 * - Currency: 1,234.56 € hoặc ر.س (dot decimal theo chuẩn quốc tế)
 * - Date: dd/MM/yyyy
 * - Direction: RTL (Right-to-Left) ⚠️ QUAN TRỌNG
 * - Script: Arabic script (ا ب ت ث ج ح خ...)
 * 
 * Lưu ý đặc biệt cho RTL:
 * - HTML phải có dir="rtl"
 * - Text alignment mặc định là right
 * - Thứ tự các phần tử UI bị đảo ngược
 */
public class ArabicStrategy implements ILocaleStrategy {

    @Override
    public String getLanguageCode() {
        return "ar";
    }

    @Override
    public String getLanguageName() {
        return "العربية";
    }

    // ==================== CURRENCY FORMAT ====================

    @Override
    public String getCurrencySymbol() {
        return "ر.س"; // Saudi Riyal
    }

    @Override
    public List<String> getAcceptedCurrencySymbols() {
        return List.of("ر.س", "€", "د.إ", "﷼", "$"); // Euro là default của PrestaShop
    }

    @Override
    public boolean isCurrencySymbolPrefix() {
        return false; // Suffix trong tiếng Ả Rập
    }

    @Override
    public String getDecimalSeparator() {
        return "."; // Theo chuẩn quốc tế
    }

    @Override
    public String getGroupingSeparator() {
        return ","; // 1,000
    }

    // ==================== DATE FORMAT ====================

    @Override
    public String getDatePattern() {
        return "dd/MM/yyyy";
    }

    @Override
    public List<String> getAcceptedDatePatterns() {
        return List.of("dd/MM/yyyy", "d/M/yyyy", "yyyy-MM-dd");
    }

    // ==================== LAYOUT & DIRECTION ====================

    @Override
    public boolean isRTL() {
        return true; // ⚠️ RTL - Right to Left
    }

    @Override
    public String getExpectedTextAlignment() {
        return "right";
    }

    // ==================== UNTRANSLATED CONTENT ====================

    @Override
    public List<String> getForbiddenWords() {
        return List.of(
                "Add to cart",
                "Sign in",
                "My account",
                "Search our catalog",
                "Contact us",
                "About us",
                "Create account",
                "Checkout",
                "Cart",
                "Quick view",
                "Add to wishlist",
                "Free shipping",
                "Discount",
                "New products",
                "Best sales",
                "Home",
                "Clothes",
                "Accessories");
    }

    @Override
    public List<String> getExpectedKeywords() {
        return List.of(
                "أضف إلى السلة",
                "تسجيل الدخول",
                "بحث",
                "الرئيسية",
                "ملابس",
                "إكسسوارات",
                "فن",
                "السعر",
                "اتصل بنا",
                "سلة التسوق");
    }

    // ==================== ENCODING & FONTS ====================

    @Override
    public ScriptGroup getScriptGroup() {
        return ScriptGroup.ARABIC;
    }

    @Override
    public String getCharacterSample() {
        return "ابتثجحخدذرزسشصضطظعغفقكلمنهوي";
    }

    @Override
    public String getCharacterValidationPattern() {
        return "[\\u0600-\\u06FF]+"; // Unicode range cho Arabic script
    }
}
