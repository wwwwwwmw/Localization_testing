package org.example.strategy;

import java.util.List;

/**
 * Interface định nghĩa các quy tắc Localization cho từng ngôn ngữ.
 * Sử dụng Strategy Pattern để tách biệt Test Logic và Test Data.
 * 
 * Mỗi ngôn ngữ sẽ implement interface này với các quy tắc riêng.
 */
public interface ILocaleStrategy {

    // ==================== THÔNG TIN CƠ BẢN ====================

    /**
     * Mã ngôn ngữ ISO 639-1 (VD: "en", "fr", "vi", "ar")
     */
    String getLanguageCode();

    /**
     * Tên ngôn ngữ đầy đủ (VD: "English", "Français", "Tiếng Việt", "العربية")
     */
    String getLanguageName();

    /**
     * Mã ngôn ngữ dùng cho PrestaShop URL (VD: "vi" -> "vn")
     */
    default String getPrestaShopCode() {
        return getLanguageCode();
    }

    // ==================== CURRENCY FORMAT ====================

    /**
     * Ký hiệu tiền tệ chính (VD: "$", "€", "₫")
     */
    String getCurrencySymbol();

    /**
     * Danh sách ký hiệu tiền tệ được chấp nhận (bao gồm symbol mặc định của
     * PrestaShop)
     */
    default List<String> getAcceptedCurrencySymbols() {
        return List.of(getCurrencySymbol(), "€"); // € là default của PrestaShop demo
    }

    /**
     * Vị trí ký hiệu tiền tệ
     * 
     * @return true nếu symbol ở trước số (PREFIX), false nếu ở sau (SUFFIX)
     */
    boolean isCurrencySymbolPrefix();

    /**
     * Dấu phân cách thập phân (VD: "." hoặc ",")
     */
    String getDecimalSeparator();

    /**
     * Dấu phân cách hàng nghìn (VD: ",", ".", " " hoặc "")
     */
    String getGroupingSeparator();

    /**
     * Định dạng tiền tệ đầy đủ
     * 
     * @return CurrencyFormat object chứa tất cả thông tin
     */
    default CurrencyFormat getCurrencyFormat() {
        return new CurrencyFormat(
                getCurrencySymbol(),
                isCurrencySymbolPrefix(),
                getDecimalSeparator(),
                getGroupingSeparator());
    }

    // ==================== DATE FORMAT ====================

    /**
     * Pattern định dạng ngày (VD: "dd/MM/yyyy", "MM/dd/yyyy", "yyyy-MM-dd")
     */
    String getDatePattern();

    /**
     * Danh sách pattern ngày được chấp nhận
     */
    default List<String> getAcceptedDatePatterns() {
        return List.of(getDatePattern());
    }

    // ==================== LAYOUT & DIRECTION ====================

    /**
     * Kiểm tra ngôn ngữ có phải RTL (Right-to-Left) không
     * 
     * @return true nếu là ngôn ngữ RTL (Arabic, Hebrew, Persian...)
     */
    boolean isRTL();

    /**
     * Text alignment mong đợi
     * 
     * @return "right" cho RTL, "left" cho LTR
     */
    default String getExpectedTextAlignment() {
        return isRTL() ? "right" : "left";
    }

    // ==================== UNTRANSLATED CONTENT ====================

    /**
     * Danh sách từ tiếng Anh không được phép xuất hiện (kiểm tra sót dịch)
     * Chỉ áp dụng cho các ngôn ngữ không phải tiếng Anh
     */
    List<String> getForbiddenWords();

    /**
     * Danh sách từ khóa mong đợi xuất hiện trên trang (đã dịch)
     */
    List<String> getExpectedKeywords();

    // ==================== ENCODING & FONTS ====================

    /**
     * Script group của ngôn ngữ (LATIN, ARABIC, CJK, CYRILLIC...)
     */
    ScriptGroup getScriptGroup();

    /**
     * Các ký tự đặc trưng của ngôn ngữ để kiểm tra encoding
     */
    String getCharacterSample();

    /**
     * Regex pattern để kiểm tra ký tự đặc trưng có hiển thị đúng không
     */
    String getCharacterValidationPattern();

    // ==================== INNER CLASSES & ENUMS ====================

    /**
     * Class chứa thông tin định dạng tiền tệ
     */
    class CurrencyFormat {
        public final String symbol;
        public final boolean isPrefix;
        public final String decimalSeparator;
        public final String groupingSeparator;

        public CurrencyFormat(String symbol, boolean isPrefix, String decimalSeparator, String groupingSeparator) {
            this.symbol = symbol;
            this.isPrefix = isPrefix;
            this.decimalSeparator = decimalSeparator;
            this.groupingSeparator = groupingSeparator;
        }

        @Override
        public String toString() {
            return String.format("CurrencyFormat{symbol='%s', isPrefix=%s, decimal='%s', grouping='%s'}",
                    symbol, isPrefix, decimalSeparator, groupingSeparator);
        }
    }

    /**
     * Nhóm chữ viết (Script Group) để phân loại ngôn ngữ
     */
    enum ScriptGroup {
        LATIN, // English, French, Vietnamese, German...
        ARABIC, // Arabic, Persian, Urdu...
        CYRILLIC, // Russian, Ukrainian, Bulgarian...
        CJK, // Chinese, Japanese, Korean
        HEBREW, // Hebrew
        INDIC, // Hindi, Bengali, Tamil...
        THAI, // Thai
        OTHER
    }
}
