#Requires -Version 5.1
<#
.SYNOPSIS
    统一更新 fancy-boot 多模块项目的版本号。

.DESCRIPTION
    等价于：mvn versions:set -DnewVersion=<NewVersion> -DgroupId=fan -DprocessAllModules=true -DgenerateBackupPoms=false

.EXAMPLE
    .\scripts\set-version.ps1 1.1.2
    .\scripts\set-version.ps1 1.1.2-SNAPSHOT
#>

param(
    [Parameter(Mandatory = $true, Position = 0)]
    [string]$NewVersion
)

if ($NewVersion -notmatch '^[0-9]+\.[0-9]+\.[0-9]+(-[A-Za-z0-9.-]+)?$') {
    Write-Error "版本号格式不合法: $NewVersion  期望格式: 1.1.2 或 1.1.2-SNAPSHOT"
    exit 1
}

# -DgenerateBackupPoms=false 不生成 .versionsBackup 备份文件
mvn versions:set `
    -DnewVersion="$NewVersion" `
    -DgroupId=fan `
    -DprocessAllModules=true `
    -DgenerateBackupPoms=false
