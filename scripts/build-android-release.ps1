param(
    [Parameter(Mandatory = $true)]
    [int]$MinSdk,

    [Parameter(Mandatory = $true)]
    [int]$TargetSdk,

    [Parameter(Mandatory = $true)]
    [string]$BuildToolsVersion,

    [Parameter(Mandatory = $true)]
    [string]$BundletoolVersion,

    [Parameter(Mandatory = $true)]
    [string]$ManifestMergerVersion
)

$ErrorActionPreference = 'Stop'
Add-Type -AssemblyName System.IO.Compression.FileSystem
$projectRootPath = [System.IO.Path]::GetFullPath((Join-Path $PSScriptRoot '..'))
$androidPath = Join-Path $projectRootPath 'android'
$targetPath = Join-Path $androidPath 'target'
$mavenDependencyPath = Join-Path $targetPath 'android-base-dependencies'
$workPath = Join-Path $targetPath 'android-work'
$outputPath = Join-Path $targetPath 'store\google-play'
$toolsCachePath = Join-Path $projectRootPath '.mvn\tools'
$adsDependencyLockPath = Join-Path $androidPath 'ads-dependencies.lock'
$adsDependencyCachePath = Join-Path $toolsCachePath 'android-ads-runtime'
$manifestMergerToolPath = Join-Path $toolsCachePath "manifest-merger-$ManifestMergerVersion"
$androidVersion = & (Join-Path $PSScriptRoot 'get-project-version.ps1') -ProjectRootPath $projectRootPath -Android
$Version = $androidVersion.Version
$VersionCode = $androidVersion.VersionCode
Write-Host "Building Android version $Version (versionCode $VersionCode)..."

function Invoke-ExternalTool {
    param(
        [Parameter(Mandatory = $true)]
        [string]$FilePath,

        [Parameter(Mandatory = $true)]
        [object[]]$Arguments,

        [string]$WorkingDirectory = ''
    )

    if ($WorkingDirectory) {
        Push-Location $WorkingDirectory
    }
    try {
        & $FilePath @Arguments
        if ($LASTEXITCODE -ne 0) {
            throw "$FilePath failed with exit code $LASTEXITCODE"
        }
    } finally {
        if ($WorkingDirectory) {
            Pop-Location
        }
    }
}

function Copy-DirectoryContents {
    param(
        [Parameter(Mandatory = $true)]
        [string]$Source,

        [Parameter(Mandatory = $true)]
        [string]$Destination
    )

    if (-not (Test-Path -LiteralPath $Source -PathType Container)) {
        return
    }
    New-Item -ItemType Directory -Path $Destination -Force | Out-Null
    Get-ChildItem -LiteralPath $Source -Force | ForEach-Object {
        Copy-Item -LiteralPath $_.FullName -Destination $Destination -Recurse -Force
    }
}

function Get-FileSha256 {
    param([Parameter(Mandatory = $true)][string]$Path)
    return (Get-FileHash -LiteralPath $Path -Algorithm SHA256).Hash.ToLowerInvariant()
}

function Write-Utf8File {
    param(
        [Parameter(Mandatory = $true)][string]$Path,
        [Parameter(Mandatory = $true)][string]$Content
    )
    [System.IO.File]::WriteAllText(
        $Path, $Content, [System.Text.UTF8Encoding]::new($false))
}

function Merge-JavaArchiveResources {
    param(
        [Parameter(Mandatory = $true)][string]$Archive,
        [Parameter(Mandatory = $true)][string]$Destination,
        [Parameter(Mandatory = $true)][string]$ConsumerRulesDestination,
        [Parameter(Mandatory = $true)][int]$ArchiveIndex,
        [Parameter(Mandatory = $true)]
        [System.Collections.Generic.List[string]]$ConsumerRuleInventory
    )

    $destinationRoot = [System.IO.Path]::GetFullPath($Destination).TrimEnd('\') + '\'
    $zip = [System.IO.Compression.ZipFile]::OpenRead($Archive)
    try {
        foreach ($entry in $zip.Entries) {
            $entryName = $entry.FullName.Replace('\', '/')
            if (-not $entryName -or $entryName.EndsWith('/') -or
                $entryName.EndsWith('.class', [System.StringComparison]::OrdinalIgnoreCase) -or
                $entryName -match '^META-INF/(MANIFEST\.MF|[^/]+\.(SF|RSA|DSA|EC))$' -or
                $entryName -match '^META-INF/maven/' -or
                $entryName -match '^META-INF/(LICENSE|NOTICE)(\..*)?$') {
                continue
            }

            if ($entryName -match '^META-INF/(proguard|com\.android\.tools/(proguard|r8[^/]*))/' -and
                $entryName -match '\.(pro|txt)$') {
                $safeRuleName = $entryName.Replace('/', '-').Replace('\', '-')
                $ruleDestination = Join-Path $ConsumerRulesDestination `
                    ("jar-{0:D3}-{1}-{2}" -f $ArchiveIndex,
                        [System.IO.Path]::GetFileNameWithoutExtension($Archive), $safeRuleName)
                $ruleStream = $entry.Open()
                try {
                    $ruleFile = [System.IO.File]::Create($ruleDestination)
                    try { $ruleStream.CopyTo($ruleFile) } finally { $ruleFile.Dispose() }
                } finally {
                    $ruleStream.Dispose()
                }
                $ConsumerRuleInventory.Add(
                    "$([System.IO.Path]::GetFileName($Archive))|$entryName|$ruleDestination")
                continue
            }

            $relativePath = $entryName.Replace('/', [System.IO.Path]::DirectorySeparatorChar)
            $outputFile = [System.IO.Path]::GetFullPath((Join-Path $Destination $relativePath))
            if (-not $outputFile.StartsWith(
                $destinationRoot, [System.StringComparison]::OrdinalIgnoreCase)) {
                throw "Unsafe Java resource entry '$entryName' in $Archive"
            }
            $outputDirectory = [System.IO.Path]::GetDirectoryName($outputFile)
            New-Item -ItemType Directory -Path $outputDirectory -Force | Out-Null

            if ($entryName.StartsWith('META-INF/services/',
                [System.StringComparison]::OrdinalIgnoreCase)) {
                $reader = [System.IO.StreamReader]::new($entry.Open(), [System.Text.Encoding]::UTF8)
                try { $newLines = @($reader.ReadToEnd() -split '\r?\n') } finally { $reader.Dispose() }
                $existingLines = if (Test-Path -LiteralPath $outputFile) {
                    @(Get-Content -LiteralPath $outputFile)
                } else {
                    @()
                }
                $mergedLines = @($existingLines + $newLines |
                    Where-Object { $_.Trim() } | Select-Object -Unique)
                Write-Utf8File -Path $outputFile -Content (($mergedLines -join "`n") + "`n")
                continue
            }

            if (Test-Path -LiteralPath $outputFile) {
                $existingSha256 = Get-FileSha256 $outputFile
                $sha256 = [System.Security.Cryptography.SHA256]::Create()
                $entryStream = $entry.Open()
                try {
                    $entrySha256 = [System.BitConverter]::ToString(
                        $sha256.ComputeHash($entryStream)).Replace('-', '').ToLowerInvariant()
                } finally {
                    $entryStream.Dispose()
                    $sha256.Dispose()
                }
                if ($existingSha256 -ne $entrySha256) {
                    throw "Conflicting Java resource '$entryName' from $Archive"
                }
                continue
            }

            $inputStream = $entry.Open()
            try {
                $outputStream = [System.IO.File]::Create($outputFile)
                try { $inputStream.CopyTo($outputStream) } finally { $outputStream.Dispose() }
            } finally {
                $inputStream.Dispose()
            }
        }
    } finally {
        $zip.Dispose()
    }
}

function Stage-LockedAndroidDependencies {
    param(
        [Parameter(Mandatory = $true)][string]$LockPath,
        [Parameter(Mandatory = $true)][string]$CachePath,
        [Parameter(Mandatory = $true)][string]$Destination
    )

    if (-not (Test-Path -LiteralPath $LockPath -PathType Leaf)) {
        throw "Android dependency lock is missing: $LockPath"
    }
    New-Item -ItemType Directory -Path $CachePath, $Destination -Force | Out-Null
    $repositories = @{
        google = 'https://dl.google.com/dl/android/maven2'
        central = 'https://repo.maven.apache.org/maven2'
    }
    $stagedNames = [System.Collections.Generic.HashSet[string]]::new(
        [System.StringComparer]::OrdinalIgnoreCase)

    foreach ($line in Get-Content -LiteralPath $LockPath) {
        $trimmed = $line.Trim()
        if (-not $trimmed -or $trimmed.StartsWith('#')) {
            continue
        }
        $parts = $trimmed.Split('|')
        if ($parts.Count -ne 6) {
            throw "Invalid dependency lock entry: $trimmed"
        }
        $repository, $group, $artifact, $artifactVersion, $extension, $expectedSha256 = $parts
        if (-not $repositories.ContainsKey($repository)) {
            throw "Unknown Android dependency repository '$repository'"
        }
        if ($extension -notin @('aar', 'jar') -or
            $expectedSha256 -notmatch '^[0-9a-f]{64}$') {
            throw "Invalid Android dependency type or checksum: $trimmed"
        }

        $fileName = "$artifact-$artifactVersion.$extension"
        if (-not $stagedNames.Add($fileName)) {
            throw "Duplicate locked Android dependency filename: $fileName"
        }
        $cacheFile = Join-Path $CachePath $fileName
        $artifactPath = ($group.Replace('.', '/') + "/$artifact/$artifactVersion/$fileName")
        $artifactUrl = $repositories[$repository] + '/' + $artifactPath
        $downloadPath = "$cacheFile.download"

        if ((Test-Path -LiteralPath $cacheFile) -and
            (Get-FileSha256 $cacheFile) -ne $expectedSha256) {
            Remove-Item -LiteralPath $cacheFile -Force
        }
        if (-not (Test-Path -LiteralPath $cacheFile)) {
            Write-Host "Downloading locked Android dependency $group`:$artifact`:$artifactVersion..."
            if (Test-Path -LiteralPath $downloadPath) {
                Remove-Item -LiteralPath $downloadPath -Force
            }
            try {
                Invoke-WebRequest -UseBasicParsing -Uri $artifactUrl -OutFile $downloadPath
                $actualSha256 = Get-FileSha256 $downloadPath
                if ($actualSha256 -ne $expectedSha256) {
                    throw "SHA-256 mismatch for $fileName. Expected $expectedSha256, got $actualSha256"
                }
                Move-Item -LiteralPath $downloadPath -Destination $cacheFile -Force
            } finally {
                if (Test-Path -LiteralPath $downloadPath) {
                    Remove-Item -LiteralPath $downloadPath -Force
                }
            }
        }
        $actualCachedSha256 = Get-FileSha256 $cacheFile
        if ($actualCachedSha256 -ne $expectedSha256) {
            throw "Cached SHA-256 mismatch for $fileName"
        }
        Copy-Item -LiteralPath $cacheFile -Destination (Join-Path $Destination $fileName)
    }
}

function Resolve-AdConfiguration {
    $testAppId = 'ca-app-pub-3940256099942544~3347511713'
    $testRewardedId = 'ca-app-pub-3940256099942544/5224354917'
    $testInterstitialId = 'ca-app-pub-3940256099942544/1033173712'
    $mode = if ($env:DEEPDRIFT_ADS_MODE) {
        $env:DEEPDRIFT_ADS_MODE.Trim().ToUpperInvariant()
    } else {
        'DISABLED'
    }
    if ($mode -notin @('DISABLED', 'TEST', 'PRODUCTION')) {
        throw 'DEEPDRIFT_ADS_MODE must be DISABLED, TEST, or PRODUCTION.'
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
    if ($appId -notmatch '^ca-app-pub-[0-9]+~[0-9]+$' -or
        $rewardedId -notmatch '^ca-app-pub-[0-9]+/[0-9]+$' -or
        $interstitialId -notmatch '^ca-app-pub-[0-9]+/[0-9]+$') {
        throw 'PRODUCTION ads require valid external DEEPDRIFT_ADMOB_APP_ID, DEEPDRIFT_REWARDED_AD_UNIT_ID, and DEEPDRIFT_INTERSTITIAL_AD_UNIT_ID values.'
    }
    if ($appId -eq $testAppId -or $rewardedId -eq $testRewardedId -or
        $interstitialId -eq $testInterstitialId) {
        throw 'PRODUCTION ads must not use any official Google demo identifier.'
    }
    Write-Host 'Advertising configuration: PRODUCTION (external IDs validated)'
    return [pscustomobject]@{
        Mode = $mode
        AppId = $appId
        RewardedId = $rewardedId
        InterstitialId = $interstitialId
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

function Resolve-AndroidSdkPath {
    $localPropertiesPath = Join-Path $projectRootPath 'local.properties'
    if (Test-Path -LiteralPath $localPropertiesPath) {
        $properties = Read-PropertiesFile $localPropertiesPath
        if ($properties.ContainsKey('sdk.dir')) {
            $configuredPath = $properties['sdk.dir'].Replace('\:', ':').Replace('\\', '\')
            return [System.IO.Path]::GetFullPath($configuredPath)
        }
    }
    if ($env:ANDROID_SDK_ROOT) {
        return [System.IO.Path]::GetFullPath($env:ANDROID_SDK_ROOT)
    }
    if ($env:ANDROID_HOME) {
        return [System.IO.Path]::GetFullPath($env:ANDROID_HOME)
    }
    throw 'Android SDK path is missing. Set sdk.dir in local.properties or ANDROID_SDK_ROOT.'
}

function Expand-JavaArchive {
    param(
        [Parameter(Mandatory = $true)][string]$Archive,
        [Parameter(Mandatory = $true)][string]$Destination,
        [Parameter(Mandatory = $true)][string]$JarTool
    )

    New-Item -ItemType Directory -Path $Destination -Force | Out-Null
    Invoke-ExternalTool -FilePath $JarTool -Arguments @('xf', $Archive) -WorkingDirectory $Destination
}

$androidSdkPath = Resolve-AndroidSdkPath
$buildToolsPath = Join-Path $androidSdkPath "build-tools\$BuildToolsVersion"
$aapt2Path = Join-Path $buildToolsPath 'aapt2.exe'
$d8Path = Join-Path $buildToolsPath 'd8.bat'
$zipalignPath = Join-Path $buildToolsPath 'zipalign.exe'
$androidJarPath = Join-Path $androidSdkPath "platforms\android-$TargetSdk\android.jar"
$javaPath = (Get-Command java.exe -ErrorAction Stop).Source
$javacPath = (Get-Command javac.exe -ErrorAction Stop).Source
$jarPath = (Get-Command jar.exe -ErrorAction Stop).Source
$jarsignerPath = (Get-Command jarsigner.exe -ErrorAction Stop).Source
$keytoolPath = (Get-Command keytool.exe -ErrorAction Stop).Source
$mavenWrapperPath = Join-Path $projectRootPath 'mvnw.cmd'

foreach ($requiredPath in @(
    $aapt2Path, $d8Path, $zipalignPath, $androidJarPath, $mavenDependencyPath,
    $adsDependencyLockPath, $mavenWrapperPath
)) {
    if (-not (Test-Path -LiteralPath $requiredPath)) {
        throw "Required Android build input is missing: $requiredPath"
    }
}

$manifestMergerJar = Join-Path $manifestMergerToolPath "manifest-merger-$ManifestMergerVersion.jar"
if (-not (Test-Path -LiteralPath $manifestMergerJar)) {
    New-Item -ItemType Directory -Path $manifestMergerToolPath -Force | Out-Null
    Invoke-ExternalTool -FilePath $mavenWrapperPath -Arguments @(
        '-B', '-ntp',
        '-f', (Join-Path $PSScriptRoot 'android-manifest-merger-pom.xml'),
        "-Dmanifest-merger.version=$ManifestMergerVersion",
        'org.apache.maven.plugins:maven-dependency-plugin:3.11.0:copy-dependencies',
        "-DoutputDirectory=$manifestMergerToolPath",
        '-DincludeScope=runtime'
    )
}
if (-not (Test-Path -LiteralPath $manifestMergerJar)) {
    throw "Official Android manifest merger $ManifestMergerVersion was not staged."
}

$resolvedTargetPath = [System.IO.Path]::GetFullPath($targetPath).TrimEnd('\') + '\'
$resolvedWorkPath = [System.IO.Path]::GetFullPath($workPath)
if (-not $resolvedWorkPath.StartsWith($resolvedTargetPath, [System.StringComparison]::OrdinalIgnoreCase)) {
    throw "Refusing to clean a work directory outside Android target: $resolvedWorkPath"
}
if (Test-Path -LiteralPath $resolvedWorkPath) {
    Remove-Item -LiteralPath $resolvedWorkPath -Recurse -Force
}

$aarPath = Join-Path $workPath 'aar'
$dependencyPath = Join-Path $workPath 'resolved-dependencies'
$compiledResourcePath = Join-Path $workPath 'compiled-resources'
$generatedSourcePath = Join-Path $workPath 'generated-sources'
$compiledClassPath = Join-Path $workPath 'compiled-classes'
$dexPath = Join-Path $workPath 'dex'
$mergedAssetPath = Join-Path $workPath 'merged-assets'
$mergedJavaResourcePath = Join-Path $workPath 'merged-java-resources'
$consumerRulesPath = Join-Path $workPath 'consumer-rules'
$modulePath = Join-Path $workPath 'module'
foreach ($directory in @(
    $aarPath, $dependencyPath, $compiledResourcePath, $generatedSourcePath,
    $compiledClassPath, $dexPath, $mergedAssetPath, $mergedJavaResourcePath, $consumerRulesPath,
    $modulePath, $outputPath, $toolsCachePath
)) {
    New-Item -ItemType Directory -Path $directory -Force | Out-Null
}

Get-ChildItem -LiteralPath $mavenDependencyPath -File | ForEach-Object {
    Copy-Item -LiteralPath $_.FullName -Destination (Join-Path $dependencyPath $_.Name)
}
Stage-LockedAndroidDependencies -LockPath $adsDependencyLockPath `
    -CachePath $adsDependencyCachePath -Destination $dependencyPath
$adConfiguration = Resolve-AdConfiguration

$resourceArchives = [System.Collections.Generic.List[string]]::new()
$programArchives = [System.Collections.Generic.List[string]]::new()
$androidResourcePackages = [System.Collections.Generic.List[string]]::new()
$aarDirectories = [System.Collections.Generic.List[string]]::new()
$libraryManifestPaths = [System.Collections.Generic.List[string]]::new()
$consumerRuleInventory = [System.Collections.Generic.List[string]]::new()
$aarFiles = @(Get-ChildItem -LiteralPath $dependencyPath -Filter '*.aar' | Sort-Object Name)

for ($aarIndex = 0; $aarIndex -lt $aarFiles.Count; $aarIndex++) {
    $aarFile = $aarFiles[$aarIndex]
    $unpackedAarPath = Join-Path $aarPath ("{0:D2}-{1}" -f $aarIndex, $aarFile.BaseName)
    Expand-JavaArchive -Archive $aarFile.FullName -Destination $unpackedAarPath -JarTool $jarPath
    $aarDirectories.Add($unpackedAarPath)

    $aarManifestPath = Join-Path $unpackedAarPath 'AndroidManifest.xml'
    if (Test-Path -LiteralPath $aarManifestPath) {
        $libraryManifestPaths.Add($aarManifestPath)
        [xml]$aarManifest = Get-Content -LiteralPath $aarManifestPath -Raw
        $aarPackage = [string]$aarManifest.manifest.package
        if ($aarPackage -and -not $androidResourcePackages.Contains($aarPackage)) {
            $androidResourcePackages.Add($aarPackage)
        }
    }

    $aarClassesPath = Join-Path $unpackedAarPath 'classes.jar'
    if (Test-Path -LiteralPath $aarClassesPath) {
        $programArchives.Add($aarClassesPath)
    }
    Get-ChildItem -LiteralPath (Join-Path $unpackedAarPath 'libs') -Filter '*.jar' -ErrorAction SilentlyContinue |
        ForEach-Object { $programArchives.Add($_.FullName) }

    $aarResourcePath = Join-Path $unpackedAarPath 'res'
    if (Test-Path -LiteralPath $aarResourcePath) {
        $compiledAarResource = Join-Path $compiledResourcePath ("{0:D2}-{1}.zip" -f $aarIndex, $aarFile.BaseName)
        Invoke-ExternalTool -FilePath $aapt2Path -Arguments @(
            'compile', '--dir', $aarResourcePath, '-o', $compiledAarResource
        )
        $resourceArchives.Add($compiledAarResource)
    }
    Copy-DirectoryContents -Source (Join-Path $unpackedAarPath 'assets') -Destination $mergedAssetPath

    foreach ($consumerRuleName in @('consumer-rules.pro', 'proguard.txt')) {
        $consumerRuleSource = Join-Path $unpackedAarPath $consumerRuleName
        if (Test-Path -LiteralPath $consumerRuleSource -PathType Leaf) {
            $consumerRuleDestination = Join-Path $consumerRulesPath `
                ("{0:D2}-{1}-{2}" -f $aarIndex, $aarFile.BaseName, $consumerRuleName)
            Copy-Item -LiteralPath $consumerRuleSource -Destination $consumerRuleDestination
            $consumerRuleInventory.Add("$($aarFile.Name)|$consumerRuleName|$consumerRuleDestination")
        }
    }
}

Set-Content -LiteralPath (Join-Path $consumerRulesPath 'inventory.txt') -Encoding UTF8 `
    -Value @(
        '# Consumer rules are recorded for a future R8 integration; this build does not shrink.'
        $consumerRuleInventory
    )

$appCompiledResources = Join-Path $compiledResourcePath 'app.zip'
Invoke-ExternalTool -FilePath $aapt2Path -Arguments @(
    'compile', '--dir', (Join-Path $androidPath 'src\main\res'), '-o', $appCompiledResources
)
$resourceArchives.Add($appCompiledResources)
Copy-DirectoryContents -Source (Join-Path $projectRootPath 'assets') -Destination $mergedAssetPath

$generatedAdPackagePath = Join-Path $generatedSourcePath 'com\game\diver\android\generated'
New-Item -ItemType Directory -Path $generatedAdPackagePath -Force | Out-Null
$adFactoryExpression = switch ($adConfiguration.Mode) {
    'DISABLED' { 'AdConfiguration.disabled()' }
    'TEST' { 'AdConfiguration.test()' }
    'PRODUCTION' {
        'AdConfiguration.production("{0}", "{1}", "{2}")' -f `
            $adConfiguration.AppId, $adConfiguration.RewardedId, $adConfiguration.InterstitialId
    }
}
$generatedAdConfiguration = @"
package com.game.diver.android.generated;

import com.game.ads.AdConfiguration;

/** Generated in android/target from external build configuration. */
public final class AdBuildConfiguration {
    private AdBuildConfiguration() {
    }

    public static AdConfiguration create() {
        return $adFactoryExpression;
    }
}
"@
Write-Utf8File -Path (Join-Path $generatedAdPackagePath 'AdBuildConfiguration.java') `
    -Content $generatedAdConfiguration

$adMetadata = @"
        <meta-data
            android:name="com.game.diver.deepdivedrift.ADS_MODE"
            android:value="$($adConfiguration.Mode)" />
"@
if ($adConfiguration.Mode -ne 'DISABLED') {
    $adMetadata += @"

        <meta-data
            android:name="com.google.android.gms.ads.APPLICATION_ID"
            android:value="$($adConfiguration.AppId)" />
"@
}
$configurationManifestPath = Join-Path $workPath 'advertising-overlay-manifest.xml'
$configurationManifest = @"
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    package="com.game.diver.deepdivedrift">
    <application>
$adMetadata
    </application>
</manifest>
"@
Write-Utf8File -Path $configurationManifestPath -Content $configurationManifest

$mergedManifestPath = Join-Path $workPath 'AndroidManifest.xml'
$manifestMergerArguments = [System.Collections.Generic.List[object]]::new()
foreach ($argument in @(
    '-cp', (Join-Path $manifestMergerToolPath '*'),
    'com.android.manifmerger.Merger',
    '--main', (Join-Path $androidPath 'src\main\AndroidManifest.xml'),
    '--overlays', $configurationManifestPath,
    '--property', "MIN_SDK_VERSION=$MinSdk",
    '--property', "TARGET_SDK_VERSION=$TargetSdk",
    '--out', $mergedManifestPath,
    '--remove-tools-declarations'
)) {
    $manifestMergerArguments.Add($argument)
}
if ($libraryManifestPaths.Count -gt 0) {
    $manifestMergerArguments.Add('--libs')
    $manifestMergerArguments.Add(($libraryManifestPaths -join [System.IO.Path]::PathSeparator))
}
Invoke-ExternalTool -FilePath $javaPath -Arguments $manifestMergerArguments.ToArray()
if (-not (Test-Path -LiteralPath $mergedManifestPath -PathType Leaf)) {
    throw 'Official Android manifest merger did not produce a manifest.'
}

$linkedResourcesPath = Join-Path $workPath 'linked-resources.ap_'
$linkArguments = [System.Collections.Generic.List[object]]::new()
foreach ($argument in @(
    'link', '--proto-format', '-o', $linkedResourcesPath,
    '-I', $androidJarPath,
    '--manifest', $mergedManifestPath,
    '--min-sdk-version', [string]$MinSdk,
    '--target-sdk-version', [string]$TargetSdk,
    '--version-code', [string]$VersionCode,
    '--version-name', $Version,
    '--replace-version', '--auto-add-overlay',
    '--java', $generatedSourcePath,
    '-A', $mergedAssetPath
)) {
    $linkArguments.Add($argument)
}
if ($androidResourcePackages.Count -gt 0) {
    $linkArguments.Add('--extra-packages')
    $linkArguments.Add(($androidResourcePackages -join ':'))
}
foreach ($resourceArchive in $resourceArchives) {
    $linkArguments.Add('-R')
    $linkArguments.Add($resourceArchive)
}
Invoke-ExternalTool -FilePath $aapt2Path -Arguments $linkArguments.ToArray()

$regularDependencyJars = @(Get-ChildItem -LiteralPath $dependencyPath -Filter '*.jar' |
    Where-Object { $_.Name -notmatch '-natives-[^.]+\.jar$' } |
    Sort-Object Name)
foreach ($dependencyJar in $regularDependencyJars) {
    $programArchives.Add($dependencyJar.FullName)
}

for ($programIndex = 0; $programIndex -lt $programArchives.Count; $programIndex++) {
    Merge-JavaArchiveResources -Archive $programArchives[$programIndex] `
        -Destination $mergedJavaResourcePath -ConsumerRulesDestination $consumerRulesPath `
        -ArchiveIndex $programIndex -ConsumerRuleInventory $consumerRuleInventory
}
Set-Content -LiteralPath (Join-Path $consumerRulesPath 'inventory.txt') -Encoding UTF8 `
    -Value @(
        '# Consumer rules are recorded for a future R8 integration; this build does not shrink.'
        $consumerRuleInventory
    )

$compileClasspath = @($androidJarPath) + @($programArchives)
$javaSources = @(
    Get-ChildItem -LiteralPath (Join-Path $androidPath 'src\main\java') -Filter '*.java' -Recurse
    Get-ChildItem -LiteralPath $generatedSourcePath -Filter '*.java' -Recurse
) | ForEach-Object { $_.FullName }
if ($javaSources.Count -eq 0) {
    throw 'Android launcher or generated R.java sources are missing.'
}
$javacArguments = @(
    '--release', '17',
    '-encoding', 'UTF-8',
    '-classpath', ($compileClasspath -join [System.IO.Path]::PathSeparator),
    '-d', $compiledClassPath
) + $javaSources
Invoke-ExternalTool -FilePath $javacPath -Arguments $javacArguments

$launcherArchivePath = Join-Path $workPath 'android-launcher.jar'
Invoke-ExternalTool -FilePath $jarPath -Arguments @(
    '--create', '--file', $launcherArchivePath, '-C', $compiledClassPath, '.'
)
$d8Inputs = @($launcherArchivePath) + @($programArchives)
$d8Arguments = @(
    '--release', '--min-api', [string]$MinSdk,
    '--lib', $androidJarPath,
    '--output', $dexPath
) + $d8Inputs
$d8ArgumentFile = Join-Path $workPath 'd8-arguments.txt'
$responseFileQuote = [string][char]34
$d8ArgumentLines = $d8Arguments | ForEach-Object {
    $value = [string]$_
    if ($value -match '[\s"]') {
        $responseFileQuote + $value.Replace(
            $responseFileQuote, ('\' + $responseFileQuote)) + $responseFileQuote
    } else {
        $value
    }
}
Write-Utf8File -Path $d8ArgumentFile -Content ($d8ArgumentLines -join "`n")
Invoke-ExternalTool -FilePath $d8Path -Arguments @("@$d8ArgumentFile")

Expand-JavaArchive -Archive $linkedResourcesPath -Destination $modulePath -JarTool $jarPath
$moduleManifestPath = Join-Path $modulePath 'manifest'
New-Item -ItemType Directory -Path $moduleManifestPath -Force | Out-Null
Move-Item -LiteralPath (Join-Path $modulePath 'AndroidManifest.xml') `
    -Destination (Join-Path $moduleManifestPath 'AndroidManifest.xml') -Force
Copy-DirectoryContents -Source $dexPath -Destination (Join-Path $modulePath 'dex')
Copy-DirectoryContents -Source $mergedJavaResourcePath -Destination (Join-Path $modulePath 'root')

$expectedAbis = @('armeabi-v7a', 'arm64-v8a', 'x86', 'x86_64')
foreach ($abi in $expectedAbis) {
    $nativeDestination = Join-Path $modulePath "lib\$abi"
    New-Item -ItemType Directory -Path $nativeDestination -Force | Out-Null
    $nativeArchives = @(Get-ChildItem -LiteralPath $dependencyPath -Filter "*-natives-$abi.jar")
    foreach ($nativeArchive in $nativeArchives) {
        $nativeWorkPath = Join-Path $workPath ("native-{0}-{1}" -f $abi, $nativeArchive.BaseName)
        Expand-JavaArchive -Archive $nativeArchive.FullName -Destination $nativeWorkPath -JarTool $jarPath
        Get-ChildItem -LiteralPath $nativeWorkPath -Filter '*.so' -Recurse |
            ForEach-Object { Copy-Item -LiteralPath $_.FullName -Destination $nativeDestination -Force }
    }
    foreach ($unpackedAarPath in $aarDirectories) {
        $aarNativePath = Join-Path $unpackedAarPath "jni\$abi"
        if (Test-Path -LiteralPath $aarNativePath) {
            Copy-DirectoryContents -Source $aarNativePath -Destination $nativeDestination
        }
    }
    if ((Get-ChildItem -LiteralPath $nativeDestination -Filter '*.so').Count -lt 2) {
        throw "Expected native libraries are missing for $abi"
    }
}

$baseModuleArchivePath = Join-Path $workPath 'base.zip'
Invoke-ExternalTool -FilePath $jarPath -Arguments @(
    'cMf', $baseModuleArchivePath, '-C', $modulePath, '.'
)

$bundletoolPath = Join-Path $toolsCachePath "bundletool-all-$BundletoolVersion.jar"
$bundletoolSha256 = 'a099cfa1543f55593bc2ed16a70a7c67fe54b1747bb7301f37fdfd6d91028e29'
if (-not (Test-Path -LiteralPath $bundletoolPath)) {
    $bundletoolUrl = "https://github.com/google/bundletool/releases/download/$BundletoolVersion/bundletool-all-$BundletoolVersion.jar"
    Write-Host "Downloading bundletool $BundletoolVersion..."
    Invoke-WebRequest -UseBasicParsing -Uri $bundletoolUrl -OutFile $bundletoolPath
}
$actualBundletoolSha256 = (Get-FileHash -LiteralPath $bundletoolPath -Algorithm SHA256).Hash.ToLowerInvariant()
if ($actualBundletoolSha256 -ne $bundletoolSha256) {
    throw "bundletool SHA-256 mismatch. Expected $bundletoolSha256, got $actualBundletoolSha256"
}

$unsignedBundlePath = Join-Path $workPath "DeepDiveDrift-$Version-google-play-unsigned.aab"
$bundleConfigPath = Join-Path $androidPath 'BundleConfig.json'
# Android MediaPlayer/SoundPool use AssetManager.openFd, which requires STORED audio.
# Store this rule in the AAB so Google Play's generated APKs also preserve it.
Invoke-ExternalTool -FilePath $javaPath -Arguments @(
    '-jar', $bundletoolPath, 'build-bundle',
    "--modules=$baseModuleArchivePath", "--output=$unsignedBundlePath",
    "--config=$bundleConfigPath", '--overwrite'
)

$keystorePropertiesPath = Join-Path $projectRootPath 'keystore.properties'
$isSigned = Test-Path -LiteralPath $keystorePropertiesPath
if ($isSigned) {
    $keystoreProperties = Read-PropertiesFile $keystorePropertiesPath
    foreach ($requiredKey in @('storeFile', 'storePassword', 'keyAlias', 'keyPassword')) {
        if (-not $keystoreProperties.ContainsKey($requiredKey) -or -not $keystoreProperties[$requiredKey]) {
            throw "keystore.properties is missing $requiredKey"
        }
    }
    $keystorePath = [System.IO.Path]::GetFullPath(
        (Join-Path $projectRootPath $keystoreProperties['storeFile']))
    if (-not (Test-Path -LiteralPath $keystorePath)) {
        throw "Android upload keystore is missing: $keystorePath"
    }
    $env:DEEPDIVE_STORE_PASSWORD = $keystoreProperties['storePassword']
    $env:DEEPDIVE_KEY_PASSWORD = $keystoreProperties['keyPassword']
    try {
        Invoke-ExternalTool -FilePath $jarsignerPath -Arguments @(
            '-sigalg', 'SHA256withRSA', '-digestalg', 'SHA-256',
            '-keystore', $keystorePath,
            '-storepass:env', 'DEEPDIVE_STORE_PASSWORD',
            '-keypass:env', 'DEEPDIVE_KEY_PASSWORD',
            $unsignedBundlePath, $keystoreProperties['keyAlias']
        )
    } finally {
        Remove-Item Env:DEEPDIVE_STORE_PASSWORD -ErrorAction SilentlyContinue
        Remove-Item Env:DEEPDIVE_KEY_PASSWORD -ErrorAction SilentlyContinue
    }
}

$bundleName = "DeepDiveDrift-$Version-google-play$(if ($isSigned) { '' } else { '-unsigned' }).aab"
$releaseBundlePath = Join-Path $outputPath $bundleName
Copy-Item -LiteralPath $unsignedBundlePath -Destination $releaseBundlePath -Force

$debugKeystorePath = Join-Path $toolsCachePath 'android-debug.keystore'
if (-not (Test-Path -LiteralPath $debugKeystorePath)) {
    Invoke-ExternalTool -FilePath $keytoolPath -Arguments @(
        '-genkeypair', '-noprompt',
        '-keystore', $debugKeystorePath,
        '-storepass', 'android',
        '-alias', 'androiddebugkey',
        '-keypass', 'android',
        '-dname', 'CN=Android Debug,O=Android,C=US',
        '-keyalg', 'RSA', '-keysize', '2048', '-validity', '10000'
    )
}

$apksPath = Join-Path $workPath 'universal.apks'
Invoke-ExternalTool -FilePath $javaPath -Arguments @(
    '-jar', $bundletoolPath, 'build-apks',
    "--bundle=$releaseBundlePath", "--output=$apksPath", '--mode=universal', '--overwrite',
    "--ks=$debugKeystorePath", '--ks-pass=pass:android',
    '--ks-key-alias=androiddebugkey', '--key-pass=pass:android'
)
$universalApkPath = Join-Path $workPath 'universal-apk'
Expand-JavaArchive -Archive $apksPath -Destination $universalApkPath -JarTool $jarPath
$releaseApkPath = Join-Path $outputPath "DeepDiveDrift-$Version-universal.apk"
Copy-Item -LiteralPath (Join-Path $universalApkPath 'universal.apk') -Destination $releaseApkPath -Force

Invoke-ExternalTool -FilePath $zipalignPath -Arguments @(
    '-c', '-P', '16', '-v', '4', $releaseApkPath
)
Invoke-ExternalTool -FilePath $javaPath -Arguments @(
    (Join-Path $PSScriptRoot 'VerifyAndroidAudio.java'), $releaseApkPath,
    (Join-Path $projectRootPath 'assets')
)
$apkSha256 = (Get-FileHash -LiteralPath $releaseApkPath -Algorithm SHA256).Hash.ToLowerInvariant()
Set-Content -LiteralPath "$releaseApkPath.sha256" -Encoding ASCII `
    -Value "$apkSha256  $([System.IO.Path]::GetFileName($releaseApkPath))"

Write-Host 'Android release package is ready:'
Write-Host "  AAB: $releaseBundlePath"
Write-Host "  Universal test APK: $releaseApkPath"
Write-Host "  Signature: $(if ($isSigned) { 'upload-key signed' } else { 'unsigned AAB' })"
