param(
    [string]$OutputPath = "upload-keystore.jks",
    [string]$Alias = "deepdive-upload"
)

$ErrorActionPreference = "Stop"
$resolvedOutput = [System.IO.Path]::GetFullPath((Join-Path $PSScriptRoot "..\$OutputPath"))
$javaHomePath = [Environment]::GetEnvironmentVariable("JAVA_HOME")
$keytoolPath = if ($javaHomePath) {
    Join-Path $javaHomePath "bin\keytool.exe"
} else {
    "keytool.exe"
}

if (Test-Path -LiteralPath $resolvedOutput) {
    throw "The keystore already exists: $resolvedOutput"
}

Write-Host "Creating the Google Play upload key at $resolvedOutput"
Write-Host "Keytool will ask for the private passwords and certificate identity."
& $keytoolPath -genkeypair -v -keystore $resolvedOutput -alias $Alias `
    -keyalg RSA -keysize 4096 -validity 10000

if ($LASTEXITCODE -ne 0) {
    throw "keytool failed with exit code $LASTEXITCODE"
}

Write-Host "Upload key created. Copy keystore.properties.example to keystore.properties"
Write-Host "and replace CHANGE_ME with the passwords you entered."
