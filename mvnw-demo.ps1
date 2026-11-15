param([switch]$Test)
$ErrorActionPreference = 'Stop'
Set-Location $PSScriptRoot
$version = '3.9.11'
$tools = Join-Path $PSScriptRoot '.demo-tools'
$maven = Join-Path $tools "apache-maven-$version/bin/mvn.cmd"
if (-not (Test-Path -LiteralPath $maven)) {
  New-Item -ItemType Directory -Force -Path $tools | Out-Null
  $archive = Join-Path $tools 'maven.zip'
  $url = "https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/$version/apache-maven-$version-bin.zip"
  Write-Host 'Downloading Apache Maven for this project...'
  Invoke-WebRequest -UseBasicParsing -Uri $url -OutFile $archive
  $expected = (Invoke-WebRequest -UseBasicParsing -Uri "$url.sha512").Content.Trim().Split(' ')[0]
  if ((Get-FileHash -LiteralPath $archive -Algorithm SHA512).Hash -ine $expected) { throw 'Maven checksum verification failed.' }
  Expand-Archive -LiteralPath $archive -DestinationPath $tools -Force
}
if (-not (Get-Command java -ErrorAction SilentlyContinue)) { throw 'Install a Java 21 or newer JDK, reopen VS Code, and try again.' }
# Maven needs JAVA_HOME pointing at a JDK; discover it when only java is on PATH.
if (-not $env:JAVA_HOME) {
  $ErrorActionPreference = 'Continue'
  $settings = & java -XshowSettings:properties -version 2>&1 | Out-String
  $ErrorActionPreference = 'Stop'
  if ($settings -match 'java.home\s*=\s*([^\r\n]+)') { $env:JAVA_HOME = $Matches[1].Trim() }
}
if ($Test) { & $maven test } else { & $maven spring-boot:run '-Dspring-boot.run.profiles=demo' }
exit $LASTEXITCODE