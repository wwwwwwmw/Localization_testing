package org.example.tests;

import org.example.core.L10nError;
import org.example.strategies.ILocaleStrategy;
import org.example.strategies.LocaleStrategyProvider.SupportedLocale;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Skeleton regression suite, map truc tiep voi test-case-catalog.md.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Automation Regression Skeleton - Localization")
public class LocalizationAutomationRegressionSkeletonTest extends BaseTest {

    private String currentLanguage;

    private void ensureLanguage(ILocaleStrategy strategy) {
        String code = strategy.getLanguageCode();
        String actualUiCode = page.getCurrentLanguageCode();
        boolean needSwitch = !code.equals(currentLanguage) || !code.equals(actualUiCode);
        if (needSwitch) {
            boolean ok = page.switchLanguage(code);
            assertTrue(ok, "Cannot switch language: " + code);
            assertTrue(code.equals(page.getCurrentLanguageCode()),
                    "UI language mismatch after switch: expected=" + code + ", actual="
                            + page.getCurrentLanguageCode());
            currentLanguage = code;
        }
    }

    @Order(1)
    @DisplayName("REG_A_TRANSLATION")
    @ParameterizedTest(name = "[{0}] Translation core")
    @EnumSource(SupportedLocale.class)
    void regTranslationCore(SupportedLocale locale) {
        ILocaleStrategy strategy = locale.getStrategy();
        ensureLanguage(strategy);

        List<L10nError> errors = validator.validateUntranslatedText(page, strategy);
        assertNoErrors(errors, "Translation core " + strategy.getLanguageName());
    }

    @Order(2)
    @DisplayName("REG_A_DATE")
    @ParameterizedTest(name = "[{0}] Date core")
    @EnumSource(SupportedLocale.class)
    void regDateCore(SupportedLocale locale) {
        ILocaleStrategy strategy = locale.getStrategy();
        ensureLanguage(strategy);

        List<L10nError> errors = validator.validateDateFormat(page, strategy);
        Assumptions.assumeTrue(errors != null, "No date samples found");
        assertNoErrors(errors, "Date core " + strategy.getLanguageName());
    }

    @Order(3)
    @DisplayName("REG_A_CURRENCY")
    @ParameterizedTest(name = "[{0}] Currency core")
    @EnumSource(SupportedLocale.class)
    void regCurrencyCore(SupportedLocale locale) {
        ILocaleStrategy strategy = locale.getStrategy();
        ensureLanguage(strategy);

        List<L10nError> errors = validator.validateCurrency(page, strategy);
        assertNoErrors(errors, "Currency core " + strategy.getLanguageName());
    }

    @Order(4)
    @DisplayName("REG_A_OVERFLOW")
    @ParameterizedTest(name = "[{0}] Overflow core")
    @EnumSource(SupportedLocale.class)
    void regOverflowCore(SupportedLocale locale) {
        ILocaleStrategy strategy = locale.getStrategy();
        ensureLanguage(strategy);

        List<L10nError> errors = validator.validateTextOverflow(page, strategy);
        assertNoErrors(errors, "Overflow core " + strategy.getLanguageName());
    }

    @Order(5)
    @DisplayName("REG_A_DIRECTION")
    @ParameterizedTest(name = "[{0}] Direction core")
    @EnumSource(SupportedLocale.class)
    void regDirectionCore(SupportedLocale locale) {
        ILocaleStrategy strategy = locale.getStrategy();
        ensureLanguage(strategy);

        List<L10nError> errors = validator.validateLayoutDirection(page, strategy);
        assertNoErrors(errors, "Direction core " + strategy.getLanguageName());
    }
}
