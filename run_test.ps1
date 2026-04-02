# Script để chạy test localization
param(
    [string]$lang = "en",
    [switch]$auto
)

$validLangs = @("en", "fr", "vi", "de", "ja", "ru", "ar")

if ($lang -notin $validLangs) {
    Write-Host "═══════════════════════════════════════════════════════════════" -ForegroundColor Cyan
    Write-Host "         PRESTASHOP LOCALIZATION TESTER" -ForegroundColor Yellow
    Write-Host "═══════════════════════════════════════════════════════════════" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "Các ngôn ngữ hỗ trợ:" -ForegroundColor Green
    Write-Host "  en - English"
    Write-Host "  fr - Français"
    Write-Host "  vi - Tiếng Việt"
    Write-Host "  de - Deutsch"
    Write-Host "  ja - 日本語"
    Write-Host "  ru - Русский"
    Write-Host "  ar - العربية"
    Write-Host ""
    Write-Host "Cách sử dụng:" -ForegroundColor Green
    Write-Host "  .\run_test.ps1 -lang fr          # Test tiếng Pháp (manual)"
    Write-Host "  .\run_test.ps1 -lang de -auto    # Test tiếng Đức (auto scan)"
    Write-Host ""
    exit
}

Write-Host ""
Write-Host "═══════════════════════════════════════════════════════════════" -ForegroundColor Cyan
Write-Host "  Đang khởi động test với ngôn ngữ: $lang" -ForegroundColor Yellow
Write-Host "═══════════════════════════════════════════════════════════════" -ForegroundColor Cyan
Write-Host ""

if ($auto) {
    & .\mvnw.cmd exec:java "-Dexec.mainClass=org.example.PrestaShopAutoScanner" "-Dexec.args=$lang --auto" -q
}
else {
    & .\mvnw.cmd exec:java "-Dexec.mainClass=org.example.PrestaShopL10nTester" "-Dexec.args=$lang" -q
}
