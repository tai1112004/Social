param(
    [string]$DeviceId
)

$ErrorActionPreference = "Stop"
$mobileRoot = $PSScriptRoot
$projectRoot = Split-Path -Parent $mobileRoot
$adbCommand = Get-Command adb -ErrorAction SilentlyContinue

if ($null -eq $adbCommand) {
    throw "adb was not found in PATH. Add Android SDK platform-tools to PATH first."
}

$deviceIds = @(
    & $adbCommand.Source devices |
        Where-Object { $_ -match '^\S+\s+device$' } |
        ForEach-Object { ($_.Trim() -split '\s+')[0] }
)

if ([string]::IsNullOrWhiteSpace($DeviceId)) {
    if ($deviceIds.Count -eq 1) {
        $DeviceId = $deviceIds[0]
    } elseif ($deviceIds.Count -eq 0) {
        throw "No authorized Android device found. Connect the phone by USB and accept the USB debugging prompt."
    } else {
        throw "More than one Android device is connected. Run again with -DeviceId <adb-id>."
    }
}

if ($deviceIds -notcontains $DeviceId) {
    throw "Device '$DeviceId' is not listed as an authorized device. Run 'adb devices -l' to check it."
}

Write-Host "Using device: $DeviceId"
& $adbCommand.Source -s $DeviceId reverse tcp:8080 tcp:8080
if ($LASTEXITCODE -ne 0) { throw "Could not configure adb reverse for port 8080." }

$backendReady = $false
$tcpClient = New-Object System.Net.Sockets.TcpClient
try {
    $tcpClient.Connect("127.0.0.1", 8080)
    $backendReady = $true
} catch {
    Write-Warning "Backend is not listening on 127.0.0.1:8080. The app will install, but login will fail until the backend is started."
} finally {
    $tcpClient.Dispose()
}

$env:GRADLE_USER_HOME = Join-Path $projectRoot ".gradle-user-home"
Push-Location $mobileRoot
try {
    & .\gradlew.bat :app:assembleDebug -PapiBaseUrl=http://127.0.0.1:8080
    if ($LASTEXITCODE -ne 0) { throw "Android build failed." }
} finally {
    Pop-Location
}

$apk = Join-Path $mobileRoot "app\build\outputs\apk\debug\app-debug.apk"
& $adbCommand.Source -s $DeviceId install -r $apk
if ($LASTEXITCODE -ne 0) { throw "APK installation failed." }

Write-Host "Done. Keep the backend running on http://127.0.0.1:8080 while the phone is connected by USB."
