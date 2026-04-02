# Localization Testing Framework

## 🌍 Tổng Quan Kiến Trúc

Dự án này áp dụng **Data-Driven Testing** kết hợp **Strategy Pattern** trên nền tảng **JUnit 5** để kiểm thử Localization (L10n) cho nhiều ngôn ngữ.

### Nguyên tắc thiết kế: "Viết test 1 lần, chạy cho mọi ngôn ngữ"

```
┌─────────────────────────────────────────────────────────────────┐
│                  MainLocalizationTest.java                       │
│           (Generic Test Suite - JUnit 5 @ParameterizedTest)     │
├─────────────────────────────────────────────────────────────────┤
│  test01_CurrencyFormat()          → Kiểm tra định dạng tiền tệ │
│  test02_UntranslatedText()        → Kiểm tra text chưa dịch    │
│  test03_DateFormat()              → Kiểm tra định dạng ngày    │
│  test04_LayoutDirection()         → Kiểm tra RTL/LTR           │
│  test05_TextOverflow()            → Kiểm tra tràn khung UI     │
│  test06_Charset()                 → Kiểm tra bảng mã ký tự     │
│  test07_NumberAndMeasurement()    → Kiểm tra số + đơn vị đo    │
│  test08_MediaAndAltText()         → Kiểm tra media + alt text  │
│  test09_UrlRoutingLocalization()  → Kiểm tra URL theo locale   │
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
│   ├── strategies/                  # Strategy Pattern classes
│   │   ├── ILocaleStrategy.java     # Interface định nghĩa quy tắc L10n
│   │   ├── LocaleStrategyProvider.java # Factory/Provider cho JUnit 5
│   │   ├── EnglishStrategy.java     # Quy tắc cho tiếng Anh
│   │   ├── FrenchStrategy.java      # Quy tắc cho tiếng Pháp
│   │   ├── VietnameseStrategy.java  # Quy tắc cho tiếng Việt
│   │   └── ArabicStrategy.java      # Quy tắc cho tiếng Ả Rập (RTL)
│   │
│   ├── core/L10nValidator.java      # 9 module kiểm tra L10n
│   ├── pages/PrestaShopPage.java    # Page Object thao tác UI
│   └── L10nError.java               # Model lưu thông tin lỗi
│
└── test/java/org/example/
    ├── MainLocalizationTest.java    # ⭐ Generic Test Suite (JUnit 5)
    └── BaseTest.java                # Setup/teardown test runtime
```

## 🚀 Chạy Tests

### Chạy tất cả tests
```bash
./mvnw test
```

### Chạy chỉ MainLocalizationTest (JUnit 5)
```bash
./mvnw test -Dtest=MainLocalizationTest
```

### Chạy với report chi tiết
```bash
./mvnw test -Dsurefire.useFile=false
```

## 📋 9 Module Test Cốt Lõi

| Test | Mục đích | Checkpoints |
|------|----------|-------------|
| **TEST 01: Currency Format** | Kiểm tra định dạng tiền tệ | Symbol, Position (Prefix/Suffix), Decimal Separator, Grouping |
| **TEST 02: Untranslated Text** | Tìm text chưa dịch | Từ tiếng Anh còn sót theo ngữ cảnh UI |
| **TEST 03: Date Format** | Kiểm tra định dạng ngày | Pattern theo locale, rò rỉ tháng tiếng Anh |
| **TEST 04: Layout Direction** | Kiểm tra hướng trang | `dir="rtl"` cho Arabic, locale LTR không bị áp RTL |
| **TEST 05: Text Overflow** | Phát hiện vỡ giao diện | scrollWidth > offsetWidth, truncation bất thường |
| **TEST 06: Charset** | Kiểm tra bảng mã | UTF-8, ký tự đặc trưng locale, mojibake |
| **TEST 07: Number & Measurement** | Kiểm tra số và đơn vị đo | Dấu phân tách, đơn vị đo không bị rò rỉ tiếng Anh |
| **TEST 08: Media & Alt Text** | Kiểm tra media theo locale | Alt text đầy đủ, asset không sai locale |
| **TEST 09: URL & Routing** | Kiểm tra URL theo locale | Path/query phản ánh locale nhất quán |

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
    GERMAN("de", new GermanStrategy()),
    JAPANESE("ja", new JapaneseStrategy()),
    RUSSIAN("ru", new RussianStrategy()),
    ARABIC("ar", new ArabicStrategy());
}
```

### Bước 3: Chạy lại tests
```bash
./mvnw test -Dtest=MainLocalizationTest
```

## ⚡ Tối Ưu Hiệu Năng

| Kỹ thuật | Mô tả |
|----------|-------|
| **@TestInstance(PER_CLASS)** | Mở browser 1 lần cho toàn bộ test class |
| **Language Caching** | Cache kết quả switch language để không phải retry |
| **Smart Language Switch** | Chỉ switch khi cần, reuse session hiện tại |
| **Explicit Wait** | Không dùng Thread.sleep, dùng WebDriverWait |
| **assertAll()** | Gom nhiều assertions, báo cáo tất cả lỗi 1 lần |

## 📊 Quy Tắc L10n Cho 7 Ngôn Ngữ

| Aspect | EN | FR | VI | DE | JA | RU | AR |
|--------|----|----|----|----|----|----|----|
| **Currency Symbol** | $/£/€ | € | ₫ | € | ¥ | ₽ | ر.س |
| **Symbol Position** | Prefix/Suffix | Suffix | Suffix | Suffix | Prefix | Suffix | Suffix |
| **Decimal Sep** | . | , | , | , | . | , | . |
| **Grouping Sep** | , | space | . | . | , | space | , |
| **Date Pattern** | M/d/yyyy | dd/MM/yyyy | d/M/yyyy | d.M.yyyy | yyyy/M/d | dd.MM.yyyy | d/M/yyyy |
| **Direction** | LTR | LTR | LTR | LTR | LTR | LTR | **RTL** |
| **Script** | Latin | Latin | Latin | Latin | Japanese | Cyrillic | Arabic |

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
