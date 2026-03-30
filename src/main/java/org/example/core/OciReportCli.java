package org.example.core;

import java.nio.file.Path;

/**
 * CLI tao OCI report tu file CSV.
 *
 * Cach dung:
 * java org.example.core.OciReportCli <csvInputPath> <outputDir> <runId>
 */
public final class OciReportCli {

    private OciReportCli() {
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 3) {
            System.out.println("Usage: OciReportCli <csvInputPath> <outputDir> <runId>");
            System.exit(1);
        }

        Path csvPath = Path.of(args[0]);
        Path outputDir = Path.of(args[1]);
        String runId = args[2];

        OciMetrics metrics = OciCsvParser.parseFirstDataRow(csvPath);
        Path report = OciReportWriter.writeReport(outputDir, runId, metrics);

        System.out.println("OCI report generated: " + report.toAbsolutePath());
        System.out.println("OCI = " + String.format("%.2f", metrics.oci() * 100.0) + "%");
        System.out.println("Rating = " + metrics.rating());
    }
}
