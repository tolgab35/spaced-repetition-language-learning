# DeckOS JMeter Yük Testi - Çalıştırma Scripti
# Kullanım: .\run-test.ps1
# Opsiyonel: .\run-test.ps1 -Threads 100 -Loops 30

param(
    [int]$Threads = 50,
    [int]$Loops   = 20,
    [int]$RampUp  = 10,
    [string]$TargetHost = "localhost",
    [int]$Port    = 8080
)

$ErrorActionPreference = "Stop"
$scriptDir = $PSScriptRoot

# ---- JMeter kontrolü ----
$jmeterCmd = Get-Command jmeter -ErrorAction SilentlyContinue
if (-not $jmeterCmd) {
    Write-Host ""
    Write-Host "HATA: 'jmeter' komutu bulunamadi." -ForegroundColor Red
    Write-Host ""
    Write-Host "Kurulum icin:" -ForegroundColor Yellow
    Write-Host "  1. https://jmeter.apache.org/download_jmeter.cgi adresinden Apache JMeter 5.6+ indir"
    Write-Host "  2. Zip'i ornegin C:\tools\jmeter\ dizinine ac"
    Write-Host "  3. C:\tools\jmeter\bin dizinini PATH'e ekle:"
    Write-Host "     [Environment]::SetEnvironmentVariable('PATH', `$env:PATH + ';C:\tools\jmeter\bin', 'User')"
    Write-Host "  4. Terminali yeniden ac ve tekrar calistir"
    exit 1
}

# ---- Eski sonuclari temizle ----
$resultsDir = Join-Path $scriptDir "results"
$reportDir  = Join-Path $scriptDir "report"

if (Test-Path "$resultsDir\results.jtl") {
    Remove-Item "$resultsDir\results.jtl" -Force
    Write-Host "Eski results.jtl silindi." -ForegroundColor DarkGray
}
if (Test-Path $reportDir) {
    Remove-Item $reportDir -Recurse -Force
    Write-Host "Eski HTML raporu silindi." -ForegroundColor DarkGray
}
New-Item -ItemType Directory -Force -Path $reportDir | Out-Null

# ---- Teste başla ----
$jmxFile = Join-Path $scriptDir "cards-due-load-test.jmx"

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host " DeckOS Load Test Basliyor" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host " Hedef : http://${TargetHost}:${Port}/api/cards/due"
Write-Host " Thread: $Threads kullanici"
Write-Host " Ramp  : $RampUp saniye"
Write-Host " Loop  : $Loops iterasyon/kullanici"
Write-Host " Toplam: $($Threads * $Loops) istek"
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

$startTime = Get-Date

jmeter -n `
    -t $jmxFile `
    -l "$resultsDir\results.jtl" `
    -e -o $reportDir `
    -Jhost=$TargetHost `
    -Jport=$Port `
    -JTHREADS=$Threads `
    -JRAMP_UP=$RampUp `
    -JLOOPS=$Loops

$exitCode = $LASTEXITCODE
$elapsed  = (Get-Date) - $startTime

Write-Host ""
if ($exitCode -eq 0) {
    Write-Host "Test tamamlandi! Sure: $([math]::Round($elapsed.TotalSeconds, 1))s" -ForegroundColor Green
    Write-Host ""
    Write-Host "HTML Raporu: $reportDir\index.html" -ForegroundColor Yellow

    # Raporu tarayicida ac
    $indexHtml = Join-Path $reportDir "index.html"
    if (Test-Path $indexHtml) {
        Write-Host "Tarayici aciliyor..." -ForegroundColor DarkGray
        Start-Process $indexHtml
    }
} else {
    Write-Host "Test HATAYLA bitti (exit code: $exitCode)" -ForegroundColor Red
    Write-Host "Servisin calisiyor oldugundan emin ol: docker-compose up -d" -ForegroundColor Yellow
}
