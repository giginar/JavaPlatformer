param(
    [Parameter(Mandatory = $true)]
    [string]$Version,

    [Parameter(Mandatory = $true)]
    [int]$VersionCode,

    [Parameter(Mandatory = $true)]
    [int]$MinSdk,

    [Parameter(Mandatory = $true)]
    [int]$TargetSdk,

    [Parameter(Mandatory = $true)]
    [string]$BuildToolsVersion,

    [Parameter(Mandatory = $true)]
    [string]$BundletoolVersion
)

$ErrorActionPreference = 'Stop'
$projectRootPath = [System.IO.Path]::GetFullPath((Join-Path $PSScriptRoot '..'))
$androidPath = Join-Path $projectRootPath 'android'
$targetPath = Join-Path $androidPath 'target'
$dependencyPath = Join-Path $targetPath 'android-dependencies'
$workPath = Join-Path $targetPath 'android-work'
$outputPath = Join-Path $targetPath 'store\google-play'
$toolsCachePath = Join-Path $projectRootPath '.mvn\tools'

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

foreach ($requiredPath in @($aapt2Path, $d8Path, $zipalignPath, $androidJarPath, $dependencyPath)) {
    if (-not (Test-Path -LiteralPath $requiredPath)) {
        throw "Required Android build input is missing: $requiredPath"
    }
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
$compiledResourcePath = Join-Path $workPath 'compiled-resources'
$generatedSourcePath = Join-Path $workPath 'generated-sources'
$compiledClassPath = Join-Path $workPath 'compiled-classes'
$dexPath = Join-Path $workPath 'dex'
$mergedAssetPath = Join-Path $workPath 'merged-assets'
$modulePath = Join-Path $workPath 'module'
foreach ($directory in @(
    $aarPath, $compiledResourcePath, $generatedSourcePath, $compiledClassPath,
    $dexPath, $mergedAssetPath, $modulePath, $outputPath, $toolsCachePath
)) {
    New-Item -ItemType Directory -Path $directory -Force | Out-Null
}

$resourceArchives = [System.Collections.Generic.List[string]]::new()
$programArchives = [System.Collections.Generic.List[string]]::new()
$androidResourcePackages = [System.Collections.Generic.List[string]]::new()
$aarDirectories = [System.Collections.Generic.List[string]]::new()
$aarFiles = @(Get-ChildItem -LiteralPath $dependencyPath -Filter '*.aar' | Sort-Object Name)

for ($aarIndex = 0; $aarIndex -lt $aarFiles.Count; $aarIndex++) {
    $aarFile = $aarFiles[$aarIndex]
    $unpackedAarPath = Join-Path $aarPath ("{0:D2}-{1}" -f $aarIndex, $aarFile.BaseName)
    Expand-JavaArchive -Archive $aarFile.FullName -Destination $unpackedAarPath -JarTool $jarPath
    $aarDirectories.Add($unpackedAarPath)

    $aarManifestPath = Join-Path $unpackedAarPath 'AndroidManifest.xml'
    if (Test-Path -LiteralPath $aarManifestPath) {
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
}

$appCompiledResources = Join-Path $compiledResourcePath 'app.zip'
Invoke-ExternalTool -FilePath $aapt2Path -Arguments @(
    'compile', '--dir', (Join-Path $androidPath 'src\main\res'), '-o', $appCompiledResources
)
$resourceArchives.Add($appCompiledResources)
Copy-DirectoryContents -Source (Join-Path $projectRootPath 'assets') -Destination $mergedAssetPath

$linkedResourcesPath = Join-Path $workPath 'linked-resources.ap_'
$linkArguments = [System.Collections.Generic.List[object]]::new()
foreach ($argument in @(
    'link', '--proto-format', '-o', $linkedResourcesPath,
    '-I', $androidJarPath,
    '--manifest', (Join-Path $androidPath 'src\main\AndroidManifest.xml'),
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
Invoke-ExternalTool -FilePath $d8Path -Arguments $d8Arguments

Expand-JavaArchive -Archive $linkedResourcesPath -Destination $modulePath -JarTool $jarPath
$moduleManifestPath = Join-Path $modulePath 'manifest'
New-Item -ItemType Directory -Path $moduleManifestPath -Force | Out-Null
Move-Item -LiteralPath (Join-Path $modulePath 'AndroidManifest.xml') `
    -Destination (Join-Path $moduleManifestPath 'AndroidManifest.xml') -Force
Copy-DirectoryContents -Source $dexPath -Destination (Join-Path $modulePath 'dex')

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
Invoke-ExternalTool -FilePath $javaPath -Arguments @(
    '-jar', $bundletoolPath, 'build-bundle',
    "--modules=$baseModuleArchivePath", "--output=$unsignedBundlePath", '--overwrite'
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

Write-Host 'Android release package is ready:'
Write-Host "  AAB: $releaseBundlePath"
Write-Host "  Universal test APK: $releaseApkPath"
Write-Host "  Signature: $(if ($isSigned) { 'upload-key signed' } else { 'unsigned AAB' })"
