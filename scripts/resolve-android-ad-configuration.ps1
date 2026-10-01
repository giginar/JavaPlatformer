[CmdletBinding()]
param(
    [ValidateSet('false', 'true')]
    [string]$ProductionAds = 'false'
)

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

$testAppId = 'ca-app-pub-3940256099942544~3347511713'
$testInterstitialId = 'ca-app-pub-3940256099942544/1033173712'
$testRewardedId = 'ca-app-pub-3940256099942544/5224354917'
$productionAppId = 'ca-app-pub-5376669360146484~9049306652'
$productionInterstitialId = 'ca-app-pub-5376669360146484/6131409987'
$productionRewardedId = 'ca-app-pub-5376669360146484/1909183230'

$mode = if ($env:DEEPDRIFT_ADS_MODE) {
    $env:DEEPDRIFT_ADS_MODE.Trim().ToUpperInvariant()
} else {
    'DISABLED'
}
if ($mode -notin @('DISABLED', 'TEST', 'PRODUCTION')) {
    throw 'DEEPDRIFT_ADS_MODE must be DISABLED, TEST, or PRODUCTION.'
}

$productionSwitchEnabled = $ProductionAds -eq 'true'
if ($productionSwitchEnabled -ne ($mode -eq 'PRODUCTION')) {
    throw 'PRODUCTION ads require both the explicit production build switch and DEEPDRIFT_ADS_MODE=PRODUCTION.'
}

if ($mode -eq 'DISABLED') {
    Write-Host 'Advertising configuration: DISABLED (no consent, initialization, or ad requests)'
    return [pscustomobject]@{ Mode = $mode; AppId = ''; RewardedId = ''; InterstitialId = '' }
}
if ($mode -eq 'TEST') {
    Write-Host 'Advertising configuration: TEST (official Google demo IDs only)'
    return [pscustomobject]@{
        Mode = $mode
        AppId = $testAppId
        RewardedId = $testRewardedId
        InterstitialId = $testInterstitialId
    }
}

$appId = [string]$env:DEEPDRIFT_ADMOB_APP_ID
$rewardedId = [string]$env:DEEPDRIFT_REWARDED_AD_UNIT_ID
$interstitialId = [string]$env:DEEPDRIFT_INTERSTITIAL_AD_UNIT_ID
if ([string]::IsNullOrWhiteSpace($appId) -or
    [string]::IsNullOrWhiteSpace($rewardedId) -or
    [string]::IsNullOrWhiteSpace($interstitialId)) {
    throw 'PRODUCTION ads require all three external AdMob environment values.'
}
if ($appId -notmatch '^ca-app-pub-[0-9]+~[0-9]+$' -or
    $rewardedId -notmatch '^ca-app-pub-[0-9]+/[0-9]+$' -or
    $interstitialId -notmatch '^ca-app-pub-[0-9]+/[0-9]+$') {
    throw 'PRODUCTION ads require correctly formatted App, Interstitial, and Rewarded IDs.'
}
if ($appId -in @($testAppId, $testInterstitialId, $testRewardedId) -or
    $rewardedId -in @($testAppId, $testInterstitialId, $testRewardedId) -or
    $interstitialId -in @($testAppId, $testInterstitialId, $testRewardedId)) {
    throw 'PRODUCTION ads must not use any official Google demo identifier.'
}
if ($rewardedId -eq $interstitialId) {
    throw 'PRODUCTION Interstitial and Rewarded ad unit IDs must be different.'
}
if ($appId -ne $productionAppId -or
    $interstitialId -ne $productionInterstitialId -or
    $rewardedId -ne $productionRewardedId) {
    throw 'PRODUCTION ads do not match the approved Deep Drift AdMob configuration.'
}

Write-Host 'Advertising configuration: PRODUCTION (approved external IDs validated)'
[pscustomobject]@{
    Mode = $mode
    AppId = $appId
    RewardedId = $rewardedId
    InterstitialId = $interstitialId
}
