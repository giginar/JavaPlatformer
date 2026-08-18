param(
    [switch]$SkipBuild
)

$ErrorActionPreference = 'Stop'
$projectRootPath = [System.IO.Path]::GetFullPath((Join-Path $PSScriptRoot '..'))
$androidTargetPath = Join-Path $projectRootPath 'android\target'
$releasePath = Join-Path $androidTargetPath 'store\google-play'
$mavenWrapper = Join-Path $projectRootPath 'mvnw.cmd'

if (-not $SkipBuild) {
    & $mavenWrapper -B -ntp -Pandroid-release -pl android -am package
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
$sdkDirectory = [System.IO.Path]::GetFullPath($sdkDirectory)
$zipalignPath = Join-Path $sdkDirectory 'build-tools\36.0.0\zipalign.exe'
$apksignerPath = Join-Path $sdkDirectory 'build-tools\36.0.0\apksigner.bat'
$releaseApk = Get-ChildItem -LiteralPath $releasePath -Filter '*-universal.apk' |
    Sort-Object LastWriteTime -Descending | Select-Object -First 1
$releaseBundle = Get-ChildItem -LiteralPath $releasePath -Filter '*.aab' |
    Sort-Object LastWriteTime -Descending | Select-Object -First 1

if (-not $releaseApk -or -not $releaseBundle) {
    throw "Release APK or AAB is missing below $releasePath"
}

& $zipalignPath -c -P 16 -v 4 $releaseApk.FullName | Out-Null
if ($LASTEXITCODE -ne 0) {
    throw 'APK ZIP alignment is not 16 KB compatible'
}
& $apksignerPath verify --verbose $releaseApk.FullName | Out-Null
if ($LASTEXITCODE -ne 0) {
    throw 'Universal test APK signature verification failed'
}

$bundletoolPath = Get-ChildItem -LiteralPath (Join-Path $projectRootPath '.mvn\tools') `
    -Filter 'bundletool-all-*.jar' | Sort-Object Name -Descending | Select-Object -First 1
if (-not $bundletoolPath) {
    throw 'bundletool is missing; run the Android Maven release build first'
}
& java.exe -jar $bundletoolPath.FullName validate "--bundle=$($releaseBundle.FullName)"
if ($LASTEXITCODE -ne 0) {
    throw 'bundletool validation failed'
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
    return $alignments
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
    Push-Location $verificationPath
    try {
        & jar.exe xf $releaseBundle.FullName
        if ($LASTEXITCODE -ne 0) {
            throw 'Unable to extract the release AAB'
        }
    } finally {
        Pop-Location
    }

    $expectedAbis = 'armeabi-v7a', 'arm64-v8a', 'x86', 'x86_64'
    foreach ($abi in $expectedAbis) {
        $nativePath = Join-Path $verificationPath "base\lib\$abi"
        $libraries = @(Get-ChildItem -LiteralPath $nativePath -Filter '*.so' -ErrorAction SilentlyContinue)
        if ($libraries.Count -lt 2) {
            throw "Expected AAB native libraries are missing for $abi"
        }
        foreach ($library in $libraries) {
            foreach ($alignment in (Get-ElfLoadAlignments $library.FullName)) {
                if ($alignment -lt 16384) {
                    throw "$($library.FullName) has a LOAD segment aligned to only $alignment bytes"
                }
            }
        }
    }
} finally {
    if (Test-Path -LiteralPath $verificationPath) {
        Remove-Item -LiteralPath $verificationPath -Recurse -Force
    }
}

$signatureOutput = (& jarsigner.exe -verify -verbose $releaseBundle.FullName 2>&1 | Out-String)
$isSigned = $signatureOutput -notmatch 'jar is unsigned'
if ((Test-Path -LiteralPath (Join-Path $projectRootPath 'keystore.properties')) -and -not $isSigned) {
    throw 'keystore.properties exists, but the release AAB is unsigned'
}

Write-Host 'Android verification passed:'
Write-Host '  Bundletool schema: valid'
Write-Host '  APK ZIP alignment: 16 KB compatible'
Write-Host '  Universal APK signature: valid local debug key'
Write-Host '  ELF LOAD alignment: 16 KB across all four ABIs'
Write-Host "  AAB: $($releaseBundle.FullName)"
Write-Host "  AAB signature: $(if ($isSigned) { 'signed' } else { 'unsigned (upload key still required)' })"
