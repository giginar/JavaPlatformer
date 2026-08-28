param(
    [string]$Version = ''
)

$ErrorActionPreference = 'Stop'
$projectRootPath = [System.IO.Path]::GetFullPath((Join-Path $PSScriptRoot '..'))
$desktopTargetPath = Join-Path $projectRootPath 'lwjgl3\target'
$mavenWrapperPath = Join-Path $projectRootPath 'mvnw.cmd'
$pomPath = Join-Path $projectRootPath 'pom.xml'
$versionScriptPath = Join-Path $PSScriptRoot 'get-project-version.ps1'

[xml]$projectPom = Get-Content -LiteralPath $pomPath -Raw
$mavenVersion = ([string]$projectPom.project.version).Trim()

if (-not $Version) {
    $Version = (& $versionScriptPath -ProjectRootPath $projectRootPath).Trim()
}
$Version = $Version.Trim()
if ($Version -notmatch '^\d+\.\d+(\.\d+)?$') {
    throw "The Windows installer version must use major.minor or major.minor.build format, got: $Version"
}
$versionParts = @($Version.Split('.') | ForEach-Object { [int]$_ })
$windowsBuildVersion = if ($versionParts.Count -eq 3) { $versionParts[2] } else { 0 }
if ($versionParts[0] -gt 255 -or $versionParts[1] -gt 255 -or $windowsBuildVersion -gt 65535) {
    throw "The Windows installer version exceeds the 255.255.65535 limit: $Version"
}

Write-Host "Building Windows installer version $Version..."

& $mavenWrapperPath -f $pomPath -B -ntp -pl lwjgl3 -am clean package
if ($LASTEXITCODE -ne 0) {
    throw "Desktop build failed with exit code $LASTEXITCODE"
}

$builtJarName = "DeepDiveDrift-$mavenVersion.jar"
$desktopJarPath = Join-Path $desktopTargetPath $builtJarName
if (-not (Test-Path -LiteralPath $desktopJarPath)) {
    throw "Desktop JAR is missing: $desktopJarPath"
}
$packagedJarName = "DeepDiveDrift-$Version.jar"

$toolsCachePath = Join-Path $projectRootPath '.mvn\tools'
$wixArchivePath = Join-Path $toolsCachePath 'wix314-binaries.zip'
$wixDirectoryPath = Join-Path $toolsCachePath 'wix-3.14.1'
$wixDownloadUrl = 'https://github.com/wixtoolset/wix3/releases/download/wix3141rtm/wix314-binaries.zip'
$wixSha256 = '6ac824e1642d6f7277d0ed7ea09411a508f6116ba6fae0aa5f2c7daa2ff43d31'

New-Item -ItemType Directory -Path $toolsCachePath -Force | Out-Null
if (-not (Test-Path -LiteralPath $wixArchivePath)) {
    Write-Host 'Downloading WiX Toolset 3.14.1...'
    Invoke-WebRequest -UseBasicParsing -Uri $wixDownloadUrl -OutFile $wixArchivePath
}
$actualWixSha256 = (Get-FileHash -LiteralPath $wixArchivePath -Algorithm SHA256).Hash.ToLowerInvariant()
if ($actualWixSha256 -ne $wixSha256) {
    throw "WiX SHA-256 mismatch. Expected $wixSha256, got $actualWixSha256"
}

$candlePath = Get-ChildItem -LiteralPath $wixDirectoryPath -Filter 'candle.exe' `
    -Recurse -ErrorAction SilentlyContinue | Select-Object -First 1
$lightPath = Get-ChildItem -LiteralPath $wixDirectoryPath -Filter 'light.exe' `
    -Recurse -ErrorAction SilentlyContinue | Select-Object -First 1
if (-not $candlePath -or -not $lightPath) {
    New-Item -ItemType Directory -Path $wixDirectoryPath -Force | Out-Null
    Expand-Archive -LiteralPath $wixArchivePath -DestinationPath $wixDirectoryPath -Force
    $candlePath = Get-ChildItem -LiteralPath $wixDirectoryPath -Filter 'candle.exe' -Recurse |
        Select-Object -First 1
    $lightPath = Get-ChildItem -LiteralPath $wixDirectoryPath -Filter 'light.exe' -Recurse |
        Select-Object -First 1
}
if (-not $candlePath -or -not $lightPath) {
    throw 'WiX candle.exe or light.exe could not be found after extraction.'
}

$inputPath = Join-Path $desktopTargetPath 'jpackage-input'
$installerPath = Join-Path $desktopTargetPath 'installer'
New-Item -ItemType Directory -Path $inputPath, $installerPath -Force | Out-Null
Copy-Item -LiteralPath $desktopJarPath -Destination (Join-Path $inputPath $packagedJarName) -Force

$jpackageCommand = Get-Command 'jpackage.exe' -ErrorAction Stop
$originalPath = $env:Path
$env:Path = "$($candlePath.DirectoryName);$originalPath"
try {
    $jpackageArguments = @(
        '--type', 'exe',
        '--dest', $installerPath,
        '--input', $inputPath,
        '--main-jar', $packagedJarName,
        '--main-class', 'com.game.diver.lwjgl3.Lwjgl3Launcher',
        '--name', 'DeepDive Drift',
        '--app-version', $Version,
        '--description', 'Fast-paced underwater survival game',
        '--icon', (Join-Path $projectRootPath 'lwjgl3\icons\logo.ico'),
        '--install-dir', 'DeepDive Drift',
        '--win-per-user-install',
        '--win-menu',
        '--win-menu-group', 'DeepDive Drift',
        '--win-shortcut',
        '--win-shortcut-prompt',
        '--win-dir-chooser',
        '--win-upgrade-uuid', 'daf76e86-8cc9-4a6a-8b62-6cb07445301b'
    )
    & $jpackageCommand.Source $jpackageArguments
    if ($LASTEXITCODE -ne 0) {
        throw "jpackage failed with exit code $LASTEXITCODE"
    }
} finally {
    $env:Path = $originalPath
}

$installerPath = Join-Path $installerPath "DeepDive Drift-$Version.exe"
if (-not (Test-Path -LiteralPath $installerPath)) {
    throw "Windows installer was not created: $installerPath"
}
$installer = Get-Item -LiteralPath $installerPath

$installerSha256 = (Get-FileHash -LiteralPath $installer.FullName -Algorithm SHA256).Hash
Write-Host 'Windows installer is ready:'
Write-Host "  EXE: $($installer.FullName)"
Write-Host "  Size: $([math]::Round($installer.Length / 1MB, 2)) MB"
Write-Host "  SHA-256: $installerSha256"
