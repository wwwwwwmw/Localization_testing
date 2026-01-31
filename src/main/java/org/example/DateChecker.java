package org.example;

import org.openqa.selenium.*;
import java.util.*;
import java.util.regex.*;

/**
 * Kiem tra dinh dang ngay thang
 */
public class DateChecker {

    private WebDriver driver;
    private String currentLanguage;
    private List<L10nError> errors;

    public DateChecker(WebDriver driver, String language, List<L10nError> errors) {
        this.driver = driver;
        this.currentLanguage = language;
        this.errors = errors;
    }

    /**
     * Kiem tra dinh dang ngay thang tren trang
     */
    public void check(LanguageConfig config) {
        System.out.println("\n[KIEM TRA DINH DANG NGAY THANG]");

        try {
            String bodyText = driver.findElement(By.tagName("body")).getText();

            // Cac mau ngay thang pho bien
            Pattern datePattern = Pattern.compile(
                    "(\\d{1,2})[/\\.-](\\d{1,2})[/\\.-](\\d{2,4})|" +
                            "(\\d{1,2})\\s+(Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)[a-z]*\\s+(\\d{2,4})|" +
                            "(Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)[a-z]*\\s+(\\d{1,2}),?\\s+(\\d{2,4})");
            Matcher matcher = datePattern.matcher(bodyText);

            boolean foundDate = false;
            int validCount = 0;
            int errorCount = 0;
            Set<String> checkedDates = new HashSet<>();

            while (matcher.find()) {
                String dateStr = matcher.group();
                if (checkedDates.contains(dateStr))
                    continue;
                checkedDates.add(dateStr);
                foundDate = true;

                boolean hasError = false;
                String errorDetail = "";

                // Kiem tra dinh dang tieng Anh trong trang khong phai EN
                if (!currentLanguage.equals("en")) {
                    String[] englishMonths = { "January", "February", "March", "April", "May", "June",
                            "July", "August", "September", "October", "November", "December",
                            "Jan", "Feb", "Mar", "Apr", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec" };
                    for (String month : englishMonths) {
                        if (dateStr.contains(month)) {
                            hasError = true;
                            errorDetail = "Ten thang tieng Anh '" + month + "' trong trang " + config.languageName;
                            break;
                        }
                    }
                }

                // Kiem tra dinh dang US (MM/DD/YYYY) cho cac nuoc chau Au dung DD/MM/YYYY
                if (!hasError && dateStr.matches("\\d{1,2}/\\d{1,2}/\\d{2,4}")) {
                    String[] parts = dateStr.split("/");
                    int first = Integer.parseInt(parts[0]);
                    int second = Integer.parseInt(parts[1]);

                    if (!currentLanguage.equals("en") && first <= 12 && second > 12) {
                        hasError = true;
                        errorDetail = "Dinh dang MM/DD/YYYY (My), nhung " + config.languageName + " dung DD/MM/YYYY";
                    }
                }

                if (hasError) {
                    System.out.println("   [LOI] Ngay: " + dateStr + " - " + errorDetail);
                    errors.add(new L10nError("DATE_FORMAT", "Dinh dang ngay sai", errorDetail + ": " + dateStr,
                            driver.getCurrentUrl()));
                    errorCount++;
                } else {
                    System.out.println("   [OK] Ngay: " + dateStr);
                    validCount++;
                }
            }

            if (!foundDate) {
                System.out.println("   [INFO] Khong tim thay ngay thang tren trang nay.");
            } else {
                System.out.println("   >> Tong ket: " + validCount + " ngay dung, " + errorCount + " ngay loi");
            }

        } catch (Exception e) {
            System.out.println("   [LOI] Loi kiem tra ngay thang: " + e.getMessage());
        }
    }
}
