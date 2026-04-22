#!/bin/bash

# CampusAid 一键重启脚本
# 用法: ./restart.sh

set -euo pipefail

PROJECT_ROOT="$(cd "$(dirname "$0")" && pwd)"

echo "正在重启 CampusAid 服务..."
echo ""

"$PROJECT_ROOT/stop.sh"

echo ""
echo "正在启动服务..."
echo ""

"$PROJECT_ROOT/start.sh"
