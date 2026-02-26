package org.example.strategy;

import java.util.List;

/**
 * Strategy cho ngôn ngữ tiếng Pháp (Français)
 * 
 * Đặc điểm:
 * - Currency: 1 234,56 € (suffix, comma decimal, space grouping)
 * - Date: dd/MM/yyyy
 * - Direction: LTR
 * - Script: Latin với các ký tự có dấu (é, è, ê, à, ç...)
 */
public class FrenchStrategy implements ILocaleStrategy {

    @Override
    public String getLanguageCode() {
        return "fr";
    }

    @Override
    public String getLanguageName() {
        return "Français";
    }

    // ==================== CURRENCY FORMAT ====================

    @Override
    public String getCurrencySymbol() {
        return "€";
    }

    @Override
    public boolean isCurrencySymbolPrefix() {
        return false; // 100 €
    }

    @Override
    public String getDecimalSeparator() {
        return ","; // 1,99
    }

    @Override
    public String getGroupingSeparator() {
        return " "; // 1 000 (non-breaking space)
    }

    // ==================== DATE FORMAT ====================

    @Override
    public String getDatePattern() {
        return "dd/MM/yyyy";
    }

    @Override
    public List<String> getAcceptedDatePatterns() {
        return List.of("dd/MM/yyyy", "d/M/yyyy", "dd-MM-yyyy");
    }

    // ==================== LAYOUT & DIRECTION ====================

    @Override
    public boolean isRTL() {
        return false;
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
                "Best sales");
    }

    @Override
    public List<String> getExpectedKeywords() {
        return List.of(
                "Ajouter au panier",
                "Connexion",
                "Rechercher",
                "Accueil",
                "Vêtements",
                "Accessoires",
                "Art",
                "Prix",
                "Contactez-nous",
                "Panier");
    }

    // ==================== ENCODING & FONTS ====================

    @Override
    public ScriptGroup getScriptGroup() {
        return ScriptGroup.LATIN;
    }

    @Override
    public String getCharacterSample() {
        return "àâäéèêëïîôùûüçœæÀÂÄÉÈÊËÏÎÔÙÛÜÇŒÆ";
    }

    @Override
    public String getCharacterValidationPattern() {
        return "[a-zA-ZàâäéèêëïîôùûüçœæÀÂÄÉÈÊËÏÎÔÙÛÜÇŒÆ]+";
    }
}
