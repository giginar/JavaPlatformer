$ErrorActionPreference = 'Stop'
$resolver = Join-Path $PSScriptRoot 'resolve-android-ad-configuration.ps1'
$testWrapper = Join-Path $PSScriptRoot 'build-android-test.ps1'
$projectRoot = [System.IO.Path]::GetFullPath((Join-Path $PSScriptRoot '..'))
$advertisingServicePath = Join-Path $projectRoot `
    'android\src\main\java\com\game\diver\android\AndroidAdvertisingService.java'
$privacyPaths = @(
    (Join-Path $projectRoot 'store\privacy-policy-en.md'),
    (Join-Path $projectRoot 'store\privacy-policy-tr.md'),
    (Join-Path $projectRoot 'docs\privacy.html')
)
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

    $serviceText = Get-Content -LiteralPath $advertisingServicePath -Raw -Encoding UTF8
    foreach ($requiredText in @(
        'new RequestConfiguration.Builder()',
        'RequestConfiguration.MaxAdContentRating.MAX_AD_CONTENT_RATING_PG',
        '.setAgeRestrictedTreatment(AgeRestrictedTreatment.UNSPECIFIED)',
        '.setRequestConfiguration(requestConfiguration)',
        'UserMessagingPlatform.loadAndShowConsentFormIfRequired',
        'advertisingGate.updateConsent(information.canRequestAds(), privacyRequired)',
        'if (!advertisingGate.canRequestAds())',
        'MobileAds.initialize(activity.getApplicationContext(), initializationConfig'
    )) {
        if (-not $serviceText.Contains($requiredText)) {
            throw "Android advertising policy contract is missing: $requiredText"
        }
    }
    foreach ($forbiddenText in @(
        '.setTagForChildDirectedTreatment(',
        '.setTagForUnderAgeOfConsent(',
        'AgeRestrictedTreatment.CHILD',
        'AgeRestrictedTreatment.TEEN'
    )) {
        if ($serviceText.Contains($forbiddenText)) {
            throw "Android advertising policy contract contains forbidden treatment: $forbiddenText"
        }
    }
    $requestConfigIndex = $serviceText.IndexOf('new RequestConfiguration.Builder()')
    $initializationConfigIndex = $serviceText.IndexOf('new InitializationConfig.Builder(')
    $mobileAdsInitializeIndex = $serviceText.IndexOf('MobileAds.initialize(')
    if ($requestConfigIndex -lt 0 -or $requestConfigIndex -ge $initializationConfigIndex -or
        $initializationConfigIndex -ge $mobileAdsInitializeIndex) {
        throw 'RequestConfiguration must be attached to InitializationConfig before Mobile Ads initialization.'
    }
    if ($serviceText -notmatch '(?s)requestConsentInfoUpdate\(.+?\(\) -> loadRequiredConsentForm\(activity\)') {
        throw 'UMP consent refresh must continue into the required consent form flow.'
    }
    if ($serviceText -notmatch '(?s)loadAndShowConsentFormIfRequired\(.+?updateConsentStateAndAds\(\)') {
        throw 'UMP required form completion must update consent before ad initialization.'
    }
    if ($serviceText -notmatch '(?s)updateConsentStateAndAds\(\).+?canRequestAds\(\).+?initializeAdsOnce\(\)') {
        throw 'Mobile Ads initialization must remain gated by canRequestAds().'
    }

    $staleAudienceText = @(
        'target age groups have not been selected',
        'final audience configuration has not yet been'
    )
    $staleAudiencePatterns = @(
        '(?i)hedef .+ gruplar. hen.z se.ilmemi.tir',
        '(?i)nihai hedef kitle yap.land.rmas. hen.z se.ilmemi.tir'
    )
    $ageSeparator = [char]0x2013
    foreach ($privacyPath in $privacyPaths) {
        $privacyText = Get-Content -LiteralPath $privacyPath -Raw -Encoding UTF8
        foreach ($audience in @("13${ageSeparator}15", "16${ageSeparator}17", '18+')) {
            if (-not $privacyText.Contains($audience)) {
                throw "Privacy audience is missing '$audience' in $privacyPath"
            }
        }
        foreach ($staleText in $staleAudienceText) {
            if ($privacyText.IndexOf(
                    $staleText, [System.StringComparison]::OrdinalIgnoreCase) -ge 0) {
                throw "Stale audience wording remains in $privacyPath`: $staleText"
            }
        }
        foreach ($stalePattern in $staleAudiencePatterns) {
            if ($privacyText -match $stalePattern) {
                throw "Stale audience wording remains in $privacyPath"
            }
        }
    }
} finally {
    foreach ($name in $environmentNames) {
        [Environment]::SetEnvironmentVariable($name, $savedEnvironment[$name], 'Process')
    }
}

Write-Host 'Android ad configuration tests: PASS'
