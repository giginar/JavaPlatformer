param(
    [switch]$SkipBuild,
    [switch]$RequireSignedBundle,
    [ValidateSet('Unoptimized', 'Optimized')]
    [string]$OptimizationMode = 'Unoptimized',
    [ValidateSet('', 'DISABLED', 'TEST', 'PRODUCTION')]
    [string]$ExpectedAdsMode = ''
)

$ErrorActionPreference = 'Stop'
$projectRootPath = [System.IO.Path]::GetFullPath((Join-Path $PSScriptRoot '..'))
$androidTargetPath = Join-Path $projectRootPath 'android\target'
$releasePath = Join-Path $androidTargetPath 'store\google-play'
$mavenWrapper = Join-Path $projectRootPath 'mvnw.cmd'
$pomPath = Join-Path $projectRootPath 'pom.xml'
[xml]$projectPom = Get-Content -LiteralPath $pomPath -Raw
$androidVersion = & (Join-Path $PSScriptRoot 'get-project-version.ps1') -ProjectRootPath $projectRootPath -Android
$version = $androidVersion.Version
$versionCode = $androidVersion.VersionCode
$isOptimized = $OptimizationMode -eq 'Optimized'
$artifactQualifier = if ($isOptimized) { '-optimized' } else { '' }
$androidWorkPath = Join-Path $androidTargetPath $(
    if ($isOptimized) { 'android-optimized-work' } else { 'android-work' })
$buildToolsVersion = [string]$projectPom.project.properties.'android.build-tools'
$bundletoolVersion = [string]$projectPom.project.properties.'bundletool.version'
$expectedMinSdk = [string]$projectPom.project.properties.'android.min-sdk'
$expectedCompileSdk = [string]$projectPom.project.properties.'android.compile-sdk'
$expectedTargetSdk = [string]$projectPom.project.properties.'android.target-sdk'
if ($expectedCompileSdk -ne $expectedTargetSdk) {
    throw "The custom Android pipeline compiles with android.target-sdk; compileSdk ($expectedCompileSdk) and targetSdk ($expectedTargetSdk) must agree."
}
[xml]$sourceManifest = Get-Content -LiteralPath `
    (Join-Path $projectRootPath 'android\src\main\AndroidManifest.xml') -Raw
$expectedApplicationId = [string]$sourceManifest.manifest.package

if (-not $SkipBuild) {
    $mavenProfiles = if ($isOptimized) {
        'android-release,android-optimized-release'
    } else {
        'android-release'
    }
    & $mavenWrapper -f $pomPath -B -ntp "-P$mavenProfiles" -pl android -am package
    if ($LASTEXITCODE -ne 0) {
        throw "Android release build failed with exit code $LASTEXITCODE"
    }
}

function Read-PropertiesFile {
    param([Parameter(Mandatory = $true)][string]$Path)

    $properties = @{}
    foreach ($line in Get-Content -LiteralPath $Path) {
        $trimmed = $line.Trim()
        if (-not $trimmed -or $trimmed.StartsWith('#')) {
            continue
        }
        $parts = $trimmed.Split('=', 2)
        if ($parts.Count -eq 2) {
            $properties[$parts[0].Trim()] = $parts[1].Trim()
        }
    }
    return $properties
}

function Write-Utf8File {
    param(
        [Parameter(Mandatory = $true)][string]$Path,
        [Parameter(Mandatory = $true)][string]$Content
    )
    [System.IO.File]::WriteAllText(
        $Path, $Content, [System.Text.UTF8Encoding]::new($false))
}

$localPropertiesPath = Join-Path $projectRootPath 'local.properties'
$sdkDirectory = ''
if (Test-Path -LiteralPath $localPropertiesPath) {
    $localProperties = Read-PropertiesFile $localPropertiesPath
    if ($localProperties.ContainsKey('sdk.dir')) {
        $sdkDirectory = $localProperties['sdk.dir'].Replace('\:', ':').Replace('\\', '\')
    }
}
if (-not $sdkDirectory -and $env:ANDROID_SDK_ROOT) {
    $sdkDirectory = $env:ANDROID_SDK_ROOT
}
if (-not $sdkDirectory -and $env:ANDROID_HOME) {
    $sdkDirectory = $env:ANDROID_HOME
}
if (-not $sdkDirectory) {
    throw 'Android SDK path is missing. Set sdk.dir in local.properties or ANDROID_SDK_ROOT.'
}
if (-not [System.IO.Path]::IsPathRooted($sdkDirectory)) {
    $sdkDirectory = Join-Path $projectRootPath $sdkDirectory
}
$sdkDirectory = [System.IO.Path]::GetFullPath($sdkDirectory)
$zipalignPath = Join-Path $sdkDirectory "build-tools\$buildToolsVersion\zipalign.exe"
$apksignerPath = Join-Path $sdkDirectory "build-tools\$buildToolsVersion\apksigner.bat"
$aapt2Path = Join-Path $sdkDirectory "build-tools\$buildToolsVersion\aapt2.exe"
$apkAnalyzerPath = Join-Path $sdkDirectory 'cmdline-tools\latest\bin\apkanalyzer.bat'
$releaseApk = Get-Item -LiteralPath `
    (Join-Path $releasePath "DeepDiveDrift-$version$artifactQualifier-universal.apk") `
    -ErrorAction SilentlyContinue
$bundleNames = @(
    "DeepDiveDrift-$version$artifactQualifier-google-play.aab",
    "DeepDiveDrift-$version$artifactQualifier-google-play-unsigned.aab"
)
$releaseBundle = Get-ChildItem -LiteralPath $releasePath -Filter '*.aab' -ErrorAction SilentlyContinue |
    Where-Object { $_.Name -in $bundleNames } |
    Sort-Object LastWriteTime -Descending | Select-Object -First 1

if (-not $releaseApk -or -not $releaseBundle) {
    throw "Release APK or AAB is missing below $releasePath"
}
if (-not (Test-Path -LiteralPath $apkAnalyzerPath)) {
    throw "apkanalyzer is missing: $apkAnalyzerPath"
}

$apkBadging = (& $aapt2Path dump badging $releaseApk.FullName | Out-String)
if ($LASTEXITCODE -ne 0) { throw 'Unable to read the APK version metadata' }
$expectedApkVersion = "versionCode='$versionCode' versionName='$([regex]::Escape($version))'"
if ($apkBadging -notmatch $expectedApkVersion) {
    throw "APK version does not match $version (versionCode $versionCode). Rebuild the Android package."
}
if ($apkBadging -notmatch "package: name='$([regex]::Escape($expectedApplicationId))'" -or
    $apkBadging -notmatch "minSdkVersion:'$([regex]::Escape($expectedMinSdk))'" -or
    $apkBadging -notmatch "targetSdkVersion:'$([regex]::Escape($expectedTargetSdk))'") {
    throw "APK identity or SDK metadata does not match $expectedApplicationId (min $expectedMinSdk, target $expectedTargetSdk)."
}

$apkManifestText = (& $apkAnalyzerPath manifest print $releaseApk.FullName | Out-String)
if ($LASTEXITCODE -ne 0) { throw 'Unable to inspect the merged APK manifest' }
$requiredManifestValues = @(
    'android.permission.INTERNET',
    'android.permission.ACCESS_NETWORK_STATE',
    'android.permission.READ_BASIC_PHONE_STATE',
    'com.google.android.gms.permission.AD_ID',
    'com.google.android.libraries.ads.mobile.sdk.common.AdActivity',
    'com.google.android.play.core.hsdp.service.HsdpShimActivity',
    'com.google.android.gms.common.api.GoogleApiActivity',
    'androidx.startup.InitializationProvider',
    'androidx.work.impl.background.systemjob.SystemJobService',
    'com.game.diver.android.AndroidLauncher',
    'com.game.diver.deepdivedrift.DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION',
    'com.game.diver.deepdivedrift.ADS_MODE'
)
foreach ($requiredManifestValue in $requiredManifestValues) {
    if (-not $apkManifestText.Contains($requiredManifestValue)) {
        throw "Merged APK manifest is missing $requiredManifestValue"
    }
}
if ($apkManifestText.Contains('android.permission.RECEIVE_BOOT_COMPLETED')) {
    throw 'Merged APK manifest must not request RECEIVE_BOOT_COMPLETED.'
}
[xml]$apkManifest = $apkManifestText
$androidNamespace = 'http://schemas.android.com/apk/res/android'
$launcherActivities = @($apkManifest.manifest.application.activity | Where-Object {
    $_.GetAttribute('name', $androidNamespace) -eq 'com.game.diver.android.AndroidLauncher'
})
if ($launcherActivities.Count -ne 1 -or
    $launcherActivities[0].GetAttribute('exported', $androidNamespace) -ne 'true') {
    throw 'Merged APK manifest must contain exactly one exported AndroidLauncher.'
}
$componentNames = @(
    @($apkManifest.manifest.application.activity) +
    @($apkManifest.manifest.application.service) +
    @($apkManifest.manifest.application.provider) +
    @($apkManifest.manifest.application.receiver) |
        ForEach-Object { $_.GetAttribute('name', $androidNamespace) }
)
$duplicateComponents = @($componentNames | Group-Object | Where-Object { $_.Count -gt 1 })
if ($duplicateComponents.Count -gt 0) {
    throw "Merged APK manifest contains duplicate components: $($duplicateComponents.Name -join ', ')"
}
if ($apkManifestText -match 'android:value="(DISABLED|TEST|PRODUCTION)"') {
    $adsMode = $Matches[1]
} else {
    throw 'Merged APK manifest does not declare a valid advertising mode.'
}
$testAppId = 'ca-app-pub-3940256099942544~3347511713'
$hasAdMobApplicationId = $apkManifestText.Contains('com.google.android.gms.ads.APPLICATION_ID')
if ($adsMode -eq 'DISABLED' -and $hasAdMobApplicationId) {
    throw 'DISABLED package must not contain AdMob application metadata.'
}
if ($adsMode -eq 'TEST' -and
    (-not $hasAdMobApplicationId -or -not $apkManifestText.Contains($testAppId))) {
    throw 'TEST package must contain the official Google demo App ID.'
}
if ($adsMode -eq 'PRODUCTION' -and
    (-not $hasAdMobApplicationId -or $apkManifestText.Contains($testAppId))) {
    throw 'PRODUCTION package is missing external AdMob metadata or contains the demo App ID.'
}
if ($ExpectedAdsMode -and $adsMode -ne $ExpectedAdsMode) {
    throw "Expected advertising mode $ExpectedAdsMode, but the package contains $adsMode."
}

$dexPackages = (& $apkAnalyzerPath dex packages $releaseApk.FullName | Out-String)
if ($LASTEXITCODE -ne 0) { throw 'Unable to inspect APK DEX classes' }
$requiredClasses = if ($isOptimized) {
    @(
        'com.game.diver.android.AndroidLauncher',
        'com.badlogic.gdx.controllers.android.AndroidControllers'
    )
} else {
    @(
        'com.game.DeepDiveDrift',
        'com.game.diver.android.AndroidLauncher',
        'com.google.android.libraries.ads.mobile.sdk.MobileAds',
        'com.google.android.ump.UserMessagingPlatform',
        'com.game.diver.android.AndroidAdvertisingService'
    )
}
foreach ($requiredClass in $requiredClasses) {
    if ($dexPackages -notmatch "(?m)^C\s+.*\s$([regex]::Escape($requiredClass))\r?$") {
        throw "APK DEX is missing $requiredClass"
    }
}
if ($dexPackages.Contains('com.google.firebase.analytics')) {
    throw 'Firebase Analytics must not be packaged.'
}

$apkEntries = @(& jar.exe tf $releaseApk.FullName)
foreach ($requiredEntry in @(
    'assets/background.png',
    'assets/enemy_octopus_boss.png',
    'assets/fonts/Orbitron-Regular.ttf',
    'assets/shaders/diver-outline.frag',
    'assets/ASSET_PROVENANCE_AUDIT.md',
    'assets/AUDIO_RIGHTS_AUDIT.md',
    'assets/fonts/OFL.txt',
    'assets/licenses/LWJGL-BSD-3-Clause.txt',
    'assets/THIRD_PARTY_NOTICES.md',
    'assets/THIRD_PARTY_SOFTWARE_NOTICES.md',
    'res/drawable/admob_close_button_white_cross.xml',
    'res/layout/hsdp_shim_activity.xml',
    'META-INF/services/kotlinx.coroutines.internal.MainDispatcherFactory',
    'okhttp3/internal/publicsuffix/publicsuffixes.gz'
)) {
    if ($requiredEntry -notin $apkEntries) {
        throw "APK is missing SDK runtime resource $requiredEntry"
    }
}

$consumerRuleInventoryPath = Join-Path $androidWorkPath 'consumer-rules\inventory.txt'
if (-not (Test-Path -LiteralPath $consumerRuleInventoryPath)) {
    throw 'Consumer-rule inventory is missing.'
}
$consumerRuleInventory = Get-Content -LiteralPath $consumerRuleInventoryPath -Raw
foreach ($requiredRules in @('ads-mobile-sdk-1.4.0', 'user-messaging-platform-4.0.0',
    'kotlinx-coroutines-android-1.9.0')) {
    if (-not $consumerRuleInventory.Contains($requiredRules)) {
        throw "Consumer-rule inventory is missing $requiredRules"
    }
}
if ($isOptimized) {
    $r8ReportPath = Join-Path $releasePath "DeepDiveDrift-$version-r8"
    $r8Reports = @{
        Mapping = Join-Path $r8ReportPath 'mapping.txt'
        Usage = Join-Path $r8ReportPath 'usage.txt'
        Seeds = Join-Path $r8ReportPath 'seeds.txt'
        Configuration = Join-Path $r8ReportPath 'configuration.txt'
        Warnings = Join-Path $r8ReportPath 'warnings.txt'
        Version = Join-Path $r8ReportPath 'version.txt'
        ConsumerRules = Join-Path $r8ReportPath 'consumer-rules.txt'
    }
    foreach ($reportName in $r8Reports.Keys) {
        $reportPath = $r8Reports[$reportName]
        if (-not (Test-Path -LiteralPath $reportPath -PathType Leaf) -or
            (Get-Item -LiteralPath $reportPath).Length -eq 0) {
            throw "Optimized build is missing R8 $reportName output: $reportPath"
        }
    }

    $r8Version = Get-Content -LiteralPath $r8Reports.Version -Raw
    if ($r8Version -notmatch '(?m)^R8 8\.10\.9-dev\b') {
        throw 'Optimized build used an unexpected R8 version.'
    }
    $r8Warnings = Get-Content -LiteralPath $r8Reports.Warnings -Raw
    if ($r8Warnings.Trim() -ne '# No R8 warnings.') {
        throw 'Optimized build has unresolved R8 warnings.'
    }
    $r8Configuration = Get-Content -LiteralPath $r8Reports.Configuration -Raw
    if ($r8Configuration -match '(?m)^\s*-dont(shrink|optimize|obfuscate)\b' -or
        $r8Configuration -match '(?m)^\s*-dontwarn\s+\*{1,2}\s*$') {
        throw 'R8 configuration disables a required optimization or contains a blanket dontwarn.'
    }
    if ($r8Configuration -notmatch
        '(?m)^\s*-keepclasseswithmembernames,includedescriptorclasses class \* \{\s*$' -or
        $r8Configuration -match
        '(?m)^\s*-keepclasseswithmembernames[^\r\n]*allowoptimization[^\r\n]*class \* \{\s*$') {
        throw 'R8 configuration must preserve JNI names and descriptors without member optimization.'
    }
    $r8Arguments = Get-Content -LiteralPath (Join-Path $androidWorkPath 'r8-arguments.txt') -Raw
    foreach ($disabledOption in @('--no-tree-shaking', '--no-minification', '--no-desugaring')) {
        if ($r8Arguments.Contains($disabledOption)) {
            throw "Optimized R8 invocation contains forbidden option $disabledOption"
        }
    }
    if ($r8Arguments -notmatch '(?m)^--min-api\r?\n24\r?$' -or
        $r8Arguments -notmatch '(?m)^--lib\r?\n.*[\\/]platforms[\\/]android-36[\\/]android\.jar\r?$') {
        throw 'Optimized R8 invocation must use min API 24 and the Android 36 platform library.'
    }
    $mappingLines = @(Get-Content -LiteralPath $r8Reports.Mapping)
    if ($mappingLines -notcontains '# compiler: R8') {
        throw 'R8 mapping output does not identify the R8 compiler.'
    }

    function Assert-PersistedEnumMapping {
        param(
            [Parameter(Mandatory = $true)][string[]]$Lines,
            [Parameter(Mandatory = $true)][string]$ClassName,
            [Parameter(Mandatory = $true)][int]$ExpectedConstantCount
        )

        $escapedClassName = [regex]::Escape($ClassName)
        $classHeaderIndex = -1
        for ($lineIndex = 0; $lineIndex -lt $Lines.Count; $lineIndex++) {
            if ($Lines[$lineIndex] -match "^$escapedClassName -> [^:]+:`$") {
                $classHeaderIndex = $lineIndex
                break
            }
        }
        if ($classHeaderIndex -lt 0) {
            throw "R8 mapping is missing persisted enum $ClassName"
        }

        $constantCount = 0
        for ($lineIndex = $classHeaderIndex + 1; $lineIndex -lt $Lines.Count; $lineIndex++) {
            $line = $Lines[$lineIndex]
            if ($line -match '^\S+ -> \S+:$') {
                break
            }
            if ($line -match "^\s+$escapedClassName ([A-Z][A-Z0-9_]*) -> (\S+)`$") {
                $constantCount++
                if ($Matches[1] -ne $Matches[2]) {
                    throw "R8 renamed persisted enum identifier $ClassName.$($Matches[1]) to $($Matches[2])"
                }
            }
        }
        if ($constantCount -ne $ExpectedConstantCount) {
            throw "R8 mapping contains $constantCount persisted constants for $ClassName; expected $ExpectedConstantCount."
        }
    }

    $persistedEnums = @(
        [pscustomobject]@{ ClassName = 'com.game.model.Achievement'; Count = 40 },
        [pscustomobject]@{ ClassName = 'com.game.model.ChallengeModifier'; Count = 4 },
        [pscustomobject]@{ ClassName = 'com.game.model.DiverSuit'; Count = 4 },
        [pscustomobject]@{ ClassName = 'com.game.model.PermanentUpgrade'; Count = 5 },
        [pscustomobject]@{ ClassName = 'com.game.model.RunDifficulty'; Count = 3 },
        [pscustomobject]@{ ClassName = 'com.game.settings.DisplaySettingsStore$WindowMode'; Count = 3 },
        [pscustomobject]@{ ClassName = 'com.game.settings.DisplaySettingsStore$TextScale'; Count = 2 }
    )
    foreach ($persistedEnum in $persistedEnums) {
        Assert-PersistedEnumMapping -Lines $mappingLines `
            -ClassName $persistedEnum.ClassName `
            -ExpectedConstantCount $persistedEnum.Count
    }

    $hasFirstPartyObfuscation = $false
    foreach ($mappingLine in $mappingLines) {
        if ($mappingLine -match '^([^ ]+) -> ([^:]+):$' -and
            $Matches[1].StartsWith('com.game.') -and $Matches[1] -ne $Matches[2]) {
            $hasFirstPartyObfuscation = $true
            break
        }
    }
    if (-not $hasFirstPartyObfuscation) {
        throw 'R8 mapping does not prove first-party class obfuscation.'
    }
    $gdxPixmapCode = (& $apkAnalyzerPath dex code `
        --class com.badlogic.gdx.graphics.g2d.Gdx2DPixmap $releaseApk.FullName | Out-String)
    if ($LASTEXITCODE -ne 0 -or $gdxPixmapCode -notmatch
        '(?m)^\.method private static native load\(\[J\[BII\)Ljava/nio/ByteBuffer;\r?$') {
        throw 'Optimized DEX does not preserve the libGDX image-decoder JNI descriptor.'
    }
    $consumerSelection = Get-Content -LiteralPath $r8Reports.ConsumerRules -Raw
    foreach ($requiredSelection in @(
        'INCLUDED|ads-mobile-sdk-1.4.0.aar|proguard.txt',
        'INCLUDED|user-messaging-platform-4.0.0.aar|proguard.txt',
        'INCLUDED|kotlinx-coroutines-android-1.9.0.jar|META-INF/com.android.tools/r8-from-1.6.0/coroutines.pro',
        'INCLUDED|kotlinx-coroutines-core-jvm-1.9.0.jar|META-INF/com.android.tools/r8/coroutines.pro'
    )) {
        if (-not $consumerSelection.Contains($requiredSelection)) {
            throw "R8 consumer-rule selection is missing $requiredSelection"
        }
    }
}

& java.exe (Join-Path $PSScriptRoot 'VerifyAndroidAudio.java') $releaseApk.FullName (Join-Path $projectRootPath 'assets')
if ($LASTEXITCODE -ne 0) {
    throw 'APK audio assets must be present and uncompressed for Android playback'
}

& $zipalignPath -c -P 16 -v 4 $releaseApk.FullName | Out-Null
if ($LASTEXITCODE -ne 0) {
    throw 'APK ZIP alignment is not 16 KB compatible'
}
& $apksignerPath verify --verbose $releaseApk.FullName | Out-Null
if ($LASTEXITCODE -ne 0) {
    throw 'Universal test APK signature verification failed'
}

$bundletoolPath = Get-Item -LiteralPath (Join-Path $projectRootPath ".mvn\tools\bundletool-all-$bundletoolVersion.jar") -ErrorAction SilentlyContinue
if (-not $bundletoolPath) {
    throw 'bundletool is missing; run the Android Maven release build first'
}
& java.exe -jar $bundletoolPath.FullName validate "--bundle=$($releaseBundle.FullName)" | Out-Null
if ($LASTEXITCODE -ne 0) {
    throw 'bundletool validation failed'
}

$bundleEntries = @(& jar.exe tf $releaseBundle.FullName)
foreach ($requiredBundleEntry in @(
    'base/dex/classes.dex',
    'base/assets/background.png',
    'base/assets/enemy_octopus_boss.png',
    'base/assets/fonts/Orbitron-Regular.ttf',
    'base/assets/shaders/diver-outline.frag',
    'base/assets/THIRD_PARTY_NOTICES.md',
    'base/assets/THIRD_PARTY_SOFTWARE_NOTICES.md'
)) {
    if ($requiredBundleEntry -notin $bundleEntries) {
        throw "AAB is missing required content $requiredBundleEntry"
    }
}

$bundleManifestText = (& java.exe -jar $bundletoolPath.FullName dump manifest "--bundle=$($releaseBundle.FullName)" --module=base | Out-String)
if ($LASTEXITCODE -ne 0) { throw 'Unable to read the AAB version metadata' }
[xml]$bundleManifest = $bundleManifestText
if ($bundleManifest.manifest.GetAttribute('versionName', $androidNamespace) -ne $version -or
    $bundleManifest.manifest.GetAttribute('versionCode', $androidNamespace) -ne [string]$versionCode) {
    throw "AAB version does not match $version (versionCode $versionCode). Rebuild the Android package."
}
$bundleUsesSdk = $bundleManifest.manifest.'uses-sdk'
if ($bundleUsesSdk.GetAttribute('minSdkVersion', $androidNamespace) -ne $expectedMinSdk -or
    $bundleUsesSdk.GetAttribute('targetSdkVersion', $androidNamespace) -ne $expectedTargetSdk -or
    [string]$bundleManifest.manifest.package -ne $expectedApplicationId) {
    throw 'AAB identity or min/target SDK metadata is incorrect.'
}

function Get-ElfLoadAlignments([string]$Path) {
    [byte[]]$elfBytes = [System.IO.File]::ReadAllBytes($Path)
    if ($elfBytes.Length -lt 64 -or $elfBytes[0] -ne 0x7f -or
        $elfBytes[1] -ne 0x45 -or $elfBytes[2] -ne 0x4c -or $elfBytes[3] -ne 0x46) {
        throw "Not an ELF library: $Path"
    }
    if ($elfBytes[5] -ne 1) {
        throw "Only little-endian ELF libraries are supported: $Path"
    }

    $elfClass = $elfBytes[4]
    if ($elfClass -eq 1) {
        [uint64]$programOffset = [BitConverter]::ToUInt32($elfBytes, 28)
        $entrySize = [BitConverter]::ToUInt16($elfBytes, 42)
        $entryCount = [BitConverter]::ToUInt16($elfBytes, 44)
        $alignmentOffset = 28
    } elseif ($elfClass -eq 2) {
        [uint64]$programOffset = [BitConverter]::ToUInt64($elfBytes, 32)
        $entrySize = [BitConverter]::ToUInt16($elfBytes, 54)
        $entryCount = [BitConverter]::ToUInt16($elfBytes, 56)
        $alignmentOffset = 48
    } else {
        throw "Unsupported ELF class $elfClass in $Path"
    }
    $minimumEntrySize = if ($elfClass -eq 1) { 32 } else { 56 }
    if ($entrySize -lt $minimumEntrySize -or $entryCount -eq 0 -or
        $programOffset + [uint64]$entrySize * [uint64]$entryCount -gt [uint64]$elfBytes.Length) {
        throw "Invalid ELF program-header table in $Path"
    }

    $alignments = @()
    for ($entryIndex = 0; $entryIndex -lt $entryCount; $entryIndex++) {
        [int]$entryOffset = [int]($programOffset + $entryIndex * $entrySize)
        if ([BitConverter]::ToUInt32($elfBytes, $entryOffset) -ne 1) {
            continue
        }
        if ($elfClass -eq 1) {
            $alignments += [uint64][BitConverter]::ToUInt32(
                $elfBytes, $entryOffset + $alignmentOffset)
        } else {
            $alignments += [BitConverter]::ToUInt64(
                $elfBytes, $entryOffset + $alignmentOffset)
        }
    }
    if ($alignments.Count -eq 0) {
        throw "ELF library has no LOAD segments: $Path"
    }
    return $alignments
}

function Expand-ArchiveWithJar([string]$Archive, [string]$Destination, [string]$Label) {
    New-Item -ItemType Directory -Path $Destination -Force | Out-Null
    Push-Location $Destination
    try {
        & jar.exe xf $Archive
        if ($LASTEXITCODE -ne 0) {
            throw "Unable to extract $Label"
        }
    } finally {
        Pop-Location
    }
}

$verificationPath = Join-Path $androidTargetPath 'android-verification'
$resolvedAndroidTarget = [System.IO.Path]::GetFullPath($androidTargetPath).TrimEnd('\') + '\'
$resolvedVerificationPath = [System.IO.Path]::GetFullPath($verificationPath)
if (-not $resolvedVerificationPath.StartsWith(
    $resolvedAndroidTarget, [System.StringComparison]::OrdinalIgnoreCase
)) {
    throw "Refusing to clean a verification directory outside Android target: $resolvedVerificationPath"
}
if (Test-Path -LiteralPath $verificationPath) {
    Remove-Item -LiteralPath $verificationPath -Recurse -Force
}
New-Item -ItemType Directory -Path $verificationPath -Force | Out-Null

try {
    $aabVerificationPath = Join-Path $verificationPath 'aab'
    $apkVerificationPath = Join-Path $verificationPath 'apk'
    Expand-ArchiveWithJar $releaseBundle.FullName $aabVerificationPath 'the release AAB'
    Expand-ArchiveWithJar $releaseApk.FullName $apkVerificationPath 'the universal APK'

    $serviceProviders = @(
        [pscustomobject]@{
            Descriptor = 'META-INF\services\kotlinx.coroutines.internal.MainDispatcherFactory'
            ClassName = 'kotlinx.coroutines.android.AndroidDispatcherFactory'
        },
        [pscustomobject]@{
            Descriptor = 'META-INF\services\kotlinx.coroutines.CoroutineExceptionHandler'
            ClassName = 'kotlinx.coroutines.android.AndroidExceptionPreHandler'
        }
    )
    foreach ($serviceProvider in $serviceProviders) {
        $descriptorPath = Join-Path $apkVerificationPath $serviceProvider.Descriptor
        if (-not (Test-Path -LiteralPath $descriptorPath -PathType Leaf) -or
            $serviceProvider.ClassName -notin @(Get-Content -LiteralPath $descriptorPath)) {
            throw "APK service descriptor does not retain $($serviceProvider.ClassName)"
        }
        if ($dexPackages -notmatch "(?m)^C\s+.*\s$([regex]::Escape($serviceProvider.ClassName))\r?$") {
            throw "APK service provider class is missing: $($serviceProvider.ClassName)"
        }
    }

    $nativeInventoryPath = Join-Path $androidWorkPath 'native-library-inventory.txt'
    if (-not (Test-Path -LiteralPath $nativeInventoryPath -PathType Leaf)) {
        throw 'Native-library source inventory is missing.'
    }
    $nativeSources = @{}
    foreach ($line in Get-Content -LiteralPath $nativeInventoryPath) {
        $trimmed = $line.Trim()
        if (-not $trimmed -or $trimmed.StartsWith('#')) { continue }
        $parts = $trimmed.Split('|', 3)
        if ($parts.Count -ne 3 -or -not $parts[2]) {
            throw "Invalid native-library inventory entry: $trimmed"
        }
        $key = "$($parts[0])|$($parts[1])"
        if ($nativeSources.ContainsKey($key) -and $nativeSources[$key] -ne $parts[2]) {
            throw "Native library $key has conflicting source dependencies."
        }
        $nativeSources[$key] = $parts[2]
    }

    $expectedAbis = 'armeabi-v7a', 'arm64-v8a', 'x86', 'x86_64'
    $nativeVerification = [System.Collections.Generic.List[string]]::new()
    foreach ($artifact in @(
        [pscustomobject]@{ Label = 'AAB'; Root = Join-Path $aabVerificationPath 'base\lib' },
        [pscustomobject]@{ Label = 'APK'; Root = Join-Path $apkVerificationPath 'lib' }
    )) {
        $actualAbis = @(Get-ChildItem -LiteralPath $artifact.Root -Directory -ErrorAction SilentlyContinue |
            Select-Object -ExpandProperty Name | Sort-Object)
        $abiDifference = @(Compare-Object ($expectedAbis | Sort-Object) $actualAbis)
        if ($abiDifference.Count -gt 0) {
            throw "$($artifact.Label) ABI inventory differs from the required four ABIs: $($actualAbis -join ', ')"
        }
        $artifactKeys = [System.Collections.Generic.HashSet[string]]::new(
            [System.StringComparer]::Ordinal)
        foreach ($abi in $expectedAbis) {
            $nativePath = Join-Path $artifact.Root $abi
            $libraries = @(Get-ChildItem -LiteralPath $nativePath -Filter '*.so' -File `
                -ErrorAction SilentlyContinue | Sort-Object Name)
            if ($libraries.Count -lt 2) {
                throw "Expected $($artifact.Label) native libraries are missing for $abi"
            }
            foreach ($library in $libraries) {
                $key = "$abi|$($library.Name)"
                if (-not $nativeSources.ContainsKey($key)) {
                    throw "$($artifact.Label) contains untracked native library $key"
                }
                [void]$artifactKeys.Add($key)
                $alignments = @(Get-ElfLoadAlignments $library.FullName)
                foreach ($alignment in $alignments) {
                    if ($alignment -lt 16384) {
                        throw "$($library.FullName) has a LOAD segment aligned to only $alignment bytes"
                    }
                }
                $nativeVerification.Add(
                    "$($artifact.Label)|$abi|$($library.Name)|$($nativeSources[$key])|$($alignments -join ',')")
            }
        }
        foreach ($inventoryKey in $nativeSources.Keys) {
            if (-not $artifactKeys.Contains($inventoryKey)) {
                throw "$($artifact.Label) is missing inventoried native library $inventoryKey"
            }
        }
    }
    Write-Utf8File -Path (Join-Path $androidWorkPath 'native-verification.txt') `
        -Content ((@('# Artifact|ABI|Library|Source dependency|ELF LOAD alignments') +
            $nativeVerification.ToArray()) -join "`n")
} finally {
    if (Test-Path -LiteralPath $verificationPath) {
        Remove-Item -LiteralPath $verificationPath -Recurse -Force
    }
}

$signatureOutput = (& jarsigner.exe '-J-Duser.language=en' '-J-Duser.country=US' -verify -verbose $releaseBundle.FullName 2>&1 | Out-String)
if ($LASTEXITCODE -ne 0) {
    throw 'AAB signature verification failed'
}
$isSigned = $signatureOutput -match '(?m)^jar verified\.'
if (($RequireSignedBundle -or (Test-Path -LiteralPath (Join-Path $projectRootPath 'keystore.properties'))) -and -not $isSigned) {
    throw 'Google Play requires an upload-key signed AAB. Create the upload keystore, configure keystore.properties, and rebuild. The local test APK can still be installed.'
}

Write-Host "Android $OptimizationMode verification passed:"
Write-Host "  APK and AAB version: $version (versionCode $versionCode)"
Write-Host "  Package: $expectedApplicationId; minSdk $expectedMinSdk; compileSdk $expectedCompileSdk; targetSdk $expectedTargetSdk"
Write-Host "  Advertising mode: $adsMode; GMA/UMP classes, resources, and merged manifest verified"
Write-Host '  Java service/resources and consumer-rule inventory: present'
Write-Host '  Audio assets: present and uncompressed for Android openFd'
Write-Host '  Bundletool schema: valid'
Write-Host '  APK ZIP alignment: 16 KB compatible'
Write-Host '  Universal APK signature: valid local debug key'
Write-Host '  ELF LOAD alignment: 16 KB across all four ABIs'
if ($isOptimized) {
    Write-Host '  R8: shrinking, optimization, and obfuscation enabled; no warnings'
    Write-Host "  R8 mapping/reports: $r8ReportPath"
}
Write-Host "  AAB: $($releaseBundle.FullName)"
Write-Host "  AAB signature: $(if ($isSigned) { 'signed' } else { 'unsigned (upload key still required)' })"
