param(
    [switch]$SkipBuild,
    [switch]$RequireSignedBundle
)

$ErrorActionPreference = 'Stop'
Add-Type -AssemblyName System.IO.Compression.FileSystem
$projectRootPath = [System.IO.Path]::GetFullPath((Join-Path $PSScriptRoot '..'))
$pomPath = Join-Path $projectRootPath 'pom.xml'
$mavenWrapper = Join-Path $projectRootPath 'mvnw.cmd'
$androidVerifier = Join-Path $PSScriptRoot 'verify-android-release.ps1'
$savedAdsMode = [Environment]::GetEnvironmentVariable('DEEPDRIFT_ADS_MODE', 'Process')

if (-not $SkipBuild) {
    try {
        [Environment]::SetEnvironmentVariable('DEEPDRIFT_ADS_MODE', 'TEST', 'Process')
        & $mavenWrapper -f $pomPath -B -ntp `
            '-Pandroid-release,android-optimized-release' -pl android -am package
        if ($LASTEXITCODE -ne 0) {
            throw "Optimized Android TEST build failed with exit code $LASTEXITCODE"
        }
    } finally {
        [Environment]::SetEnvironmentVariable('DEEPDRIFT_ADS_MODE', $savedAdsMode, 'Process')
    }
}

$verifyArguments = @{
    SkipBuild = $true
    OptimizationMode = 'Optimized'
    ExpectedAdsMode = 'TEST'
}
if ($RequireSignedBundle) {
    $verifyArguments.RequireSignedBundle = $true
}
& $androidVerifier @verifyArguments

[xml]$projectPom = Get-Content -LiteralPath $pomPath -Raw
$versionInfo = & (Join-Path $PSScriptRoot 'get-project-version.ps1') `
    -ProjectRootPath $projectRootPath -Android
$version = $versionInfo.Version
$releasePath = Join-Path $projectRootPath 'android\target\store\google-play'
$unoptimizedApk = Join-Path $releasePath "DeepDiveDrift-$version-universal.apk"
$optimizedApk = Join-Path $releasePath "DeepDiveDrift-$version-optimized-universal.apk"
$unoptimizedBundle = Get-ChildItem -LiteralPath $releasePath -Filter "DeepDiveDrift-$version-google-play*.aab" |
    Where-Object { $_.Name -notlike '*-optimized-*' } | Select-Object -First 1
$optimizedBundle = Get-ChildItem -LiteralPath $releasePath -Filter "DeepDiveDrift-$version-optimized-google-play*.aab" |
    Select-Object -First 1
foreach ($artifact in @($unoptimizedApk, $optimizedApk)) {
    if (-not (Test-Path -LiteralPath $artifact -PathType Leaf)) {
        throw "Size comparison artifact is missing: $artifact"
    }
}
if (-not $unoptimizedBundle -or -not $optimizedBundle) {
    throw 'Both unoptimized and optimized AABs are required for size comparison.'
}

function Get-DexBytes([string]$ArchivePath) {
    $archive = [System.IO.Compression.ZipFile]::OpenRead($ArchivePath)
    try {
        return [long](($archive.Entries | Where-Object {
            $_.FullName -match '(^|/)classes[0-9]*\.dex$'
        } | Measure-Object -Property Length -Sum).Sum)
    } finally {
        $archive.Dispose()
    }
}

function Get-AndroidSdkPath {
    $localPropertiesPath = Join-Path $projectRootPath 'local.properties'
    foreach ($line in Get-Content -LiteralPath $localPropertiesPath) {
        if ($line -match '^\s*sdk\.dir\s*=(.*)$') {
            return [System.IO.Path]::GetFullPath(
                $Matches[1].Trim().Replace('\:', ':').Replace('\\', '\'))
        }
    }
    throw 'sdk.dir is required for optimized APK metrics.'
}

function Get-DexMetrics([string]$ApkPath, [string]$ApkAnalyzerPath) {
    $packages = @(& $ApkAnalyzerPath dex packages $ApkPath)
    if ($LASTEXITCODE -ne 0) { throw "Unable to count classes in $ApkPath" }
    $references = @(& $ApkAnalyzerPath dex references $ApkPath)
    if ($LASTEXITCODE -ne 0) { throw "Unable to count method references in $ApkPath" }
    $methodReferences = 0L
    foreach ($line in $references) {
        if ($line -match '\t([0-9]+)\s*$') {
            $methodReferences += [long]$Matches[1]
        }
    }
    return [pscustomobject]@{
        Classes = @($packages | Where-Object { $_ -match '^C\s' }).Count
        MethodReferences = $methodReferences
    }
}

$apkAnalyzerPath = Join-Path (Get-AndroidSdkPath) 'cmdline-tools\latest\bin\apkanalyzer.bat'
$beforeMetrics = Get-DexMetrics $unoptimizedApk $apkAnalyzerPath
$afterMetrics = Get-DexMetrics $optimizedApk $apkAnalyzerPath
$metrics = @(
    [pscustomobject]@{ Name = 'DEX bytes'; Before = Get-DexBytes $unoptimizedApk; After = Get-DexBytes $optimizedApk },
    [pscustomobject]@{ Name = 'Universal APK bytes'; Before = (Get-Item $unoptimizedApk).Length; After = (Get-Item $optimizedApk).Length },
    [pscustomobject]@{ Name = 'AAB bytes'; Before = $unoptimizedBundle.Length; After = $optimizedBundle.Length },
    [pscustomobject]@{ Name = 'Defined classes'; Before = $beforeMetrics.Classes; After = $afterMetrics.Classes },
    [pscustomobject]@{ Name = 'Method references'; Before = $beforeMetrics.MethodReferences; After = $afterMetrics.MethodReferences }
)
$sizeReportLines = [System.Collections.Generic.List[string]]::new()
$sizeReportLines.Add('# Metric|Before|After|Difference|Percentage')
foreach ($metric in $metrics) {
    $difference = [long]$metric.After - [long]$metric.Before
    $percentage = if ($metric.Before -eq 0) {
        '0.00%'
    } else {
        '{0:F2}%' -f (100.0 * $difference / $metric.Before)
    }
    $sizeReportLines.Add(
        "$($metric.Name)|$($metric.Before)|$($metric.After)|$difference|$percentage")
    Write-Host ("  {0}: {1} -> {2} ({3}, {4})" -f
        $metric.Name, $metric.Before, $metric.After, $difference, $percentage)
}
$r8ReportPath = Join-Path $releasePath "DeepDiveDrift-$version-r8"
[System.IO.File]::WriteAllText(
    (Join-Path $r8ReportPath 'size-comparison.txt'),
    (($sizeReportLines -join "`n") + "`n"),
    [System.Text.UTF8Encoding]::new($false))

Write-Host 'Optimized Android TEST artifact verification: PASS'
