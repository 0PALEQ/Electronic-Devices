param([string]$JavaHome = $env:JAVA_HOME)
$ErrorActionPreference = 'Stop'
if (!$JavaHome) { throw 'Set JAVA_HOME or pass -JavaHome with a JDK installation.' }
$root = Split-Path $PSScriptRoot
$testOut = Join-Path ([IO.Path]::GetTempPath()) ('edm-whitelist-' + [guid]::NewGuid())
New-Item -ItemType Directory $testOut | Out-Null
try {
    & (Join-Path $JavaHome 'bin/javac.exe') -d $testOut `
        "$root/shared/src/main/java/com/cookiecraftmods/edm/media/MediaUrl.java" `
        "$root/shared/src/main/java/com/cookiecraftmods/edm/media/MediaSourcePolicy.java" `
        "$root/shared/src/test/java/com/cookiecraftmods/edm/media/MediaWhitelistTest.java"
    if ($LASTEXITCODE -ne 0) { throw 'Whitelist test compilation failed.' }
    & (Join-Path $JavaHome 'bin/java.exe') -ea -cp $testOut com.cookiecraftmods.edm.media.MediaWhitelistTest
    if ($LASTEXITCODE -ne 0) { throw 'Whitelist security checks failed.' }
} finally {
    Remove-Item -LiteralPath $testOut -Recurse -Force
}
