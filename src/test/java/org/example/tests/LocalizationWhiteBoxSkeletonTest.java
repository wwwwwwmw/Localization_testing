package org.example.tests;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * White-box tests theo docs/white-box-methods.md.
 */
@DisplayName("White Box - Localization")
public class LocalizationWhiteBoxSkeletonTest {

    private static final Path PROJECT_ROOT = Paths.get("").toAbsolutePath();
    private static final String SCAN_ROOT_PROP = "l10n.scan.root";
    private static final Pattern HARDCODED_UI_TEXT_PATTERN = Pattern.compile(
            "(>\\s*[A-Za-z][^<]{2,}\\s*<)|([\"'](?:Add to cart|Checkout|Search|Sign in|My account)[\"'])",
            Pattern.CASE_INSENSITIVE);
    private static final Pattern SQL_ENCODING_PATTERN = Pattern.compile(
            "(utf8mb4|utf8|nvarchar|nchar|collate)", Pattern.CASE_INSENSITIVE);
    private static final Pattern JSON_KEY_PATTERN = Pattern.compile("\"([^\"]+)\"\\s*:");

    @Test
    @DisplayName("WB_STATIC_01_FindHardcodedUiStrings")
    void wbStaticCodeAnalysisHardcodedStrings() throws IOException {
        List<Path> sourceFiles = collectFilesFromScanRoots(this::isUiMarkupFile);

        Assumptions.assumeTrue(!sourceFiles.isEmpty(),
                "Khong tim thay UI markup file. Hay set -D" + SCAN_ROOT_PROP + "=<duong_dan_project_app>");

        List<String> findings = new ArrayList<>();
        for (Path file : sourceFiles) {
            String content = Files.readString(file, StandardCharsets.UTF_8);
            Matcher matcher = HARDCODED_UI_TEXT_PATTERN.matcher(content);
            int hitCount = 0;
            while (matcher.find() && hitCount < 3) {
                hitCount++;
                findings.add(displayPath(file) + " -> " + matcher.group().trim());
            }
        }

        assertTrue(findings.isEmpty(),
                "Phat hien qua nhieu chuoi UI hardcoded, can thay bang key i18n. Mau loi: " + findings);
    }

    @Test
    @DisplayName("WB_RESOURCE_01_LocaleKeyConsistency")
    void wbResourceFileIntegrity() throws IOException {
        List<Path> localeFiles = collectFilesFromScanRoots(path -> {
            String lower = path.getFileName().toString().toLowerCase(Locale.ROOT);
            return lower.matches("(en|fr|vi|de|ja|ru|ar)\\.(json|properties)$")
                    || lower.matches("messages_(en|fr|vi|de|ja|ru|ar)\\.properties");
        }).stream().sorted(Comparator.comparing(Path::toString)).collect(Collectors.toList());

        Assumptions.assumeTrue(!localeFiles.isEmpty(),
                "Khong tim thay resource locale file. Hay set -D" + SCAN_ROOT_PROP + "=<duong_dan_project_app>");

        Path baseline = localeFiles.stream()
                .filter(path -> path.getFileName().toString().toLowerCase(Locale.ROOT).contains("en"))
                .findFirst()
                .orElse(localeFiles.get(0));

        List<String> baselineKeys = extractResourceKeys(baseline);
        Assumptions.assumeTrue(!baselineKeys.isEmpty(), "Baseline resource khong co key");

        for (Path file : localeFiles) {
            List<String> keys = extractResourceKeys(file);
            assertEquals(baselineKeys.size(), keys.size(),
                    "So luong key khong khop voi baseline: " + displayPath(file));

            String content = Files.readString(file, StandardCharsets.UTF_8);
            assertFalse(content.contains(": \"\""),
                    "Phat hien empty value trong file: " + displayPath(file));
        }
    }

    @Test
    @DisplayName("WB_DB_01_EncodingCollationHints")
    void wbDatabaseEncodingVerification() throws IOException {
        List<Path> sqlFiles = collectFilesFromScanRoots(
                path -> path.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".sql"));

        Assumptions.assumeTrue(!sqlFiles.isEmpty(),
                "Khong tim thay file SQL de kiem tra encoding. Hay set -D" + SCAN_ROOT_PROP
                        + "=<duong_dan_project_app>");

        int hits = 0;
        List<String> missingHints = new ArrayList<>();
        for (Path sql : sqlFiles) {
            String content = Files.readString(sql, StandardCharsets.UTF_8);
            if (SQL_ENCODING_PATTERN.matcher(content).find()) {
                hits++;
            } else {
                missingHints.add(displayPath(sql));
            }
        }

        assertTrue(hits > 0,
                "Khong thay dau hieu cau hinh encoding/collation trong SQL schema. File can xem lai: " + missingHints);
    }

    @Test
    @DisplayName("WB_RESOURCE_02_DuplicateKeyDetection")
    void wbDuplicateKeyDetection() throws IOException {
        List<Path> localeFiles = collectFilesFromScanRoots(
                path -> path.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".json"));

        Assumptions.assumeTrue(!localeFiles.isEmpty(),
                "Khong co JSON locale file de check duplicate key. Hay set -D" + SCAN_ROOT_PROP
                        + "=<duong_dan_project_app>");

        for (Path file : localeFiles) {
            String content = Files.readString(file, StandardCharsets.UTF_8);
            Matcher matcher = JSON_KEY_PATTERN.matcher(content);
            Map<String, Long> counts = matcher.results()
                    .map(m -> m.group(1))
                    .collect(Collectors.groupingBy(k -> k, Collectors.counting()));

            List<String> duplicated = counts.entrySet().stream()
                    .filter(e -> e.getValue() > 1)
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toList());

            assertTrue(duplicated.isEmpty(),
                    "Phat hien duplicate key trong " + displayPath(file) + ": " + duplicated);
        }
    }

    @Test
    @DisplayName("WB_DATAFLOW_01_AllowedWordsNotForbidden")
    void wbAllowedWordsDoNotConflictForbiddenList() throws IOException {
        Path strategiesDir = PROJECT_ROOT.resolve("src/main/java/org/example/strategies");
        Assumptions.assumeTrue(Files.exists(strategiesDir), "Khong tim thay thu muc strategy");

        List<Path> strategyFiles;
        try (Stream<Path> walk = Files.walk(strategiesDir)) {
            strategyFiles = walk
                    .filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().endsWith("Strategy.java"))
                    .collect(Collectors.toList());
        }

        for (Path strategyFile : strategyFiles) {
            String content = Files.readString(strategyFile, StandardCharsets.UTF_8);
            List<String> forbidden = extractQuotedWordsForMethod(content, "getForbiddenWords");
            List<String> allowed = extractQuotedWordsForMethod(content, "getAllowedEnglishWords");

            List<String> overlaps = allowed.stream()
                    .filter(word -> forbidden.stream().anyMatch(f -> f.equalsIgnoreCase(word)))
                    .collect(Collectors.toList());

            assertTrue(overlaps.isEmpty(),
                    "Tu allowed bi trung forbidden trong " + strategyFile.getFileName() + ": " + overlaps);
        }
    }

    private boolean isUiMarkupFile(Path path) {
        String lower = path.getFileName().toString().toLowerCase(Locale.ROOT);
        return lower.endsWith(".html") || lower.endsWith(".js") || lower.endsWith(".ts")
                || lower.endsWith(".tsx") || lower.endsWith(".jsx") || lower.endsWith(".vue");
    }

    private List<Path> collectFilesFromScanRoots(java.util.function.Predicate<Path> filter) throws IOException {
        List<Path> files = new ArrayList<>();
        for (Path root : resolveScanRoots()) {
            if (!Files.exists(root)) {
                continue;
            }
            try (Stream<Path> walk = Files.walk(root)) {
                files.addAll(walk
                        .filter(Files::isRegularFile)
                        .filter(path -> !isIgnoredPath(path))
                        .filter(filter)
                        .collect(Collectors.toList()));
            }
        }
        return files;
    }

    private List<Path> resolveScanRoots() {
        Set<Path> roots = new LinkedHashSet<>();

        String configuredRoot = System.getProperty(SCAN_ROOT_PROP);
        if (configuredRoot != null && !configuredRoot.isBlank()) {
            roots.add(Paths.get(configuredRoot).toAbsolutePath().normalize());
        }

        roots.add(PROJECT_ROOT.resolve("src").normalize());

        Path siblingShop = PROJECT_ROOT.resolve("..").resolve("localization-testing-shop").normalize();
        roots.add(siblingShop.resolve("frontend").resolve("src"));
        roots.add(siblingShop.resolve("frontend").resolve("public"));
        roots.add(siblingShop.resolve("backend"));

        return new ArrayList<>(roots);
    }

    private boolean isIgnoredPath(Path path) {
        String normalized = path.toString().replace('\\', '/').toLowerCase(Locale.ROOT);
        return normalized.contains("/node_modules/")
                || normalized.contains("/_upstream/")
                || normalized.contains("/target/")
                || normalized.contains("/.git/");
    }

    private String displayPath(Path path) {
        Path abs = path.toAbsolutePath().normalize();
        if (abs.startsWith(PROJECT_ROOT)) {
            return PROJECT_ROOT.relativize(abs).toString();
        }
        return abs.toString();
    }

    private List<String> extractResourceKeys(Path file) throws IOException {
        String lower = file.getFileName().toString().toLowerCase(Locale.ROOT);
        String content = Files.readString(file, StandardCharsets.UTF_8);
        List<String> keys = new ArrayList<>();

        if (lower.endsWith(".json")) {
            Matcher m = JSON_KEY_PATTERN.matcher(content);
            while (m.find()) {
                keys.add(m.group(1));
            }
            return keys;
        }

        if (lower.endsWith(".properties")) {
            for (String line : content.split("\\R")) {
                String trimmed = line.trim();
                if (trimmed.isEmpty() || trimmed.startsWith("#") || trimmed.startsWith("!")) {
                    continue;
                }
                int eq = trimmed.indexOf('=');
                int colon = trimmed.indexOf(':');
                int splitAt = eq >= 0 ? eq : colon;
                if (splitAt > 0) {
                    keys.add(trimmed.substring(0, splitAt).trim());
                }
            }
            return keys;
        }

        return keys;
    }

    private List<String> extractQuotedWordsForMethod(String content, String methodName) {
        int methodPos = content.indexOf(methodName);
        if (methodPos < 0) {
            return List.of();
        }
        int blockStart = content.indexOf("{", methodPos);
        int blockEnd = content.indexOf("}", blockStart);
        if (blockStart < 0 || blockEnd < 0) {
            return List.of();
        }

        String methodBody = content.substring(blockStart, blockEnd);
        Matcher matcher = Pattern.compile("\"([^\"]+)\"").matcher(methodBody);
        List<String> tokens = new ArrayList<>();
        while (matcher.find()) {
            tokens.add(matcher.group(1));
        }
        return tokens;
    }
}
