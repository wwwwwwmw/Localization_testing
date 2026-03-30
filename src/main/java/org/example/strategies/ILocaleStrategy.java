package org.example.strategies;

import java.util.List;

/**
 * Interface định nghĩa các quy tắc Localization cho từng ngôn ngữ.
 * Sử dụng Strategy Pattern để tách biệt Test Logic và Test Data.
 *
 * Mỗi ngôn ngữ implement interface này với các quy tắc riêng.
 */
public interface ILocaleStrategy {

    // ==================== THÔNG TIN CƠ BẢN ====================

    /** Mã ngôn ngữ ISO 639-1 (vd: "en", "fr", "vi", "ar") */
    String getLanguageCode();

    /** Tên ngôn ngữ đầy đủ (vd: "English", "Français", "Tiếng Việt") */
    String getLanguageName();

    /** Mã ngôn ngữ trên URL PrestaShop (vd: "vi" -> "vn") */
    default String getPrestaShopCode() {
        return getLanguageCode();
    }

    // ==================== CURRENCY FORMAT ====================

    /** Ký hiệu tiền tệ chính (vd: "$", "€", "₫") */
    String getCurrencySymbol();

    /**
     * Danh sách ký hiệu tiền tệ chấp nhận — chỉ chấp nhận đúng tiền tệ của ngôn ngữ
     */
    default List<String> getAcceptedCurrencySymbols() {
        return List.of(getCurrencySymbol());
    }

    /**
     * true nếu symbol đứng trước số (PREFIX: $100), false nếu sau (SUFFIX: 100€)
     */
    boolean isCurrencySymbolPrefix();

    /** Dấu phân cách thập phân ("." hoặc ",") */
    String getDecimalSeparator();

    /** Dấu phân cách hàng nghìn (",", ".", " " hoặc "") */
    String getGroupingSeparator();

    // ==================== DATE FORMAT ====================

    /** Pattern ngày (vd: "dd/MM/yyyy", "MM/dd/yyyy") */
    String getDatePattern();

    /** Danh sách pattern ngày chấp nhận */
    default List<String> getAcceptedDatePatterns() {
        return List.of(getDatePattern());
    }

    // ==================== LAYOUT & DIRECTION ====================

    /** true nếu là ngôn ngữ RTL (Arabic, Hebrew, Persian...) */
    boolean isRTL();

    /** Text alignment mong đợi */
    default String getExpectedTextAlignment() {
        return isRTL() ? "right" : "left";
    }

    // ==================== UNTRANSLATED CONTENT ====================

    /** Từ tiếng Anh KHÔNG được phép xuất hiện (kiểm tra sót dịch) */
    List<String> getForbiddenWords();

    /**
     * Từ tiếng Anh được phép giữ nguyên theo UX/brand cho locale hiện tại.
     */
    default List<String> getAllowedEnglishWords() {
        return List.of();
    }

    /** Từ khóa mong đợi xuất hiện trên trang (đã dịch) */
    List<String> getExpectedKeywords();

    // ==================== ENCODING ====================

    /** Script group: LATIN, ARABIC, CJK, CYRILLIC... */
    ScriptGroup getScriptGroup();

    /** Ký tự đặc trưng để kiểm tra encoding */
    String getCharacterSample();

    /** Regex kiểm tra ký tự đặc trưng hiển thị đúng */
    String getCharacterValidationPattern();

    // ==================== PHONE NUMBER (MỚI) ====================

    /**
     * Regex kiểm tra định dạng số điện thoại của quốc gia.
     * Trả về null nếu không cần kiểm tra.
     */
    default String getPhoneRegex() {
        return null;
    }

    // ==================== ENUMS ====================

    enum ScriptGroup {
        LATIN, ARABIC, CYRILLIC, CJK, HEBREW, INDIC, THAI, OTHER
    }
}
