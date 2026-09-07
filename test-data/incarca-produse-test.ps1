# Incarca produsele de test in aplicatie, printr-un POST pentru fiecare,
# catre REST API-ul aplicatiei. POST necesita autentificare (HTTP Basic).
#
# Rulare (din radacina proiectului, in PowerShell):
#   .\test-data\incarca-produse-test.ps1
#
# Optional:
#   $env:BASE_URL = "http://localhost:8080"
#   $env:APP_ADMIN_USERNAME = "admin"
#   $env:APP_ADMIN_PASSWORD = "admin123"

$baseUrl = if ($env:BASE_URL) { $env:BASE_URL } else { "http://localhost:8080" }
$username = if ($env:APP_ADMIN_USERNAME) { $env:APP_ADMIN_USERNAME } else { "admin" }
$password = if ($env:APP_ADMIN_PASSWORD) { $env:APP_ADMIN_PASSWORD } else { "admin123" }
$produse = Get-Content -Path "$PSScriptRoot\produse-test.json" -Raw | ConvertFrom-Json

$pair = "${username}:${password}"
$bytes = [System.Text.Encoding]::ASCII.GetBytes($pair)
$basicAuth = [Convert]::ToBase64String($bytes)
$headers = @{
    Authorization = "Basic $basicAuth"
}

Write-Host "Se incarca $($produse.Count) produse in $baseUrl/api/produse ..." -ForegroundColor Cyan

$failures = 0
foreach ($produs in $produse) {
    $body = $produs | ConvertTo-Json

    try {
        $raspuns = Invoke-RestMethod -Uri "$baseUrl/api/produse" -Method Post -Body $body -ContentType "application/json" -Headers $headers
        Write-Host "OK   -> $($produs.nume) (id: $($raspuns.id))" -ForegroundColor Green
    }
    catch {
        Write-Host "EROARE -> $($produs.nume): $($_.Exception.Message)" -ForegroundColor Red
        $failures++
    }
}

if ($failures -gt 0) {
    Write-Host "Finalizat cu $failures erori." -ForegroundColor Red
    exit 1
}

Write-Host "Gata. Verifica lista completa la $baseUrl/api/produse sau $baseUrl/produse" -ForegroundColor Cyan
