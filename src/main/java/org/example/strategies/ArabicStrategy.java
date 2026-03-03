package org.example.strategies;

import java.util.List;

/**
 * Strategy cho tiếng Ả Rập (العربية - Arabic).
 *
 * Currency: 1,234.56 € hoặc ر.س (dot decimal theo chuẩn quốc tế)
 * Date: dd/MM/yyyy
 * Direction: RTL (Right-to-Left) ⚠️
 * Phone: +966 xx xxx xxxx (Saudi Arabia)
 *
 * Lưu ý RTL:
 * - HTML phải có dir="rtl"
 * - Text alignment mặc định là right
 * - Thứ tự UI đảo ngược
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

    // — Currency —
    @Override
    public String getCurrencySymbol() {
        return "ر.س";
    }

    @Override
    public List<String> getAcceptedCurrencySymbols() {
        return List.of("ر.س", "د.إ", "﷼");
    }

    @Override
    public boolean isCurrencySymbolPrefix() {
        return false;
    }

    @Override
    public String getDecimalSeparator() {
        return ".";
    }

    @Override
    public String getGroupingSeparator() {
        return ",";
    }

    // — Date —
    @Override
    public String getDatePattern() {
        return "dd/MM/yyyy";
    }

    @Override
    public List<String> getAcceptedDatePatterns() {
        return List.of("dd/MM/yyyy", "d/M/yyyy", "yyyy-MM-dd");
    }

    // — Layout —
    @Override
    public boolean isRTL() {
        return true;
    }

    @Override
    public String getExpectedTextAlignment() {
        return "right";
    }

    // — Untranslated —
    @Override
    public List<String> getForbiddenWords() {
        return List.of("Add to cart", "Sign in", "My account", "Search our catalog",
                "Contact us", "About us", "Create account", "Checkout", "Cart",
                "Quick view", "Add to wishlist", "Free shipping", "Discount",
                "New products", "Best sales", "Home", "Clothes", "Accessories");
    }

    @Override
    public List<String> getExpectedKeywords() {
        return List.of("أضف إلى السلة", "تسجيل الدخول", "بحث", "الرئيسية",
                "ملابس", "إكسسوارات", "فن", "السعر", "اتصل بنا", "سلة التسوق");
    }

    // — Encoding —
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
        return "[\\u0600-\\u06FF]+";
    }

    // — Phone —
    @Override
    public String getPhoneRegex() {
        return "\\+?966[\\s.-]?\\d{1,2}[\\s.-]?\\d{3}[\\s.-]?\\d{4}";
    }
}
