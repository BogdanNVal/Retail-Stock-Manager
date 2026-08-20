# Incarca produsele de test in aplicatie, printr-un POST pentru fiecare,
# catre REST API-ul aplicatiei (nu necesita autentificare).
#
# Rulare (din radacina proiectului, in PowerShell):
#   .\test-data\incarca-produse-test.ps1
#
# Daca aplicatia ruleaza pe alt port/host, modifica variabila $baseUrl mai jos.

$baseUrl = "http://localhost:8080"
$produse = Get-Content -Path "$PSScriptRoot\produse-test.json" -Raw | ConvertFrom-Json

Write-Host "Se incarca $($produse.Count) produse in $baseUrl/api/produse ..." -ForegroundColor Cyan

foreach ($produs in $produse) {
    $body = $produs | ConvertTo-Json

    try {
        $raspuns = Invoke-RestMethod -Uri "$baseUrl/api/produse" -Method Post -Body $body -ContentType "application/json"
        Write-Host "OK   -> $($produs.nume) (id: $($raspuns.id))" -ForegroundColor Green
    }
    catch {
        Write-Host "EROARE -> $($produs.nume): $($_.Exception.Message)" -ForegroundColor Red
    }
}

Write-Host "Gata. Verifica lista completa la $baseUrl/api/produse sau $baseUrl/produse" -ForegroundColor Cyan
