<#
.SYNOPSIS
Build the current project with the version calculated from Git commits.
.DESCRIPTION
Without arguments, build the Windows installer and Android APK/AAB.
Use -Target Windows, Android, or Steam to build one target.
All includes Windows and Android; Steam depots are built separately.
.EXAMPLE
powershell.exe -NoProfile -ExecutionPolicy Bypass -File .\scripts\build.ps1
.EXAMPLE
powershell.exe -NoProfile -ExecutionPolicy Bypass -File .\scripts\build.ps1 -Target Android
#>
[CmdletBinding()]
param(
    [ValidateSet('All', 'Windows', 'Android', 'Steam')]
    [string]$Target = 'All'
)

$ErrorActionPreference = 'Stop'
$projectRootPath = [System.IO.Path]::GetFullPath((Join-Path $PSScriptRoot '..'))
$version = & (Join-Path $PSScriptRoot 'get-project-version.ps1') -ProjectRootPath $projectRootPath
Write-Host "Building DeepDive Drift $version ($Target)..."

if ($Target -in @('All', 'Windows')) {
    & (Join-Path $PSScriptRoot 'build-windows-installer.ps1')
}
if ($Target -in @('All', 'Android')) {
    & (Join-Path $PSScriptRoot 'build-android-test.ps1')
}
if ($Target -eq 'Steam') {
    & (Join-Path $projectRootPath 'mvnw.cmd') -f (Join-Path $projectRootPath 'pom.xml') `
        -B -ntp -Psteam -pl lwjgl3 -am clean verify
    if ($LASTEXITCODE -ne 0) {
        throw "Steam build failed with exit code $LASTEXITCODE"
    }
}

Write-Host "Build completed: DeepDive Drift $version"
if ($Target -in @('All', 'Windows')) {
    Write-Host "  EXE: $(Join-Path $projectRootPath "lwjgl3\target\installer\DeepDive Drift-$version.exe")"
}
if ($Target -in @('All', 'Android')) {
    Write-Host "  APK: $(Join-Path $projectRootPath "android\target\store\google-play\DeepDiveDrift-$version-universal.apk")"
    Write-Host "  AAB directory: $(Join-Path $projectRootPath 'android\target\store\google-play')"
}
if ($Target -eq 'Steam') {
    Write-Host "  Steam depots: $(Join-Path $projectRootPath 'lwjgl3\target\steam')"
}
