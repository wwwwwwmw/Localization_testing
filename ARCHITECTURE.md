# Localization Testing Framework

## 🌍 Tổng Quan Kiến Trúc

Dự án này áp dụng **Data-Driven Testing** kết hợp **Strategy Pattern** trên nền tảng **JUnit 5** để kiểm thử Localization (L10n) cho nhiều ngôn ngữ.

### Nguyên tắc thiết kế: "Viết test 1 lần, chạy cho mọi ngôn ngữ"

```
┌─────────────────────────────────────────────────────────────────┐
│                    LocalizationTest.java                        │
│           (Generic Test Suite - JUnit 5 @ParameterizedTest)     │
├─────────────────────────────────────────────────────────────────┤
│  test01_CurrencyFormat()     → Kiểm tra định dạng tiền tệ      │
│  test02_DateFormat()         → Kiểm tra định dạng ngày tháng   │
│  test03_LayoutDirection()    → Kiểm tra RTL/LTR                │
│  test04_TextOverflow()       → Kiểm tra tràn khung UI          │
│  test05_UntranslatedText()   → Kiểm tra từ chưa dịch           │
│  test06_CharacterEncoding()  → Kiểm tra bảng mã ký tự          │
└───────────────────────────────┬─────────────────────────────────┘
                                │
                    @EnumSource(SupportedLocale.class)
                                │
         ┌──────────────────────┼──────────────────────┐
         │                      │                      │
         ▼                      ▼                      ▼
┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐
│ EnglishStrategy │  │ FrenchStrategy  │  │VietnameseStrategy│
└─────────────────┘  └─────────────────┘  └─────────────────┘
                                │
                    ┌───────────┴───────────┐
                    │                       │
                    ▼                       ▼
          ┌─────────────────┐    ┌─────────────────┐
          │ ArabicStrategy  │    │  (More locales) │
          │     (RTL)       │    │                 │
          └─────────────────┘    └─────────────────┘
```

## 📁 Cấu Trúc Dự Án

```
src/
├── main/java/org/example/
│   ├── strategy/                    # Strategy Pattern classes
│   │   ├── ILocaleStrategy.java     # Interface định nghĩa quy tắc L10n
│   │   ├── LocaleStrategyProvider.java # Factory/Provider cho JUnit 5
│   │   ├── EnglishStrategy.java     # Quy tắc cho tiếng Anh
│   │   ├── FrenchStrategy.java      # Quy tắc cho tiếng Pháp
│   │   ├── VietnameseStrategy.java  # Quy tắc cho tiếng Việt
│   │   └── ArabicStrategy.java      # Quy tắc cho tiếng Ả Rập (RTL)
│   │
│   ├── CurrencyChecker.java         # Utility kiểm tra tiền tệ
│   ├── DateChecker.java             # Utility kiểm tra ngày tháng
│   ├── TextChecker.java             # Utility kiểm tra text
│   └── L10nError.java               # Model lưu thông tin lỗi
│
└── test/java/org/example/
    ├── LocalizationTest.java        # ⭐ Generic Test Suite (JUnit 5)
    ├── FourLanguagesL10nTest.java   # Legacy tests (JUnit 4)
    └── LocalizationUnitTest.java    # Unit tests
```

## 🚀 Chạy Tests

### Chạy tất cả tests
```bash
./mvnw test
```

### Chạy chỉ LocalizationTest (JUnit 5)
```bash
./mvnw test -Dtest=LocalizationTest
```

### Chạy với report chi tiết
```bash
./mvnw test -Dsurefire.useFile=false
```

## 📋 6 Test Cases Cốt Lõi

| Test | Mục đích | Checkpoints |
|------|----------|-------------|
| **TEST 01: Currency Format** | Kiểm tra định dạng tiền tệ | Symbol ($, €, ₫), Position (Prefix/Suffix), Decimal Separator, Grouping |
| **TEST 02: Date Format** | Kiểm tra định dạng ngày | Pattern (dd/MM/yyyy vs MM/dd/yyyy), Dịch tên tháng |
| **TEST 03: Layout Direction** | Kiểm tra hướng trang | `dir="rtl"` cho Arabic, Text alignment |
| **TEST 04: Text Overflow** | Phát hiện vỡ giao diện | scrollWidth > offsetWidth, Truncation "..." |
| **TEST 05: Untranslated Text** | Tìm text chưa dịch | Từ tiếng Anh còn sót (Add to cart, Sign in...) |
| **TEST 06: Encoding** | Kiểm tra bảng mã | Ký tự đặc biệt (ư, ê, é, العربية), Broken encoding |

## 🔧 Cách Thêm Ngôn Ngữ Mới

### Bước 1: Tạo Strategy class mới

```java
package org.example.strategy;

public class GermanStrategy implements ILocaleStrategy {
    
    @Override
    public String getLanguageCode() { return "de"; }
    
    @Override
    public String getLanguageName() { return "Deutsch"; }
    
    @Override
    public String getCurrencySymbol() { return "€"; }
    
    @Override
    public boolean isCurrencySymbolPrefix() { return false; } // 100 €
    
    @Override
    public String getDecimalSeparator() { return ","; } // 1,99
    
    @Override
    public String getGroupingSeparator() { return "."; } // 1.000
    
    @Override
    public String getDatePattern() { return "dd.MM.yyyy"; }
    
    @Override
    public boolean isRTL() { return false; }
    
    @Override
    public List<String> getForbiddenWords() {
        return List.of("Add to cart", "Sign in", ...);
    }
    
    @Override
    public List<String> getExpectedKeywords() {
        return List.of("In den Warenkorb", "Anmelden", ...);
    }
    
    // ... other methods
}
```

### Bước 2: Thêm vào LocaleStrategyProvider

```java
public enum SupportedLocale {
    ENGLISH("en", new EnglishStrategy()),
    FRENCH("fr", new FrenchStrategy()),
    VIETNAMESE("vi", new VietnameseStrategy()),
    ARABIC("ar", new ArabicStrategy()),
    GERMAN("de", new GermanStrategy());  // ← Thêm dòng này
    // ...
}
```

### Bước 3: Chạy lại tests
```bash
./mvnw test -Dtest=LocalizationTest
```

## ⚡ Tối Ưu Hiệu Năng

| Kỹ thuật | Mô tả |
|----------|-------|
| **@TestInstance(PER_CLASS)** | Mở browser 1 lần cho toàn bộ test class |
| **Language Caching** | Cache kết quả switch language để không phải retry |
| **Smart Language Switch** | Chỉ switch khi cần, reuse session hiện tại |
| **Explicit Wait** | Không dùng Thread.sleep, dùng WebDriverWait |
| **assertAll()** | Gom nhiều assertions, báo cáo tất cả lỗi 1 lần |

## 📊 Quy Tắc L10n Cho 4 Ngôn Ngữ

| Aspect | EN (English) | FR (Français) | VI (Tiếng Việt) | AR (العربية) |
|--------|--------------|---------------|-----------------|--------------|
| **Currency Symbol** | $ | € | ₫ | ر.س |
| **Symbol Position** | Prefix ($100) | Suffix (100 €) | Suffix (100.000 ₫) | Suffix |
| **Decimal Sep** | . (dot) | , (comma) | , (comma) | . (dot) |
| **Grouping Sep** | , (comma) | (space) | . (dot) | , (comma) |
| **Date Pattern** | MM/dd/yyyy | dd/MM/yyyy | dd/MM/yyyy | dd/MM/yyyy |
| **Direction** | LTR | LTR | LTR | **RTL** ⚠️ |
| **Script** | Latin | Latin+diacritics | Latin+diacritics | Arabic |

## 🐛 Troubleshooting

### Test bị skip với message "Could not switch to language"
- PrestaShop demo có thể không có ngôn ngữ đó
- Kiểm tra network connectivity
- Tăng TIMEOUT_SECONDS trong code

### Screenshot không được chụp
- Kiểm tra thư mục `screenshots/` đã tồn tại
- Verify quyền ghi vào thư mục

### Browser không mở được
- Chạy `./mvnw clean` và thử lại
- Update Chrome browser lên bản mới nhất

## 📝 License

MIT License - Free for commercial and personal use.
