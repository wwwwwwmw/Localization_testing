package org.example.strategies;

import java.util.List;

/**
 * Strategy for Japanese (日本語).
 */
public class JapaneseStrategy implements ILocaleStrategy {

    @Override
    public String getLanguageCode() {
        return "ja";
    }

    @Override
    public String getLanguageName() {
        return "日本語";
    }

    @Override
    public String getCurrencySymbol() {
        return "¥";
    }

    @Override
    public List<String> getAcceptedCurrencySymbols() {
        return List.of("¥", "￥", "JPY");
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

    @Override
    public String getDatePattern() {
        return "yyyy/MM/dd";
    }

    @Override
    public List<String> getAcceptedDatePatterns() {
        return List.of("yyyy/MM/dd", "yyyy-MM-dd", "yyyy.MM.dd", "MM/dd/yyyy");
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
                "カートに追加", "ログイン", "検索", "ホーム", "お問い合わせ", "カート");
    }

    @Override
    public ScriptGroup getScriptGroup() {
        return ScriptGroup.CJK;
    }

    @Override
    public String getCharacterSample() {
        return "日本語あいうえおカタカナ漢字";
    }

    @Override
    public String getCharacterValidationPattern() {
        return "[\\p{IsHiragana}\\p{IsKatakana}\\p{IsHan}ー]+";
    }

    @Override
    public String getPhoneRegex() {
        return "\\+?81[\\s.-]?\\d{1,4}[\\s.-]?\\d{1,4}[\\s.-]?\\d{3,4}";
    }
}
