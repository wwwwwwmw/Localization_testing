package org.example.core;

import org.openqa.selenium.WebDriver;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Class chứa thông tin lỗi L10n.
 * Hỗ trợ tự động chụp ảnh màn hình khi phát hiện lỗi.
 */
public class L10nError {

    private final String type;
    private final String message;
    private final String languageCode;
    private final String pageUrl;
    private final ErrorSeverity severity;
    private final String timestamp;
    private String screenshotPath;

    public enum ErrorSeverity {
        LOW, MEDIUM, HIGH, CRITICAL
    }

    public L10nError(String type, String message, String languageCode, String pageUrl) {
        this.type = type;
        this.message = message;
        this.languageCode = languageCode;
        this.pageUrl = pageUrl;
        this.severity = determineSeverity(type);
        this.timestamp = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    // ==================== FACTORY METHODS ====================

    /**
     * Tạo lỗi kèm chụp screenshot tự động.
     */
    public static L10nError createWithScreenshot(
            String type, String message, String langCode,
            String pageUrl, DriverManager driverManager) {

        L10nError error = new L10nError(type, message, langCode, pageUrl);
        if (driverManager != null) {
            String name = type + "_" + langCode;
            error.screenshotPath = driverManager.captureScreenshot(name);
        }
        return error;
    }

    // ==================== SEVERITY ====================

    private ErrorSeverity determineSeverity(String type) {
        if (type == null)
            return ErrorSeverity.MEDIUM;
        switch (type.toUpperCase()) {
            case "CURRENCY_FORMAT":
                return ErrorSeverity.CRITICAL;
            case "UNTRANSLATED_TEXT":
            case "DATE_FORMAT":
            case "TEXT_OVERFLOW":
                return ErrorSeverity.HIGH;
            case "RTL_LAYOUT":
                return ErrorSeverity.HIGH;
            case "PHONE_FORMAT":
                return ErrorSeverity.MEDIUM;
            default:
                return ErrorSeverity.LOW;
        }
    }

    // ==================== GETTERS ====================

    public String getType() {
        return type;
    }

    public String getMessage() {
        return message;
    }

    public String getLanguageCode() {
        return languageCode;
    }

    public String getPageUrl() {
        return pageUrl;
    }

    public ErrorSeverity getSeverity() {
        return severity;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getScreenshotPath() {
        return screenshotPath;
    }

    public boolean hasScreenshot() {
        return screenshotPath != null;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("[%s][%s][%s] %s", severity, type, languageCode, message));
        if (hasScreenshot()) {
            sb.append(" [Screenshot: ").append(new java.io.File(screenshotPath).getName()).append("]");
        }
        return sb.toString();
    }
}
