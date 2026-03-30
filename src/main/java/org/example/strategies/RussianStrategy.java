package org.example.strategies;

import java.util.List;

/**
 * Strategy for Russian (Русский).
 */
public class RussianStrategy implements ILocaleStrategy {

    @Override
    public String getLanguageCode() {
        return "ru";
    }

    @Override
    public String getLanguageName() {
        return "Русский";
    }

    @Override
    public String getCurrencySymbol() {
        return "₽";
    }

    @Override
    public List<String> getAcceptedCurrencySymbols() {
        return List.of("₽", "RUB");
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
    public List<String> getExpectedKeywords() {
        return List.of(
                "Добавить в корзину", "Войти", "Поиск", "Главная", "Контакты", "Корзина");
    }

    @Override
    public ScriptGroup getScriptGroup() {
        return ScriptGroup.CYRILLIC;
    }

    @Override
    public String getCharacterSample() {
        return "АБВГДЕЖЗИЙКЛМНОПРСТУФХЦЧШЩЭЮЯабвгдежзийклмнопрстуфхцчшщэюя";
    }

    @Override
    public String getCharacterValidationPattern() {
        return "[\\u0400-\\u04FF]+";
    }

    @Override
    public String getPhoneRegex() {
        return "\\+?7[\\s.-]?\\(?\\d{3}\\)?[\\s.-]?\\d{3}[\\s.-]?\\d{2}[\\s.-]?\\d{2}";
    }
}
