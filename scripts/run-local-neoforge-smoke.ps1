param(
    [ValidateRange(1, 3600)]
    [int]$TimeoutSeconds = 180
)

$ErrorActionPreference = "Stop"
$projectRoot = (Resolve-Path -LiteralPath (Join-Path $PSScriptRoot "..")).Path
$gradleWrapper = Join-Path $projectRoot "gradlew.bat"

Push-Location -LiteralPath $projectRoot
try {
    & $gradleWrapper writeTargetManifest
    if ($LASTEXITCODE -ne 0) { throw "Could not generate the target manifest." }
} finally {
    Pop-Location
}

$manifest = Get-Content -LiteralPath (Join-Path $projectRoot "build/target-manifest.json") -Raw | ConvertFrom-Json
$smokeLogDirectory = Join-Path $projectRoot "build/neoforge-smoke-logs"
New-Item -ItemType Directory -Path $smokeLogDirectory -Force | Out-Null

function Stop-ProcessTree {
    param([int]$ProcessId)

    $children = @(Get-CimInstance Win32_Process -Filter "ParentProcessId = $ProcessId" -ErrorAction SilentlyContinue)
    foreach ($child in $children) { Stop-ProcessTree -ProcessId $child.ProcessId }
    Stop-Process -Id $ProcessId -Force -ErrorAction SilentlyContinue
}

foreach ($target in $manifest.targets) {
    $startedAt = Get-Date
    $log = Join-Path $projectRoot "run/neoforge-$target/logs/latest.log"
    $arguments = @(
        ":versions:neoforge-${target}:runClient",
        "--console=plain",
        "--no-daemon",
        "--project-cache-dir",
        (Join-Path $projectRoot "build/neoforge-smoke-cache/$target")
    )
    $runId = Get-Date -Format "yyyyMMdd-HHmmss-fff"
    $standardOutput = Join-Path $smokeLogDirectory "$target-$runId.out.log"
    $standardError = Join-Path $smokeLogDirectory "$target-$runId.err.log"
    Write-Host "Starting NeoForge $target smoke client..."
    $process = Start-Process `
        -FilePath $gradleWrapper `
        -ArgumentList $arguments `
        -WorkingDirectory $projectRoot `
        -WindowStyle Hidden `
        -RedirectStandardOutput $standardOutput `
        -RedirectStandardError $standardError `
        -PassThru
    $passed = $false
    $failure = $null

    try {
        $deadline = $startedAt.AddSeconds($TimeoutSeconds)
        while ((Get-Date) -lt $deadline) {
            $process.Refresh()
            if ($process.HasExited) {
                $failure = "Gradle exited before startup (exit $($process.ExitCode)); inspect $standardOutput and $standardError."
                break
            }
            if (Test-Path -LiteralPath $log) {
                $logItem = Get-Item -LiteralPath $log
                if ($logItem.LastWriteTime -ge $startedAt) {
                    $content = Get-Content -LiteralPath $log -Raw
                    foreach ($consoleLog in @($standardOutput, $standardError)) {
                        if (Test-Path -LiteralPath $consoleLog) {
                            $content += "`n" + (Get-Content -LiteralPath $consoleLog -Raw)
                        }
                    }
                    if ($content -match "InjectionError|InvalidInjection|MixinApplyError|MixinTransformerError") {
                        $failure = "A Mixin error was reported in $log."
                        break
                    }
                    $modLoaded = $content -match [regex]::Escape("Initialized $($manifest.modId)")
                    $clientReady = $content -match "Sound engine started|SoundEngine.*started"
                    if ($modLoaded -and $clientReady) {
                        $passed = $true
                        break
                    }
                }
            }
            Start-Sleep -Seconds 2
        }
    } finally {
        $process.Refresh()
        if (-not $process.HasExited) {
            Stop-ProcessTree -ProcessId $process.Id
            $process.WaitForExit(10000) | Out-Null
        }
    }

    if (-not $passed) {
        if ($null -eq $failure) { $failure = "Timed out after $TimeoutSeconds seconds; inspect $log." }
        throw "NeoForge $target smoke test failed: $failure"
    }
    Write-Host "NeoForge $target smoke test passed."
}

Write-Host "All NeoForge startup smoke tests passed."
