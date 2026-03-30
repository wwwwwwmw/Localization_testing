# Test Architecture - Localization

## 1. Tong quan kien truc
Kien truc test duoc to chuc theo 4 lop:
1. Test Orchestration Layer: quan ly suite, tag, run profile.
2. Scenario Layer: test case theo locale va theo domain.
3. Validation Layer: assert cho text/date/currency/layout.
4. Data Layer: locale profiles, expected dictionary, golden rules.

## 2. Thanh phan chinh
- Locale Strategy: xac dinh quy tac rieng cho tung locale.
- Page Objects: thao tac UI va lay du lieu hien thi.
- Validators:
  - TranslationValidator
  - DateFormatValidator
  - CurrencyFormatValidator
  - LongTextOverflowValidator
  - DirectionValidator (LTR/RTL)
- Evidence Collector: screenshot, html snippet, console log.

## 3. Kieu test trong kien truc
- Unit:
  - test parser/formatter theo tung locale.
  - test mapping tu locale sang pattern date/currency.
- Integration:
  - test luong switch locale -> render -> validate.
- E2E:
  - test user journey day du qua cac ngon ngu.

## 4. Rule thiet ke test
- Moi test phai gan locale, domain, risk level.
- Moi defect phai co evidence (anh + locator + expected/actual).
- Test data phai co bien the ngan, vua, dai cho cung 1 field.

## 5. Tich hop bao cao
- Bao cao theo 3 truc:
  - Theo locale
  - Theo domain (translation/date/currency/overflow/rtl)
  - Theo severity
- Bao cao tong hop su dung chi so trong coverage-formula.md.
