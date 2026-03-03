package org.example.core;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.apache.commons.io.FileUtils;

/**
 * DriverManager - Quản lý WebDriver lifecycle (Singleton per thread).
 *
 * Trách nhiệm:
 * - Khởi tạo ChromeDriver với các options phù hợp
 * - Cung cấp WebDriverWait, JavascriptExecutor
 * - Chụp screenshot khi có lỗi
 * - Đóng trình duyệt an toàn
 */
public final class DriverManager {

    private static final int DEFAULT_TIMEOUT = 15;
    private static final String SCREENSHOTS_DIR = "screenshots";

    private WebDriver driver;
    private WebDriverWait wait;
    private JavascriptExecutor js;

    // ==================== KHỞI TẠO ====================

    /**
     * Khởi tạo ChromeDriver.
     *
     * @param headless true để chạy headless (không hiển thị trình duyệt)
     */
    public void initDriver(boolean headless) {
        WebDriverManager.chromedriver().setup();

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--start-maximized");
        options.addArguments("--disable-notifications");
        options.addArguments("--remote-allow-origins=*");
        options.addArguments("--disable-blink-features=AutomationControlled");

        if (headless) {
            options.addArguments("--headless=new");
        }

        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(DEFAULT_TIMEOUT));
        js = (JavascriptExecutor) driver;

        // Tạo thư mục screenshots
        try {
            Files.createDirectories(Paths.get(SCREENSHOTS_DIR));
        } catch (IOException e) {
            System.err.println("[WARN] Không thể tạo thư mục screenshots: " + e.getMessage());
        }
    }

    // ==================== GETTERS ====================

    public WebDriver getDriver() {
        return driver;
    }

    public WebDriverWait getWait() {
        return wait;
    }

    public JavascriptExecutor getJs() {
        return js;
    }

    // ==================== SCREENSHOT ====================

    /**
     * Chụp screenshot và lưu vào thư mục screenshots/.
     *
     * @param testName tên test / ngữ cảnh
     * @return đường dẫn tuyệt đối file ảnh, hoặc null nếu thất bại
     */
    public String captureScreenshot(String testName) {
        if (driver == null)
            return null;

        try {
            Path dir = Paths.get(SCREENSHOTS_DIR);
            if (!Files.exists(dir)) {
                Files.createDirectories(dir);
            }

            String timestamp = LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss_SSS"));
            String safeName = testName.replaceAll("[^a-zA-Z0-9_-]", "_").toLowerCase();
            String filename = safeName + "_" + timestamp + ".png";

            File destFile = new File(SCREENSHOTS_DIR + "/" + filename);
            File srcFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            FileUtils.copyFile(srcFile, destFile);

            System.out.println("[SCREENSHOT] Đã lưu: " + destFile.getName());
            return destFile.getAbsolutePath();

        } catch (IOException e) {
            System.err.println("[ERROR] Không thể chụp screenshot: " + e.getMessage());
            return null;
        }
    }

    // ==================== WAIT HELPERS ====================

    /**
     * Chờ trang load xong (document.readyState == complete).
     */
    public void waitForPageLoad() {
        wait.until(d -> js.executeScript("return document.readyState").equals("complete"));
    }

    // ==================== ĐÓNG ====================

    /**
     * Đóng trình duyệt an toàn.
     */
    public void quitDriver() {
        if (driver != null) {
            try {
                driver.quit();
            } catch (Exception e) {
                System.err.println("[WARN] Lỗi khi đóng trình duyệt: " + e.getMessage());
            } finally {
                driver = null;
                wait = null;
                js = null;
            }
        }
    }
}
