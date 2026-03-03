package org.example.pages;

import org.example.core.DriverManager;
import org.openqa.selenium.*;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.*;
import java.util.stream.Collectors;

/**
 * PrestaShopPage - Page Object Model cho trang PrestaShop Demo.
 *
 * Trách nhiệm:
 * - Quản lý iframe framelive (switch vào/ra)
 * - Quản lý locators cho các element trên trang
 * - Cung cấp hành động: đổi ngôn ngữ, lấy giá, lấy text, kiểm tra overflow
 * - Xử lý retry khi StaleElementReferenceException
 */
public class PrestaShopPage {

    private static final String PRESTASHOP_URL = "https://demo.prestashop.com/";
    private static final int MAX_RETRY = 3;
    private static final long RETRY_DELAY_MS = 500;

    private final DriverManager driverManager;
    private WebDriver driver;
    private WebDriverWait wait;
    private JavascriptExecutor js;

    // Lưu trạng thái iframe
    private boolean insideIframe = false;

    // Map chuyển mã ISO -> mã PrestaShop
    private static final Map<String, String> PRESTASHOP_CODE_MAP = Map.of(
            "vi", "vn",
            "sl", "si");

    // ==================== CONSTRUCTOR ====================

    public PrestaShopPage(DriverManager driverManager) {
        this.driverManager = driverManager;
        this.driver = driverManager.getDriver();
        this.wait = driverManager.getWait();
        this.js = driverManager.getJs();
    }

    // ==================== MỞ TRANG & IFRAME ====================

    /**
     * Mở trang PrestaShop Demo và switch vào iframe framelive.
     */
    public void open() {
        System.out.println("[PAGE] Mở PrestaShop Demo...");
        driver.get(PRESTASHOP_URL);
        driverManager.waitForPageLoad();
        switchToIframe();
    }

    /**
     * Switch vào iframe #framelive.
     * PrestaShop demo nhúng shop bên trong một iframe.
     */
    public boolean switchToIframe() {
        try {
            // Về default content trước
            driver.switchTo().defaultContent();
            insideIframe = false;

            wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(By.id("framelive")));
            insideIframe = true;
            System.out.println("[PAGE] ✓ Đã switch vào iframe #framelive");
            return true;
        } catch (Exception e) {
            try {
                // Fallback: tìm bất kỳ iframe nào
                driver.switchTo().defaultContent();
                wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(By.cssSelector("iframe")));
                insideIframe = true;
                System.out.println("[PAGE] ✓ Đã switch vào iframe (fallback)");
                return true;
            } catch (Exception e2) {
                System.err.println("[PAGE] ✗ Không thể switch vào iframe: " + e2.getMessage());
                return false;
            }
        }
    }

    /**
     * Switch ra khỏi iframe về default content.
     */
    public void switchToDefaultContent() {
        driver.switchTo().defaultContent();
        insideIframe = false;
    }

    // ==================== ĐỔI NGÔN NGỮ ====================

    /**
     * Đổi ngôn ngữ trên PrestaShop với cơ chế retry.
     *
     * @param isoCode mã ngôn ngữ ISO (vd: "en", "fr", "vi", "ar")
     * @return true nếu switch thành công
     */
    public boolean switchLanguage(String isoCode) {
        String psCode = toPrestashopCode(isoCode);

        for (int attempt = 1; attempt <= MAX_RETRY; attempt++) {
            System.out.println("[PAGE] Đổi ngôn ngữ -> " + isoCode + " (attempt " + attempt + "/" + MAX_RETRY + ")");
            try {
                // Đảm bảo đang ở trong iframe
                if (!insideIframe)
                    switchToIframe();

                // Click language selector dropdown
                WebElement langDropdown = wait.until(ExpectedConditions.elementToBeClickable(
                        By.cssSelector(".language-selector, #_desktop_language_selector, [class*='language']")));
                langDropdown.click();
                Thread.sleep(500);

                // Click option ngôn ngữ
                WebElement langOption = wait.until(ExpectedConditions.elementToBeClickable(
                        By.cssSelector("a[href*='/" + psCode + "/'], a[data-iso-code='" + isoCode + "']")));
                langOption.click();

                // Chờ trang load lại
                driverManager.waitForPageLoad();

                // Switch lại vào iframe (vì trang đã reload)
                switchToIframe();

                // Verify
                if (verifyLanguage(isoCode, psCode)) {
                    System.out.println("[PAGE] ✓ Đã chuyển sang " + isoCode);
                    return true;
                }
            } catch (Exception e) {
                System.out.println("[PAGE] ⚠ Attempt " + attempt + " thất bại: " + e.getMessage());
                if (attempt < MAX_RETRY) {
                    try {
                        Thread.sleep(RETRY_DELAY_MS * attempt);
                        driver.navigate().refresh();
                        driverManager.waitForPageLoad();
                        switchToIframe();
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }

        System.err.println("[PAGE] ✗ Không thể chuyển sang ngôn ngữ " + isoCode);
        return false;
    }

    private boolean verifyLanguage(String isoCode, String psCode) {
        try {
            // Check URL
            String url = driver.getCurrentUrl();
            if (url.contains("/" + psCode + "/") || url.contains("/" + isoCode + "/")) {
                return true;
            }
            // Check HTML lang attribute
            String htmlLang = driver.findElement(By.tagName("html")).getAttribute("lang");
            if (htmlLang != null && (htmlLang.startsWith(isoCode) || htmlLang.startsWith(psCode))) {
                return true;
            }
            // English thường là default
            return "en".equals(isoCode);
        } catch (Exception e) {
            return false;
        }
    }

    private String toPrestashopCode(String isoCode) {
        return PRESTASHOP_CODE_MAP.getOrDefault(isoCode, isoCode);
    }

    // ==================== LẤY DỮ LIỆU TỪ TRANG ====================

    /**
     * Lấy tất cả các text giá trên trang.
     */
    public List<String> getAllPriceTexts() {
        return getTextsWithRetry(
                By.cssSelector(".price, .product-price, .current-price, [class*='price']:not([class*='price-'])"));
    }

    /**
     * Lấy toàn bộ body text.
     */
    public String getBodyText() {
        try {
            return driver.findElement(By.tagName("body")).getText();
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * Lấy direction (rtl / ltr) của trang.
     * Dùng JavaScript để đảm bảo chính xác bên trong iframe.
     */
    public String getPageDirection() {
        try {
            // JavaScript-based detection: kiểm tra dir attribute rồi computed style
            Object result = js.executeScript(
                    "var html = document.documentElement;" +
                            "var dir = html.getAttribute('dir');" +
                            "if (dir && dir.trim() !== '') return dir.trim().toLowerCase();" +
                            "var body = document.body;" +
                            "if (body) { dir = body.getAttribute('dir'); " +
                            "  if (dir && dir.trim() !== '') return dir.trim().toLowerCase(); }" +
                            "return window.getComputedStyle(html).direction;");

            if (result instanceof String) {
                String dir = ((String) result).trim().toLowerCase();
                if (!dir.isEmpty())
                    return dir;
            }
            return "ltr"; // default
        } catch (Exception e) {
            // Fallback: Selenium attribute approach
            try {
                WebElement html = driver.findElement(By.tagName("html"));
                String dir = html.getAttribute("dir");
                if (dir != null && !dir.trim().isEmpty())
                    return dir.trim().toLowerCase();
                dir = driver.findElement(By.tagName("body")).getAttribute("dir");
                if (dir != null && !dir.trim().isEmpty())
                    return dir.trim().toLowerCase();
                return html.getCssValue("direction");
            } catch (Exception e2) {
                return null;
            }
        }
    }

    /**
     * Lấy URL hiện tại.
     */
    public String getCurrentUrl() {
        try {
            return driver.getCurrentUrl();
        } catch (Exception e) {
            return "N/A";
        }
    }

    /**
     * Lấy tất cả buttons / nav links.
     */
    public List<WebElement> getAllButtons() {
        try {
            return driver.findElements(
                    By.cssSelector(".btn, button, .add-to-cart, [class*='btn'], .nav-link, .menu-item"));
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    /**
     * Kiểm tra element có bị overflow không (scrollWidth > clientWidth).
     */
    public boolean isElementOverflowing(WebElement element) {
        try {
            Long scrollWidth = (Long) js.executeScript("return arguments[0].scrollWidth", element);
            Long clientWidth = (Long) js.executeScript("return arguments[0].clientWidth", element);
            return scrollWidth != null && clientWidth != null && scrollWidth > clientWidth + 2;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Lấy HTML lang attribute.
     */
    public String getHtmlLang() {
        try {
            return driver.findElement(By.tagName("html")).getAttribute("lang");
        } catch (Exception e) {
            return null;
        }
    }

    // ==================== NAVIGATION ====================

    /**
     * Click vào sản phẩm đầu tiên trên trang.
     */
    public boolean clickFirstProduct() {
        try {
            WebElement product = wait.until(ExpectedConditions.elementToBeClickable(
                    By.cssSelector(".product-miniature a, .product-thumbnail a, .thumbnail-container a")));
            product.click();
            driverManager.waitForPageLoad();
            return true;
        } catch (Exception e) {
            System.out.println("[PAGE] ⚠ Không thể click sản phẩm: " + e.getMessage());
            return false;
        }
    }

    /**
     * Quay lại trang chủ.
     */
    public void goHome() {
        try {
            driver.navigate().back();
            driverManager.waitForPageLoad();
            // Có thể cần switch lại iframe nếu page reload hoàn toàn
            if (!insideIframe) {
                switchToIframe();
            }
        } catch (Exception e) {
            System.out.println("[PAGE] ⚠ Không thể quay lại: " + e.getMessage());
        }
    }

    // ==================== PRIVATE HELPERS ====================

    /**
     * Lấy danh sách text từ các element với cơ chế retry chống StaleElement.
     */
    private List<String> getTextsWithRetry(By locator) {
        for (int attempt = 1; attempt <= MAX_RETRY; attempt++) {
            try {
                List<WebElement> elements = driver.findElements(locator);
                return elements.stream()
                        .map(el -> {
                            try {
                                return el.getText().trim();
                            } catch (StaleElementReferenceException e) {
                                return "";
                            }
                        })
                        .filter(t -> !t.isEmpty())
                        .collect(Collectors.toList());
            } catch (Exception e) {
                if (attempt < MAX_RETRY) {
                    try {
                        Thread.sleep(RETRY_DELAY_MS);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }
        return Collections.emptyList();
    }
}
