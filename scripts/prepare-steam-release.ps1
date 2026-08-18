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
$mavenWrapper = Join-Path $projectRootPath 'mvnw.cmd'

& $mavenWrapper -B -ntp -Psteam -pl lwjgl3 -am verify
if ($LASTEXITCODE -ne 0) {
    throw "Steam package build failed with exit code $LASTEXITCODE"
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
$windowsContent = Join-Path $projectRootPath 'lwjgl3\target\steam\windows-x64'
$windowsVdf = $depotTemplate.Replace('{{DEPOT_ID}}', $WindowsDepotId)
$windowsVdf = $windowsVdf.Replace('{{CONTENT_ROOT}}', (Convert-ToVdfPath $windowsContent))
[System.IO.File]::WriteAllText($windowsDepotScript, $windowsVdf)

$linuxDepotEntry = ''
if ($LinuxDepotId) {
    $linuxDepotScript = Join-Path $generatedDirectory 'depot_linux.vdf'
    $linuxContent = Join-Path $projectRootPath 'lwjgl3\target\steam\linux-x64'
    $linuxVdf = $depotTemplate.Replace('{{DEPOT_ID}}', $LinuxDepotId)
    $linuxVdf = $linuxVdf.Replace('{{CONTENT_ROOT}}', (Convert-ToVdfPath $linuxContent))
    [System.IO.File]::WriteAllText($linuxDepotScript, $linuxVdf)
    $linuxDepotEntry = '        "' + $LinuxDepotId + '" "' + (Convert-ToVdfPath $linuxDepotScript) + '"'
}

[xml]$projectPom = Get-Content -LiteralPath (Join-Path $projectRootPath 'pom.xml') -Raw
$version = [string]$projectPom.project.version
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
