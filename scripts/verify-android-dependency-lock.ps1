param(
    [string]$LockPath = '',
    [string]$ArtifactDirectory = ''
)

$ErrorActionPreference = 'Stop'
$projectRootPath = [System.IO.Path]::GetFullPath((Join-Path $PSScriptRoot '..'))
if (-not $LockPath) {
    $LockPath = Join-Path $projectRootPath 'android\ads-dependencies.lock'
}
if (-not $ArtifactDirectory) {
    $ArtifactDirectory = Join-Path $projectRootPath '.mvn\tools\android-ads-runtime'
}
$LockPath = [System.IO.Path]::GetFullPath($LockPath)
$ArtifactDirectory = [System.IO.Path]::GetFullPath($ArtifactDirectory)

if (-not (Test-Path -LiteralPath $LockPath -PathType Leaf)) {
    throw "Android dependency lock is missing: $LockPath"
}
if (-not (Test-Path -LiteralPath $ArtifactDirectory -PathType Container)) {
    throw "Locked Android artifact directory is missing: $ArtifactDirectory"
}

$expectedFiles = [System.Collections.Generic.HashSet[string]]::new(
    [System.StringComparer]::OrdinalIgnoreCase)
$coordinates = [System.Collections.Generic.HashSet[string]]::new(
    [System.StringComparer]::Ordinal)
$entryCount = 0
foreach ($line in Get-Content -LiteralPath $LockPath) {
    $trimmed = $line.Trim()
    if (-not $trimmed -or $trimmed.StartsWith('#')) { continue }
    $parts = $trimmed.Split('|')
    if ($parts.Count -ne 6) { throw "Invalid dependency lock entry: $trimmed" }
    $repository, $group, $artifact, $version, $extension, $expectedSha256 = $parts
    if ($repository -notin @('google', 'central') -or $extension -notin @('aar', 'jar') -or
        $expectedSha256 -notmatch '^[0-9a-f]{64}$') {
        throw "Invalid dependency lock repository, type, or checksum: $trimmed"
    }
    $coordinate = "$group`:$artifact`:$version`:$extension"
    $fileName = "$artifact-$version.$extension"
    if (-not $coordinates.Add($coordinate)) { throw "Duplicate locked coordinate: $coordinate" }
    if (-not $expectedFiles.Add($fileName)) { throw "Duplicate locked filename: $fileName" }
    $artifactPath = Join-Path $ArtifactDirectory $fileName
    if (-not (Test-Path -LiteralPath $artifactPath -PathType Leaf)) {
        throw "Locked Android artifact is missing: $fileName"
    }
    $actualSha256 = (Get-FileHash -LiteralPath $artifactPath -Algorithm SHA256).Hash.ToLowerInvariant()
    if ($actualSha256 -ne $expectedSha256) {
        throw "SHA-256 mismatch for $fileName. Expected $expectedSha256, got $actualSha256"
    }
    $entryCount++
}
if ($entryCount -eq 0) { throw 'Android dependency lock contains no artifacts.' }

$unexpectedFiles = @(Get-ChildItem -LiteralPath $ArtifactDirectory -File |
    Where-Object { $_.Extension -in @('.aar', '.jar') -and -not $expectedFiles.Contains($_.Name) })
if ($unexpectedFiles.Count -gt 0) {
    throw "Unexpected locked Android artifact(s): $($unexpectedFiles.Name -join ', ')"
}

Write-Host "Android dependency lock: PASS ($entryCount pinned artifacts, SHA-256 verified, exact inventory)"
