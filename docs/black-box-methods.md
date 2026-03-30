# Black-Box Testing Methods

Tai lieu nay mo ta phuong phap test black-box cho localization.

## 1. Phan vung tuong duong (Equivalence Partitioning)
### Ap dung cho date format
- Partition hop le:
  - en: MM/dd/yyyy
  - fr/vi: dd/MM/yyyy
  - ar: dd/MM/yyyy + script phu hop
- Partition khong hop le:
  - Sai separator
  - Sai thu tu ngay-thang
  - Ky tu khong thuoc locale

### Ap dung cho currency
- Partition hop le:
  - Symbol dung locale
  - Decimal separator dung locale
  - Vi tri symbol dung (prefix/suffix)
- Partition khong hop le:
  - Symbol cua locale khac
  - Vua co dot vua co comma sai logic

## 2. Gia tri bien (Boundary Value Analysis)
### Long string
- Test do dai 0, 1, max-1, max, max+1, 2x max.
- Kiem tra overflow, wrapping, clipping, ellipsis.

### So tien
- 0, 0.01, 1, 999, 1000, 9999999.99
- So am neu domain cho phep.

## 3. Bang quyet dinh (Decision Table)
Dieu kien:
- Locale la RTL/LTR
- Chuoi dai/khong dai
- Viewport nho/lon

Hanh dong mong doi:
- Layout dung huong
- Khong vo dong
- Khong che text quan trong

## 4. Chuyen trang thai (State Transition)
Trang thai:
- locale hien tai
- locale moi
- load page
- refresh

Kiem tra:
- switch locale 1 lan
- switch locale lien tuc A->B->C->A
- switch locale trong khi modal dang mo

## 5. Use-case testing
- Dang nhap, tim kiem, xem chi tiet, checkout (neu co)
- Moi use-case lap lai tren nhom locale A.

## 6. Danh sach ten test black-box mau
1. BB_EQ_DATE_01_ValidFormat_English
2. BB_EQ_DATE_02_InvalidSeparator_French
3. BB_EQ_CUR_01_ValidCurrencySymbol_Vietnamese
4. BB_EQ_CUR_02_InvalidCurrencyPosition_Arabic
5. BB_BVA_TEXT_01_Length_MaxMinus1
6. BB_BVA_TEXT_02_Length_MaxPlus1
7. BB_DECISION_RTL_01_RTL_SmallViewport_LongText
8. BB_STATE_SWITCH_01_MultiLocale_RoundTrip
9. BB_USECASE_SEARCH_01_QueryWithAccent
10. BB_USECASE_CART_01_TotalFormatting_PerLocale
