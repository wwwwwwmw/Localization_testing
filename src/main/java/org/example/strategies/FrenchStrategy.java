package org.example.strategies;

import java.util.List;

/**
 * Strategy cho tiếng Pháp (Français).
 *
 * Currency: 1 234,56 € (suffix, comma decimal, space grouping)
 * Date: dd/MM/yyyy
 * Direction: LTR
 * Phone: +33 x xx xx xx xx
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

    // — Currency —
    @Override
    public String getCurrencySymbol() {
        return "€";
    }

    @Override
    public boolean isCurrencySymbolPrefix() {
        return false;
    }

    @Override
    public String getDecimalSeparator() {
        return ",";
    }

    @Override
    public String getGroupingSeparator() {
        return " ";
    }

    // — Date —
    @Override
    public String getDatePattern() {
        return "dd/MM/yyyy";
    }

    @Override
    public List<String> getAcceptedDatePatterns() {
        return List.of("dd/MM/yyyy", "d/M/yyyy", "dd-MM-yyyy");
    }

    // — Layout —
    @Override
    public boolean isRTL() {
        return false;
    }

    // — Untranslated —
    @Override
    public List<String> getForbiddenWords() {
        return List.of("Add to cart", "Sign in", "My account", "Search our catalog",
                "Contact us", "About us", "Create account", "Checkout", "Cart",
                "Quick view", "Add to wishlist", "Free shipping", "Discount",
                "New products", "Best sales");
    }

    @Override
    public List<String> getExpectedKeywords() {
        return List.of("Ajouter au panier", "Connexion", "Rechercher", "Accueil",
                "Vêtements", "Accessoires", "Art", "Prix", "Contactez-nous", "Panier");
    }

    // — Encoding —
    @Override
    public ScriptGroup getScriptGroup() {
        return ScriptGroup.LATIN;
    }

    @Override
    public String getCharacterSample() {
        return "àâäéèêëïîôùûüçœæ";
    }

    @Override
    public String getCharacterValidationPattern() {
        return "[a-zA-ZàâäéèêëïîôùûüçœæÀÂÄÉÈÊËÏÎÔÙÛÜÇŒÆ]+";
    }

    // — Phone —
    @Override
    public String getPhoneRegex() {
        return "\\+?33[\\s.-]?[1-9]([\\s.-]?\\d{2}){4}";
    }
}
