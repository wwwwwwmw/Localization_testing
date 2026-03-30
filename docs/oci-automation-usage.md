# OCI Automation Usage

## 1. Cach 1 - Tu CSV input template
1. Chinh sua file docs/templates/oci-input-template.csv
2. Chay CLI:

```bash
./mvnw.cmd -q exec:java -Dexec.mainClass="org.example.core.OciReportCli" -Dexec.args="docs/templates/oci-input-template.csv report run-001"
```

## 2. Cach 2 - Tu ket qua test run (Surefire)
Chay test + tao OCI report tu surefire XML:

```powershell
./run_oci_report.ps1 -RunId run-20260328 -LocaleTarget 4 -LocaleCovered 4 -FeatureTarget 20 -FeatureCovered 18 -MethodTarget 12 -MethodCovered 10 -RiskWeightTotal 100 -RiskWeightCovered 88 -BoundaryTarget 16 -BoundaryCovered 12 -AutoTarget 60
```

## 3. Dau ra
- File OCI report markdown nam trong thu muc report/
- Co thong tin component score, OCI, rating, raw inputs.

## 4. Luu y
- autoCovered duoc tinh bang so test PASSED trong surefire XML.
- Cac thanh phan khac nhap theo muc tieu sprint/hien trang coverage.
- Neu muon chi tiet hon theo domain, ket hop them mapping testcase-domain trong docs.
