# Yêu Cầu Hệ Thống - Localization Testing Tool

## Trạng thái các công cụ:

| Công cụ | Yêu cầu | Trạng thái | Ghi chú |
|---------|---------|------------|---------|
| Java JDK 17+ | ✅ Bắt buộc | ✅ Đã cài | Version 17.0.12 |
| Google Chrome | ✅ Bắt buộc | ✅ Đã cài | Version 144.0.7559.110 |
| Maven | ✅ Bắt buộc | ✅ Wrapper có sẵn | Sử dụng mvnw.cmd |
| ChromeDriver | ✅ Bắt buộc | ✅ Tự động | WebDriverManager tự tải |
| Selenium | ✅ Bắt buộc | ✅ Trong pom.xml | Version 4.16.1 |
| WebDriverManager | ✅ Bắt buộc | ✅ Trong pom.xml | Version 5.6.3 |

## Cách chạy chương trình:

### Cách 1: Sử dụng file batch (Windows)
```bash
.\run_test.bat
```
Sau đó chọn ngôn ngữ và chế độ quét.

### Cách 2: Sử dụng PowerShell
```powershell
# Test tiếng Pháp (chế độ tương tác)
.\run_test.ps1 -lang fr

# Test tiếng Đức (chế độ tự động)
.\run_test.ps1 -lang de -auto
```

### Cách 3: Sử dụng Maven trực tiếp

#### Chế độ tương tác (Manual):
```bash
.\mvnw.cmd exec:java -Dexec.mainClass="org.example.PrestaShopL10nTester" -Dexec.args="fr"
```

#### Chế độ tự động quét nhanh (Auto):
```bash
.\mvnw.cmd exec:java -Dexec.mainClass="org.example.QuickL10nScanner" -Dexec.args="en"
```

## Mã ngôn ngữ hỗ trợ:
| Mã | Ngôn ngữ | Tiền tệ | Dấu thập phân |
|----|----------|---------|---------------|
| `en` | English | £/$/€ | . |
| `fr` | Français | € | , |
| `vi` | Tiếng Việt | ₫ | , |
| `de` | Deutsch | € | , |
| `ja` | 日本語 | ¥ | . |
| `ru` | Русский | ₽ | , |
| `ar` | العربية | ر.س | . |

## Các module kiểm tra:

1. **💰 Tiền tệ**: 
   - Kiểm tra ký hiệu tiền tệ phù hợp với locale
   - Kiểm tra dấu phân cách thập phân (. hoặc ,)

2. **📝 Dịch thuật**:
   - Tìm các từ khóa mong đợi theo ngôn ngữ
   - Phát hiện văn bản chưa được dịch (tiếng Anh còn sót)

3. **📅 Ngày tháng**:
   - Kiểm tra định dạng ngày phù hợp với locale
   - DD/MM/YYYY hoặc DD.MM.YYYY tùy ngôn ngữ

4. **↔️ Hướng bố cục (RTL/LTR)**:
   - Kiểm tra `dir="rtl"` cho Arabic
   - Kiểm tra locale LTR không bị áp sai RTL

5. **📐 UI Overflow**:
   - Kiểm tra nút bấm có text quá dài
   - Phát hiện khả năng vỡ layout

6. **🔤 Encoding/Charset**:
   - Phát hiện các ký tự bị lỗi encoding (Ã, â€, etc.)

7. **🔢 Number & Measurement**:
   - Kiểm tra định dạng số theo locale
   - Phát hiện rò rỉ đơn vị đo không đúng ngôn ngữ

8. **🖼 Media & Alt Text**:
   - Kiểm tra alt text được địa phương hóa
   - Kiểm tra asset/media không bị rò rỉ locale sai

9. **🔗 URL & Routing Localization**:
   - Kiểm tra URL phản ánh locale đúng chuẩn
   - Phát hiện route/path không nhất quán theo ngôn ngữ

## Kết quả test:
- **Screenshots lỗi**: `./screenshots/` 
- **Log chi tiết**: Console output + file log trong screenshots
- **Báo cáo**: File report_[lang]_[timestamp].txt

## Cấu trúc dự án:

```
Localization_testing/
├── src/main/java/org/example/
│   ├── PrestaShopL10nTester.java  # Chế độ tương tác
│   ├── QuickL10nScanner.java      # Chế độ quét nhanh
│   └── PrestaShopAutoScanner.java # Chế độ tự động đầy đủ
├── screenshots/                    # Ảnh chụp lỗi
├── run_test.bat                   # Script Windows
├── run_test.ps1                   # Script PowerShell
├── mvnw.cmd                       # Maven Wrapper
└── pom.xml                        # Cấu hình Maven
```

## Lưu ý:
- PrestaShop demo sử dụng iframe, chương trình tự động switch context
- Có thể mất vài giây để trang load hoàn toàn
- Screenshots sẽ có đánh dấu vị trí lỗi bằng khung đỏ
