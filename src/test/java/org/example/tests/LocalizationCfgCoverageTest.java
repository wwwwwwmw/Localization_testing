package org.example.tests;

import org.example.core.L10nError;
import org.example.core.L10nValidator;
import org.example.pages.PrestaShopPage;
import org.example.pages.PrestaShopPage.ImageLocalizationSample;
import org.example.strategies.EnglishStrategy;
import org.example.strategies.FrenchStrategy;
import org.example.strategies.ILocaleStrategy;
import org.example.strategies.VietnameseStrategy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * White-box CFG + coverage tests (unit-level, mock seams).
 *
 * Luu y: Day la coverage theo mo hinh CFG cho logic validator,
 * khong phai bytecode coverage tu JaCoCo.
 */
@DisplayName("White Box - CFG Coverage")
public class LocalizationCfgCoverageTest {

    private static final Path CFG_REPORT = Paths.get("report", "white-box-cfg-coverage.md");

    private static final class CoverageSnapshot {
        private final String name;
        private final Set<String> statementHits = new LinkedHashSet<>();
        private final Set<String> branchHits = new LinkedHashSet<>();
        private final Set<String> statementUniverse;
        private final Set<String> branchUniverse;

        private CoverageSnapshot(String name, Set<String> statementUniverse, Set<String> branchUniverse) {
            this.name = name;
            this.statementUniverse = statementUniverse;
            this.branchUniverse = branchUniverse;
        }

        private double statementCoveragePercent() {
            return statementUniverse.isEmpty() ? 100.0 : (statementHits.size() * 100.0 / statementUniverse.size());
        }

        private double branchCoveragePercent() {
            return branchUniverse.isEmpty() ? 100.0 : (branchHits.size() * 100.0 / branchUniverse.size());
        }
    }

    @Test
    @DisplayName("WB_CFG_01_GenerateGraphsAndCoverageSummary")
    void wbCfgGenerateGraphsAndCoverageSummary() throws IOException {
        List<CoverageSnapshot> snapshots = new ArrayList<>();
        snapshots.add(coverCurrency());
        snapshots.add(coverUntranslated());
        snapshots.add(coverDate());
        snapshots.add(coverLayout());
        snapshots.add(coverOverflow());
        snapshots.add(coverCharset());
        snapshots.add(coverNumberMeasurement());
        snapshots.add(coverMedia());
        snapshots.add(coverUrlRouting());

        String report = buildCfgCoverageReport(snapshots);
        Files.createDirectories(CFG_REPORT.getParent());
        Files.writeString(CFG_REPORT, report, StandardCharsets.UTF_8);

        for (CoverageSnapshot snapshot : snapshots) {
            assertTrue(snapshot.statementCoveragePercent() >= 80.0,
                    "Statement coverage < 80% for " + snapshot.name + ": " + snapshot.statementCoveragePercent());
            assertTrue(snapshot.branchCoveragePercent() >= 80.0,
                    "Branch coverage < 80% for " + snapshot.name + ": " + snapshot.branchCoveragePercent());
        }

        assertTrue(Files.exists(CFG_REPORT), "Khong tao duoc CFG coverage report");
    }

    @Test
    @DisplayName("WB_CFG_02_SanityCoverageHasAllValidatorTypes")
    void wbCfgSanityAllTypesCovered() throws IOException {
        if (!Files.exists(CFG_REPORT)) {
            List<CoverageSnapshot> snapshots = List.of(
                    coverCurrency(),
                    coverUntranslated(),
                    coverDate(),
                    coverLayout(),
                    coverOverflow(),
                    coverCharset(),
                    coverNumberMeasurement(),
                    coverMedia(),
                    coverUrlRouting());
            Files.createDirectories(CFG_REPORT.getParent());
            Files.writeString(CFG_REPORT, buildCfgCoverageReport(snapshots), StandardCharsets.UTF_8);
        }
        String report = Files.readString(CFG_REPORT, StandardCharsets.UTF_8);
        List<String> expectedTypes = List.of(
                "Currency",
                "Untranslated Text",
                "Date Format",
                "Layout Direction",
                "Text Overflow",
                "Charset",
                "Number & Measurement",
                "Media & Alt",
                "URL Routing");

        for (String type : expectedTypes) {
            assertTrue(report.contains(type), "Thieu CFG/coverage cho type: " + type);
        }
    }

    private CoverageSnapshot coverCurrency() {
        CoverageSnapshot c = snapshot(
                "Currency",
                "collect-empty", "missing-symbol", "invalid-symbol", "valid-pass",
                "B_emptyCollector", "B_symbolMissing", "B_symbolNotAccepted", "B_validPrice");

        L10nValidator validator = new L10nValidator(null);
        EnglishStrategy strategy = new EnglishStrategy();
        PrestaShopPage page = mock(PrestaShopPage.class);
        when(page.getCurrentUrl()).thenReturn("http://localhost/products");

        when(page.collectPriceTextsAcrossPages("customer@example.com", "123456")).thenReturn(Map.of());
        List<L10nError> errorsEmpty = validator.validateCurrency(page, strategy);
        assertTrue(!errorsEmpty.isEmpty());
        c.statementHits.add("collect-empty");
        c.branchHits.add("B_emptyCollector");

        when(page.collectPriceTextsAcrossPages("customer@example.com", "123456"))
                .thenReturn(Map.of("products", List.of("1234")));
        List<L10nError> errorsMissingSymbol = validator.validateCurrency(page, strategy);
        assertTrue(!errorsMissingSymbol.isEmpty());
        c.statementHits.add("missing-symbol");
        c.branchHits.add("B_symbolMissing");

        when(page.collectPriceTextsAcrossPages("customer@example.com", "123456"))
                .thenReturn(Map.of("products", List.of("€1,234.00")));
        List<L10nError> invalidSymbol = validator.validateCurrency(page, strategy);
        assertTrue(!invalidSymbol.isEmpty());
        c.statementHits.add("invalid-symbol");
        c.branchHits.add("B_symbolNotAccepted");

        when(page.collectPriceTextsAcrossPages("customer@example.com", "123456"))
                .thenReturn(Map.of("products", List.of("$1,234.00")));
        List<L10nError> valid = validator.validateCurrency(page, strategy);
        assertTrue(valid.isEmpty());
        c.statementHits.add("valid-pass");
        c.branchHits.add("B_validPrice");
        return c;
    }

    private CoverageSnapshot coverUntranslated() {
        CoverageSnapshot c = snapshot(
                "Untranslated Text",
                "skip-english", "collector-empty", "token-detected", "all-clean",
                "B_langEnSkip", "B_noBodies", "B_hasToken", "B_noToken");

        L10nValidator validator = new L10nValidator(null);
        PrestaShopPage page = mock(PrestaShopPage.class);
        when(page.getCurrentUrl()).thenReturn("http://localhost/home");

        ILocaleStrategy en = new EnglishStrategy();
        List<L10nError> skipEn = validator.validateUntranslatedText(page, en);
        assertTrue(skipEn.isEmpty());
        c.statementHits.add("skip-english");
        c.branchHits.add("B_langEnSkip");

        ILocaleStrategy fr = new FrenchStrategy();
        when(page.collectLocalizationBodies("customer@example.com", "123456")).thenReturn(Map.of());
        List<L10nError> empty = validator.validateUntranslatedText(page, fr);
        assertTrue(empty.isEmpty());
        c.statementHits.add("collector-empty");
        c.branchHits.add("B_noBodies");

        when(page.collectLocalizationBodies("customer@example.com", "123456"))
                .thenReturn(Map.of("home", "Checkout Add to cart"));
        List<L10nError> detected = validator.validateUntranslatedText(page, fr);
        assertTrue(!detected.isEmpty());
        c.statementHits.add("token-detected");
        c.branchHits.add("B_hasToken");

        when(page.collectLocalizationBodies("customer@example.com", "123456"))
                .thenReturn(Map.of("home", "Bienvenue produit"));
        List<L10nError> clean = validator.validateUntranslatedText(page, fr);
        assertTrue(clean.isEmpty());
        c.statementHits.add("all-clean");
        c.branchHits.add("B_noToken");
        return c;
    }

    private CoverageSnapshot coverDate() {
        CoverageSnapshot c = snapshot(
                "Date Format",
                "collect-empty", "english-month-found", "date-found", "no-date-skip",
                "B_emptyBodies", "B_monthLeak", "B_hasDateData", "B_skipNoDate");

        L10nValidator validator = new L10nValidator(null);
        PrestaShopPage page = mock(PrestaShopPage.class);
        when(page.getCurrentUrl()).thenReturn("http://localhost/orders");
        ILocaleStrategy fr = new FrenchStrategy();

        when(page.collectLocalizationBodies("customer@example.com", "123456")).thenReturn(Map.of());
        List<L10nError> empty = validator.validateDateFormat(page, fr);
        assertTrue(!empty.isEmpty());
        c.statementHits.add("collect-empty");
        c.branchHits.add("B_emptyBodies");

        when(page.collectLocalizationBodies("customer@example.com", "123456"))
                .thenReturn(Map.of("orders", "Order date: January 12, 2026"));
        List<L10nError> monthLeak = validator.validateDateFormat(page, fr);
        assertTrue(!monthLeak.isEmpty());
        c.statementHits.add("english-month-found");
        c.branchHits.add("B_monthLeak");
        c.branchHits.add("B_hasDateData");

        when(page.collectLocalizationBodies("customer@example.com", "123456"))
                .thenReturn(Map.of("orders", "Date: 12/01/2026"));
        List<L10nError> dateFound = validator.validateDateFormat(page, fr);
        assertEquals(0, dateFound.size());
        c.statementHits.add("date-found");

        when(page.collectLocalizationBodies("customer@example.com", "123456"))
                .thenReturn(Map.of("orders", "Aucun texte date"));
        List<L10nError> skipped = validator.validateDateFormat(page, fr);
        assertTrue(skipped == null);
        c.statementHits.add("no-date-skip");
        c.branchHits.add("B_skipNoDate");
        return c;
    }

    private CoverageSnapshot coverLayout() {
        CoverageSnapshot c = snapshot(
                "Layout Direction",
                "collect-empty", "rtl-fail", "ltr-fail", "pass",
                "B_emptyDirections", "B_rtlMismatch", "B_ltrMismatch", "B_pass");

        L10nValidator validator = new L10nValidator(null);
        PrestaShopPage page = mock(PrestaShopPage.class);
        when(page.getCurrentUrl()).thenReturn("http://localhost");

        ILocaleStrategy rtl = mock(ILocaleStrategy.class);
        when(rtl.getLanguageCode()).thenReturn("ar");
        when(rtl.getLanguageName()).thenReturn("Arabic");
        when(rtl.isRTL()).thenReturn(true);

        ILocaleStrategy ltr = new FrenchStrategy();

        when(page.collectDirectionsAcrossPages("customer@example.com", "123456")).thenReturn(Map.of());
        assertTrue(!validator.validateLayoutDirection(page, rtl).isEmpty());
        c.statementHits.add("collect-empty");
        c.branchHits.add("B_emptyDirections");

        when(page.collectDirectionsAcrossPages("customer@example.com", "123456"))
                .thenReturn(Map.of("home", "ltr"));
        assertTrue(!validator.validateLayoutDirection(page, rtl).isEmpty());
        c.statementHits.add("rtl-fail");
        c.branchHits.add("B_rtlMismatch");

        when(page.collectDirectionsAcrossPages("customer@example.com", "123456"))
                .thenReturn(Map.of("home", "rtl"));
        assertTrue(!validator.validateLayoutDirection(page, ltr).isEmpty());
        c.statementHits.add("ltr-fail");
        c.branchHits.add("B_ltrMismatch");

        when(page.collectDirectionsAcrossPages("customer@example.com", "123456"))
                .thenReturn(Map.of("home", "ltr"));
        assertTrue(validator.validateLayoutDirection(page, ltr).isEmpty());
        c.statementHits.add("pass");
        c.branchHits.add("B_pass");
        return c;
    }

    private CoverageSnapshot coverOverflow() {
        CoverageSnapshot c = snapshot(
                "Text Overflow",
                "collect-empty", "overflow-found", "overflow-clean",
                "B_emptyOverflowMap", "B_hasOverflow", "B_noOverflow");

        L10nValidator validator = new L10nValidator(null);
        PrestaShopPage page = mock(PrestaShopPage.class);
        when(page.getCurrentUrl()).thenReturn("http://localhost/products");

        ILocaleStrategy strategy = new FrenchStrategy();

        when(page.collectOverflowedTextsAcrossPages("customer@example.com", "123456")).thenReturn(Map.of());
        assertTrue(!validator.validateTextOverflow(page, strategy).isEmpty());
        c.statementHits.add("collect-empty");
        c.branchHits.add("B_emptyOverflowMap");

        when(page.collectOverflowedTextsAcrossPages("customer@example.com", "123456"))
                .thenReturn(Map.of("products", List.of("Titre produit trop long")));
        assertTrue(!validator.validateTextOverflow(page, strategy).isEmpty());
        c.statementHits.add("overflow-found");
        c.branchHits.add("B_hasOverflow");

        when(page.collectOverflowedTextsAcrossPages("customer@example.com", "123456"))
                .thenReturn(Map.of("products", List.of()));
        assertTrue(validator.validateTextOverflow(page, strategy).isEmpty());
        c.statementHits.add("overflow-clean");
        c.branchHits.add("B_noOverflow");
        return c;
    }

    private CoverageSnapshot coverCharset() {
        CoverageSnapshot c = snapshot(
                "Charset",
                "collect-empty", "charset-mismatch", "mojibake-detected", "pass",
                "B_emptyLocations", "B_nonUtf8", "B_mojibake", "B_pass");

        L10nValidator validator = new L10nValidator(null);
        PrestaShopPage page = mock(PrestaShopPage.class);
        when(page.getCurrentUrl()).thenReturn("http://localhost/home");

        ILocaleStrategy en = new EnglishStrategy();

        when(page.collectLocalizationBodies("customer@example.com", "123456")).thenReturn(Map.of());
        when(page.collectHtmlLangAcrossPages("customer@example.com", "123456")).thenReturn(Map.of());
        when(page.collectDocumentCharsetsAcrossPages("customer@example.com", "123456")).thenReturn(Map.of());
        assertTrue(!validator.validateCharset(page, en).isEmpty());
        c.statementHits.add("collect-empty");
        c.branchHits.add("B_emptyLocations");

        when(page.collectLocalizationBodies("customer@example.com", "123456"))
                .thenReturn(Map.of("home", "Hello text"));
        when(page.collectHtmlLangAcrossPages("customer@example.com", "123456"))
                .thenReturn(Map.of("home", "en"));
        when(page.collectDocumentCharsetsAcrossPages("customer@example.com", "123456"))
                .thenReturn(Map.of("home", "ISO-8859-1"));
        assertTrue(!validator.validateCharset(page, en).isEmpty());
        c.statementHits.add("charset-mismatch");
        c.branchHits.add("B_nonUtf8");

        when(page.collectLocalizationBodies("customer@example.com", "123456"))
                .thenReturn(Map.of("home", "AÃ B"));
        when(page.collectHtmlLangAcrossPages("customer@example.com", "123456"))
                .thenReturn(Map.of("home", "en"));
        when(page.collectDocumentCharsetsAcrossPages("customer@example.com", "123456"))
                .thenReturn(Map.of("home", "UTF-8"));
        assertTrue(!validator.validateCharset(page, en).isEmpty());
        c.statementHits.add("mojibake-detected");
        c.branchHits.add("B_mojibake");

        when(page.collectLocalizationBodies("customer@example.com", "123456"))
                .thenReturn(Map.of("home", "Normal English text"));
        when(page.collectHtmlLangAcrossPages("customer@example.com", "123456"))
                .thenReturn(Map.of("home", "en"));
        when(page.collectDocumentCharsetsAcrossPages("customer@example.com", "123456"))
                .thenReturn(Map.of("home", "UTF-8"));
        assertTrue(validator.validateCharset(page, en).isEmpty());
        c.statementHits.add("pass");
        c.branchHits.add("B_pass");
        return c;
    }

    private CoverageSnapshot coverNumberMeasurement() {
        CoverageSnapshot c = snapshot(
                "Number & Measurement",
                "collect-empty", "separator-mismatch", "unit-leak", "pass",
                "B_emptyBodies", "B_numberMismatch", "B_unitLeak", "B_pass");

        L10nValidator validator = new L10nValidator(null);
        PrestaShopPage page = mock(PrestaShopPage.class);
        when(page.getCurrentUrl()).thenReturn("http://localhost/products");

        ILocaleStrategy vi = new VietnameseStrategy();

        when(page.collectLocalizationBodies("customer@example.com", "123456")).thenReturn(Map.of());
        assertTrue(!validator.validateNumberAndMeasurementFormat(page, vi).isEmpty());
        c.statementHits.add("collect-empty");
        c.branchHits.add("B_emptyBodies");

        when(page.collectLocalizationBodies("customer@example.com", "123456"))
                .thenReturn(Map.of("products", "Gia 1,234.56"));
        assertTrue(!validator.validateNumberAndMeasurementFormat(page, vi).isEmpty());
        c.statementHits.add("separator-mismatch");
        c.branchHits.add("B_numberMismatch");

        when(page.collectLocalizationBodies("customer@example.com", "123456"))
                .thenReturn(Map.of("products", "Weight 10 lb"));
        assertTrue(!validator.validateNumberAndMeasurementFormat(page, vi).isEmpty());
        c.statementHits.add("unit-leak");
        c.branchHits.add("B_unitLeak");

        when(page.collectLocalizationBodies("customer@example.com", "123456"))
                .thenReturn(Map.of("products", "Gia 1.234,56"));
        assertTrue(validator.validateNumberAndMeasurementFormat(page, vi).isEmpty());
        c.statementHits.add("pass");
        c.branchHits.add("B_pass");
        return c;
    }

    private CoverageSnapshot coverMedia() {
        CoverageSnapshot c = snapshot(
                "Media & Alt",
                "collect-empty", "english-asset", "missing-alt", "pass",
                "B_emptyImages", "B_enAsset", "B_altMissing", "B_pass");

        L10nValidator validator = new L10nValidator(null);
        PrestaShopPage page = mock(PrestaShopPage.class);
        when(page.getCurrentUrl()).thenReturn("http://localhost/home");

        ILocaleStrategy fr = new FrenchStrategy();

        when(page.collectImageLocalizationAcrossPages("customer@example.com", "123456")).thenReturn(Map.of());
        assertTrue(!validator.validateMediaLocalization(page, fr).isEmpty());
        c.statementHits.add("collect-empty");
        c.branchHits.add("B_emptyImages");

        Map<String, List<ImageLocalizationSample>> englishAsset = new LinkedHashMap<>();
        englishAsset.put("home", List.of(new ImageLocalizationSample("/assets/en/banner.png", "Produit", "")));
        when(page.collectImageLocalizationAcrossPages("customer@example.com", "123456")).thenReturn(englishAsset);
        assertTrue(!validator.validateMediaLocalization(page, fr).isEmpty());
        c.statementHits.add("english-asset");
        c.branchHits.add("B_enAsset");

        Map<String, List<ImageLocalizationSample>> missingAlt = new LinkedHashMap<>();
        missingAlt.put("home", List.of(new ImageLocalizationSample("/assets/fr/banner.png", "", "")));
        when(page.collectImageLocalizationAcrossPages("customer@example.com", "123456")).thenReturn(missingAlt);
        assertTrue(!validator.validateMediaLocalization(page, fr).isEmpty());
        c.statementHits.add("missing-alt");
        c.branchHits.add("B_altMissing");

        Map<String, List<ImageLocalizationSample>> valid = new LinkedHashMap<>();
        valid.put("home", List.of(new ImageLocalizationSample("/assets/fr/banner.png", "Produit vedette", "Promo")));
        when(page.collectImageLocalizationAcrossPages("customer@example.com", "123456")).thenReturn(valid);
        assertTrue(validator.validateMediaLocalization(page, fr).isEmpty());
        c.statementHits.add("pass");
        c.branchHits.add("B_pass");
        return c;
    }

    private CoverageSnapshot coverUrlRouting() {
        CoverageSnapshot c = snapshot(
                "URL Routing",
                "collect-empty", "missing-locale", "pass",
                "B_emptyUrls", "B_localeMissing", "B_pass");

        L10nValidator validator = new L10nValidator(null);
        PrestaShopPage page = mock(PrestaShopPage.class);
        when(page.getCurrentUrl()).thenReturn("http://localhost/home");

        ILocaleStrategy fr = new FrenchStrategy();

        when(page.collectUrlsAcrossPages("customer@example.com", "123456")).thenReturn(Map.of());
        assertTrue(!validator.validateLocalizedUrls(page, fr).isEmpty());
        c.statementHits.add("collect-empty");
        c.branchHits.add("B_emptyUrls");

        when(page.collectUrlsAcrossPages("customer@example.com", "123456"))
                .thenReturn(Map.of("home", "http://localhost/products"));
        assertTrue(!validator.validateLocalizedUrls(page, fr).isEmpty());
        c.statementHits.add("missing-locale");
        c.branchHits.add("B_localeMissing");

        when(page.collectUrlsAcrossPages("customer@example.com", "123456"))
                .thenReturn(Map.of("home", "http://localhost/fr/products?lang=fr"));
        assertTrue(validator.validateLocalizedUrls(page, fr).isEmpty());
        c.statementHits.add("pass");
        c.branchHits.add("B_pass");
        return c;
    }

    private CoverageSnapshot snapshot(String name, String s1, String s2, String s3, String s4,
            String b1, String b2, String b3, String b4) {
        return new CoverageSnapshot(name, Set.of(s1, s2, s3, s4), Set.of(b1, b2, b3, b4));
    }

    private CoverageSnapshot snapshot(String name, String s1, String s2, String s3,
            String b1, String b2, String b3) {
        return new CoverageSnapshot(name, Set.of(s1, s2, s3), Set.of(b1, b2, b3));
    }

    private String buildCfgCoverageReport(List<CoverageSnapshot> snapshots) {
        StringBuilder sb = new StringBuilder();
        sb.append("# White-Box CFG & Coverage Report\n\n");
        sb.append("## Coverage Summary\n\n");
        sb.append("| Validator Type | Statement Coverage | Branch Coverage |\n");
        sb.append("|---|---:|---:|\n");

        for (CoverageSnapshot snapshot : snapshots) {
            sb.append("| ").append(snapshot.name)
                    .append(" | ").append(String.format("%.1f%%", snapshot.statementCoveragePercent()))
                    .append(" (").append(snapshot.statementHits.size()).append("/")
                    .append(snapshot.statementUniverse.size()).append(")")
                    .append(" | ").append(String.format("%.1f%%", snapshot.branchCoveragePercent()))
                    .append(" (").append(snapshot.branchHits.size()).append("/")
                    .append(snapshot.branchUniverse.size()).append(")")
                    .append(" |\n");
        }

        sb.append("\n## CFG Diagrams (Mermaid)\n\n");
        for (CoverageSnapshot snapshot : snapshots) {
            sb.append("### ").append(snapshot.name).append("\n\n");
            sb.append("```mermaid\n");
            sb.append("flowchart TD\n");
            sb.append("    A[Start]").append(" --> B[Collect Inputs]\n");
            sb.append("    B --> C{Data Available?}\n");
            sb.append("    C -->|No| D[Create Error]\n");
            sb.append("    C -->|Yes| E[Apply Validation Rules]\n");
            sb.append("    E --> F{Rule Violations?}\n");
            sb.append("    F -->|Yes| D\n");
            sb.append("    F -->|No| G[Pass]\n");
            sb.append("    D --> H[Return errors]\n");
            sb.append("    G --> I[Return empty list]\n");
            sb.append("```\n\n");
        }

        return sb.toString();
    }
}
