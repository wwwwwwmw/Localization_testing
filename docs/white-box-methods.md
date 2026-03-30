# White-Box Testing Methods

Muc tieu white-box: bao phu logic ben trong formatter, validator, strategy va flow switch locale.

## 1. Statement Coverage
- Dam bao moi dong lenh trong formatter date/currency duoc thuc thi it nhat 1 lan.

## 2. Branch Coverage
Cac nhanh can test:
- Locale support vs locale fallback
- Separator dot/comma/space
- Currency prefix vs suffix
- RTL vs LTR
- Overflow detected vs no overflow

## 3. Condition Coverage
- Dieu kien ket hop (AND/OR) cho validator:
  - symbol match
  - separator match
  - decimal digits match

## 4. Path Coverage (muc uu tien)
Tap trung cac path quan trong:
1. Switch locale thanh cong ngay lan dau.
2. Switch locale retry 1-2 lan roi thanh cong.
3. Switch locale that bai -> fallback + report loi.

## 5. Loop Testing
- Vong lap quet text node tren page:
  - empty page
  - page nho
  - page lon nhieu node

## 6. Data Flow Testing
- Def-use chain cho bien localeCode, currencySymbol, datePattern.
- Kiem tra bien duoc gan dung truoc khi validate.

## 7. Mutation-oriented checks (de xuat)
- Dao nguoc dieu kien separator.
- Thay symbol expected sang symbol sai.
- Loai bo buoc trim/truncate.
Neu test khong fail -> bo test chua du manh.

## 8. Danh sach ten test white-box mau
1. WB_STMT_FMTDATE_01_AllStatements_English
2. WB_BRANCH_CUR_01_PrefixBranch
3. WB_BRANCH_CUR_02_SuffixBranch
4. WB_BRANCH_DIR_01_RTLBranch_Arabic
5. WB_COND_VAL_01_AllConditionsTrue
6. WB_COND_VAL_02_OneConditionFalse
7. WB_PATH_SWITCH_01_SuccessFirstTry
8. WB_PATH_SWITCH_02_RetryThenSuccess
9. WB_PATH_SWITCH_03_FallbackOnFailure
10. WB_DATAFLOW_01_LocaleCode_DefUse
