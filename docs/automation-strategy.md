# Automation Strategy - Localization Testing

## 1. Muc tieu automation
- Tu dong hoa nhom test lap lai nhieu va de hoi quy.
- Giam cong manual khi mo rong locale.
- Tao du lieu bao cao phuc vu coverage tinh toan.

## 2. Kim tu thap automation
1. Unit (nhanh nhat): formatter, parser, locale mapper.
2. Integration: locale switch + validator.
3. UI E2E: journey chinh tren browser.

## 3. Nguyen tac thiet ke automation
- Data-driven theo locale profile.
- Page Object de giam duplicate selector.
- Evidence-first: fail la luu screenshot + HTML snippet.
- Retry co gioi han cho thao tac de flaky (toi da 2 lan).

## 4. Tag de chay linh hoat
- @smoke
- @regression
- @locale-en, @locale-fr, @locale-vi, @locale-ar
- @date, @currency, @overflow, @rtl, @encoding

## 5. Lich chay de xuat
- Moi pull request:
  - smoke + locale-en + locale-vi
- Nightly:
  - regression core cho en/fr/vi/ar
- Weekly:
  - full matrix all locale + viewport matrix

## 6. Tieu chi anti-flaky
- Dung explicit wait cho phan tu va state.
- Khong assert text ngay sau click ma chua dong bo.
- Tach du lieu dynamic khoi expected static.

## 7. Bao cao automation
- Bao cao pass/fail theo locale
- Top 10 test fail lap lai cao nhat
- Trend defect theo domain localization

## 8. Mapping tu catalog sang automation
- Nhom A-D-E-F: bat buoc automation
- Nhom G-H: uu tien cao
- Test exploratory van can manual bo sung
