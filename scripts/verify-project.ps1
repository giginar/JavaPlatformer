param(
    [switch]$SkipDesktopSmoke,
    [switch]$SkipAndroid
)

$ErrorActionPreference = 'Stop'
$projectRootPath = [System.IO.Path]::GetFullPath((Join-Path $PSScriptRoot '..'))
$mavenWrapper = Join-Path $projectRootPath 'mvnw.cmd'
$androidVerifier = Join-Path $PSScriptRoot 'verify-android-release.ps1'

function Invoke-Checked {
    param(
        [Parameter(Mandatory = $true)][string]$Label,
        [Parameter(Mandatory = $true)][string]$FilePath,
        [Parameter(Mandatory = $true)][object[]]$Arguments
    )
    Write-Host "`n=== $Label ==="
    Write-Host "$FilePath $($Arguments -join ' ')"
    & $FilePath @Arguments
    $exitCode = $LASTEXITCODE
    if ($exitCode -ne 0) {
        throw "$Label failed with exit code $exitCode"
    }
    Write-Host "$Label`: PASS (exit 0)"
}

function Set-AdEnvironment {
    param(
        [Parameter(Mandatory = $true)][string]$Mode,
        [string]$AppId = '',
        [string]$RewardedId = '',
        [string]$InterstitialId = ''
    )
    $env:DEEPDRIFT_ADS_MODE = $Mode
    if ($AppId) { $env:DEEPDRIFT_ADMOB_APP_ID = $AppId } else { Remove-Item Env:DEEPDRIFT_ADMOB_APP_ID -ErrorAction SilentlyContinue }
    if ($RewardedId) { $env:DEEPDRIFT_REWARDED_AD_UNIT_ID = $RewardedId } else { Remove-Item Env:DEEPDRIFT_REWARDED_AD_UNIT_ID -ErrorAction SilentlyContinue }
    if ($InterstitialId) { $env:DEEPDRIFT_INTERSTITIAL_AD_UNIT_ID = $InterstitialId } else { Remove-Item Env:DEEPDRIFT_INTERSTITIAL_AD_UNIT_ID -ErrorAction SilentlyContinue }
}

$environmentNames = @(
    'DEEPDRIFT_ADS_MODE',
    'DEEPDRIFT_ADMOB_APP_ID',
    'DEEPDRIFT_REWARDED_AD_UNIT_ID',
    'DEEPDRIFT_INTERSTITIAL_AD_UNIT_ID'
)
$savedEnvironment = @{}
foreach ($name in $environmentNames) {
    $savedEnvironment[$name] = [Environment]::GetEnvironmentVariable($name, 'Process')
}

try {
    Push-Location $projectRootPath
    try {
        Invoke-Checked -Label 'Automated tests' -FilePath $mavenWrapper `
            -Arguments @('-B', '-ntp', 'test')
        Invoke-Checked -Label 'Android ad configuration tests' -FilePath 'powershell.exe' `
            -Arguments @('-NoProfile', '-ExecutionPolicy', 'Bypass', '-File',
                (Join-Path $PSScriptRoot 'test-android-ad-configuration.ps1'))
        Invoke-Checked -Label 'Desktop package' -FilePath $mavenWrapper `
            -Arguments @('-B', '-ntp', '-pl', 'lwjgl3', '-am', 'package')
        if (-not $SkipDesktopSmoke) {
            Invoke-Checked -Label 'Desktop runtime smoke' -FilePath 'powershell.exe' `
                -Arguments @('-NoProfile', '-ExecutionPolicy', 'Bypass', '-File',
                    (Join-Path $PSScriptRoot 'verify-desktop-smoke.ps1'), '-SkipBuild')
        }

        if (-not $SkipAndroid) {
            Set-AdEnvironment -Mode 'DISABLED'
            Invoke-Checked -Label 'Android DISABLED build' -FilePath $mavenWrapper `
                -Arguments @('-B', '-ntp', '-Pandroid-release', '-pl', 'android', '-am',
                    'clean', 'package')
            Invoke-Checked -Label 'Android DISABLED verification' -FilePath 'powershell.exe' `
                -Arguments @('-NoProfile', '-ExecutionPolicy', 'Bypass', '-File',
                    $androidVerifier, '-SkipBuild')
            Invoke-Checked -Label 'Android dependency lock' -FilePath 'powershell.exe' `
                -Arguments @('-NoProfile', '-ExecutionPolicy', 'Bypass', '-File',
                    (Join-Path $PSScriptRoot 'verify-android-dependency-lock.ps1'))

            # Build TEST last so -SkipBuild verification remains repeatable after this script.
            Set-AdEnvironment -Mode 'TEST'
            Invoke-Checked -Label 'Android TEST build' -FilePath $mavenWrapper `
                -Arguments @('-B', '-ntp', '-Pandroid-release', '-pl', 'android', '-am',
                    'clean', 'package')
            Invoke-Checked -Label 'Android TEST verification' -FilePath 'powershell.exe' `
                -Arguments @('-NoProfile', '-ExecutionPolicy', 'Bypass', '-File',
                    $androidVerifier, '-SkipBuild')
        }
    } finally {
        Pop-Location
    }
} finally {
    foreach ($name in $environmentNames) {
        $savedValue = $savedEnvironment[$name]
        if ($null -eq $savedValue) {
            [Environment]::SetEnvironmentVariable($name, $null, 'Process')
        } else {
            [Environment]::SetEnvironmentVariable($name, $savedValue, 'Process')
        }
    }
}

Write-Host "`nProject verification: PASS"
