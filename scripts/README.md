# 脚本

`set-version.sh` / `set-version.ps1` — 统一更新项目所有模块的版本号，等价于：

```bash
mvn versions:set -DnewVersion=<version> -DgroupId=fan -DprocessAllModules=true -DgenerateBackupPoms=false
```

## 用法

**Git Bash / Linux / macOS：**

```bash
./scripts/set-version.sh 1.1.2
./scripts/set-version.sh 1.1.2-SNAPSHOT
```

**PowerShell（Windows）：**

```powershell
.\scripts\set-version.ps1 1.1.2
.\scripts\set-version.ps1 1.1.2-SNAPSHOT
```

参数校验：版本号必须匹配 `x.y.z` 或 `x.y.z-suffix`（如 `1.1.2-SNAPSHOT`）。
