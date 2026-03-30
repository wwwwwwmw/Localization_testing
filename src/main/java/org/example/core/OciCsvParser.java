package org.example.core;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Doc dong dau tien du lieu tu file CSV OCI input.
 */
public final class OciCsvParser {

    private OciCsvParser() {
    }

    public static OciMetrics parseFirstDataRow(Path csvPath) throws IOException {
        try (BufferedReader br = Files.newBufferedReader(csvPath, StandardCharsets.UTF_8)) {
            String header = br.readLine();
            String row = br.readLine();
            if (header == null || row == null) {
                throw new IOException("CSV khong du du lieu: " + csvPath);
            }

            String[] values = row.split(",");
            if (values.length < 13) {
                throw new IOException("CSV can toi thieu 13 cot: " + csvPath);
            }

            return new OciMetrics(
                    toDouble(values[1]),
                    toDouble(values[2]),
                    toDouble(values[3]),
                    toDouble(values[4]),
                    toDouble(values[5]),
                    toDouble(values[6]),
                    toDouble(values[7]),
                    toDouble(values[8]),
                    toDouble(values[9]),
                    toDouble(values[10]),
                    toDouble(values[11]),
                    toDouble(values[12]));
        }
    }

    private static double toDouble(String raw) {
        return Double.parseDouble(raw.trim());
    }
}
