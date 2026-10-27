[CmdletBinding()]
param(
    [ValidateSet("fabric", "neoforge")]
    [string[]]$Loader = @("fabric", "neoforge"),
    [string[]]$MinecraftVersion = @("1.21.1", "1.21.11", "26.2", "26.3"),
    [ValidateRange(30, 900)]
    [int]$TimeoutSeconds = 180,
    [switch]$NestedGradle
)
$ErrorActionPreference = "Stop"
$projectRoot = (Resolve-Path -LiteralPath (Join-Path $PSScriptRoot "..")).Path
$evidenceDirectory = Join-Path $projectRoot "build/client-smoke"
New-Item -ItemType Directory -Path $evidenceDirectory -Force | Out-Null
$results = @()
function Stop-SmokeProcessTree {
    param([int]$ProcessId)
    foreach ($child in @(Get-CimInstance Win32_Process -Filter "ParentProcessId = $ProcessId" -ErrorAction SilentlyContinue)) {
        Stop-SmokeProcessTree -ProcessId $child.ProcessId
    }
    Stop-Process -Id $ProcessId -Force -ErrorAction SilentlyContinue
}
foreach ($currentLoader in $Loader) {
    foreach ($version in $MinecraftVersion) {
        if ($version -notin @("1.21.1", "1.21.11", "26.2", "26.3")) { throw "Unknown target: $version" }
        $target = "$currentLoader-$version"
        $gameLog = Join-Path $projectRoot "run/$target/logs/latest.log"
        $stdout = Join-Path $evidenceDirectory "$target.out.log"
        $stderr = Join-Path $evidenceDirectory "$target.err.log"
        $startedAt = Get-Date
        $passed = $false
        $reason = "Startup timed out"
        Write-Host "Starting $target"
        $arguments = @("`:versions`:$target`:runClient", "--no-daemon", "--no-parallel", "--console=plain")
        if ($NestedGradle) { $arguments += @("--project-cache-dir", (Join-Path $projectRoot "build/client-smoke-cache/$target")) }
        $process = Start-Process -FilePath (Join-Path $projectRoot "gradlew.bat") `
            -ArgumentList $arguments `
            -WorkingDirectory $projectRoot -WindowStyle Hidden `
            -RedirectStandardOutput $stdout -RedirectStandardError $stderr -PassThru
        try {
            while ((Get-Date) -lt $startedAt.AddSeconds($TimeoutSeconds)) {
                $process.Refresh()
                $content = @($stdout, $stderr) | Where-Object { Test-Path -LiteralPath $_ } | ForEach-Object { Get-Content -LiteralPath $_ -Raw }
                if ((Test-Path -LiteralPath $gameLog) -and (Get-Item -LiteralPath $gameLog).LastWriteTime -ge $startedAt) {
                    $content += Get-Content -LiteralPath $gameLog -Raw
                }
                $content = $content -join "`n"
                if ($content -match "InjectionError|InvalidInjection|MixinApplyError|MixinTransformerError|Reported exception thrown|BUILD FAILED") {
                    $reason = "Client or Mixin failure; inspect captured logs"
                    break
                }
                if ($content -match "HUD Visibility client ready" -and $content -match "Sound engine started|SoundEngine.*started") {
                    $passed = $true; $reason = "Client initialized and sound engine started"; break
                }
                if ($process.HasExited) { $reason = "Gradle exited before client startup"; break }
                Start-Sleep -Seconds 2
            }
        } finally {
            $process.Refresh()
            if (-not $process.HasExited) {
                Stop-SmokeProcessTree -ProcessId $process.Id
                $process.WaitForExit(10000) | Out-Null
            }
        }
        if (Test-Path -LiteralPath $gameLog) { Copy-Item -LiteralPath $gameLog -Destination (Join-Path $evidenceDirectory "$target.game.log") -Force }
        $results += [pscustomobject]@{ target = $target; passed = $passed; reason = $reason; startedAt = $startedAt.ToString("o"); finishedAt = (Get-Date).ToString("o") }
        $results | ConvertTo-Json -Depth 4 | Set-Content -LiteralPath (Join-Path $evidenceDirectory "results.json") -Encoding utf8
        Write-Host "$target passed=$passed ($reason)"
        if (-not $passed) { throw "$target startup failed: $reason" }
    }
}
