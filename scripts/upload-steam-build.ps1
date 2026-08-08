param(
    [Parameter(Mandatory = $true)]
    [string]$SteamCmdPath,

    [Parameter(Mandatory = $true)]
    [string]$Username,

    [string]$AppBuildScript = "steam\generated\app_build.vdf"
)

$ErrorActionPreference = "Stop"
$projectRootPath = [System.IO.Path]::GetFullPath((Join-Path $PSScriptRoot '..'))
$resolvedSteamCmd = [System.IO.Path]::GetFullPath($SteamCmdPath)
$resolvedBuildScript = [System.IO.Path]::GetFullPath((Join-Path $projectRootPath $AppBuildScript))

if (-not (Test-Path -LiteralPath $resolvedSteamCmd)) {
    throw "steamcmd was not found: $resolvedSteamCmd"
}
if (-not (Test-Path -LiteralPath $resolvedBuildScript)) {
    throw "Generate the app build script first: $resolvedBuildScript"
}

Write-Host "SteamCMD will request your password and Steam Guard code when required."
& $resolvedSteamCmd +login $Username +run_app_build $resolvedBuildScript +quit
if ($LASTEXITCODE -ne 0) {
    throw "Steam upload failed with exit code $LASTEXITCODE"
}
