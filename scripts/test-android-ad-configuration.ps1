$ErrorActionPreference = 'Stop'
$resolver = Join-Path $PSScriptRoot 'resolve-android-ad-configuration.ps1'
$testWrapper = Join-Path $PSScriptRoot 'build-android-test.ps1'
$environmentNames = @(
    'DEEPDRIFT_ADS_MODE',
    'DEEPDRIFT_ADMOB_APP_ID',
    'DEEPDRIFT_INTERSTITIAL_AD_UNIT_ID',
    'DEEPDRIFT_REWARDED_AD_UNIT_ID'
)
$savedEnvironment = @{}
foreach ($name in $environmentNames) {
    $savedEnvironment[$name] = [Environment]::GetEnvironmentVariable($name, 'Process')
}

$productionAppId = 'ca-app-pub-5376669360146484~9049306652'
$productionInterstitialId = 'ca-app-pub-5376669360146484/6131409987'
$productionRewardedId = 'ca-app-pub-5376669360146484/1909183230'

function Set-AdEnvironment {
    param(
        [string]$Mode = '',
        [string]$AppId = '',
        [string]$InterstitialId = '',
        [string]$RewardedId = ''
    )
    foreach ($item in @{
        DEEPDRIFT_ADS_MODE = $Mode
        DEEPDRIFT_ADMOB_APP_ID = $AppId
        DEEPDRIFT_INTERSTITIAL_AD_UNIT_ID = $InterstitialId
        DEEPDRIFT_REWARDED_AD_UNIT_ID = $RewardedId
    }.GetEnumerator()) {
        [Environment]::SetEnvironmentVariable(
            $item.Key, $(if ($item.Value) { $item.Value } else { $null }), 'Process')
    }
}

function Assert-Equal([string]$Expected, [string]$Actual, [string]$Label) {
    if ($Expected -ne $Actual) { throw "$Label expected '$Expected', got '$Actual'." }
}

function Assert-Failure {
    param(
        [Parameter(Mandatory = $true)][string]$Label,
        [Parameter(Mandatory = $true)][string]$ExpectedMessage,
        [ValidateSet('false', 'true')][string]$ProductionAds = 'true'
    )
    try {
        & $resolver -ProductionAds $ProductionAds | Out-Null
        throw "$Label unexpectedly succeeded."
    } catch {
        if ($_.Exception.Message -notlike "*$ExpectedMessage*") {
            throw "$Label failed for an unexpected reason: $($_.Exception.Message)"
        }
    }
}

try {
    Set-AdEnvironment -Mode TEST
    $testConfiguration = & $resolver
    Assert-Equal 'TEST' $testConfiguration.Mode 'TEST mode'
    Assert-Equal 'ca-app-pub-3940256099942544~3347511713' $testConfiguration.AppId 'TEST App ID'
    Assert-Equal 'ca-app-pub-3940256099942544/1033173712' $testConfiguration.InterstitialId 'TEST Interstitial ID'
    Assert-Equal 'ca-app-pub-3940256099942544/5224354917' $testConfiguration.RewardedId 'TEST Rewarded ID'

    Set-AdEnvironment -Mode PRODUCTION -AppId $productionAppId `
        -InterstitialId $productionInterstitialId -RewardedId $productionRewardedId
    Assert-Failure -Label 'Environment-only production selection' `
        -ExpectedMessage 'explicit production build switch' -ProductionAds false

    Set-AdEnvironment -Mode TEST
    Assert-Failure -Label 'Switch-only production selection' `
        -ExpectedMessage 'explicit production build switch'

    foreach ($missingName in @('App', 'Interstitial', 'Rewarded')) {
        $arguments = @{
            Mode = 'PRODUCTION'
            AppId = $productionAppId
            InterstitialId = $productionInterstitialId
            RewardedId = $productionRewardedId
        }
        $arguments["${missingName}Id"] = ''
        Set-AdEnvironment @arguments
        Assert-Failure -Label "Missing production $missingName ID" `
            -ExpectedMessage 'require all three'
    }

    Set-AdEnvironment -Mode PRODUCTION -AppId 'ca-app-pub-5376669360146484/9049306652' `
        -InterstitialId $productionInterstitialId -RewardedId $productionRewardedId
    Assert-Failure -Label 'App ID separator validation' -ExpectedMessage 'correctly formatted'
    Set-AdEnvironment -Mode PRODUCTION -AppId $productionAppId `
        -InterstitialId 'ca-app-pub-5376669360146484~6131409987' `
        -RewardedId $productionRewardedId
    Assert-Failure -Label 'Interstitial separator validation' -ExpectedMessage 'correctly formatted'
    Set-AdEnvironment -Mode PRODUCTION -AppId $productionAppId `
        -InterstitialId $productionInterstitialId `
        -RewardedId 'ca-app-pub-5376669360146484~1909183230'
    Assert-Failure -Label 'Rewarded separator validation' -ExpectedMessage 'correctly formatted'

    Set-AdEnvironment -Mode PRODUCTION `
        -AppId 'ca-app-pub-3940256099942544~3347511713' `
        -InterstitialId 'ca-app-pub-3940256099942544/1033173712' `
        -RewardedId 'ca-app-pub-3940256099942544/5224354917'
    Assert-Failure -Label 'Demo IDs in production' -ExpectedMessage 'demo identifier'

    Set-AdEnvironment -Mode PRODUCTION `
        -AppId 'ca-app-pub-1111111111111111~1111111111' `
        -InterstitialId 'ca-app-pub-1111111111111111/2222222222' `
        -RewardedId 'ca-app-pub-1111111111111111/3333333333'
    Assert-Failure -Label 'Unapproved production IDs' -ExpectedMessage 'approved Deep Drift'

    Set-AdEnvironment -Mode PRODUCTION -AppId $productionAppId `
        -InterstitialId $productionInterstitialId -RewardedId $productionInterstitialId
    Assert-Failure -Label 'Identical production ad units' -ExpectedMessage 'must be different'

    Set-AdEnvironment -Mode PRODUCTION -AppId $productionAppId `
        -InterstitialId $productionInterstitialId -RewardedId $productionRewardedId
    $productionConfiguration = & $resolver -ProductionAds true
    Assert-Equal 'PRODUCTION' $productionConfiguration.Mode 'PRODUCTION mode'
    Assert-Equal $productionAppId $productionConfiguration.AppId 'PRODUCTION App ID'
    Assert-Equal $productionInterstitialId $productionConfiguration.InterstitialId 'PRODUCTION Interstitial ID'
    Assert-Equal $productionRewardedId $productionConfiguration.RewardedId 'PRODUCTION Rewarded ID'

    $wrapperText = Get-Content -LiteralPath $testWrapper -Raw
    foreach ($requiredText in @(
        "SetEnvironmentVariable('DEEPDRIFT_ADS_MODE', 'TEST', 'Process')",
        '-Dandroid.production-ads=false',
        '-ExpectedAdsMode TEST',
        "SetEnvironmentVariable('DEEPDRIFT_ADS_MODE', `$savedAdsMode, 'Process')"
    )) {
        if (-not $wrapperText.Contains($requiredText)) {
            throw "Routine Android test wrapper is missing safety behavior: $requiredText"
        }
    }
} finally {
    foreach ($name in $environmentNames) {
        [Environment]::SetEnvironmentVariable($name, $savedEnvironment[$name], 'Process')
    }
}

Write-Host 'Android ad configuration tests: PASS'
