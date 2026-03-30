# Test Plan - Localization Testing

## 1. Muc tieu test
- Xac minh he thong ho tro da ngon ngu dung ve noi dung va dinh dang.
- Phat hien loi dinh dang ngay, tien te theo locale.
- Phat hien loi chuoi dai, text overflow, vo bo cuc.
- Kiem tra su on dinh khi chuyen doi locale lien tuc.

## 2. Pham vi
### Trong pham vi
- UI localization: menu, nut, form, thong bao, error text.
- Dinh dang date/time theo locale.
- Dinh dang currency (symbol, separator, position).
- Long string behavior tren desktop va viewport nho.
- Huong layout LTR/RTL (dac biet locale Arabic).

### Ngoai pham vi
- Noi dung marketing ben thu ba khong quan ly trong he thong.
- Hieu nang tai cao tai cap backend infrastructure.

## 3. Muc tieu chat luong
- Ty le pass test quan trong: >= 98%.
- Ty le bao phu localization topic (theo coverage-formula.md): >= 90%.
- Khong con blocker lien quan date/currency/overflow truoc release.

## 4. Muc test
- Unit test: utility format, parser, locale resolver.
- Integration test: service + formatter + strategy.
- E2E UI test: hanh vi thuc te tren browser.
- Regression test: bo test co dinh cho cac locale chinh.

## 5. Nhom locale uu tien
- Nhom A (bat buoc): en, fr, vi, ar.
- Nhom B (mo rong): de, es, it, pl, pt.

## 6. Tieu chi vao/ra
### Entry criteria
- Moi truong test san sang.
- Test data da seed.
- Build pass va app truy cap duoc.

### Exit criteria
- Tat ca test critical da chay xong.
- Khong con defect Priority 1/2 mo.
- Bao cao coverage dat nguong.

## 7. Ke hoach theo sprint
1. Sprint 1: Setup framework + smoke localization.
2. Sprint 2: Black-box full matrix date/currency/overflow.
3. Sprint 3: White-box + branch/path cho formatter va strategy.
4. Sprint 4: Automation regression + coverage dashboard.

## 8. Risk va giai phap
- Risk: Locale data khong dong nhat.
  Mitigation: Chuan hoa test data by locale.
- Risk: Flaky test do UI async.
  Mitigation: Explicit wait + retry co kiem soat.
- Risk: RTL bug kho tai lap.
  Mitigation: Bat buoc screenshot + DOM evidence.
