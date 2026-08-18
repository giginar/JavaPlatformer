param(
    [Parameter(Mandatory = $true)]
    [string]$Version
)

$ErrorActionPreference = 'Stop'
$projectRootPath = [System.IO.Path]::GetFullPath((Join-Path $PSScriptRoot '..'))
$desktopTargetPath = Join-Path $projectRootPath 'lwjgl3\target'
$steamTargetPath = Join-Path $desktopTargetPath 'steam'
$toolsCachePath = Join-Path $projectRootPath '.mvn\tools'
$windowsContentPath = Join-Path $steamTargetPath 'windows-x64'
$linuxContentPath = Join-Path $steamTargetPath 'linux-x64'
$desktopJarPath = Join-Path $desktopTargetPath "DeepDiveDrift-$Version.jar"
$windowsLauncherPath = Join-Path $desktopTargetPath 'steam-launcher\DeepDiveDrift.exe'

$runtimeVersion = '21.0.11_10'
$runtimeReleaseTag = 'jdk-21.0.11%2B10'
$windowsRuntimeName = "OpenJDK21U-jre_x64_windows_hotspot_$runtimeVersion.zip"
$linuxRuntimeName = "OpenJDK21U-jre_x64_linux_hotspot_$runtimeVersion.tar.gz"
$windowsRuntimeUrl = "https://github.com/adoptium/temurin21-binaries/releases/download/$runtimeReleaseTag/$windowsRuntimeName"
$linuxRuntimeUrl = "https://github.com/adoptium/temurin21-binaries/releases/download/$runtimeReleaseTag/$linuxRuntimeName"
$windowsRuntimeSha256 = 'be26677aaa20b39a62edcaab4c8857a8b76673b0f45abc0b6143b142b62717e4'
$linuxRuntimeSha256 = 'e5038aae3ca9ff670bc696496b0728dbd23d280026bad30291cb919221ecfdcb'

function Get-VerifiedDownload {
    param(
        [Parameter(Mandatory = $true)][string]$Url,
        [Parameter(Mandatory = $true)][string]$Destination,
        [Parameter(Mandatory = $true)][string]$Sha256
    )

    if (-not (Test-Path -LiteralPath $Destination)) {
        Write-Host "Downloading $([System.IO.Path]::GetFileName($Destination))..."
        Invoke-WebRequest -UseBasicParsing -Uri $Url -OutFile $Destination
    }
    $actualSha256 = (Get-FileHash -LiteralPath $Destination -Algorithm SHA256).Hash.ToLowerInvariant()
    if ($actualSha256 -ne $Sha256) {
        throw "SHA-256 mismatch for $Destination. Expected $Sha256, got $actualSha256"
    }
}

function Copy-ExtractedRuntime {
    param(
        [Parameter(Mandatory = $true)][string]$ExtractedPath,
        [Parameter(Mandatory = $true)][string]$Destination
    )

    $runtimeRoot = Get-ChildItem -LiteralPath $ExtractedPath -Directory | Select-Object -First 1
    if (-not $runtimeRoot -or -not (Test-Path -LiteralPath (Join-Path $runtimeRoot.FullName 'bin'))) {
        throw "Extracted Java runtime has an unexpected layout: $ExtractedPath"
    }
    New-Item -ItemType Directory -Path $Destination -Force | Out-Null
    Get-ChildItem -LiteralPath $runtimeRoot.FullName -Force | ForEach-Object {
        Copy-Item -LiteralPath $_.FullName -Destination $Destination -Recurse -Force
    }
}

foreach ($requiredPath in @($desktopJarPath, $windowsLauncherPath)) {
    if (-not (Test-Path -LiteralPath $requiredPath)) {
        throw "Required Steam build input is missing: $requiredPath"
    }
}

$resolvedDesktopTarget = [System.IO.Path]::GetFullPath($desktopTargetPath).TrimEnd('\') + '\'
$resolvedSteamTarget = [System.IO.Path]::GetFullPath($steamTargetPath)
if (-not $resolvedSteamTarget.StartsWith($resolvedDesktopTarget, [System.StringComparison]::OrdinalIgnoreCase)) {
    throw "Refusing to clean a Steam directory outside lwjgl3 target: $resolvedSteamTarget"
}
if (Test-Path -LiteralPath $steamTargetPath) {
    Remove-Item -LiteralPath $steamTargetPath -Recurse -Force
}
New-Item -ItemType Directory -Path $toolsCachePath, $windowsContentPath, $linuxContentPath -Force | Out-Null

$windowsRuntimeArchive = Join-Path $toolsCachePath $windowsRuntimeName
$linuxRuntimeArchive = Join-Path $toolsCachePath $linuxRuntimeName
Get-VerifiedDownload -Url $windowsRuntimeUrl -Destination $windowsRuntimeArchive -Sha256 $windowsRuntimeSha256
Get-VerifiedDownload -Url $linuxRuntimeUrl -Destination $linuxRuntimeArchive -Sha256 $linuxRuntimeSha256

$temporaryRootPath = Join-Path ([System.IO.Path]::GetTempPath()) `
    ("deepdive-steam-{0}" -f [System.Guid]::NewGuid().ToString('N'))
$windowsRuntimeWorkPath = Join-Path $temporaryRootPath 'windows'
$linuxRuntimeWorkPath = Join-Path $temporaryRootPath 'linux'
New-Item -ItemType Directory -Path $windowsRuntimeWorkPath, $linuxRuntimeWorkPath -Force | Out-Null
try {
    Expand-Archive -LiteralPath $windowsRuntimeArchive -DestinationPath $windowsRuntimeWorkPath -Force
    # The Linux archive models most module license files as symlinks. Windows
    # cannot create those links without Developer Mode, so extract the runtime
    # without that tree and then add the canonical java.base license files.
    & tar.exe -xzf $linuxRuntimeArchive -C $linuxRuntimeWorkPath '--exclude=*/legal/*'
    if ($LASTEXITCODE -ne 0) {
        throw "Linux Java runtime extraction failed with exit code $LASTEXITCODE"
    }
    & tar.exe -xzf $linuxRuntimeArchive -C $linuxRuntimeWorkPath `
        'jdk-21.0.11+10-jre/legal/java.base'
    if ($LASTEXITCODE -ne 0) {
        throw "Linux Java license extraction failed with exit code $LASTEXITCODE"
    }

    Copy-ExtractedRuntime -ExtractedPath $windowsRuntimeWorkPath `
        -Destination (Join-Path $windowsContentPath 'runtime')
    Copy-ExtractedRuntime -ExtractedPath $linuxRuntimeWorkPath `
        -Destination (Join-Path $linuxContentPath 'runtime')
    Copy-Item -LiteralPath $windowsLauncherPath -Destination (Join-Path $windowsContentPath 'DeepDiveDrift.exe')
    Copy-Item -LiteralPath $desktopJarPath -Destination (Join-Path $linuxContentPath 'DeepDiveDrift.jar')
} finally {
    $resolvedSystemTempPath = [System.IO.Path]::GetFullPath([System.IO.Path]::GetTempPath()).TrimEnd('\') + '\'
    $resolvedTemporaryRootPath = [System.IO.Path]::GetFullPath($temporaryRootPath)
    if ($resolvedTemporaryRootPath.StartsWith(
        $resolvedSystemTempPath, [System.StringComparison]::OrdinalIgnoreCase
    ) -and (Test-Path -LiteralPath $resolvedTemporaryRootPath)) {
        Remove-Item -LiteralPath $resolvedTemporaryRootPath -Recurse -Force
    }
}

Write-Host 'Steam depot content is ready:'
Write-Host "  Windows: $windowsContentPath"
Write-Host "  Linux: $linuxContentPath"
Write-Host '  Linux launch command: runtime/bin/java -jar DeepDiveDrift.jar'
