package org.example;

import org.openqa.selenium.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.apache.commons.io.FileUtils;

/**
 * Class luu thong tin loi L10n
 * Ho tro tu dong chup anh man hinh khi Assert that bai
 */
public class L10nError {
    public String type;
    public String title;
    public String description;
    public String pageUrl;
    public String screenshotPath;
    public String timestamp;
    public String languageCode;
    public ErrorSeverity severity;

    // Thu muc mac dinh de luu screenshot
    private static final String SCREENSHOTS_DIR = "screenshots";

    public enum ErrorSeverity {
        LOW, // Canh bao, khong anh huong chuc nang
        MEDIUM, // Loi can sua nhung khong urgent
        HIGH, // Loi nghiem trong, anh huong UX
        CRITICAL // Loi nghiem trong, can sua ngay
    }

    public L10nError(String type, String title, String description, String pageUrl) {
        this.type = type;
        this.title = title;
        this.description = description;
        this.pageUrl = pageUrl;
        this.timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        this.severity = determineSeverity(type);
    }

    public L10nError(String type, String title, String description, String pageUrl, String languageCode) {
        this(type, title, description, pageUrl);
        this.languageCode = languageCode;
    }

    /**
     * Xac dinh muc do nghiem trong cua loi dua tren loai loi
     */
    private ErrorSeverity determineSeverity(String type) {
        if (type == null)
            return ErrorSeverity.MEDIUM;
        switch (type.toUpperCase()) {
            case "CURRENCY_MISMATCH":
            case "CRITICAL_TEXT_MISSING":
                return ErrorSeverity.CRITICAL;
            case "DATE_FORMAT":
            case "TEXT_OVERFLOW":
            case "UNTRANSLATED_TEXT":
                return ErrorSeverity.HIGH;
            case "KEYWORD_MISSING":
            case "FORMAT_WARNING":
                return ErrorSeverity.MEDIUM;
            default:
                return ErrorSeverity.LOW;
        }
    }

    // ==================== SCREENSHOT METHODS ====================

    /**
     * Chup anh man hinh khi co loi
     * 
     * @param driver WebDriver instance
     * @return Duong dan file screenshot
     */
    public String captureScreenshot(WebDriver driver) {
        return captureScreenshot(driver, null);
    }

    /**
     * Chup anh man hinh voi ten file tuy chinh
     * 
     * @param driver     WebDriver instance
     * @param customName Ten file tuy chinh (khong bao gom extension)
     * @return Duong dan file screenshot
     */
    public String captureScreenshot(WebDriver driver, String customName) {
        if (driver == null)
            return null;

        try {
            // Tao thu muc screenshots neu chua co
            Path screenshotsPath = Paths.get(SCREENSHOTS_DIR);
            if (!Files.exists(screenshotsPath)) {
                Files.createDirectories(screenshotsPath);
            }

            // Tao ten file
            String filename;
            if (customName != null && !customName.isEmpty()) {
                filename = sanitizeFilename(customName);
            } else {
                String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss_SSS"));
                String safeType = sanitizeFilename(type);
                filename = String.format("%s_%s_%s", safeType, languageCode != null ? languageCode : "unknown",
                        timestamp);
            }

            File screenshotFile = new File(SCREENSHOTS_DIR + "/" + filename + ".png");

            // Chup anh man hinh
            TakesScreenshot takesScreenshot = (TakesScreenshot) driver;
            File sourceFile = takesScreenshot.getScreenshotAs(OutputType.FILE);

            // Copy file
            FileUtils.copyFile(sourceFile, screenshotFile);

            this.screenshotPath = screenshotFile.getAbsolutePath();
            System.out.println("   [SCREENSHOT] Da luu: " + screenshotFile.getName());

            return this.screenshotPath;

        } catch (IOException e) {
            System.err.println("   [LOI] Khong the chup screenshot: " + e.getMessage());
            return null;
        }
    }

    /**
     * Chup anh man hinh cua mot element cu the
     * 
     * @param driver  WebDriver instance
     * @param element WebElement can chup
     * @return Duong dan file screenshot
     */
    public String captureElementScreenshot(WebDriver driver, WebElement element) {
        if (driver == null || element == null)
            return null;

        try {
            // Tao thu muc screenshots neu chua co
            Path screenshotsPath = Paths.get(SCREENSHOTS_DIR);
            if (!Files.exists(screenshotsPath)) {
                Files.createDirectories(screenshotsPath);
            }

            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss_SSS"));
            String filename = String.format("element_%s_%s.png", type, timestamp);
            File screenshotFile = new File(SCREENSHOTS_DIR + "/" + filename);

            // Chup anh element
            File sourceFile = element.getScreenshotAs(OutputType.FILE);
            FileUtils.copyFile(sourceFile, screenshotFile);

            this.screenshotPath = screenshotFile.getAbsolutePath();
            return this.screenshotPath;

        } catch (IOException e) {
            System.err.println("   [LOI] Khong the chup element screenshot: " + e.getMessage());
            return null;
        }
    }

    /**
     * Chuan hoa ten file, loai bo ky tu khong hop le
     */
    private String sanitizeFilename(String name) {
        if (name == null)
            return "unknown";
        return name.replaceAll("[^a-zA-Z0-9_-]", "_").toLowerCase();
    }

    // ==================== JUNIT INTEGRATION ====================

    /**
     * Tao L10nError tu JUnit assertion failure va chup screenshot
     * 
     * @param driver         WebDriver instance
     * @param assertionError AssertionError tu JUnit
     * @param languageCode   Ma ngon ngu dang test
     * @param pageUrl        URL cua trang
     * @return L10nError instance voi screenshot
     */
    public static L10nError fromAssertionError(WebDriver driver, AssertionError assertionError,
            String languageCode, String pageUrl) {
        L10nError error = new L10nError(
                "ASSERTION_FAILURE",
                "JUnit Assertion Failed",
                assertionError.getMessage(),
                pageUrl,
                languageCode);
        error.severity = ErrorSeverity.CRITICAL;

        // Tu dong chup screenshot
        if (driver != null) {
            error.captureScreenshot(driver);
        }

        return error;
    }

    /**
     * Tao L10nError tu Exception va chup screenshot
     * 
     * @param driver       WebDriver instance
     * @param exception    Exception
     * @param type         Loai loi
     * @param languageCode Ma ngon ngu
     * @param pageUrl      URL cua trang
     * @return L10nError instance voi screenshot
     */
    public static L10nError fromException(WebDriver driver, Exception exception, String type,
            String languageCode, String pageUrl) {
        L10nError error = new L10nError(
                type,
                exception.getClass().getSimpleName(),
                exception.getMessage(),
                pageUrl,
                languageCode);

        // Tu dong chup screenshot
        if (driver != null) {
            error.captureScreenshot(driver);
        }

        return error;
    }

    // ==================== UTILITY METHODS ====================

    /**
     * Kiem tra xem loi co screenshot khong
     */
    public boolean hasScreenshot() {
        return screenshotPath != null && !screenshotPath.isEmpty();
    }

    /**
     * Lay ten file screenshot
     */
    public String getScreenshotFilename() {
        if (screenshotPath == null)
            return null;
        return new File(screenshotPath).getName();
    }

    /**
     * Tao HTML report snippet cho loi nay
     */
    public String toHtmlReport() {
        StringBuilder html = new StringBuilder();
        html.append("<div class='error-item severity-").append(severity.name().toLowerCase()).append("'>");
        html.append("<h4>[").append(type).append("] ").append(title).append("</h4>");
        html.append("<p><strong>Description:</strong> ").append(description).append("</p>");
        html.append("<p><strong>URL:</strong> <a href='").append(pageUrl).append("'>").append(pageUrl)
                .append("</a></p>");
        html.append("<p><strong>Timestamp:</strong> ").append(timestamp).append("</p>");
        html.append("<p><strong>Severity:</strong> ").append(severity).append("</p>");

        if (hasScreenshot()) {
            html.append("<p><strong>Screenshot:</strong></p>");
            html.append("<img src='").append(screenshotPath).append("' style='max-width:800px;' />");
        }

        html.append("</div>");
        return html.toString();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("[%s][%s] %s: %s", severity, type, title, description));
        if (pageUrl != null)
            sb.append(" (URL: ").append(pageUrl).append(")");
        if (screenshotPath != null)
            sb.append(" [Screenshot: ").append(getScreenshotFilename()).append("]");
        return sb.toString();
    }
}
