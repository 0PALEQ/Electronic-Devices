param(
    [Parameter(Mandatory = $true)][string]$WaterMediaJar,
    [string]$JavaHome = $env:JAVA_HOME
)
$ErrorActionPreference = 'Stop'
$javap = Join-Path $JavaHome 'bin/javap.exe'
$contracts = @{
    'org.watermedia.api.player.PlayerAPI' = @(
        'mrl(java.net.URI)',
        'player(org.watermedia.api.player.MRL, java.util.function.Supplier<org.watermedia.api.player.GFXEngine>, java.util.function.Supplier<org.watermedia.api.player.SFXEngine>)',
        'glEngine(java.lang.Thread, java.util.concurrent.Executor)', 'alEngine()'
    )
    'org.watermedia.api.player.MRL' = @('status()', 'exception()')
    'org.watermedia.api.player.MediaPlayer' = @(
        'texture()', 'canPlay()', 'canSeek()', 'live()', 'playing()', 'paused()', 'error()',
        'seek(long)', 'time()', 'duration()', 'repeat(boolean)', 'volume(int)',
        'resume()', 'pause()', 'release()', 'start()', 'audioLanguage(java.lang.String)'
    )
    'org.watermedia.api.codecs.CodecsAPI' = @('ffmpegLoaded()', 'ffmpegError()')
    'org.watermedia.api.platform.PlatformAPI' = @(
        'platforms()', 'unregister(org.watermedia.api.platform.Platform)'
    )
}
foreach ($type in $contracts.Keys) {
    $declaration = (& $javap -classpath $WaterMediaJar $type) -join "`n"
    if ($LASTEXITCODE -ne 0) { throw "Could not inspect $type" }
    foreach ($method in $contracts[$type]) {
        if (!$declaration.Contains(" $method;")) { throw "Missing WaterMedia API: $type.$method" }
    }
}
$root = Split-Path $PSScriptRoot
$bridges = Get-ChildItem "$root/versions/*/src/main/java/com/cookiecraftmods/edm/client/WaterMediaBridge.java"
foreach ($bridge in $bridges) {
    $code = [IO.File]::ReadAllText($bridge.FullName)
    foreach ($required in @('org.watermedia.api.player.PlayerAPI', 'org.watermedia.api.player.MRL',
        'org.watermedia.api.player.MediaPlayer', 'org.watermedia.api.codecs.CodecsAPI',
        'getMethod("player", mrlType, Supplier.class, Supplier.class)', 'getMethod("audioLanguage", String.class)')) {
        if (!$code.Contains($required)) { throw "Missing adapter contract in $bridge : $required" }
    }
}
$shims = Get-ChildItem "$root/versions/*/src/main/java/org/watermedia/api/platform/DataSlave.java"
if ($shims) { throw 'Obsolete split-package DataSlave shim remains' }
Write-Output "WaterMedia binary API contract passed for $($bridges.Count) adapters."
