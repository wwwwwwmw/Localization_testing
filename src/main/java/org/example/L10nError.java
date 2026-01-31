package org.example;

/**
 * Class luu thong tin loi L10n
 */
public class L10nError {
    public String type;
    public String title;
    public String description;
    public String pageUrl;
    public String screenshotPath;

    public L10nError(String type, String title, String description, String pageUrl) {
        this.type = type;
        this.title = title;
        this.description = description;
        this.pageUrl = pageUrl;
    }

    @Override
    public String toString() {
        return String.format("[%s] %s: %s (URL: %s)", type, title, description, pageUrl);
    }
}
