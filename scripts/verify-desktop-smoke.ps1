param(
    [switch]$SkipBuild,
    [int]$TimeoutSeconds = 30
)

$ErrorActionPreference = 'Stop'
$projectRootPath = [System.IO.Path]::GetFullPath((Join-Path $PSScriptRoot '..'))
[xml]$projectPom = Get-Content -LiteralPath (Join-Path $projectRootPath 'pom.xml') -Raw
$baseVersion = [string]$projectPom.project.version
$jarPath = Join-Path $projectRootPath "lwjgl3\target\DeepDiveDrift-$baseVersion.jar"
$outputPath = Join-Path $projectRootPath 'lwjgl3\target\desktop-smoke'

if (-not $SkipBuild) {
    & (Join-Path $projectRootPath 'mvnw.cmd') -B -ntp -pl lwjgl3 -am package
    if ($LASTEXITCODE -ne 0) {
        throw "Desktop package failed with exit code $LASTEXITCODE"
    }
}
if (-not (Test-Path -LiteralPath $jarPath -PathType Leaf)) {
    throw "Desktop executable JAR is missing: $jarPath"
}

New-Item -ItemType Directory -Path $outputPath -Force | Out-Null
$routes = @(
    [pscustomobject]@{ Name = 'main-menu'; Arguments = @() },
    [pscustomobject]@{ Name = 'dive-setup'; Arguments = @('--open-setup') },
    [pscustomobject]@{ Name = 'store'; Arguments = @('--open-store') },
    [pscustomobject]@{ Name = 'achievements'; Arguments = @('--open-achievements') },
    [pscustomobject]@{ Name = 'options'; Arguments = @('--open-options') },
    [pscustomobject]@{ Name = 'controls'; Arguments = @('--open-controls') },
    [pscustomobject]@{ Name = 'about'; Arguments = @('--open-about') },
    [pscustomobject]@{ Name = 'gameplay'; Arguments = @('--autostart', '--hide-tutorial') }
)

foreach ($route in $routes) {
    $capturePath = Join-Path $outputPath "$($route.Name).png"
    $stdoutPath = Join-Path $outputPath "$($route.Name).stdout.log"
    $stderrPath = Join-Path $outputPath "$($route.Name).stderr.log"
    Remove-Item -LiteralPath $capturePath, $stdoutPath, $stderrPath -Force -ErrorAction SilentlyContinue

    $arguments = @(
        '-jar', $jarPath,
        '--windowed', '--resolution=960x540', '--no-vsync', '--fps=60',
        "--capture=$capturePath", '--capture-delay=0.75', '--capture-exit'
    ) + $route.Arguments
    Write-Host "Desktop smoke [$($route.Name)]: java $($arguments -join ' ')"
    $process = Start-Process -FilePath 'java.exe' -ArgumentList $arguments -NoNewWindow `
        -PassThru -RedirectStandardOutput $stdoutPath -RedirectStandardError $stderrPath
    # Force PowerShell to retain the native process handle so ExitCode remains available.
    $null = $process.Handle
    if (-not $process.WaitForExit($TimeoutSeconds * 1000)) {
        Stop-Process -Id $process.Id -Force
        throw "Desktop smoke [$($route.Name)] timed out after $TimeoutSeconds seconds."
    }
    $process.WaitForExit()
    $process.Refresh()
    $stdout = if (Test-Path -LiteralPath $stdoutPath) {
        Get-Content -LiteralPath $stdoutPath -Raw
    } else { '' }
    $stderr = if (Test-Path -LiteralPath $stderrPath) {
        Get-Content -LiteralPath $stderrPath -Raw
    } else { '' }
    if ($stdout) { Write-Host $stdout.TrimEnd() }
    if ($stderr) { Write-Warning $stderr.TrimEnd() }
    $exitCode = $process.ExitCode
    if ($exitCode -ne 0) {
        throw "Desktop smoke [$($route.Name)] failed with exit code $exitCode."
    }
    if (-not (Test-Path -LiteralPath $capturePath -PathType Leaf) -or
        (Get-Item -LiteralPath $capturePath).Length -eq 0) {
        throw "Desktop smoke [$($route.Name)] exited without framebuffer evidence."
    }
    if ($stdout -notmatch 'Captured storefront screenshot:' -or
        ($stdout + "`n" + $stderr) -match '(?i)(Exception|Shader.*fail|Diver outline unavailable)') {
        throw "Desktop smoke [$($route.Name)] reported an initialization or rendering failure."
    }
    Write-Host "Desktop smoke [$($route.Name)]: PASS (exit 0, rendered $capturePath)"
}

Write-Host "Desktop runtime smoke: PASS ($($routes.Count) screens rendered and disposed cleanly)"
