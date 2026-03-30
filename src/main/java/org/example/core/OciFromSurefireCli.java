package org.example.core;

import java.nio.file.Path;

/**
 * Sinh OCI report tu ket qua test run surefire.
 *
 * Usage:
 * OciFromSurefireCli <surefireDir> <outputDir> <runId>
 * <localeTarget> <localeCovered>
 * <featureTarget> <featureCovered>
 * <methodTarget> <methodCovered>
 * <riskWeightTotal> <riskWeightCovered>
 * <boundaryTarget> <boundaryCovered>
 * <autoTarget>
 *
 * autoCovered duoc tinh tu so testcase PASSED trong surefire XML.
 */
public final class OciFromSurefireCli {

    private OciFromSurefireCli() {
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 14) {
            System.out.println("Usage: OciFromSurefireCli <surefireDir> <outputDir> <runId>"
                    + " <localeTarget> <localeCovered>"
                    + " <featureTarget> <featureCovered>"
                    + " <methodTarget> <methodCovered>"
                    + " <riskWeightTotal> <riskWeightCovered>"
                    + " <boundaryTarget> <boundaryCovered>"
                    + " <autoTarget>");
            System.exit(1);
        }

        Path surefireDir = Path.of(args[0]);
        Path outputDir = Path.of(args[1]);
        String runId = args[2];

        double localeTarget = parse(args[3]);
        double localeCovered = parse(args[4]);
        double featureTarget = parse(args[5]);
        double featureCovered = parse(args[6]);
        double methodTarget = parse(args[7]);
        double methodCovered = parse(args[8]);
        double riskWeightTotal = parse(args[9]);
        double riskWeightCovered = parse(args[10]);
        double boundaryTarget = parse(args[11]);
        double boundaryCovered = parse(args[12]);
        double autoTarget = parse(args[13]);

        SurefireSummary summary = SurefireReportReader.read(surefireDir);
        double autoCovered = summary.passed();

        OciMetrics metrics = new OciMetrics(
                localeTarget,
                localeCovered,
                featureTarget,
                featureCovered,
                methodTarget,
                methodCovered,
                riskWeightTotal,
                riskWeightCovered,
                boundaryTarget,
                boundaryCovered,
                autoTarget,
                autoCovered);

        Path report = OciReportWriter.writeReport(outputDir, runId, metrics);

        System.out.println("Surefire summary: tests=" + summary.tests
                + ", passed=" + summary.passed()
                + ", failures=" + summary.failures
                + ", errors=" + summary.errors
                + ", skipped=" + summary.skipped);
        System.out.println("OCI report: " + report.toAbsolutePath());
        System.out.println("OCI: " + String.format("%.2f", metrics.oci() * 100.0) + "%");
    }

    private static double parse(String raw) {
        return Double.parseDouble(raw.trim());
    }
}
