param(
    [switch]$SkipBuild
)

$ErrorActionPreference = "Stop"
$projectRootPath = [System.IO.Path]::GetFullPath((Join-Path $PSScriptRoot '..'))
$gradleWrapper = Join-Path $projectRootPath 'gradlew.bat'

if (-not $SkipBuild) {
    & $gradleWrapper :android:prepareGooglePlayBundle :android:assembleRelease
    if ($LASTEXITCODE -ne 0) {
        throw "Android release build failed with exit code $LASTEXITCODE"
    }
}

$sdkDirectoryLine = Get-Content (Join-Path $projectRootPath 'local.properties') |
    Where-Object { $_ -like 'sdk.dir=*' } | Select-Object -First 1
if (-not $sdkDirectoryLine) {
    throw "local.properties does not contain sdk.dir"
}
$sdkDirectory = $sdkDirectoryLine.Split('=', 2)[1].Trim()
$sdkDirectory = $sdkDirectory.Replace('\:', ':').Replace('\\', '\')
$zipalignPath = Join-Path $sdkDirectory 'build-tools\36.0.0\zipalign.exe'
$releaseApk = Get-ChildItem (Join-Path $projectRootPath 'android\build\outputs\apk\release\*.apk') |
    Select-Object -First 1
$releaseBundle = Get-ChildItem (Join-Path $projectRootPath 'android\build\outputs\bundle\release\*.aab') |
    Select-Object -First 1

if (-not $releaseApk -or -not $releaseBundle) {
    throw "Release APK or AAB is missing"
}

& $zipalignPath -c -P 16 -v 4 $releaseApk.FullName | Out-Null
if ($LASTEXITCODE -ne 0) {
    throw "APK ZIP alignment is not 16 KB compatible"
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

$expectedAbis = 'armeabi-v7a', 'arm64-v8a', 'x86', 'x86_64'
$bundleEntries = & jar.exe tf $releaseBundle.FullName
if ($LASTEXITCODE -ne 0) {
    throw "Unable to inspect the release AAB"
}
foreach ($abi in $expectedAbis) {
    $libraries = Get-ChildItem (Join-Path $projectRootPath "android\libs\$abi\*.so")
    if ($libraries.Count -lt 2) {
        throw "Expected native libraries are missing for $abi"
    }
    foreach ($library in $libraries) {
        foreach ($alignment in (Get-ElfLoadAlignments $library.FullName)) {
            if ($alignment -lt 16384) {
                throw "$($library.FullName) has a LOAD segment aligned to only $alignment bytes"
            }
        }
    }
    if (-not ($bundleEntries -match "^base/lib/$([regex]::Escape($abi))/.+\.so$")) {
        throw "Release AAB does not contain native libraries for $abi"
    }
}

$signatureOutput = (& jarsigner.exe -verify -verbose $releaseBundle.FullName 2>&1 | Out-String)
$isSigned = $signatureOutput -notmatch 'jar is unsigned'
if ((Test-Path (Join-Path $projectRootPath 'keystore.properties')) -and -not $isSigned) {
    throw "keystore.properties exists, but the release AAB is unsigned"
}

Write-Host "Android verification passed:"
Write-Host "  APK ZIP alignment: 16 KB"
Write-Host "  ELF LOAD alignment: 16 KB across all four ABIs"
Write-Host "  AAB native libraries: all four ABIs"
Write-Host "  AAB: $($releaseBundle.FullName)"
Write-Host "  AAB signature: $(if ($isSigned) { 'signed' } else { 'unsigned (upload key still required)' })"
