param(
    [switch]$Install,
    [switch]$SkipBuild,
    [string]$DeviceId = ''
)

$ErrorActionPreference = 'Stop'
$projectRootPath = [System.IO.Path]::GetFullPath((Join-Path $PSScriptRoot '..'))
$pomPath = Join-Path $projectRootPath 'pom.xml'
[xml]$projectPom = Get-Content -LiteralPath $pomPath -Raw
$androidVersion = & (Join-Path $PSScriptRoot 'get-project-version.ps1') -ProjectRootPath $projectRootPath -Android
$version = $androidVersion.Version
$apkPath = Join-Path $projectRootPath "android\target\store\google-play\DeepDiveDrift-$version-universal.apk"

if ($DeviceId -and -not $Install) {
    throw 'Use -DeviceId together with -Install.'
}

if (-not $SkipBuild) {
    & (Join-Path $projectRootPath 'mvnw.cmd') -f $pomPath -B -ntp -Pandroid-release -pl android -am clean package
    if ($LASTEXITCODE -ne 0) {
        throw "Android build failed with exit code $LASTEXITCODE"
    }
}

& (Join-Path $PSScriptRoot 'verify-android-release.ps1') -SkipBuild
if (-not (Test-Path -LiteralPath $apkPath -PathType Leaf)) {
    throw "Test APK is missing: $apkPath"
}

$apkSizeMb = [Math]::Round((Get-Item -LiteralPath $apkPath).Length / 1MB, 1)
Write-Host "Phone test APK ($apkSizeMb MB): $apkPath"
Write-Host 'This APK uses a local test signing key. Google Play uploads use the signed AAB.'
if (-not $Install) {
    Write-Host 'Copy the APK to your phone and open it to install.'
    Write-Host 'For USB installation, enable USB debugging and rerun with -SkipBuild -Install.'
    return
}

function Resolve-AdbPath {
    $sdkDirectory = ''
    $localPropertiesPath = Join-Path $projectRootPath 'local.properties'
    if (Test-Path -LiteralPath $localPropertiesPath) {
        foreach ($line in Get-Content -LiteralPath $localPropertiesPath) {
            if ($line -match '^\s*sdk\.dir\s*=(.*)$') {
                $sdkDirectory = $Matches[1].Trim().Replace('\:', ':').Replace('\\', '\')
                break
            }
        }
    }
    if (-not $sdkDirectory) { $sdkDirectory = $env:ANDROID_SDK_ROOT }
    if (-not $sdkDirectory) { $sdkDirectory = $env:ANDROID_HOME }
    if ($sdkDirectory) {
        if (-not [System.IO.Path]::IsPathRooted($sdkDirectory)) {
            $sdkDirectory = Join-Path $projectRootPath $sdkDirectory
        }
        $sdkAdbPath = Join-Path $sdkDirectory 'platform-tools\adb.exe'
        if (Test-Path -LiteralPath $sdkAdbPath -PathType Leaf) { return $sdkAdbPath }
    }
    $adbCommand = Get-Command adb.exe -ErrorAction SilentlyContinue
    if ($adbCommand) { return $adbCommand.Source }
    throw 'adb.exe is missing. Install Android SDK Platform-Tools in the configured SDK.'
}

$adbPath = Resolve-AdbPath
& $adbPath start-server
if ($LASTEXITCODE -ne 0) { throw 'Unable to start ADB.' }
$deviceOutput = @(& $adbPath devices -l)
if ($LASTEXITCODE -ne 0) { throw 'Unable to list Android devices.' }
$devices = @(
    foreach ($line in $deviceOutput) {
        if ($line -match '^(\S+)\s+(device|unauthorized|offline)\b') {
            [pscustomobject]@{ Id = $Matches[1]; State = $Matches[2] }
        }
    }
)
if ($devices.Count -eq 0) {
    throw "No Android device found. Connect a USB data cable, enable USB debugging, and accept the phone's authorization prompt. APK is ready at: $apkPath"
}
if ($DeviceId) {
    $selectedDevice = $devices | Where-Object { $_.Id -eq $DeviceId } | Select-Object -First 1
    if (-not $selectedDevice) { throw "Device '$DeviceId' was not found. Run adb devices -l to list devices." }
} else {
    if ($devices.Count -gt 1) {
        throw "Multiple devices found: $($devices.Id -join ', '). Select one with -DeviceId SERIAL."
    }
    $selectedDevice = $devices[0]
}
if ($selectedDevice.State -ne 'device') {
    throw "Device '$($selectedDevice.Id)' is $($selectedDevice.State). Unlock the phone, accept USB debugging authorization, or reconnect the cable."
}
$adbTarget = @('-s', $selectedDevice.Id)
$deviceSdkOutput = @(& $adbPath @adbTarget shell getprop ro.build.version.sdk)
if ($LASTEXITCODE -ne 0) { throw 'Unable to read the Android version from the device.' }
$deviceSdk = 0
if (-not [int]::TryParse(($deviceSdkOutput -join '').Trim(), [ref]$deviceSdk)) {
    throw 'The device did not return a valid Android API level.'
}
$minSdk = [int]$projectPom.project.properties.'android.min-sdk'
if ($deviceSdk -lt $minSdk) {
    throw "This build requires Android API $minSdk or later; the device has API $deviceSdk."
}

Write-Host "Installing on $($selectedDevice.Id) (Android API $deviceSdk)..."
$installOutput = @(& $adbPath @adbTarget install -r $apkPath)
$installExitCode = $LASTEXITCODE
$installOutput | ForEach-Object { Write-Host $_ }
if ($installExitCode -ne 0 -or ($installOutput -join "`n") -notmatch '(?m)^Success\s*$') {
    throw 'APK installation failed. If INSTALL_FAILED_UPDATE_INCOMPATIBLE appears, the installed copy has a different signing key. Uninstalling it removes its local saves; no automatic uninstall was performed.'
}

[xml]$manifest = Get-Content -LiteralPath (Join-Path $projectRootPath 'android\src\main\AndroidManifest.xml') -Raw
$packageName = [string]$manifest.manifest.package
$activityName = $manifest.manifest.application.activity.GetAttribute('name', 'http://schemas.android.com/apk/res/android')
$launchOutput = @(& $adbPath @adbTarget shell am start -W -n "$packageName/$activityName")
$launchExitCode = $LASTEXITCODE
$launchOutput | ForEach-Object { Write-Host $_ }
if ($launchExitCode -ne 0 -or ($launchOutput -join "`n") -match '(?im)^\s*(Error|Exception)' -or
    ($launchOutput -join "`n") -notmatch '(?im)^Status:\s*ok\s*$') {
    throw 'The APK was installed, but Android could not start the game. Check adb logcat for details.'
}
Write-Host 'Android accepted the launch request. Check the phone for the game menu and test touch controls.'
