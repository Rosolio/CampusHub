#!/bin/bash

# CampusAid 一键停止脚本
# 用法: ./stop.sh

set -euo pipefail

GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
BLUE='\033[0;34m'
NC='\033[0m'

PROJECT_ROOT="$(cd "$(dirname "$0")" && pwd)"
LOG_DIR="$PROJECT_ROOT/logs"
BACKEND_PID_FILE="$LOG_DIR/backend.pid"
FRONTEND_PID_FILE="$LOG_DIR/frontend.pid"
BACKEND_PORT=8080
FRONTEND_PORT=5173

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}   CampusAid 停止服务${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""

is_pid_running() {
    local pid=$1
    kill -0 "$pid" >/dev/null 2>&1
}

port_listener_pids() {
    local port=$1
    lsof -tiTCP:"$port" -sTCP:LISTEN 2>/dev/null || true
}

wait_for_port_release() {
    local port=$1
    for _ in {1..20}; do
        if [ -z "$(port_listener_pids "$port")" ]; then
            return 0
        fi
        sleep 1
    done
    return 1
}

terminate_pid() {
    local pid=$1
    local label=$2

    if ! is_pid_running "$pid"; then
        return 0
    fi

    kill "$pid" >/dev/null 2>&1 || true
    for _ in {1..10}; do
        if ! is_pid_running "$pid"; then
            return 0
        fi
        sleep 1
    done

    echo -e "${YELLOW}${label} 未在预期时间内退出，正在强制停止 (PID: $pid)...${NC}"
    kill -9 "$pid" >/dev/null 2>&1 || true
}

stop_service() {
    local name=$1
    local pid_file=$2
    local port=$3
    local pids=""

    if [ -f "$pid_file" ]; then
        pids="$(cat "$pid_file" 2>/dev/null || true)"
    fi

    local port_pids
    port_pids="$(port_listener_pids "$port")"
    if [ -n "$port_pids" ]; then
        pids="${pids}"$'\n'"${port_pids}"
    fi

    pids="$(printf '%s\n' "$pids" | awk 'NF {print $1}' | sort -u)"

    if [ -z "$pids" ]; then
        echo "${name}服务未运行"
        rm -f "$pid_file"
        return 0
    fi

    echo -e "${YELLOW}正在停止${name}服务...${NC}"
    while IFS= read -r pid; do
        [ -z "$pid" ] && continue
        terminate_pid "$pid" "$name"
    done <<< "$pids"

    rm -f "$pid_file"

    if ! wait_for_port_release "$port"; then
        echo -e "${RED}✗ ${name}端口 $port 仍被占用，请查看: lsof -nP -iTCP:$port${NC}"
        return 1
    fi

    echo -e "${GREEN}✓ ${name}服务已停止${NC}"
}

stop_service "后端" "$BACKEND_PID_FILE" "$BACKEND_PORT"
stop_service "前端" "$FRONTEND_PID_FILE" "$FRONTEND_PORT"

echo ""
echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}前后端服务已停止${NC}"
echo -e "${GREEN}========================================${NC}"
echo ""
echo -e "${YELLOW}注意: MySQL 和 Redis 服务仍在运行${NC}"
echo -e "${YELLOW}如需停止，请执行:${NC}"
echo "  brew services stop mysql"
echo "  brew services stop redis"
echo ""
