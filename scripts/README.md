# 脚本

`set-version.ps1` — 统一更新项目所有模块的版本号，等价于：

```bash
mvn versions:set -DnewVersion=<version> -DgroupId=fan -DprocessAllModules=true -DgenerateBackupPoms=false
```

## 用法


**PowerShell（Windows）：**

```powershell
.\scripts\set-version.ps1 1.1.2
.\scripts\set-version.ps1 1.1.2-SNAPSHOT
```