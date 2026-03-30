# Coverage Formula and Algorithm

Tai lieu nay cung cap mo hinh tinh do bao phu cho de tai localization testing.

## 1. Dinh nghia truc bao phu
Do bao phu tong duoc tinh theo 6 truc:
- Locale coverage (L)
- Feature coverage (F)
- Test method coverage (M)
- Risk coverage (R)
- Data boundary coverage (B)
- Automation coverage (A)

Gia tri moi truc nam trong [0, 1].

## 2. Cong thuc tong hop
Chi so bao phu tong hop (Overall Coverage Index - OCI):

OCI = wL*L + wF*F + wM*M + wR*R + wB*B + wA*A

De xuat trong so:
- wL = 0.20
- wF = 0.20
- wM = 0.15
- wR = 0.20
- wB = 0.10
- wA = 0.15

Tong trong so = 1.00

## 3. Cong thuc tung thanh phan
- L = so_locale_da_test / tong_locale_muc_tieu
- F = so_feature_localization_da_cover / tong_feature
- M = so_phuong_phap_da_ap_dung / tong_phuong_phap_muc_tieu
- R = tong_risk_weight_da_cover / tong_risk_weight
- B = so_boundary_case_da_cover / tong_boundary_case
- A = so_testcase_duoc_automation / tong_testcase_uu_tien_automation

## 4. Thuat toan tinh OCI
Input:
- localeTarget, localeCovered
- featureTarget, featureCovered
- methodTarget, methodCovered
- riskWeightTotal, riskWeightCovered
- boundaryTarget, boundaryCovered
- autoTarget, autoCovered

Algorithm:
1. Tinh L, F, M, R, B, A theo cong thuc thanh phan.
2. Clamp moi gia tri ve [0, 1].
3. Tinh OCI theo tong co trong so.
4. Phan loai:
   - OCI < 0.70: Chua dat
   - 0.70 <= OCI < 0.85: Dat co ban
   - 0.85 <= OCI < 0.95: Tot
   - OCI >= 0.95: Rat tot

## 5. Vi du tinh nhanh
Gia su:
- L=0.80, F=0.90, M=0.83, R=0.88, B=0.75, A=0.70

OCI = 0.20*0.80 + 0.20*0.90 + 0.15*0.83 + 0.20*0.88 + 0.10*0.75 + 0.15*0.70
OCI = 0.8205 ~ 82.05%

Ket luan: Dat co ban, can day manh boundary va automation.

## 6. KPI de theo doi hang sprint
- OCI sprint hien tai
- Delta OCI so voi sprint truoc
- Coverage theo domain: date/currency/overflow/rtl/encoding
- Ty le defect critical phat hien truoc UAT

## 7. Muc tieu de tai de xuat
- OCI >= 0.90 truoc release chinh thuc
- L (nhom A) = 1.00
- F >= 0.95
- A >= 0.85
