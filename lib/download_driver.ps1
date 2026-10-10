# Download MySQL Connector/J JDBC Driver into lib/
$ErrorActionPreference = "Stop"
$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$TargetJar = Join-Path $ScriptDir "mysql-connector-j-8.3.0.jar"
$DownloadUrl = "https://repo1.maven.org/maven2/com/mysql/mysql-connector-j/8.3.0/mysql-connector-j-8.3.0.jar"

if (Test-Path $TargetJar) {
    Write-Host "MySQL Connector/J already exists at: $TargetJar"
    exit 0
}

Write-Host "Downloading MySQL Connector/J 8.3.0 from $DownloadUrl..."
if (Get-Command curl.exe -ErrorAction SilentlyContinue) {
    curl.exe --noproxy "*" -L -k --ssl-no-revoke -o "$TargetJar" "$DownloadUrl"
} else {
    [System.Net.ServicePointManager]::ServerCertificateValidationCallback = {$true}
    $webClient = New-Object System.Net.WebClient
    $webClient.Proxy = $null
    $webClient.DownloadFile($DownloadUrl, $TargetJar)
}

if ((Test-Path $TargetJar) -and ((Get-Item $TargetJar).Length -gt 1000000)) {
    Write-Host "Successfully downloaded: $TargetJar"
} else {
    Write-Error "Failed to download a valid MySQL Connector/J JAR."
}
