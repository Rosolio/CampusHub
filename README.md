# SECII CampusHub

校园互助平台项目，包含前端 `Vue 3 + Vite` 和后端 `Spring Boot 3 + MySQL + Redis`。

## 项目结构

- `frontend`：前端工程
- `backend`：后端工程
- `start.sh` / `stop.sh` / `restart.sh`：本地联调脚本
- `deploy.sh`：服务器部署脚本

## 本地开发

### 前置依赖

- Node.js 20+
- Java 17+
- Maven 3.9+
- MySQL 8+
- Redis 6+

### 启动方式

1. 启动 MySQL 和 Redis。
2. 在 `backend` 目录构建后端：

```bash
cd backend
mvn clean package -DskipTests
```

3. 在 `frontend` 目录安装依赖并启动前端：

```bash
cd frontend
npm install
npm run dev
```

4. 单独运行后端：

```bash
cd backend
mvn spring-boot:run
```

也可以直接使用仓库根目录脚本：

```bash
./start.sh
```

## 配置说明

后端通过环境变量读取运行配置，默认值定义在 [backend/src/main/resources/application.yml](/Users/Zhuanz/Documents/sec-ii-2026/backend/src/main/resources/application.yml:1)。

常用变量如下：

- `DB_URL`
- `DB_USERNAME`
- `DB_PASSWORD`
- `REDIS_HOST`
- `REDIS_PORT`
- `JWT_SECRET`
- `SERVER_PORT`
- `SERVER_CONTEXT_PATH`

前端默认通过同源 `/api` 访问后端；如需覆盖，可设置 `VITE_API_BASE_URL`。

开发环境下，Vite 已在 [frontend/vite.config.ts](/Users/Zhuanz/Documents/sec-ii-2026/frontend/vite.config.ts:1) 中代理 `/api` 到 `http://127.0.0.1:8080`。

## 构建

前端构建：

```bash
cd frontend
npm install
npm run build
```

后端构建：

```bash
cd backend
mvn clean package -DskipTests
```

后端产物默认输出为：

```bash
backend/target/campushub-backend-1.0.0.jar
```

## 服务器部署

当前已采用非容器化方式部署到：

- [http://42.193.183.160](http://42.193.183.160)

部署结构：

- `nginx` 提供前端静态资源
- `systemd` 托管后端服务 `campushub-backend`
- `MySQL` 作为主数据库
- `Redis` 作为缓存

服务器上的关键路径：

- 项目目录：`/home/ubuntu/apps/SECII-CampusHub`
- 前端静态目录：`/var/www/campushub`
- 后端服务文件：`/etc/systemd/system/campushub-backend.service`
- Nginx 站点配置：`/etc/nginx/sites-available/campushub`
- 后端日志：`/var/log/campushub-backend.log`

### 后端服务管理

```bash
sudo systemctl status campushub-backend
sudo systemctl restart campushub-backend
sudo tail -f /var/log/campushub-backend.log
```

### Nginx 管理

```bash
sudo nginx -t
sudo systemctl reload nginx
sudo systemctl status nginx
```

### 推荐部署方式

服务器上推荐直接使用仓库根目录脚本：

```bash
cd /home/ubuntu/apps/SECII-CampusHub
./deploy.sh frontend
```

可选目标：

- `./deploy.sh frontend`：同步仓库、构建前端并发布到 `/var/www/campushub`
- `./deploy.sh backend`：同步仓库、构建后端并重启 `campushub-backend`
- `./deploy.sh all`：前后端一起部署

脚本会先备份服务器当前工作区差异到 `~/deploy-backups/`，再将仓库强制对齐到 `origin/dev`，以避免服务器上的临时改动阻塞后续发布。前端部署使用 `npm ci`，减少 `package-lock.json` 漂移。

## 测试账号

初始化数据见 [backend/src/main/resources/data.sql](/Users/Zhuanz/Documents/sec-ii-2026/backend/src/main/resources/data.sql:1)。

可直接使用：

- 学号：`20230001`
- 密码：`123456`

## 提交要求

各阶段交付物以课程要求时间为准。当前仓库已包含前后端代码、脚本及线上部署配置说明。
