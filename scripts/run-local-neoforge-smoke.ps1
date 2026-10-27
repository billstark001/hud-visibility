param([int]$TimeoutSeconds = 180, [switch]$NestedGradle)
& (Join-Path $PSScriptRoot "run-local-client-smoke.ps1") -Loader neoforge -TimeoutSeconds $TimeoutSeconds -NestedGradle:$NestedGradle
