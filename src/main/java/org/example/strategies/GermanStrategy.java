package org.example.strategies;

import java.util.List;

/**
 * Strategy for German (Deutsch).
 */
public class GermanStrategy implements ILocaleStrategy {

    @Override
    public String getLanguageCode() {
        return "de";
    }

    @Override
    public String getLanguageName() {
        return "Deutsch";
    }

    @Override
    public String getCurrencySymbol() {
        return "€";
    }

    @Override
    public List<String> getAcceptedCurrencySymbols() {
        return List.of("€", "EUR");
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
        return ".";
    }

    @Override
    public String getDatePattern() {
        return "dd.MM.yyyy";
    }

    @Override
    public List<String> getAcceptedDatePatterns() {
        return List.of("dd.MM.yyyy", "d.M.yyyy", "dd/MM/yyyy", "yyyy-MM-dd");
    }

    @Override
    public boolean isRTL() {
        return false;
    }

    @Override
    public List<String> getForbiddenWords() {
        return List.of(
                "Add to cart", "Sign in", "Search", "Home", "Contact us", "Cart");
    }

    @Override
    public List<String> getAllowedEnglishWords() {
        return List.of("Sale", "Hot", "Menu");
    }

    @Override
    public List<String> getExpectedKeywords() {
        return List.of(
                "In den Warenkorb", "Anmelden", "Suche", "Startseite", "Kontakt", "Warenkorb");
    }

    @Override
    public ScriptGroup getScriptGroup() {
        return ScriptGroup.LATIN;
    }

    @Override
    public String getCharacterSample() {
        return "äöüÄÖÜß";
    }

    @Override
    public String getCharacterValidationPattern() {
        return "[a-zA-ZäöüÄÖÜß]+";
    }

    @Override
    public String getPhoneRegex() {
        return "\\+?49[\\s.-]?\\d{2,5}[\\s.-]?\\d{3,8}";
    }
}
