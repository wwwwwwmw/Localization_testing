param(
    [string]$RunId = "local-run",
    [double]$LocaleTarget = 4,
    [double]$LocaleCovered = 4,
    [double]$FeatureTarget = 20,
    [double]$FeatureCovered = 18,
    [double]$MethodTarget = 12,
    [double]$MethodCovered = 10,
    [double]$RiskWeightTotal = 100,
    [double]$RiskWeightCovered = 88,
    [double]$BoundaryTarget = 16,
    [double]$BoundaryCovered = 12,
    [double]$AutoTarget = 60
)

$ErrorActionPreference = "Stop"

$projectRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $projectRoot

# Ensure latest test run exists.
./mvnw.cmd test

# Generate OCI report from surefire XML.
./mvnw.cmd -q exec:java `
    -Dexec.mainClass="org.example.core.OciFromSurefireCli" `
    -Dexec.args="target/surefire-reports report $RunId $LocaleTarget $LocaleCovered $FeatureTarget $FeatureCovered $MethodTarget $MethodCovered $RiskWeightTotal $RiskWeightCovered $BoundaryTarget $BoundaryCovered $AutoTarget"

Write-Host "Done. Check report folder for OCI markdown output."
