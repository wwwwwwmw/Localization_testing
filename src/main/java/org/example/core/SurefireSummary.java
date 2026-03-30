package org.example.core;

/**
 * Tong hop ket qua tu surefire XML.
 */
public class SurefireSummary {
    public int tests;
    public int failures;
    public int errors;
    public int skipped;

    public int executed() {
        return Math.max(0, tests - skipped);
    }

    public int passed() {
        return Math.max(0, tests - failures - errors - skipped);
    }
}
