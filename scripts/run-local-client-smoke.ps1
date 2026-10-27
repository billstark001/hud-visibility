[CmdletBinding()]
param(
    [ValidateSet("fabric", "neoforge")]
    [string[]]$Loader = @("fabric", "neoforge"),
    [string[]]$MinecraftVersion = @(),
    [switch]$VerifyExisting,
    [ValidateRange(30, 3600)]
    [int]$TimeoutSeconds = 600
)

$ErrorActionPreference = "Stop"
$projectRoot = (Resolve-Path -LiteralPath (Join-Path $PSScriptRoot "..")).Path
$gradleWrapper = Join-Path $projectRoot "gradlew.bat"
$evidenceDirectory = Join-Path $projectRoot "build/client-smoke"
New-Item -ItemType Directory -Path $evidenceDirectory -Force | Out-Null
New-Item -ItemType Directory -Path (Join-Path $evidenceDirectory "screenshots") -Force | Out-Null

# System.Logger uses JUL on some targets, which writes to stderr instead of latest.log.
function Read-ClientEvidence {
    param([string[]]$Paths)
    return (@($Paths | Where-Object { Test-Path -LiteralPath $_ } | ForEach-Object {
        Get-Content -LiteralPath $_ -Raw
    }) -join "`n")
}

function Update-ClientResult {
    param($Result, [string]$Content)
    $rawVersion = ($Result.target -split '-', 2)[1]
    $version = [regex]::Escape($rawVersion)
    $parts = @($rawVersion.Split('.') | ForEach-Object { [int]$_ })
    $macro = $parts[0] * 10000 + $parts[1] * 100
    if ($parts.Count -gt 2) { $macro += $parts[2] }
    $api = if ($macro -ge 260200) { 'Gui.setScreen' } else { 'Minecraft.setScreen' }
    $escapedApi = [regex]::Escape($api)
    $Result.titleScreen = [bool]($Content -match "EXAMPLE_MOD_DEMO_READY compiled=$version")
    $Result.versionMatched = [bool]($Content -match "EXAMPLE_MOD_DEMO_OPEN compiled=$version runtime=$version macro=$macro api=$escapedApi matches=true")
    $Result.returnedToMenu = [bool]($Content -match "EXAMPLE_MOD_DEMO_CLOSE compiled=$version")
    $errors = $Content -match "InjectionError|InvalidInjection|MixinApplyError|MixinTransformerError|Reported exception thrown"
    $Result.passed = $Result.titleScreen -and $Result.versionMatched -and $Result.returnedToMenu `
        -and -not $errors -and -not $Result.timedOut -and $Result.exitCode -eq 0
}

if ($VerifyExisting) {
    $results = @(Get-Content -LiteralPath (Join-Path $evidenceDirectory "results.json") -Raw | ConvertFrom-Json)
    foreach ($result in $results) {
        $paths = @("game", "out", "err") | ForEach-Object { Join-Path $evidenceDirectory "$($result.target).$_.log" }
        Update-ClientResult -Result $result -Content (Read-ClientEvidence -Paths $paths)
    }
    $results | ConvertTo-Json -Depth 4 | Set-Content -LiteralPath (Join-Path $evidenceDirectory "results.json") -Encoding utf8
    $results | Format-Table target, passed, titleScreen, versionMatched, returnedToMenu, exitCode
    if (@($results | Where-Object { -not $_.passed }).Count -gt 0) { throw "Saved client evidence contains failures." }
    Write-Host "All $($results.Count) saved graphical client checks passed."
    return
}

Push-Location -LiteralPath $projectRoot
try {
    & $gradleWrapper writeTargetManifest --console=plain
    if ($LASTEXITCODE -ne 0) { throw "Could not generate the target manifest." }
} finally {
    Pop-Location
}
$manifest = Get-Content -LiteralPath (Join-Path $projectRoot "build/target-manifest.json") -Raw | ConvertFrom-Json
if ($MinecraftVersion.Count -eq 0) { $MinecraftVersion = @($manifest.targets) }
foreach ($version in $MinecraftVersion) {
    if ($version -notin $manifest.targets) { throw "Unsupported Minecraft version: $version" }
}

function Stop-ProcessTree {
    param([int]$ProcessId)
    $children = @(Get-CimInstance Win32_Process -Filter "ParentProcessId = $ProcessId" -ErrorAction SilentlyContinue)
    foreach ($child in $children) { Stop-ProcessTree -ProcessId $child.ProcessId }
    Stop-Process -Id $ProcessId -Force -ErrorAction SilentlyContinue
}

$selectedTargets = @($Loader | ForEach-Object {
    $selectedLoader = $_
    $MinecraftVersion | ForEach-Object { "$selectedLoader-$_" }
})
$resultsFile = Join-Path $evidenceDirectory "results.json"
$results = if (Test-Path -LiteralPath $resultsFile) {
    @(Get-Content -LiteralPath $resultsFile -Raw | ConvertFrom-Json | Where-Object { $_.target -notin $selectedTargets })
} else { @() }
$results = @($results)
foreach ($currentLoader in $Loader) {
    foreach ($version in $MinecraftVersion) {
        $target = "$currentLoader-$version"
        $startedAt = Get-Date
        $log = Join-Path $projectRoot "run/$target/logs/latest.log"
        $standardOutput = Join-Path $evidenceDirectory "$target.out.log"
        $standardError = Join-Path $evidenceDirectory "$target.err.log"
        $arguments = @(
            ":versions:${target}:runClient", "--console=plain", "--no-daemon", "--no-parallel"
        )
        Write-Host "START $target - open Template Info, verify PASS, click Back, then quit the game."
        $process = Start-Process -FilePath $gradleWrapper -ArgumentList $arguments `
            -WorkingDirectory $projectRoot -WindowStyle Hidden `
            -RedirectStandardOutput $standardOutput -RedirectStandardError $standardError -PassThru
        $readyReported = $false
        $timedOut = $false
        try {
            while (-not $process.HasExited) {
                if ((Get-Date) -ge $startedAt.AddSeconds($TimeoutSeconds)) {
                    $timedOut = $true
                    break
                }
                if (-not $readyReported -and (Test-Path -LiteralPath $log)) {
                    $fresh = (Get-Item -LiteralPath $log).LastWriteTime -ge $startedAt
                    if ($fresh -and (Read-ClientEvidence -Paths @($log, $standardOutput, $standardError)) -match "EXAMPLE_MOD_DEMO_READY") {
                        Write-Host "READY $target"
                        $readyReported = $true
                    }
                }
                Start-Sleep -Seconds 2
                $process.Refresh()
            }
        } finally {
            $process.Refresh()
            if (-not $process.HasExited) {
                Stop-ProcessTree -ProcessId $process.Id
                $process.WaitForExit(10000) | Out-Null
            }
        }
        $process.Refresh()
        $freshLog = (Test-Path -LiteralPath $log) -and (Get-Item -LiteralPath $log).LastWriteTime -ge $startedAt
        $content = if ($freshLog) { Read-ClientEvidence -Paths @($log, $standardOutput, $standardError) } else { "" }
        if ($freshLog) { Copy-Item -LiteralPath $log -Destination (Join-Path $evidenceDirectory "$target.game.log") -Force }
        $result = [pscustomobject]@{
            target = $target
            passed = $false
            titleScreen = $false
            versionMatched = $false
            returnedToMenu = $false
            exitCode = $process.ExitCode
            timedOut = $timedOut
            startedAt = $startedAt.ToString("o")
            finishedAt = (Get-Date).ToString("o")
            screenshot = "screenshots/$target.png"
        }
        Update-ClientResult -Result $result -Content $content
        $results += $result
        $results | ConvertTo-Json -Depth 4 | Set-Content -LiteralPath (Join-Path $evidenceDirectory "results.json") -Encoding utf8
        Write-Host "RESULT $target passed=$($result.passed) exit=$($process.ExitCode)"
    }
}

$results | Format-Table target, passed, titleScreen, versionMatched, returnedToMenu, exitCode
if (@($results | Where-Object { -not $_.passed }).Count -gt 0) {
    throw "One or more client checks failed; inspect build/client-smoke."
}
Write-Host "All $($results.Count) graphical client checks passed."
