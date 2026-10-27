[CmdletBinding()]
param(
    [switch]$SkipBuild
)

$ErrorActionPreference = "Stop"
$projectRoot = (Resolve-Path -LiteralPath (Join-Path $PSScriptRoot "..")).Path
$arguments = @("collectModrinthArtifacts")
if ($SkipBuild) { $arguments += @("-x", "buildAll") }

Push-Location -LiteralPath $projectRoot
try {
    & (Join-Path $projectRoot "gradlew.bat") @arguments
    if ($LASTEXITCODE -ne 0) {
        throw "Modrinth bundle failed with exit code $LASTEXITCODE."
    }
    Write-Host "Modrinth upload files: $(Join-Path $projectRoot 'build/modrinth')"
} finally {
    Pop-Location
}
