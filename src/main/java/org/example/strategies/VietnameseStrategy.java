package org.example.strategies;

import java.util.List;

/**
 * Strategy cho tiếng Việt (Vietnamese).
 *
 * Currency: 100.000 ₫ (suffix, comma decimal, dot grouping)
 * Date: dd/MM/yyyy
 * Direction: LTR
 * Script: Latin + dấu thanh (à, á, ả, ã, ạ, ă, â, đ, ê, ô, ơ, ư...)
 * Phone: +84 xxx xxx xxxx
 *
 * Lưu ý: PrestaShop dùng mã "vn" cho tiếng Việt thay vì "vi".
 */
public class VietnameseStrategy implements ILocaleStrategy {

    @Override
    public String getLanguageCode() {
        return "vi";
    }

    @Override
    public String getLanguageName() {
        return "Tiếng Việt";
    }

    @Override
    public String getPrestaShopCode() {
        return "vn";
    }

    // — Currency —
    @Override
    public String getCurrencySymbol() {
        return "₫";
    }

    @Override
    public List<String> getAcceptedCurrencySymbols() {
        return List.of("₫", "VND");
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

    // — Date —
    @Override
    public String getDatePattern() {
        return "dd/MM/yyyy";
    }

    @Override
    public List<String> getAcceptedDatePatterns() {
        return List.of("dd/MM/yyyy", "d/M/yyyy", "dd-MM-yyyy", "dd.MM.yyyy");
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
                "New products", "Best sales", "Delivery", "Terms and conditions");
    }

    @Override
    public List<String> getExpectedKeywords() {
        return List.of("Thêm vào giỏ", "Đăng nhập", "Tìm kiếm", "Trang chủ",
                "Quần áo", "Phụ kiện", "Nghệ thuật", "Giá", "Liên hệ", "Giỏ hàng");
    }

    // — Encoding —
    @Override
    public ScriptGroup getScriptGroup() {
        return ScriptGroup.LATIN;
    }

    @Override
    public String getCharacterSample() {
        return "àáạảãâầấậẩẫăằắặẳẵèéẹẻẽêềếệểễìíịỉĩòóọỏõôồốộổỗơờớợởỡùúụủũưừứựửữỳýỵỷỹđ";
    }

    @Override
    public String getCharacterValidationPattern() {
        return "[a-zA-ZàáạảãâầấậẩẫăằắặẳẵèéẹẻẽêềếệểễìíịỉĩòóọỏõôồốộổỗơờớợởỡùúụủũưừứựửữỳýỵỷỹđĐ]+";
    }

    // — Phone —
    @Override
    public String getPhoneRegex() {
        return "\\+?84[\\s.-]?\\d{2,3}[\\s.-]?\\d{3}[\\s.-]?\\d{3,4}";
    }
}
