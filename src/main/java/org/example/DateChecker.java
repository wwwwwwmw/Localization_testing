package org.example;

import org.openqa.selenium.*;
import java.util.*;
import java.util.regex.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Kiem tra dinh dang ngay thang
 * 
 * Da tach cac ham static de ho tro Unit Test khong can Selenium Driver
 */
public class DateChecker {

    private WebDriver driver;
    private String currentLanguage;
    private List<L10nError> errors;

    // ==================== STATIC PATTERNS ====================

    // Cac ten thang tieng Anh
    private static final String[] ENGLISH_MONTHS_FULL = {
            "January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"
    };

    private static final String[] ENGLISH_MONTHS_SHORT = {
            "Jan", "Feb", "Mar", "Apr", "May", "Jun",
            "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
    };

    // Pattern kiem tra dinh dang ngay
    private static final Pattern DATE_PATTERN_NUMERIC = Pattern.compile(
            "(\\d{1,4})[/\\.-](\\d{1,2})[/\\.-](\\d{1,4})");

    private static final Pattern DATE_PATTERN_MONTH_NAME = Pattern.compile(
            "(\\d{1,2})\\s+(Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)[a-z]*\\s+(\\d{2,4})|" +
                    "(Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)[a-z]*\\s+(\\d{1,2}),?\\s+(\\d{2,4})");

    public DateChecker(WebDriver driver, String language, List<L10nError> errors) {
        this.driver = driver;
        this.currentLanguage = language;
        this.errors = errors;
    }

    // ==================== STATIC UTILITY METHODS (cho Unit Test)
    // ====================

    /**
     * Phat hien dinh dang ngay tu chuoi
     * 
     * @param dateText Chuoi chua ngay
     * @return DateFormat enum hoac null
     */
    public static DateFormat detectDateFormat(String dateText) {
        if (dateText == null || dateText.isEmpty())
            return null;

        // Kiem tra dinh dang so
        Matcher numericMatcher = DATE_PATTERN_NUMERIC.matcher(dateText);
        if (numericMatcher.find()) {
            String first = numericMatcher.group(1);
            String second = numericMatcher.group(2);
            String third = numericMatcher.group(3);

            int firstNum = Integer.parseInt(first);
            int secondNum = Integer.parseInt(second);
            int thirdNum = Integer.parseInt(third);

            // YYYY-MM-DD (ISO)
            if (first.length() == 4) {
                return DateFormat.ISO;
            }

            // Kiem tra xem co phai nam o cuoi khong
            if (third.length() == 4) {
                // MM/DD/YYYY (US) - first <= 12 va second > 12 (ngay chac chan)
                if (firstNum <= 12 && secondNum > 12) {
                    return DateFormat.MDY;
                }
                // DD/MM/YYYY - first > 12 (ngay chac chan)
                if (firstNum > 12 && firstNum <= 31) {
                    return DateFormat.DMY;
                }
                // Ambiguous - ca hai deu co the la thang hoac ngay
                if (firstNum <= 12 && secondNum <= 12) {
                    return DateFormat.AMBIGUOUS;
                }
                // Mac dinh DD/MM/YYYY cho cac truong hop khac
                return DateFormat.DMY;
            }
        }

        // Kiem tra dinh dang co ten thang
        if (DATE_PATTERN_MONTH_NAME.matcher(dateText).find()) {
            return DateFormat.MONTH_NAME;
        }

        return null;
    }

    /**
     * Kiem tra xem ngay co chua ten thang tieng Anh khong
     * 
     * @param dateText Chuoi ngay
     * @return true neu co ten thang tieng Anh
     */
    public static boolean containsEnglishMonth(String dateText) {
        if (dateText == null)
            return false;

        for (String month : ENGLISH_MONTHS_FULL) {
            if (dateText.contains(month))
                return true;
        }
        for (String month : ENGLISH_MONTHS_SHORT) {
            if (dateText.contains(month))
                return true;
        }
        return false;
    }

    /**
     * Xac thuc dinh dang ngay theo cau hinh ngon ngu
     * 
     * @param dateText        Chuoi ngay
     * @param languageCode    Ma ngon ngu
     * @param expectedPattern Pattern mong doi tu LanguageConfig
     * @return DateCheckResult
     */
    public static DateCheckResult validateDate(String dateText, String languageCode, String expectedPattern) {
        DateCheckResult result = new DateCheckResult();
        result.originalText = dateText;
        result.isValid = false;

        if (dateText == null || dateText.isEmpty()) {
            result.errorMessage = "Chuoi ngay rong";
            return result;
        }

        result.detectedFormat = detectDateFormat(dateText);

        // Kiem tra ten thang tieng Anh trong trang khong phai tieng Anh
        if (!"en".equals(languageCode) && containsEnglishMonth(dateText)) {
            result.hasEnglishMonth = true;
            result.errorMessage = "Tim thay ten thang tieng Anh trong trang " + languageCode;
            return result;
        }

        // Kiem tra dinh dang US trong cac nuoc chau Au
        if (result.detectedFormat == DateFormat.MDY && !isUSDateFormat(languageCode)) {
            result.errorMessage = "Dinh dang MM/DD/YYYY (My), nhung " + languageCode + " dung DD/MM/YYYY";
            return result;
        }

        // Kiem tra voi pattern mong doi
        if (expectedPattern != null && !expectedPattern.isEmpty()) {
            if (!dateText.matches(expectedPattern)) {
                result.warningMessage = "Ngay khong khop voi pattern mong doi: " + expectedPattern;
            }
        }

        result.isValid = true;
        return result;
    }

    /**
     * Kiem tra ngon ngu co dung dinh dang ngay kieu My khong
     */
    public static boolean isUSDateFormat(String languageCode) {
        // Chi My va mot so nuoc dung MM/DD/YYYY
        return "en".equals(languageCode) || "mx".equals(languageCode);
    }

    /**
     * Phan tich ngay tu chuoi voi dinh dang cu the
     * 
     * @param dateText Chuoi ngay
     * @param format   Dinh dang ngay
     * @return LocalDate hoac null
     */
    public static LocalDate parseDate(String dateText, DateFormat format) {
        if (dateText == null || format == null)
            return null;

        try {
            Matcher m = DATE_PATTERN_NUMERIC.matcher(dateText);
            if (m.find()) {
                int first = Integer.parseInt(m.group(1));
                int second = Integer.parseInt(m.group(2));
                int third = Integer.parseInt(m.group(3));

                int year, month, day;
                switch (format) {
                    case ISO:
                        year = first;
                        month = second;
                        day = third;
                        break;
                    case DMY:
                        day = first;
                        month = second;
                        year = third;
                        break;
                    case MDY:
                        month = first;
                        day = second;
                        year = third;
                        break;
                    default:
                        return null;
                }

                // Xu ly nam 2 chu so
                if (year < 100) {
                    year += (year > 50) ? 1900 : 2000;
                }

                return LocalDate.of(year, month, day);
            }
        } catch (Exception e) {
            // Parse failed
        }
        return null;
    }

    /**
     * Chuyen doi ngay tu dinh dang nay sang dinh dang khac
     * 
     * @param dateText   Chuoi ngay goc
     * @param fromFormat Dinh dang goc
     * @param toFormat   Dinh dang dich
     * @param separator  Dau phan cach mong muon
     * @return Chuoi ngay da chuyen doi
     */
    public static String convertDateFormat(String dateText, DateFormat fromFormat, DateFormat toFormat,
            String separator) {
        LocalDate date = parseDate(dateText, fromFormat);
        if (date == null)
            return null;

        switch (toFormat) {
            case ISO:
                return String.format("%04d%s%02d%s%02d", date.getYear(), separator, date.getMonthValue(), separator,
                        date.getDayOfMonth());
            case DMY:
                return String.format("%02d%s%02d%s%04d", date.getDayOfMonth(), separator, date.getMonthValue(),
                        separator, date.getYear());
            case MDY:
                return String.format("%02d%s%02d%s%04d", date.getMonthValue(), separator, date.getDayOfMonth(),
                        separator, date.getYear());
            default:
                return null;
        }
    }

    /**
     * Trich xuat tat ca ngay tu chuoi van ban
     * 
     * @param text Van ban chua ngay
     * @return Danh sach cac chuoi ngay tim duoc
     */
    public static List<String> extractDates(String text) {
        List<String> dates = new ArrayList<>();
        if (text == null)
            return dates;

        // Tim dinh dang so
        Matcher numericMatcher = DATE_PATTERN_NUMERIC.matcher(text);
        while (numericMatcher.find()) {
            dates.add(numericMatcher.group());
        }

        // Tim dinh dang co ten thang
        Matcher monthMatcher = DATE_PATTERN_MONTH_NAME.matcher(text);
        while (monthMatcher.find()) {
            dates.add(monthMatcher.group());
        }

        return dates;
    }

    // ==================== ENUMS & RESULT CLASSES ====================

    public enum DateFormat {
        ISO, // YYYY-MM-DD
        DMY, // DD/MM/YYYY
        MDY, // MM/DD/YYYY
        MONTH_NAME, // Jan 1, 2024 or 1 Jan 2024
        AMBIGUOUS // Co the la DMY hoac MDY
    }

    /**
     * Class chua ket qua kiem tra ngay
     */
    public static class DateCheckResult {
        public String originalText;
        public DateFormat detectedFormat;
        public boolean isValid;
        public boolean hasEnglishMonth;
        public String errorMessage;
        public String warningMessage;

        @Override
        public String toString() {
            if (isValid) {
                String msg = "OK: " + originalText + " (format: " + detectedFormat + ")";
                if (warningMessage != null)
                    msg += " [Warning: " + warningMessage + "]";
                return msg;
            } else {
                return "ERROR: " + originalText + " - " + errorMessage;
            }
        }
    }

    // ==================== SELENIUM-DEPENDENT METHODS ====================

    /**
     * Kiem tra dinh dang ngay thang tren trang
     */
    public void check(LanguageConfig config) {
        System.out.println("\n[KIEM TRA DINH DANG NGAY THANG]");

        try {
            String bodyText = driver.findElement(By.tagName("body")).getText();

            // Su dung ham static de trich xuat ngay
            List<String> dates = extractDates(bodyText);

            if (dates.isEmpty()) {
                System.out.println("   [INFO] Khong tim thay ngay thang tren trang nay.");
                return;
            }

            Set<String> checkedDates = new HashSet<>();
            int validCount = 0;
            int errorCount = 0;

            for (String dateStr : dates) {
                if (checkedDates.contains(dateStr))
                    continue;
                checkedDates.add(dateStr);

                // Su dung ham static de kiem tra
                DateCheckResult result = validateDate(dateStr, currentLanguage, config.datePattern);

                if (result.isValid) {
                    System.out.println("   [OK] Ngay: " + dateStr + " (format: " + result.detectedFormat + ")");
                    if (result.warningMessage != null) {
                        System.out.println("        [CANH BAO] " + result.warningMessage);
                    }
                    validCount++;
                } else {
                    System.out.println("   [LOI] Ngay: " + dateStr + " - " + result.errorMessage);
                    errors.add(new L10nError("DATE_FORMAT", "Dinh dang ngay sai",
                            result.errorMessage + ": " + dateStr, driver.getCurrentUrl()));
                    errorCount++;
                }
            }

            System.out.println("   >> Tong ket: " + validCount + " ngay dung, " + errorCount + " ngay loi");

        } catch (Exception e) {
            System.out.println("   [LOI] Loi kiem tra ngay thang: " + e.getMessage());
        }
    }

    /**
     * Lay tat ca ngay tu trang web
     * 
     * @return Danh sach chuoi ngay
     */
    public List<String> extractDatesFromPage() {
        try {
            String bodyText = driver.findElement(By.tagName("body")).getText();
            return extractDates(bodyText);
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }
}
