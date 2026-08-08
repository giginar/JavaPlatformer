param(
    [Parameter(Mandatory = $true)]
    [string]$AppId,

    [Parameter(Mandatory = $true)]
    [string]$WindowsDepotId,

    [string]$LinuxDepotId = "",
    [string]$SetLiveBranch = ""
)

$ErrorActionPreference = "Stop"
$projectRootPath = [System.IO.Path]::GetFullPath((Join-Path $PSScriptRoot '..'))
$generatedDirectory = Join-Path $projectRootPath 'steam\generated'
$gradleWrapper = Join-Path $projectRootPath 'gradlew.bat'

& $gradleWrapper :lwjgl3:prepareSteamWinX64
if ($LASTEXITCODE -ne 0) {
    throw "Windows Steam package failed with exit code $LASTEXITCODE"
}

if ($LinuxDepotId) {
    & $gradleWrapper :lwjgl3:prepareSteamLinuxX64
    if ($LASTEXITCODE -ne 0) {
        throw "Linux Steam package failed with exit code $LASTEXITCODE"
    }
}

New-Item -ItemType Directory -Path $generatedDirectory -Force | Out-Null

function Convert-ToVdfPath([string]$Path) {
    return [System.IO.Path]::GetFullPath($Path).Replace('\', '/')
}

$depotTemplatePath = Join-Path $projectRootPath 'steam\depot_build.vdf.template'
$appTemplatePath = Join-Path $projectRootPath 'steam\app_build.vdf.template'
$depotTemplate = [System.IO.File]::ReadAllText($depotTemplatePath)
$appTemplate = [System.IO.File]::ReadAllText($appTemplatePath)

$windowsDepotScript = Join-Path $generatedDirectory 'depot_windows.vdf'
$windowsContent = Join-Path $projectRootPath 'lwjgl3\build\steam\windows-x64'
$windowsVdf = $depotTemplate.Replace('{{DEPOT_ID}}', $WindowsDepotId)
$windowsVdf = $windowsVdf.Replace('{{CONTENT_ROOT}}', (Convert-ToVdfPath $windowsContent))
[System.IO.File]::WriteAllText($windowsDepotScript, $windowsVdf)

$linuxDepotEntry = ''
if ($LinuxDepotId) {
    $linuxDepotScript = Join-Path $generatedDirectory 'depot_linux.vdf'
    $linuxContent = Join-Path $projectRootPath 'lwjgl3\build\steam\linux-x64'
    $linuxVdf = $depotTemplate.Replace('{{DEPOT_ID}}', $LinuxDepotId)
    $linuxVdf = $linuxVdf.Replace('{{CONTENT_ROOT}}', (Convert-ToVdfPath $linuxContent))
    [System.IO.File]::WriteAllText($linuxDepotScript, $linuxVdf)
    $linuxDepotEntry = '        "' + $LinuxDepotId + '" "' + (Convert-ToVdfPath $linuxDepotScript) + '"'
}

$version = (Get-Content (Join-Path $projectRootPath 'gradle.properties') |
    Where-Object { $_ -like 'projectVersion=*' }).Split('=', 2)[1]
$appVdf = $appTemplate.Replace('{{APP_ID}}', $AppId)
$appVdf = $appVdf.Replace('{{VERSION}}', $version)
$appVdf = $appVdf.Replace('{{BUILD_OUTPUT}}', (Convert-ToVdfPath (Join-Path $generatedDirectory 'output')))
$appVdf = $appVdf.Replace('{{PROJECT_ROOT}}', (Convert-ToVdfPath $projectRootPath))
$appVdf = $appVdf.Replace('{{SET_LIVE_BRANCH}}', $SetLiveBranch)
$appVdf = $appVdf.Replace('{{WINDOWS_DEPOT_ID}}', $WindowsDepotId)
$appVdf = $appVdf.Replace('{{WINDOWS_DEPOT_SCRIPT}}', (Convert-ToVdfPath $windowsDepotScript))
$appVdf = $appVdf.Replace('{{LINUX_DEPOT_ENTRY}}', $linuxDepotEntry)

$appBuildScript = Join-Path $generatedDirectory 'app_build.vdf'
[System.IO.File]::WriteAllText($appBuildScript, $appVdf)
Write-Host "Steam depot content and VDF files are ready: $appBuildScript"
