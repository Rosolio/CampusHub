#!/bin/bash

# CampusHub 一键启动脚本
# 用法: ./start.sh

set -euo pipefail

GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
BLUE='\033[0;34m'
NC='\033[0m'

PROJECT_ROOT="$(cd "$(dirname "$0")" && pwd)"
BACKEND_DIR="$PROJECT_ROOT/backend"
FRONTEND_DIR="$PROJECT_ROOT/frontend"
LOG_DIR="$PROJECT_ROOT/logs"
BACKEND_PID_FILE="$LOG_DIR/backend.pid"
FRONTEND_PID_FILE="$LOG_DIR/frontend.pid"
BACKEND_LOG="$LOG_DIR/backend.log"
FRONTEND_LOG="$LOG_DIR/frontend.log"
BACKEND_PORT=8080
FRONTEND_PORT=5173
FRONTEND_HOST="0.0.0.0"
BACKEND_URL="http://127.0.0.1:${BACKEND_PORT}/api/tasks"
FRONTEND_URL="http://127.0.0.1:${FRONTEND_PORT}"

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}   CampusHub 一键启动脚本${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""

check_command() {
    if ! command -v "$1" >/dev/null 2>&1; then
        echo -e "${RED}错误: 未找到 $1 命令，请先安装 $1${NC}"
        exit 1
    fi
}

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

stop_app_service() {
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
        rm -f "$pid_file"
        return 0
    fi

    echo -e "${YELLOW}清理旧的${name}进程...${NC}"
    while IFS= read -r pid; do
        [ -z "$pid" ] && continue
        terminate_pid "$pid" "$name"
    done <<< "$pids"

    rm -f "$pid_file"

    if ! wait_for_port_release "$port"; then
        echo -e "${RED}✗ ${name} 端口 $port 仍被占用，请查看: lsof -nP -iTCP:$port${NC}"
        exit 1
    fi
}

wait_for_http_ready() {
    local url=$1
    local pid=$2
    local name=$3
    local log_file=$4

    for _ in {1..30}; do
        if curl -s -o /dev/null "$url" >/dev/null 2>&1; then
            return 0
        fi
        if ! is_pid_running "$pid"; then
            echo -e "${RED}✗ ${name} 启动失败，请查看日志:${NC}"
            tail -n 40 "$log_file" 2>/dev/null || true
            return 1
        fi
        sleep 1
    done

    echo -e "${RED}✗ ${name} 启动超时，请查看日志:${NC}"
    tail -n 40 "$log_file" 2>/dev/null || true
    return 1
}

get_primary_lan_ip() {
    if command -v ipconfig >/dev/null 2>&1; then
        local ip
        ip="$(ipconfig getifaddr en0 2>/dev/null || true)"
        if [ -n "$ip" ]; then
            echo "$ip"
            return 0
        fi

        ip="$(ipconfig getifaddr en1 2>/dev/null || true)"
        if [ -n "$ip" ]; then
            echo "$ip"
            return 0
        fi
    fi

    hostname -I 2>/dev/null | awk '{print $1}'
}

start_mysql() {
    echo -e "${YELLOW}[1/4] 检查 MySQL 服务...${NC}"

    if lsof -i :3306 >/dev/null 2>&1; then
        echo -e "${GREEN}✓ MySQL 已在运行${NC}"
        return 0
    fi

    echo "正在启动 MySQL..."
    if command -v brew >/dev/null 2>&1; then
        brew services start mysql || brew services start mysql@8.0
    elif command -v systemctl >/dev/null 2>&1; then
        sudo systemctl start mysql
    else
        echo -e "${RED}无法自动启动 MySQL，请手动启动${NC}"
        exit 1
    fi

    sleep 5

    if lsof -i :3306 >/dev/null 2>&1; then
        echo -e "${GREEN}✓ MySQL 启动成功${NC}"
    else
        echo -e "${RED}✗ MySQL 启动失败${NC}"
        exit 1
    fi
}

start_redis() {
    echo -e "${YELLOW}[2/4] 检查 Redis 服务...${NC}"

    if lsof -i :6379 >/dev/null 2>&1; then
        echo -e "${GREEN}✓ Redis 已在运行${NC}"
        return 0
    fi

    echo "正在启动 Redis..."
    if command -v brew >/dev/null 2>&1; then
        brew services start redis
    elif command -v systemctl >/dev/null 2>&1; then
        sudo systemctl start redis
    else
        redis-server --daemonize yes
    fi

    sleep 2

    if lsof -i :6379 >/dev/null 2>&1; then
        echo -e "${GREEN}✓ Redis 启动成功${NC}"
    else
        echo -e "${RED}✗ Redis 启动失败${NC}"
        exit 1
    fi
}

start_backend() {
    echo -e "${YELLOW}[3/4] 启动后端服务...${NC}"
    stop_app_service "后端" "$BACKEND_PID_FILE" "$BACKEND_PORT"

    cd "$BACKEND_DIR"
    : > "$BACKEND_LOG"

    echo "正在启动 Spring Boot 应用..."
    nohup mvn spring-boot:run > "$BACKEND_LOG" 2>&1 &
    local backend_pid=$!
    echo "$backend_pid" > "$BACKEND_PID_FILE"

    echo -e "${GREEN}✓ 后端启动中 (PID: $backend_pid)${NC}"
    echo "  日志文件: logs/backend.log"
    echo "等待后端服务就绪..."

    wait_for_http_ready "$BACKEND_URL" "$backend_pid" "后端" "$BACKEND_LOG"
    echo -e "${GREEN}✓ 后端服务就绪${NC}"
}

start_frontend() {
    echo -e "${YELLOW}[4/4] 启动前端服务...${NC}"
    stop_app_service "前端" "$FRONTEND_PID_FILE" "$FRONTEND_PORT"

    cd "$FRONTEND_DIR"

    # `node_modules` may exist but still be incomplete after cleanup or partial installs.
    if [ ! -x "$FRONTEND_DIR/node_modules/.bin/vite" ]; then
        echo "正在安装或修复前端依赖..."
        npm install
    fi

    : > "$FRONTEND_LOG"

    echo "正在启动 Vite 开发服务器..."
    nohup npm run dev -- --host "$FRONTEND_HOST" --port "$FRONTEND_PORT" --strictPort > "$FRONTEND_LOG" 2>&1 &
    local frontend_pid=$!
    echo "$frontend_pid" > "$FRONTEND_PID_FILE"

    echo -e "${GREEN}✓ 前端启动中 (PID: $frontend_pid)${NC}"
    echo "  日志文件: logs/frontend.log"

    wait_for_http_ready "$FRONTEND_URL" "$frontend_pid" "前端" "$FRONTEND_LOG"
    echo -e "${GREEN}✓ 前端服务就绪${NC}"
}

show_status() {
    local lan_ip
    lan_ip="$(get_primary_lan_ip)"

    echo ""
    echo -e "${BLUE}========================================${NC}"
    echo -e "${GREEN}所有服务已启动！${NC}"
    echo -e "${BLUE}========================================${NC}"
    echo ""
    echo -e "${YELLOW}服务信息:${NC}"
    echo "  📦 后端 API:  http://localhost:${BACKEND_PORT}/api"
    echo "  🌐 前端页面:  http://localhost:${FRONTEND_PORT}"
    if [ -n "$lan_ip" ]; then
        echo "  📱 手机访问:  http://${lan_ip}:${FRONTEND_PORT}"
    fi
    echo "  💾 MySQL:     localhost:3306"
    echo "  🔴 Redis:     localhost:6379"
    echo ""
    echo -e "${YELLOW}进程信息:${NC}"
    echo "  后端 PID: $(cat "$BACKEND_PID_FILE" 2>/dev/null || echo 'N/A')"
    echo "  前端 PID: $(cat "$FRONTEND_PID_FILE" 2>/dev/null || echo 'N/A')"
    echo ""
    echo -e "${YELLOW}查看日志:${NC}"
    echo "  后端: tail -f logs/backend.log"
    echo "  前端: tail -f logs/frontend.log"
    echo ""
    echo -e "${YELLOW}停止服务: ./stop.sh${NC}"
    echo ""
}

main() {
    mkdir -p "$LOG_DIR"

    check_command java
    check_command mvn
    check_command node
    check_command npm
    check_command lsof
    check_command curl

    start_mysql
    start_redis
    start_backend
    start_frontend
    show_status
}

main
