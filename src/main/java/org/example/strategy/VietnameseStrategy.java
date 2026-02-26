package org.example.strategy;

import java.util.List;

/**
 * Strategy cho ngôn ngữ tiếng Việt (Vietnamese)
 * 
 * Đặc điểm:
 * - Currency: 1.234.567 ₫ (suffix, comma decimal, dot grouping)
 * - Date: dd/MM/yyyy
 * - Direction: LTR
 * - Script: Latin với dấu thanh (à, á, ả, ã, ạ, ă, â, đ, ê, ô, ơ, ư...)
 * 
 * Lưu ý: PrestaShop dùng mã "vn" cho tiếng Việt thay vì "vi"
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
        return "vn"; // PrestaShop dùng "vn" thay vì "vi"
    }

    // ==================== CURRENCY FORMAT ====================

    @Override
    public String getCurrencySymbol() {
        return "₫";
    }

    @Override
    public List<String> getAcceptedCurrencySymbols() {
        return List.of("₫", "€", "VND", "đ"); // Chấp nhận nhiều dạng ký hiệu
    }

    @Override
    public boolean isCurrencySymbolPrefix() {
        return false; // 100.000 ₫
    }

    @Override
    public String getDecimalSeparator() {
        return ","; // 1,99 (ít dùng vì VND không có xu)
    }

    @Override
    public String getGroupingSeparator() {
        return "."; // 1.000.000
    }

    // ==================== DATE FORMAT ====================

    @Override
    public String getDatePattern() {
        return "dd/MM/yyyy";
    }

    @Override
    public List<String> getAcceptedDatePatterns() {
        return List.of("dd/MM/yyyy", "d/M/yyyy", "dd-MM-yyyy", "dd.MM.yyyy");
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
                "Best sales",
                "Delivery",
                "Terms and conditions");
    }

    @Override
    public List<String> getExpectedKeywords() {
        return List.of(
                "Thêm vào giỏ",
                "Đăng nhập",
                "Tìm kiếm",
                "Trang chủ",
                "Quần áo",
                "Phụ kiện",
                "Nghệ thuật",
                "Giá",
                "Liên hệ",
                "Giỏ hàng");
    }

    // ==================== ENCODING & FONTS ====================

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
        return "[a-zA-ZàáạảãâầấậẩẫăằắặẳẵèéẹẻẽêềếệểễìíịỉĩòóọỏõôồốộổỗơờớợởỡùúụủũưừứựửữỳýỵỷỹđĐÀÁẠẢÃÂẦẤẬẨẪĂẰẮẶẲẴÈÉẸẺẼÊỀẾỆỂỄÌÍỊỈĨÒÓỌỎÕÔỒỐỘỔỖƠỜỚỢỞỠÙÚỤỦŨƯỪỨỰỬỮỲÝỴỶỸ]+";
    }
}
