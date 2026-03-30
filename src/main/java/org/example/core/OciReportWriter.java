package org.example.core;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Tao file Markdown report tu du lieu OCI.
 */
public final class OciReportWriter {

    private OciReportWriter() {
    }

    public static Path writeReport(Path outputDir, String runId, OciMetrics metrics) throws IOException {
        Files.createDirectories(outputDir);
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        Path reportPath = outputDir.resolve("oci-report-" + runId + "-" + timestamp + ".md");

        String content = toMarkdown(runId, metrics);
        Files.writeString(reportPath, content, StandardCharsets.UTF_8);
        return reportPath;
    }

    private static String toMarkdown(String runId, OciMetrics m) {
        double ociPercent = m.oci() * 100.0;
        return "# OCI Auto Report\n\n"
                + "- Run ID: " + runId + "\n"
                + "- Generated At: " + LocalDateTime.now() + "\n\n"
                + "## Component Scores\n"
                + "- L: " + pct(m.l()) + "\n"
                + "- F: " + pct(m.f()) + "\n"
                + "- M: " + pct(m.m()) + "\n"
                + "- R: " + pct(m.r()) + "\n"
                + "- B: " + pct(m.b()) + "\n"
                + "- A: " + pct(m.a()) + "\n\n"
                + "## OCI\n"
                + "- OCI: " + String.format("%.2f", ociPercent) + "%\n"
                + "- Rating: " + m.rating() + "\n\n"
                + "## Raw Inputs\n"
                + "- localeCovered/localeTarget: " + m.localeCovered + "/" + m.localeTarget + "\n"
                + "- featureCovered/featureTarget: " + m.featureCovered + "/" + m.featureTarget + "\n"
                + "- methodCovered/methodTarget: " + m.methodCovered + "/" + m.methodTarget + "\n"
                + "- riskWeightCovered/riskWeightTotal: " + m.riskWeightCovered + "/" + m.riskWeightTotal + "\n"
                + "- boundaryCovered/boundaryTarget: " + m.boundaryCovered + "/" + m.boundaryTarget + "\n"
                + "- autoCovered/autoTarget: " + m.autoCovered + "/" + m.autoTarget + "\n";
    }

    private static String pct(double value) {
        return String.format("%.2f%%", value * 100.0);
    }
}
