# PrestaShop Localization Tester - Hướng Dẫn Nhanh

## Mở đầu
Chương trình này kiểm tra trang web PrestaShop Demo về các vấn đề localization:
- Định dạng tiền tệ
- Dịch thuật
- Định dạng ngày tháng
- Encoding
- UI overflow

## Chạy nhanh

### Windows (Double-click):
```
run_test.bat
```

### PowerShell:
```powershell
# Tiếng Anh
.\run_test.ps1 -lang en

# Tiếng Pháp  
.\run_test.ps1 -lang fr

# Tiếng Đức
.\run_test.ps1 -lang de
```

### Maven:
```bash
.\mvnw.cmd exec:java "-Dexec.mainClass=org.example.PrestaShopL10nTester" "-Dexec.args=fr"
```

## Menu trong chương trình

Khi chương trình chạy, bạn sẽ thấy menu:
```
╔════════════════════════════════════════╗
║       PRESTASHOP L10N TESTER           ║
╠════════════════════════════════════════╣
║ 1. Quét trang hiện tại                 ║
║ 2. Quét tất cả các trang chính         ║
║ 3. Đổi trang thủ công rồi quét         ║
║ 4. Xem báo cáo lỗi                     ║
║ 5. Thoát                               ║
╚════════════════════════════════════════╝
```

- **1**: Quét trang đang hiển thị trong Chrome
- **2**: Tự động quét Home, Clothes, Accessories, Art và Product Detail
- **3**: Bạn tự điều hướng trong Chrome, nhấn Enter để quét
- **4**: Xem danh sách lỗi đã phát hiện
- **5**: Thoát và xem tổng kết

## Kết quả

- **Ảnh chụp lỗi**: `./screenshots/` với đánh dấu vị trí lỗi
- **Log file**: `./screenshots/l10n_test_[timestamp].log`
- **Báo cáo**: `./screenshots/report_[lang]_[timestamp].txt`

## Các loại lỗi phát hiện

| Mã lỗi | Ý nghĩa |
|--------|---------|
| CURRENCY_SYMBOL | Ký hiệu tiền tệ sai |
| DECIMAL_SEPARATOR | Dấu phân cách thập phân sai |
| UNTRANSLATED_TEXT | Văn bản chưa dịch |
| DATE_FORMAT | Định dạng ngày sai |
| ENCODING | Lỗi encoding ký tự |
| UI_OVERFLOW | Text quá dài gây vỡ layout |

## Ngôn ngữ hỗ trợ
- en (English)
- fr (Français)
- de (Deutsch)
- es (Español)
- it (Italiano)
- pl (Polski)
- pt (Português)

## Lưu ý
- Trang PrestaShop demo nằm trong iframe, chương trình tự động xử lý
- Cần chờ vài giây để trang load hoàn toàn
- Chrome sẽ tự động mở khi chạy test
