# Advanced Test Taxonomy - Maximum Coverage

Tai lieu nay tong hop nhieu loai test de bao phu toan ven de tai localization.

## 1. Functional localization tests
- Text translation accuracy
- Placeholder localization
- Validation message localization
- Dynamic content localization
- Email/template localization

Test name mau:
1. FX_L10N_01_StaticText_Translated
2. FX_L10N_02_Placeholder_Translated
3. FX_L10N_03_FormError_Translated
4. FX_L10N_04_DynamicBanner_Translated
5. FX_L10N_05_Notification_Translated

## 2. Date/time tests
- Locale pattern validation
- Timezone display behavior
- Relative time phrase localization
- 12h/24h format consistency

Test name mau:
1. FX_DATE_01_PatternByLocale
2. FX_DATE_02_TimezoneConversion
3. FX_DATE_03_RelativeTimeLocalization
4. FX_DATE_04_24hFormatRules

## 3. Currency and number tests
- Symbol correctness
- Position correctness
- Separator correctness
- Rounding correctness
- Very large number display

Test name mau:
1. FX_CUR_01_SymbolCorrectness
2. FX_CUR_02_PositionCorrectness
3. FX_CUR_03_SeparatorCorrectness
4. FX_CUR_04_RoundingPolicy
5. FX_CUR_05_LargeAmountFormat

## 4. Layout and rendering tests
- Long text overflow
- Multi-line wrapping
- Element overlap
- Truncation with ellipsis
- Responsive localization

Test name mau:
1. UI_LYT_01_LongTextCardOverflow
2. UI_LYT_02_WrapPolicyButtons
3. UI_LYT_03_NoOverlapInHeader
4. UI_LYT_04_EllipsisBehavior
5. UI_LYT_05_MobileLocaleLayout

## 5. RTL/LTR tests
- Direction attribute
- Input caret and alignment
- Icon mirroring
- Mixed-script rendering

Test name mau:
1. UI_RTL_01_DocumentDirectionArabic
2. UI_RTL_02_InputAlignmentArabic
3. UI_RTL_03_DirectionalIconMirroring
4. UI_RTL_04_MixedScriptStability

## 6. Encoding and charset tests
- UTF-8 integrity
- Mojibake detection
- Diacritics preservation
- Copy/paste integrity

Test name mau:
1. ENC_01_UTF8Integrity
2. ENC_02_NoMojibake
3. ENC_03_DiacriticPreservation
4. ENC_04_CopyPasteIntegrity

## 7. Compatibility tests
- Browser compatibility by locale
- Viewport matrix
- OS font rendering differences

Test name mau:
1. CMP_01_Chrome_AllLocaleSmoke
2. CMP_02_Firefox_CoreLocale
3. CMP_03_Edge_RTLVerification
4. CMP_04_ViewportMatrixLocalization

## 8. Accessibility tests with localization
- Screen reader language metadata
- Contrast after locale switch
- Keyboard navigation with RTL

Test name mau:
1. A11Y_01_LangAttributePerLocale
2. A11Y_02_ContrastNotBrokenByLocale
3. A11Y_03_KeyboardFlowInRTL

## 9. Security-adjacent localization tests
- XSS in translated strings
- Unsafe interpolation in locale templates
- HTML escaping in dynamic localized message

Test name mau:
1. SEC_L10N_01_TranslatedStringEscaping
2. SEC_L10N_02_TemplateInterpolationSafe
3. SEC_L10N_03_NoScriptInjectionViaLocale

## 10. Performance and resilience tests
- Locale switch latency
- Bulk render with long texts
- Retry/recovery when locale resource fail

Test name mau:
1. PERF_L10N_01_SwitchLatencyThreshold
2. PERF_L10N_02_LongTextRenderCost
3. REL_L10N_01_ResourceLoadFailureFallback
4. REL_L10N_02_RetryPolicyValidation

## 11. Data integrity tests
- API and UI consistency
- Persisted locale preference
- Correct rehydrate after refresh

Test name mau:
1. DATA_L10N_01_UIvsAPIConsistency
2. DATA_L10N_02_PersistLocalePreference
3. DATA_L10N_03_RefreshRehydrateLocale

## 12. Exploratory charters
- Charter 1: Tim text khong dich trong flow mua hang
- Charter 2: Tim vo layout voi string dai tieng Duc
- Charter 3: Tim loi huong bo cuc voi Arabic RTL

## 13. Regression suite design
- Smoke suite: test quan trong nhanh
- Core suite: date/currency/overflow/rtl
- Full suite: toan bo matrix locale x viewport x domain

## 14. Muc tieu bao phu de tai
- Co it nhat 1 test tu dong cho moi domain chinh.
- Moi locale nhom A co du smoke + core.
- Moi bug critical da co test hoi quy tuong ung.
