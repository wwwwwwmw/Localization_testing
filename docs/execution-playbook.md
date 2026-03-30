# Execution Playbook

## 1. Chuan bi truoc khi chay test
- Xac nhan app endpoint san sang.
- Xac nhan locale danh sach muc tieu.
- Lam sach evidence cu neu can.

## 2. Trinh tu chay de xuat
1. Smoke test nhom locale A.
2. Black-box regression date/currency/overflow.
3. White-box unit + branch/path.
4. Full automation nightly.
5. Tong hop bao cao va tinh OCI.

## 3. Rule thu thap evidence
- Moi case fail can co:
  - Screenshot
  - Gia tri expected/actual
  - Locale + viewport + timestamp

## 4. Rule xu ly defect
- P1: Loi blocker checkout/data sai nghiem trong
- P2: Loi hinh anh/hien thi gay sai nghia
- P3: Loi nho khong chan flow chinh

## 5. Mau bao cao ngay
- Tong so case da chay
- Pass/Fail/Blocked
- Top defect theo domain
- OCI va xu huong tang/giam

## 6. Gate truoc release
- Khong con defect P1/P2 mo
- OCI >= nguong muc tieu
- Regression core pass 100%
