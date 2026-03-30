# Test Software Roadmap (Based on Docs)

## Phase 1 - Foundation (Done)
- Co framework Selenium + JUnit
- Co strategy locale
- Co validator core (translation/date/currency/overflow/rtl/charset)
- Co docs chuan hoa methodology

## Phase 2 - Structured Automation (In progress)
- Tao skeleton test class theo black-box/white-box/regression
- Chuan hoa naming theo catalog
- Gan @Tag de chay theo domain

## Phase 3 - OCI and Reporting (In progress)
- Co OciMetrics, parser, writer, cli
- Co script run_oci_report.ps1
- Muc tieu tiep theo: map domain-level auto coverage tu testcase tags

## Phase 4 - Advanced Validation (Next)
- Them validator date/timezone
- Them validator icon mirroring RTL
- Them validator long-text theo viewport matrix

## Phase 5 - Full Regression Platform (Next)
- Suite smoke/core/full
- Nightly run + trend report
- Defect clustering by locale/domain

## Deliverables hien tai
- docs/gwt-high-priority-test-cases.md (60 cases)
- docs/templates/oci-report-template.md
- docs/templates/oci-input-template.csv
- src/main/java/org/example/core/Oci*.java
- src/test/java/org/example/tests/*SkeletonTest.java
- manual-testing/*
