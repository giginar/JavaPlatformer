param(
    [string]$OutputDirectory = '',
    [string]$Alias = 'deep-drift-upload'
)

$ErrorActionPreference = 'Stop'

if ($env:OS -ne 'Windows_NT') {
    throw 'This helper requires Windows DPAPI and must be run on Windows.'
}
if (-not $OutputDirectory) {
    $OutputDirectory = Join-Path ([Environment]::GetFolderPath('UserProfile')) `
        '.blueborn-games\deep-drift\signing'
}
$signingPath = [System.IO.Path]::GetFullPath($OutputDirectory)
$keystorePath = Join-Path $signingPath 'deep-drift-upload.jks'
$certificatePath = Join-Path $signingPath 'deep-drift-upload-cert.pem'
$credentialsPath = Join-Path $signingPath 'credentials.dpapi.json'
$loaderPath = Join-Path $signingPath 'load-signing-env.ps1'
$readmePath = Join-Path $signingPath 'README.txt'

foreach ($path in @($keystorePath, $certificatePath, $credentialsPath, $loaderPath)) {
    if (Test-Path -LiteralPath $path) {
        throw "Refusing to replace existing signing material: $path"
    }
}

New-Item -ItemType Directory -Path $signingPath -Force | Out-Null
$currentUserSid = [System.Security.Principal.WindowsIdentity]::GetCurrent().User.Value
$aclArguments = @(
    $signingPath,
    '/inheritance:r',
    '/grant:r',
    ("*{0}:(OI)(CI)F" -f $currentUserSid),
    '*S-1-5-18:(OI)(CI)F'
)
& icacls.exe @aclArguments | Out-Null
if ($LASTEXITCODE -ne 0) {
    throw "Unable to restrict the signing directory ACL: $signingPath"
}

function New-RandomPassword {
    $bytes = [byte[]]::new(48)
    $random = [System.Security.Cryptography.RandomNumberGenerator]::Create()
    try {
        $random.GetBytes($bytes)
    } finally {
        $random.Dispose()
    }
    return [Convert]::ToBase64String($bytes).TrimEnd('=').Replace('+', '-').Replace('/', '_')
}

$storePassword = New-RandomPassword
$keyPassword = New-RandomPassword
$protectedStorePassword = ConvertFrom-SecureString (
    ConvertTo-SecureString $storePassword -AsPlainText -Force)
$protectedKeyPassword = ConvertFrom-SecureString (
    ConvertTo-SecureString $keyPassword -AsPlainText -Force)
$credentialDocument = [ordered]@{
    version = 1
    keystoreFile = 'deep-drift-upload.jks'
    alias = $Alias
    protectedStorePassword = $protectedStorePassword
    protectedKeyPassword = $protectedKeyPassword
}
[System.IO.File]::WriteAllText(
    $credentialsPath,
    (($credentialDocument | ConvertTo-Json) + "`n"),
    [System.Text.UTF8Encoding]::new($false))

$loader = @'
$ErrorActionPreference = 'Stop'
$credentialsPath = Join-Path $PSScriptRoot 'credentials.dpapi.json'
if (-not (Test-Path -LiteralPath $credentialsPath -PathType Leaf)) {
    throw "DPAPI credential file is missing: $credentialsPath"
}
$credentials = Get-Content -LiteralPath $credentialsPath -Raw | ConvertFrom-Json
$keystorePath = Join-Path $PSScriptRoot $credentials.keystoreFile
if (-not (Test-Path -LiteralPath $keystorePath -PathType Leaf)) {
    throw "Upload keystore is missing: $keystorePath"
}
function ConvertFrom-LocalSecureString([string]$ProtectedValue) {
    $secureValue = ConvertTo-SecureString $ProtectedValue
    $pointer = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($secureValue)
    try {
        return [Runtime.InteropServices.Marshal]::PtrToStringBSTR($pointer)
    } finally {
        [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($pointer)
    }
}
$storePassword = ConvertFrom-LocalSecureString $credentials.protectedStorePassword
$keyPassword = ConvertFrom-LocalSecureString $credentials.protectedKeyPassword
try {
    [Environment]::SetEnvironmentVariable(
        'DEEPDRIFT_UPLOAD_KEYSTORE', $keystorePath, 'Process')
    [Environment]::SetEnvironmentVariable(
        'DEEPDRIFT_UPLOAD_STORE_PASSWORD', $storePassword, 'Process')
    [Environment]::SetEnvironmentVariable(
        'DEEPDRIFT_UPLOAD_KEY_ALIAS', [string]$credentials.alias, 'Process')
    [Environment]::SetEnvironmentVariable(
        'DEEPDRIFT_UPLOAD_KEY_PASSWORD', $keyPassword, 'Process')
} finally {
    $storePassword = $null
    $keyPassword = $null
}
Write-Host 'Deep Drift upload signing environment loaded for this PowerShell process.'
'@
[System.IO.File]::WriteAllText(
    $loaderPath, $loader, [System.Text.UTF8Encoding]::new($false))

$localReadme = @'
Project Blue: Deep Drift - Google Play upload signing material

This JKS is the Google Play UPLOAD key. It signs bundles sent to Play Console.
It is not the Google Play app-signing private key managed by Google Play.

Back up this entire directory securely before production. Never commit or share
the JKS, DPAPI credential file, or decrypted passwords. Losing the upload key
requires the Google Play upload-key reset/recovery process.

Run load-signing-env.ps1 in the PowerShell process that will run the build.
The encrypted passwords can be decrypted only by the Windows user that created them.
'@
[System.IO.File]::WriteAllText(
    $readmePath, $localReadme, [System.Text.UTF8Encoding]::new($false))

$javaHomePath = [Environment]::GetEnvironmentVariable('JAVA_HOME')
$keytoolPath = if ($javaHomePath) {
    Join-Path $javaHomePath 'bin\keytool.exe'
} else {
    (Get-Command keytool.exe -ErrorAction Stop).Source
}
if (-not (Test-Path -LiteralPath $keytoolPath -PathType Leaf)) {
    throw "keytool.exe is missing: $keytoolPath"
}

$env:DEEPDRIFT_UPLOAD_STORE_PASSWORD = $storePassword
$env:DEEPDRIFT_UPLOAD_KEY_PASSWORD = $keyPassword
try {
    & $keytoolPath -genkeypair -noprompt -v `
        -keystore $keystorePath -storetype JKS `
        '-storepass:env' DEEPDRIFT_UPLOAD_STORE_PASSWORD `
        -alias $Alias '-keypass:env' DEEPDRIFT_UPLOAD_KEY_PASSWORD `
        -keyalg RSA -keysize 4096 -sigalg SHA256withRSA -validity 10000 `
        -dname 'CN=Project Blue Deep Drift Upload, OU=Blueborn Games, C=TR'
    if ($LASTEXITCODE -ne 0) {
        throw "keytool key generation failed with exit code $LASTEXITCODE"
    }
    & $keytoolPath -exportcert -rfc `
        -keystore $keystorePath '-storepass:env' DEEPDRIFT_UPLOAD_STORE_PASSWORD `
        -alias $Alias -file $certificatePath
    if ($LASTEXITCODE -ne 0) {
        throw "keytool certificate export failed with exit code $LASTEXITCODE"
    }
} finally {
    Remove-Item Env:DEEPDRIFT_UPLOAD_STORE_PASSWORD -ErrorAction SilentlyContinue
    Remove-Item Env:DEEPDRIFT_UPLOAD_KEY_PASSWORD -ErrorAction SilentlyContinue
    $storePassword = $null
    $keyPassword = $null
}

Write-Host "Deep Drift upload key created outside the repository: $keystorePath"
Write-Host "Public upload certificate exported: $certificatePath"
Write-Host "Current-user DPAPI credentials created: $credentialsPath"
Write-Host "Signing environment loader created: $loaderPath"
Write-Host 'Signing directory ACL restricted to the current user and SYSTEM.'
Write-Host 'UPLOAD KEY BACKUP REQUIRED BEFORE PRODUCTION'
