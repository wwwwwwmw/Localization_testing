package org.example.strategies;

import java.util.Collections;
import java.util.List;

/**
 * Strategy cho tiếng Anh (English - US).
 *
 * Currency: $1,234.56 (prefix, dot decimal, comma grouping)
 * Date: MM/dd/yyyy
 * Direction: LTR
 * Phone: +1 (xxx) xxx-xxxx
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

    // — Currency —
    @Override
    public String getCurrencySymbol() {
        return "$";
    }

    @Override
    public boolean isCurrencySymbolPrefix() {
        return true;
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
        return "MM/dd/yyyy";
    }

    @Override
    public List<String> getAcceptedDatePatterns() {
        return List.of("MM/dd/yyyy", "M/d/yyyy", "yyyy-MM-dd");
    }

    // — Layout —
    @Override
    public boolean isRTL() {
        return false;
    }

    // — Untranslated —
    @Override
    public List<String> getForbiddenWords() {
        return Collections.emptyList();
    }

    @Override
    public List<String> getExpectedKeywords() {
        return List.of("Add to cart", "Sign in", "Search", "Home",
                "Clothes", "Accessories", "Art", "Price", "Contact us", "Cart");
    }

    // — Encoding —
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

    // — Phone —
    @Override
    public String getPhoneRegex() {
        return "\\+?1?[\\s.-]?\\(?\\d{3}\\)?[\\s.-]?\\d{3}[\\s.-]?\\d{4}";
    }
}
