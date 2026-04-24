#!/bin/bash

# CampusHub 服务器部署脚本
# 用法:
#   ./deploy.sh
#   ./deploy.sh frontend
#   ./deploy.sh backend
#   ./deploy.sh all

set -euo pipefail

GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
BLUE='\033[0;34m'
NC='\033[0m'

PROJECT_ROOT="$(cd "$(dirname "$0")" && pwd)"
FRONTEND_DIR="$PROJECT_ROOT/frontend"
BACKEND_DIR="$PROJECT_ROOT/backend"
DEPLOY_BACKUP_DIR="${DEPLOY_BACKUP_DIR:-$HOME/deploy-backups}"
GIT_REMOTE="${GIT_REMOTE:-origin}"
GIT_BRANCH="${GIT_BRANCH:-dev}"
FRONTEND_DIST_DIR="${FRONTEND_DIST_DIR:-/var/www/campushub}"
BACKEND_SERVICE_NAME="${BACKEND_SERVICE_NAME:-campushub-backend}"
DEPLOY_TARGET="${1:-frontend}"

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}   CampusHub 服务器部署脚本${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""

check_command() {
    if ! command -v "$1" >/dev/null 2>&1; then
        echo -e "${RED}错误: 未找到 $1 命令，请先安装 $1${NC}"
        exit 1
    fi
}

ensure_supported_target() {
    case "$DEPLOY_TARGET" in
        frontend|backend|all)
            ;;
        *)
            echo -e "${RED}错误: 不支持的部署目标 '$DEPLOY_TARGET'${NC}"
            echo "可选值: frontend, backend, all"
            exit 1
            ;;
    esac
}

backup_worktree_if_needed() {
    local status_output
    status_output="$(git -C "$PROJECT_ROOT" status --short)"

    if [ -z "$status_output" ]; then
        echo -e "${GREEN}✓ 工作区干净，无需备份${NC}"
        return 0
    fi

    mkdir -p "$DEPLOY_BACKUP_DIR"
    local timestamp
    timestamp="$(date +%Y%m%d-%H%M%S)"
    local backup_file="$DEPLOY_BACKUP_DIR/sec-ii-2026-${timestamp}-working-tree.patch"
    local untracked_file="$DEPLOY_BACKUP_DIR/sec-ii-2026-${timestamp}-untracked.txt"

    echo -e "${YELLOW}检测到工作区有本地改动，正在备份...${NC}"
    git -C "$PROJECT_ROOT" diff > "$backup_file"
    git -C "$PROJECT_ROOT" ls-files --others --exclude-standard > "$untracked_file"

    echo "  补丁备份: $backup_file"
    echo "  未跟踪文件列表: $untracked_file"
}

sync_repo() {
    echo -e "${YELLOW}[1/4] 同步仓库到 ${GIT_REMOTE}/${GIT_BRANCH}...${NC}"
    git -C "$PROJECT_ROOT" fetch "$GIT_REMOTE" "$GIT_BRANCH"
    git -C "$PROJECT_ROOT" reset --hard "${GIT_REMOTE}/${GIT_BRANCH}"
    echo -e "${GREEN}✓ 仓库已同步到 $(git -C "$PROJECT_ROOT" rev-parse --short HEAD)${NC}"
}

deploy_frontend() {
    echo -e "${YELLOW}[2/4] 构建前端并发布静态资源...${NC}"
    cd "$FRONTEND_DIR"

    npm ci
    npm run build

    sudo rsync -av --delete "$FRONTEND_DIR/dist/" "$FRONTEND_DIST_DIR/"
    sudo nginx -t
    sudo systemctl reload nginx

    echo -e "${GREEN}✓ 前端已发布到 $FRONTEND_DIST_DIR${NC}"
}

deploy_backend() {
    echo -e "${YELLOW}[3/4] 构建并重启后端服务...${NC}"
    cd "$BACKEND_DIR"

    mvn -DskipTests clean package
    sudo systemctl restart "$BACKEND_SERVICE_NAME"
    sudo systemctl status "$BACKEND_SERVICE_NAME" --no-pager

    echo -e "${GREEN}✓ 后端服务已重启: $BACKEND_SERVICE_NAME${NC}"
}

show_done() {
    echo ""
    echo -e "${GREEN}========================================${NC}"
    echo -e "${GREEN}部署完成${NC}"
    echo -e "${GREEN}========================================${NC}"
    echo ""
    echo "当前提交: $(git -C "$PROJECT_ROOT" rev-parse --short HEAD)"
}

main() {
    ensure_supported_target

    check_command git
    check_command npm
    check_command rsync
    check_command sudo

    if [ "$DEPLOY_TARGET" = "backend" ] || [ "$DEPLOY_TARGET" = "all" ]; then
        check_command mvn
    fi

    backup_worktree_if_needed
    sync_repo

    case "$DEPLOY_TARGET" in
        frontend)
            deploy_frontend
            ;;
        backend)
            deploy_backend
            ;;
        all)
            deploy_frontend
            deploy_backend
            ;;
    esac

    show_done
}

main
