# Localization_testing

Bo du an kiem thu localization cho shop da ngon ngu.

## Tai lieu chinh
- docs/README.md
- docs/test-plan.md
- docs/black-box-methods.md
- docs/white-box-methods.md
- docs/gwt-high-priority-test-cases.md
- docs/coverage-formula.md
- docs/oci-automation-usage.md

## Manual testing pack
- manual-testing/README.md
- manual-testing/manual-scenarios.md
- manual-testing/manual-steps-checklist.md
- manual-testing/result-template.md
- manual-testing/evidence-guide.md

## OCI report automation
1. Chay test: .\\mvnw.cmd test
2. Tao OCI report tu surefire:
	.\\run_oci_report.ps1 -RunId run-local
