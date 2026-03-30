package org.example.core;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

/**
 * Doc cac file TEST-*.xml trong target/surefire-reports.
 */
public final class SurefireReportReader {

    private SurefireReportReader() {
    }

    public static SurefireSummary read(Path surefireDir) throws Exception {
        SurefireSummary summary = new SurefireSummary();

        if (!Files.exists(surefireDir)) {
            throw new IOException("Khong tim thay thu muc surefire: " + surefireDir);
        }

        try (Stream<Path> files = Files.list(surefireDir)) {
            files.filter(p -> p.getFileName().toString().startsWith("TEST-")
                    && p.getFileName().toString().endsWith(".xml"))
                    .forEach(path -> accumulate(path, summary));
        }

        return summary;
    }

    private static void accumulate(Path path, SurefireSummary summary) {
        try {
            Document doc = DocumentBuilderFactory.newInstance()
                    .newDocumentBuilder()
                    .parse(path.toFile());
            Element suite = doc.getDocumentElement();
            summary.tests += parseIntAttr(suite, "tests");
            summary.failures += parseIntAttr(suite, "failures");
            summary.errors += parseIntAttr(suite, "errors");
            summary.skipped += parseIntAttr(suite, "skipped");
        } catch (Exception ignored) {
            // Bo qua file hong de tiep tuc thong ke cac file khac.
        }
    }

    private static int parseIntAttr(Element element, String attr) {
        String raw = element.getAttribute(attr);
        if (raw == null || raw.isBlank()) {
            return 0;
        }
        try {
            return Integer.parseInt(raw.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
