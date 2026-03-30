# Test Case Catalog - Localization

Tai lieu nay liet ke test case theo nhom, voi ten test cu the de dua vao automation hoac manual checklist.

## A. Translation correctness
1. TC_TR_01_Header_Menu_Translated_AllLocales
2. TC_TR_02_Button_Primary_Translated_AllLocales
3. TC_TR_03_ErrorMessage_Translated_OnValidation
4. TC_TR_04_MissingKey_FallbackBehavior
5. TC_TR_05_UntranslatedEnglishWords_NotPresent_FR_VI_AR

## B. Date and time formatting
1. TC_DT_01_DatePattern_English_MMddyyyy
2. TC_DT_02_DatePattern_French_ddMMyyyy
3. TC_DT_03_DatePattern_Vietnamese_ddMMyyyy
4. TC_DT_04_DatePattern_Arabic_ddMMyyyy
5. TC_DT_05_MonthName_Localized_Display
6. TC_DT_06_InvalidDate_InputHandling

## C. Currency formatting
1. TC_CUR_01_Symbol_English
2. TC_CUR_02_Symbol_French
3. TC_CUR_03_Symbol_Vietnamese
4. TC_CUR_04_Symbol_Arabic
5. TC_CUR_05_ThousandsSeparator_PerLocale
6. TC_CUR_06_DecimalSeparator_PerLocale
7. TC_CUR_07_SymbolPosition_PrefixSuffix
8. TC_CUR_08_NegativeAmount_Format
9. TC_CUR_09_ZeroAmount_Format

## D. Long text and overflow
1. TC_OVF_01_ProductName_Overflow_Card
2. TC_OVF_02_ButtonLabel_LongText_WrapOrEllipsis
3. TC_OVF_03_FormError_LongSentence_NotOverlap
4. TC_OVF_04_TableCell_LongWord_BreakWord
5. TC_OVF_05_MobileViewport_LongText_Stability

## E. Direction and layout
1. TC_RTL_01_PageDirection_Arabic_RTL
2. TC_RTL_02_IconDirection_Mirrored_Arabic
3. TC_RTL_03_InputAlignment_Arabic
4. TC_RTL_04_MixContent_ArabicLatin_Stable
5. TC_RTL_05_LTRLocales_NotImpacted

## F. Locale switching and persistence
1. TC_SW_01_Switch_EN_to_FR_ImmediateUpdate
2. TC_SW_02_Switch_FR_to_AR_LayoutDirectionChanged
3. TC_SW_03_Switch_AR_to_VI_NoResidualRTL
4. TC_SW_04_RefreshPage_PreserveLocale
5. TC_SW_05_MultiSwitch_Stress_10Rounds

## G. Encoding and character set
1. TC_ENC_01_Vietnamese_Diacritics_RenderCorrectly
2. TC_ENC_02_French_Accents_RenderCorrectly
3. TC_ENC_03_Arabic_Glyphs_RenderCorrectly
4. TC_ENC_04_NoMojibake_Patterns
5. TC_ENC_05_CopyPaste_RoundTripIntegrity

## H. API and data consistency (neu co API)
1. TC_API_01_LocaleParameter_Accepted
2. TC_API_02_ResponseLocalizedFields
3. TC_API_03_DateCurrencyConsistency_UIvsAPI
4. TC_API_04_FallbackOnUnsupportedLocale

## I. Regression packs
- REG_SMOKE_A: TC_TR_01, TC_DT_01, TC_CUR_01, TC_OVF_01, TC_RTL_01
- REG_CORE_B: Toan bo A + C + E + F
- REG_FULL_C: Chay toan bo danh muc.

## Mau dinh dang test case chi tiet
- Test ID: TC_CUR_06
- Test Name: DecimalSeparator_PerLocale
- Preconditions: User da chon locale
- Steps: Mo page co gia tien -> ghi nhan hien thi
- Expected: Separator dung theo locale profile
- Evidence: screenshot + DOM text
- Priority: High
