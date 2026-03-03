package org.example.tests;

import org.example.core.DriverManager;
import org.example.core.L10nError;
import org.example.core.L10nValidator;
import org.example.pages.PrestaShopPage;
import org.junit.jupiter.api.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * BaseTest - Lớp cơ sở cho tất cả test L10n.
 *
 * Trách nhiệm:
 * - Quản lý lifecycle của DriverManager (khởi tạo / đóng)
 * - Tạo sẵn PrestaShopPage và L10nValidator
 * - In thông tin test trước/sau mỗi test
 * - Cung cấp helper assertion
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class BaseTest {

    // Đặt false để hiển thị trình duyệt, true để chạy headless
    protected static final boolean HEADLESS = false;

    protected DriverManager driverManager;
    protected PrestaShopPage page;
    protected L10nValidator validator;

    // ==================== LIFECYCLE ====================

    @BeforeAll
    void setUpOnce() {
        System.out.println("╔══════════════════════════════════════════════════════════╗");
        System.out.println("║       L10N TEST SUITE — PrestaShop Demo                  ║");
        System.out.println("╚══════════════════════════════════════════════════════════╝");

        driverManager = new DriverManager();
        driverManager.initDriver(HEADLESS);

        page = new PrestaShopPage(driverManager);
        validator = new L10nValidator(driverManager);

        // Mở trang một lần duy nhất
        page.open();
    }

    @AfterAll
    void tearDownOnce() {
        System.out.println("\n╔══════════════════════════════════════════════════════════╗");
        System.out.println("║                 ✅ ALL TESTS COMPLETED                    ║");
        System.out.println("╚══════════════════════════════════════════════════════════╝");

        if (driverManager != null) {
            driverManager.quitDriver();
        }
    }

    @BeforeEach
    void beforeEach(TestInfo testInfo) {
        System.out.println("\n──────────────────────────────────────────────────────────");
        System.out.println("▶ " + testInfo.getDisplayName());
    }

    @AfterEach
    void afterEach(TestInfo testInfo) {
        System.out.println("◀ Hoàn thành: " + testInfo.getDisplayName());
    }

    // ==================== ASSERTION HELPERS ====================

    /**
     * Assert không có lỗi L10n nào. In chi tiết lỗi nếu có.
     */
    protected void assertNoErrors(List<L10nError> errors, String context) {
        if (!errors.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            sb.append("Tìm thấy ").append(errors.size()).append(" lỗi L10n cho ").append(context).append(":\n");
            for (L10nError err : errors) {
                sb.append("  • ").append(err).append("\n");
            }
            fail(sb.toString());
        }
    }

    /**
     * Assert số lỗi không vượt quá ngưỡng cho phép.
     */
    protected void assertErrorsWithinThreshold(List<L10nError> errors, int maxAllowed, String context) {
        if (errors.size() > maxAllowed) {
            StringBuilder sb = new StringBuilder();
            sb.append("Quá nhiều lỗi L10n (").append(errors.size())
                    .append(" > ").append(maxAllowed).append(") cho ").append(context).append(":\n");
            for (L10nError err : errors) {
                sb.append("  • ").append(err).append("\n");
            }
            fail(sb.toString());
        }
    }
}
