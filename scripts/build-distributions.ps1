[CmdletBinding()]
param()

$ErrorActionPreference = "Stop"
$projectRoot = (Resolve-Path -LiteralPath (Join-Path $PSScriptRoot "..")).Path

Push-Location -LiteralPath $projectRoot
try {
    & (Join-Path $projectRoot "gradlew.bat") collectArtifacts
    if ($LASTEXITCODE -ne 0) {
        throw "Distribution build failed with exit code $LASTEXITCODE."
    }
    Write-Host "Distributable JARs: $(Join-Path $projectRoot 'build/distributions')"
} finally {
    Pop-Location
}
