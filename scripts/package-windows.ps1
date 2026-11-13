# Run after mvn -B -pl tabpro-app -am package, using a Windows x64 JDK 25
# and WiX Toolset (preinstalled on GitHub's windows-2025 runner).
$ErrorActionPreference = 'Stop'

if (-not $IsWindows) {
    throw 'Windows packages must be built on Windows with PowerShell 7.'
}

$repoRoot = Split-Path $PSScriptRoot -Parent
[xml]$pom = Get-Content (Join-Path $repoRoot 'tabpro-app/pom.xml') -Raw
$version = $pom.project.parent.version
$target = Join-Path $repoRoot 'tabpro-app/target'
$jar = Get-Item (Join-Path $target "tabpro-app-$version.jar")
$workDir = Join-Path $target 'windows'
$inputDir = Join-Path $workDir 'input'
$imagesDir = Join-Path $workDir 'images'
$distDir = Join-Path $workDir 'dist'

# Stage only the shaded jar, keeping old builds and test resources out of the packages.
if (Test-Path $workDir) {
    Remove-Item $workDir -Recurse -Force
}
New-Item -ItemType Directory -Path $inputDir, $imagesDir, $distDir | Out-Null
Copy-Item $jar.FullName $inputDir
Copy-Item (Join-Path $repoRoot 'LICENSE') $inputDir

$imageArgs = @(
    '--type', 'app-image',
    '--name', 'tabpro',
    '--app-version', $version,
    '--vendor', 'gstn-caruso',
    '--description', 'Guitar tablature and score editor',
    '--input', $inputDir,
    '--main-jar', $jar.Name,
    '--dest', $imagesDir,
    '--add-modules', 'java.se,jdk.unsupported,jdk.localedata',
    # A native launcher does not apply the jar manifest's Add-Exports entry.
    # Wave export needs the same access that java -jar grants it.
    '--java-options', '--add-exports=java.desktop/com.sun.media.sound=ALL-UNNAMED',
    '--java-options', '--enable-native-access=ALL-UNNAMED'
)
& jpackage @imageArgs
if ($LASTEXITCODE -ne 0) {
    throw "jpackage app-image failed (exit code $LASTEXITCODE)"
}

$appImage = Join-Path $imagesDir 'tabpro'
$installerArgs = @(
    '--type', 'exe',
    '--name', 'tabpro',
    '--app-version', $version,
    '--vendor', 'gstn-caruso',
    '--app-image', $appImage,
    '--dest', $distDir,
    '--win-per-user-install',
    '--win-dir-chooser',
    '--win-menu',
    '--win-shortcut',
    # Keep this UUID stable so a new release upgrades an existing installation.
    '--win-upgrade-uuid', 'ba9b9538-05cd-4c82-9d0c-e433a021e82f'
)
& jpackage @installerArgs
if ($LASTEXITCODE -ne 0) {
    throw "jpackage installer failed (exit code $LASTEXITCODE)"
}

Move-Item (Join-Path $distDir "tabpro-$version.exe") (Join-Path $distDir "tabpro_${version}_windows-x64.exe")
Compress-Archive -Path $appImage -DestinationPath (Join-Path $distDir "tabpro_${version}_windows-x64.zip")
