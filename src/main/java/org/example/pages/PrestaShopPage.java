package org.example.pages;

import org.example.core.DriverManager;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.*;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * PrestaShopPage - Page Object Model cho shop dang duoc kiem thu.
 *
 * Trách nhiệm:
 * - Ho tro iframe neu can (co the tat bang cau hinh)
 * - Quản lý locators cho các element trên trang
 * - Cung cấp hành động: đổi ngôn ngữ, lấy giá, lấy text, kiểm tra overflow
 * - Xử lý retry khi StaleElementReferenceException
 */
public class PrestaShopPage {

    public static final class ImageLocalizationSample {
        private final String src;
        private final String alt;
        private final String title;

        public ImageLocalizationSample(String src, String alt, String title) {
            this.src = src;
            this.alt = alt;
            this.title = title;
        }

        public String getSrc() {
            return src;
        }

        public String getAlt() {
            return alt;
        }

        public String getTitle() {
            return title;
        }
    }

    private static final String DEFAULT_SHOP_URL = "http://localhost:5173";
    private static final String SHOP_URL_PROP = "l10n.baseUrl";
    private static final String SHOP_URL_ENV = "L10N_BASE_URL";
    private static final String USE_IFRAME_PROP = "l10n.useIframe";
    private static final String FORBIDDEN_DEMO_HOST = "demo.prestashop.com";
    private static final int MAX_RETRY = 3;
    private static final long RETRY_DELAY_MS = 500;
    private static final String LANGUAGE_STORAGE_KEY = "lts.language";
    private static final Set<String> SUPPORTED_LANGUAGE_CODES = Set.of("en", "vi", "fr", "ar", "de", "ja", "ru");

    private final DriverManager driverManager;
    private WebDriver driver;
    private WebDriverWait wait;
    private JavascriptExecutor js;
    private final String shopUrl;
    private final boolean useIframe;

    // Lưu trạng thái iframe
    private boolean insideIframe = false;

    private static final By LANGUAGE_SELECT_LOCATOR = By.cssSelector("select[aria-label='Language switcher']");
    private static final By FALLBACK_LANGUAGE_SELECT_LOCATOR = By
            .cssSelector("select.language-switcher, .language-switcher select");
    private static final By CUSTOMER_LOGIN_FORM_LOCATOR = By.cssSelector("form.customer-form-card");
    private static final By CUSTOMER_EMAIL_INPUT_LOCATOR = By.cssSelector(
            "form.customer-form-card input:not([type='password'])");
    private static final By CUSTOMER_PASSWORD_INPUT_LOCATOR = By.cssSelector(
            "form.customer-form-card input[type='password']");
    private static final By CUSTOMER_LOGIN_SUBMIT_LOCATOR = By.cssSelector(
            "form.customer-form-card button[type='submit']");
    private static final By ORDER_DETAIL_LINK_LOCATOR = By.cssSelector("a[href*='/orders/']");

    // ==================== CONSTRUCTOR ====================

    public PrestaShopPage(DriverManager driverManager) {
        this.driverManager = driverManager;
        this.driver = driverManager.getDriver();
        this.wait = driverManager.getWait();
        this.js = driverManager.getJs();
        this.shopUrl = resolveShopUrl();
        this.useIframe = Boolean.parseBoolean(System.getProperty(USE_IFRAME_PROP, "false"));
    }

    // ==================== MỞ TRANG & IFRAME ====================

    /**
     * Mo trang shop tu cau hinh. Mac dinh khong dung iframe.
     */
    public void open() {
        System.out.println("[PAGE] Mo shop URL: " + shopUrl);
        driver.get(shopUrl);
        driverManager.waitForPageLoad();
        if (useIframe) {
            switchToIframe();
        } else {
            insideIframe = false;
        }
    }

    /**
     * Switch vao iframe khi can. Disabled hoan toan neu l10n.useIframe=false.
     */
    public boolean switchToIframe() {
        if (!useIframe) {
            insideIframe = false;
            return true;
        }

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
        if (isoCode == null || isoCode.isBlank()) {
            return false;
        }
        String normalizedCode = isoCode.trim().toLowerCase(Locale.ROOT);

        for (int attempt = 1; attempt <= MAX_RETRY; attempt++) {
            System.out.println(
                    "[PAGE] Đổi ngôn ngữ -> " + normalizedCode + " (attempt " + attempt + "/" + MAX_RETRY + ")");
            try {
                // Dam bao context iframe neu duoc bat
                if (useIframe && !insideIframe)
                    switchToIframe();

                boolean switched = trySwitchLanguageBySelect(normalizedCode);
                if (!switched) {
                    switched = trySwitchLanguageByStorage(normalizedCode);
                }
                if (!switched) {
                    throw new org.openqa.selenium.NoSuchElementException(
                            "Language switcher not found or locale unsupported: " + normalizedCode);
                }

                // Chờ trang load lại
                driverManager.waitForPageLoad();

                // Switch lai vao iframe neu su dung che do iframe
                if (useIframe) {
                    switchToIframe();
                }

                // Verify
                if (verifyLanguage(normalizedCode)) {
                    System.out.println("[PAGE] ✓ Đã chuyển sang " + normalizedCode);
                    return true;
                }
            } catch (Exception e) {
                System.out.println("[PAGE] ⚠ Attempt " + attempt + " thất bại: " + e.getMessage());
                if (attempt < MAX_RETRY) {
                    try {
                        Thread.sleep(RETRY_DELAY_MS * attempt);
                        driver.navigate().refresh();
                        driverManager.waitForPageLoad();
                        if (useIframe) {
                            switchToIframe();
                        }
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }

        System.err.println("[PAGE] ✗ Không thể chuyển sang ngôn ngữ " + normalizedCode);
        return false;
    }

    private boolean verifyLanguage(String isoCode) {
        try {
            String htmlLang = driver.findElement(By.tagName("html")).getAttribute("lang");
            if (htmlLang != null && htmlLang.toLowerCase(Locale.ROOT).startsWith(isoCode)) {
                return true;
            }

            Object storageValue = js.executeScript("return localStorage.getItem(arguments[0]);", LANGUAGE_STORAGE_KEY);
            if (storageValue != null && isoCode.equals(storageValue.toString().toLowerCase(Locale.ROOT))) {
                return true;
            }

            WebElement languageSelect = findLanguageSelect();
            if (languageSelect != null) {
                Select select = new Select(languageSelect);
                return isoCode.equalsIgnoreCase(select.getFirstSelectedOption().getAttribute("value"));
            }

            return false;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean trySwitchLanguageBySelect(String isoCode) {
        WebElement languageSelect = findLanguageSelect();
        if (languageSelect == null) {
            return false;
        }

        Select select = new Select(languageSelect);
        try {
            String selectedValue = select.getFirstSelectedOption().getAttribute("value");
            if (isoCode.equalsIgnoreCase(selectedValue)) {
                return true;
            }
        } catch (Exception ignored) {
            // Continue and force selection below.
        }

        select.selectByValue(isoCode);
        wait.until(d -> verifyLanguage(isoCode));
        return true;
    }

    private WebElement findLanguageSelect() {
        List<WebElement> candidates = new ArrayList<>();
        candidates.addAll(driver.findElements(LANGUAGE_SELECT_LOCATOR));
        if (candidates.isEmpty()) {
            candidates.addAll(driver.findElements(FALLBACK_LANGUAGE_SELECT_LOCATOR));
        }

        for (WebElement candidate : candidates) {
            try {
                if (!candidate.isDisplayed() || !candidate.isEnabled()) {
                    continue;
                }

                Select select = new Select(candidate);
                long knownOptionCount = select.getOptions().stream()
                        .map(option -> option.getAttribute("value"))
                        .filter(Objects::nonNull)
                        .map(value -> value.trim().toLowerCase(Locale.ROOT))
                        .filter(SUPPORTED_LANGUAGE_CODES::contains)
                        .count();

                if (knownOptionCount > 0) {
                    return candidate;
                }
            } catch (Exception ex) {
                // continue scanning other dropdowns
            }
        }

        return null;
    }

    private boolean trySwitchLanguageByStorage(String isoCode) {
        try {
            js.executeScript(
                    "localStorage.setItem(arguments[0], arguments[1]);" +
                            "document.documentElement.lang = arguments[1];",
                    LANGUAGE_STORAGE_KEY,
                    isoCode);
            driver.navigate().refresh();
            return true;
        } catch (Exception e) {
            return false;
        }
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
     * Đọc locale hiện tại từ UI/browser state (ưu tiên html lang).
     */
    public String getCurrentLanguageCode() {
        String htmlLang = normalizeLanguageCode(getHtmlLang());
        if (htmlLang != null) {
            return htmlLang;
        }

        try {
            WebElement languageSelect = findLanguageSelect();
            if (languageSelect != null) {
                Select select = new Select(languageSelect);
                String selected = normalizeLanguageCode(select.getFirstSelectedOption().getAttribute("value"));
                if (selected != null) {
                    return selected;
                }
            }
        } catch (Exception ignored) {
            // fallback phía dưới
        }

        try {
            Object storageValue = js.executeScript("return localStorage.getItem(arguments[0]);", LANGUAGE_STORAGE_KEY);
            return normalizeLanguageCode(storageValue != null ? storageValue.toString() : null);
        } catch (Exception ignored) {
            return null;
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
            // Chi switch iframe khi duoc cau hinh.
            if (useIframe && !insideIframe) {
                switchToIframe();
            }
        } catch (Exception e) {
            System.out.println("[PAGE] ⚠ Không thể quay lại: " + e.getMessage());
        }
    }

    /**
     * Điều hướng tới đường dẫn tương đối của shop (SPA route).
     */
    public boolean navigateToPath(String relativePath) {
        try {
            driver.navigate().to(buildShopUrl(relativePath));
            driverManager.waitForPageLoad();
            if (useIframe) {
                switchToIframe();
            }
            return true;
        } catch (Exception e) {
            System.out.println("[PAGE] ⚠ Không thể mở path " + relativePath + ": " + e.getMessage());
            return false;
        }
    }

    /**
     * Đăng nhập customer bằng email/password để truy cập các trang protected.
     */
    public boolean loginCustomer(String email, String password) {
        if (email == null || email.isBlank() || password == null) {
            return false;
        }

        if (hasCustomerToken() && !isOnLoginPage()) {
            return true;
        }

        if (!navigateToPath("/login")) {
            return false;
        }

        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(CUSTOMER_LOGIN_FORM_LOCATOR));

            WebElement emailInput = wait
                    .until(ExpectedConditions.visibilityOfElementLocated(CUSTOMER_EMAIL_INPUT_LOCATOR));
            WebElement passwordInput = wait
                    .until(ExpectedConditions.visibilityOfElementLocated(CUSTOMER_PASSWORD_INPUT_LOCATOR));

            emailInput.clear();
            emailInput.sendKeys(email);
            passwordInput.clear();
            passwordInput.sendKeys(password);

            WebElement submit = wait.until(ExpectedConditions.elementToBeClickable(CUSTOMER_LOGIN_SUBMIT_LOCATOR));
            submit.click();

            wait.until(d -> hasCustomerToken() || !isOnLoginPage());
            return hasCustomerToken() || !isOnLoginPage();
        } catch (Exception e) {
            System.out.println("[PAGE] ⚠ Đăng nhập customer thất bại: " + e.getMessage());
            return false;
        }
    }

    /**
     * Mở trang đơn hàng, tự login nếu chưa đăng nhập.
     */
    public boolean openOrdersPageForCustomer(String email, String password) {
        if (!navigateToPath("/orders")) {
            return false;
        }

        if (isOnLoginPage()) {
            boolean loggedIn = loginCustomer(email, password);
            if (!loggedIn) {
                return false;
            }
            if (!navigateToPath("/orders")) {
                return false;
            }
        }

        return !isOnLoginPage();
    }

    /**
     * Mở order detail đầu tiên nếu có và trả về body text của trang chi tiết.
     */
    public String openFirstOrderDetailAndGetBodyText() {
        try {
            List<WebElement> links = driver.findElements(ORDER_DETAIL_LINK_LOCATOR);
            for (WebElement link : links) {
                String href = link.getAttribute("href");
                if (href == null) {
                    continue;
                }
                if (!href.matches(".*/orders/\\d+.*")) {
                    continue;
                }
                if (!link.isDisplayed() || !link.isEnabled()) {
                    continue;
                }

                link.click();
                driverManager.waitForPageLoad();
                return getBodyText();
            }
        } catch (Exception e) {
            System.out.println("[PAGE] ⚠ Không thể mở order detail: " + e.getMessage());
        }
        return null;
    }

    /**
     * Thu thập body text qua nhiều tab/trang customer để giảm false pass
     * untranslated.
     */
    public Map<String, String> collectLocalizationBodies(String customerEmail, String customerPassword) {
        Map<String, String> snapshots = new LinkedHashMap<>();

        visitLocalizationPages(customerEmail, customerPassword, location -> snapshots.put(location, getBodyText()));

        return snapshots;
    }

    /**
     * Thu thập URL thực tế tại từng trang được duyệt.
     */
    public Map<String, String> collectUrlsAcrossPages(String customerEmail, String customerPassword) {
        Map<String, String> urlsByPage = new LinkedHashMap<>();
        visitLocalizationPages(customerEmail, customerPassword,
                location -> urlsByPage.put(location, getCurrentUrl()));
        return urlsByPage;
    }

    /**
     * Thu thập metadata ảnh (src/alt/title) tại từng trang để kiểm tra media
     * localization.
     */
    public Map<String, List<ImageLocalizationSample>> collectImageLocalizationAcrossPages(String customerEmail,
            String customerPassword) {
        Map<String, List<ImageLocalizationSample>> imagesByPage = new LinkedHashMap<>();
        visitLocalizationPages(customerEmail, customerPassword,
                location -> imagesByPage.put(location, getImageLocalizationSamples()));
        return imagesByPage;
    }

    /**
     * Thu thập toàn bộ giá hiển thị theo từng tab/trang để kiểm tra currency trên
     * toàn
     * bộ UI.
     */
    public Map<String, List<String>> collectPriceTextsAcrossPages(String customerEmail, String customerPassword) {
        Map<String, List<String>> pricesByPage = new LinkedHashMap<>();

        visitLocalizationPages(customerEmail, customerPassword,
                location -> pricesByPage.put(location, getAllPriceTexts()));

        return pricesByPage;
    }

    /**
     * Thu thập direction của từng tab/trang đã duyệt.
     */
    public Map<String, String> collectDirectionsAcrossPages(String customerEmail, String customerPassword) {
        Map<String, String> directions = new LinkedHashMap<>();
        visitLocalizationPages(customerEmail, customerPassword,
                location -> directions.put(location, getPageDirection()));
        return directions;
    }

    /**
     * Thu thập html lang của từng tab/trang đã duyệt.
     */
    public Map<String, String> collectHtmlLangAcrossPages(String customerEmail, String customerPassword) {
        Map<String, String> htmlLangs = new LinkedHashMap<>();
        visitLocalizationPages(customerEmail, customerPassword, location -> htmlLangs.put(location, getHtmlLang()));
        return htmlLangs;
    }

    /**
     * Thu thập charset document của từng tab/trang đã duyệt.
     */
    public Map<String, String> collectDocumentCharsetsAcrossPages(String customerEmail, String customerPassword) {
        Map<String, String> charsets = new LinkedHashMap<>();
        visitLocalizationPages(customerEmail, customerPassword,
                location -> charsets.put(location, getDocumentCharset()));
        return charsets;
    }

    /**
     * Thu thập các text đang bị overflow theo từng tab/trang.
     */
    public Map<String, List<String>> collectOverflowedTextsAcrossPages(String customerEmail, String customerPassword) {
        Map<String, List<String>> overflowByPage = new LinkedHashMap<>();
        visitLocalizationPages(customerEmail, customerPassword, location -> {
            List<String> overflowedTexts = new ArrayList<>();
            for (WebElement btn : getAllButtons()) {
                try {
                    if (isElementOverflowing(btn)) {
                        String text = btn.getText().trim();
                        if (!text.isEmpty()) {
                            overflowedTexts.add(text);
                        }
                    }
                } catch (Exception e) {
                    // Skip stale elements
                }
            }
            overflowByPage.put(location, overflowedTexts);
        });
        return overflowByPage;
    }

    /**
     * Lấy charset document hiện tại (meta charset / document.characterSet).
     */
    public String getDocumentCharset() {
        try {
            Object result = js.executeScript(
                    "var meta = document.querySelector('meta[charset]');" +
                            "if (meta && meta.getAttribute('charset')) return meta.getAttribute('charset');" +
                            "var metas = document.querySelectorAll('meta[http-equiv]');" +
                            "for (var i = 0; i < metas.length; i++) {" +
                            "  var value = metas[i].getAttribute('http-equiv');" +
                            "  if (value && value.toLowerCase() === 'content-type') {" +
                            "    var content = metas[i].getAttribute('content') || '';" +
                            "    var m = content.match(/charset\\s*=\\s*([^;]+)/i);" +
                            "    if (m) return m[1];" +
                            "  }" +
                            "}" +
                            "return document.characterSet || document.charset || null;");

            if (result != null) {
                String charset = result.toString().trim();
                if (!charset.isEmpty()) {
                    return charset;
                }
            }
        } catch (Exception ignored) {
            // fallback bên dưới
        }

        try {
            String source = driver.getPageSource();
            if (source != null && !source.isEmpty()) {
                Matcher m = Pattern.compile(
                        "charset\\s*=\\s*['\"]?([^\\s\"'/>;]+)",
                        Pattern.CASE_INSENSITIVE).matcher(source);
                if (m.find()) {
                    return m.group(1).trim();
                }
            }
        } catch (Exception ignored) {
            // ignore
        }

        return null;
    }

    /**
     * Lấy metadata ảnh hiện có trên trang hiện tại.
     */
    public List<ImageLocalizationSample> getImageLocalizationSamples() {
        List<ImageLocalizationSample> result = new ArrayList<>();
        try {
            List<WebElement> images = driver.findElements(By.cssSelector("img"));
            for (WebElement image : images) {
                try {
                    String src = image.getAttribute("src");
                    String alt = image.getAttribute("alt");
                    String title = image.getAttribute("title");

                    boolean hasUsefulData = (src != null && !src.isBlank())
                            || (alt != null && !alt.isBlank())
                            || (title != null && !title.isBlank());
                    if (hasUsefulData) {
                        result.add(new ImageLocalizationSample(src, alt, title));
                    }
                } catch (Exception ignored) {
                    // Skip stale/unreachable element.
                }
            }
        } catch (Exception ignored) {
            // ignore
        }
        return result;
    }

    private void visitLocalizationPages(String customerEmail, String customerPassword, Consumer<String> visitor) {
        if (visitor == null) {
            return;
        }

        if (navigateToPath("/")) {
            visitor.accept("home");
        }

        if (navigateToPath("/products")) {
            visitor.accept("products");
            if (clickFirstProduct()) {
                visitor.accept("productDetail");
            }
        }

        if (navigateToPath("/cart")) {
            visitor.accept("cart");
        }

        if (loginCustomer(customerEmail, customerPassword)) {
            if (navigateToPath("/account")) {
                visitor.accept("account");
            }
            if (openOrdersPageForCustomer(customerEmail, customerPassword)) {
                visitor.accept("orders");
                String orderDetailText = openFirstOrderDetailAndGetBodyText();
                if (orderDetailText != null && !orderDetailText.isBlank()) {
                    visitor.accept("orderDetail");
                }
            }
        }
    }

    private String resolveShopUrl() {
        String fromProp = System.getProperty(SHOP_URL_PROP);
        if (fromProp != null && !fromProp.isBlank()) {
            return validateShopUrl(fromProp.trim());
        }

        String fromEnv = System.getenv(SHOP_URL_ENV);
        if (fromEnv != null && !fromEnv.isBlank()) {
            return validateShopUrl(fromEnv.trim());
        }

        return validateShopUrl(DEFAULT_SHOP_URL);
    }

    private String buildShopUrl(String relativePath) {
        if (relativePath == null || relativePath.isBlank()) {
            return shopUrl;
        }
        if (relativePath.startsWith("http://") || relativePath.startsWith("https://")) {
            return relativePath;
        }
        String normalizedBase = shopUrl.endsWith("/") ? shopUrl.substring(0, shopUrl.length() - 1) : shopUrl;
        String normalizedPath = relativePath.startsWith("/") ? relativePath : "/" + relativePath;
        return normalizedBase + normalizedPath;
    }

    private boolean hasCustomerToken() {
        try {
            Object token = js.executeScript("return localStorage.getItem('lts.token');");
            return token != null && !token.toString().isBlank();
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isOnLoginPage() {
        try {
            String current = driver.getCurrentUrl();
            return current != null && current.contains("/login");
        } catch (Exception e) {
            return false;
        }
    }

    private String validateShopUrl(String candidate) {
        String lowered = candidate.toLowerCase(Locale.ROOT);
        if (lowered.contains(FORBIDDEN_DEMO_HOST)) {
            throw new IllegalArgumentException(
                    "Blocked URL: demo.prestashop.com da bi vo hieu hoa cho giai doan test hien tai");
        }
        return candidate;
    }

    private String normalizeLanguageCode(String rawCode) {
        if (rawCode == null || rawCode.isBlank()) {
            return null;
        }
        String cleaned = rawCode.trim().toLowerCase(Locale.ROOT).replace('_', '-');
        if (cleaned.contains("-")) {
            cleaned = cleaned.substring(0, cleaned.indexOf('-'));
        }
        if ("vn".equals(cleaned)) {
            return "vi";
        }
        return cleaned;
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
