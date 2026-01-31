@echo off
chcp 65001 >nul
echo ═══════════════════════════════════════════════════════════════
echo          PRESTASHOP LOCALIZATION TESTER
echo ═══════════════════════════════════════════════════════════════
echo.
echo Chọn ngôn ngữ để test:
echo   1. English (en)
echo   2. Français (fr)
echo   3. Deutsch (de)
echo   4. Español (es)
echo   5. Italiano (it)
echo   6. Polski (pl)
echo   7. Português (pt)
echo.
set /p choice="Nhập số (1-7): "

if "%choice%"=="1" set lang=en
if "%choice%"=="2" set lang=fr
if "%choice%"=="3" set lang=de
if "%choice%"=="4" set lang=es
if "%choice%"=="5" set lang=it
if "%choice%"=="6" set lang=pl
if "%choice%"=="7" set lang=pt

if not defined lang (
    echo Lựa chọn không hợp lệ!
    pause
    exit /b
)

echo.
echo Chọn chế độ:
echo   1. Tương tác (Manual) - Quét từng trang theo yêu cầu
echo   2. Tự động (Auto) - Quét tất cả các trang
echo.
set /p mode="Nhập số (1-2): "

echo.
echo ═══════════════════════════════════════════════════════════════
echo Đang khởi động test với ngôn ngữ: %lang%
echo ═══════════════════════════════════════════════════════════════
echo.

if "%mode%"=="2" (
    .\mvnw.cmd exec:java -Dexec.mainClass="org.example.PrestaShopAutoScanner" -Dexec.args="%lang% --auto" -q
) else (
    .\mvnw.cmd exec:java -Dexec.mainClass="org.example.PrestaShopL10nTester" -Dexec.args="%lang%" -q
)

echo.
pause
