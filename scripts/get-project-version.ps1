[CmdletBinding()]
param(
    [string]$ProjectRootPath = '',
    [switch]$Android
)

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

if (-not $ProjectRootPath) {
    $ProjectRootPath = Join-Path $PSScriptRoot '..'
}
$ProjectRootPath = [System.IO.Path]::GetFullPath($ProjectRootPath)
$pomPath = Join-Path $ProjectRootPath 'pom.xml'

[xml]$projectPom = Get-Content -LiteralPath $pomPath -Raw
$baseVersion = ([string]$projectPom.project.version).Trim()
$baseVersionMatch = [regex]::Match(
    $baseVersion,
    '^(?<major>\d+)\.(?<minor>\d+)\.(?<build>\d+)$'
)
if (-not $baseVersionMatch.Success) {
    throw "The project version must use major.minor.build format, got: $baseVersion"
}

$major = [int]$baseVersionMatch.Groups['major'].Value
$minor = [int]$baseVersionMatch.Groups['minor'].Value
$baseBuild = [int]$baseVersionMatch.Groups['build'].Value
if ($major -gt 255 -or $minor -gt 255) {
    throw "Windows limits major and minor versions to 255, got: $baseVersion"
}

$gitCommand = Get-Command 'git.exe' -ErrorAction SilentlyContinue
if (-not $gitCommand) {
    $gitCommand = Get-Command 'git' -ErrorAction Stop
}

$commitCountOutput = & $gitCommand.Source -C $ProjectRootPath rev-list --count HEAD
if ($LASTEXITCODE -ne 0) {
    throw "Could not determine the Git commit count (exit code $LASTEXITCODE)."
}
$commitCountText = ([string]$commitCountOutput).Trim()
if ($commitCountText -notmatch '^\d+$') {
    throw "Git returned an invalid commit count: $commitCountText"
}

$build = [long]$baseBuild + [long]$commitCountText
if ($build -gt 65535) {
    throw "Windows limits the build version to 65535, got: $build"
}

$workingTreeChanges = & $gitCommand.Source -C $ProjectRootPath status --porcelain
if ($LASTEXITCODE -eq 0 -and $workingTreeChanges) {
    Write-Warning 'The working tree has uncommitted changes; the version identifies HEAD, not those changes.'
}

$version = "$major.$minor.$build"
if ($Android) {
    # Keep Android's update counter independent of changes to the base semantic version.
    $baseVersionCodeText = [string]$projectPom.project.properties.'android.version-code'
    if ($baseVersionCodeText -notmatch '^\d+$') {
        throw 'android.version-code must be a positive integer in pom.xml.'
    }
    $baseVersionCode = [long]$baseVersionCodeText
    $versionCode = $baseVersionCode + [long]$commitCountText
    if ($baseVersionCode -lt 1 -or $versionCode -gt 2100000000) {
        throw "The Android version code must be between 1 and 2100000000, got: $versionCode"
    }
    Write-Output ([pscustomobject]@{ Version = $version; VersionCode = [int]$versionCode })
} else {
    Write-Output $version
}
