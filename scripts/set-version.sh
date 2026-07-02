#!/usr/bin/env bash
set -euo pipefail

usage() {
    echo "Usage: $0 <version>" >&2
    echo "Example: $0 1.1.2" >&2
    exit 1
}

[ $# -eq 1 ] || usage
NEW_VERSION="$1"

# 校验版本号格式：x.y.z 或 x.y.z-suffix（如 1.1.2-SNAPSHOT）
if ! [[ "$NEW_VERSION" =~ ^[0-9]+\.[0-9]+\.[0-9]+(-[A-Za-z0-9.-]+)?$ ]]; then
    echo "ERROR: 版本号格式不合法: $NEW_VERSION" >&2
    echo "      期望格式: 1.1.2 或 1.1.2-SNAPSHOT" >&2
    exit 1
fi

# -DgenerateBackupPoms=false 不生成 .versionsBackup 备份文件
mvn versions:set \
    -DnewVersion="$NEW_VERSION" \
    -DgroupId=fan \
    -DprocessAllModules=true \
    -DgenerateBackupPoms=false
