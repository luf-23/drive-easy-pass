# drive easy pass

## 安装依赖

### 前端

1. 安装 Node.js 和 npm（Node 包管理器）。可以从 [Node.js 官网](https://nodejs.org/) 下载并安装最新版本。

2. 在项目根目录下运行以下命令安装项目依赖：

```bash
cd ./frontend
npm install
```

## 后端

1. 安装 Java Development Kit (JDK) 17 或更高版本。可以从 [Oracle 官网](https://www.oracle.com/java/technologies/javase-jdk17-downloads.html) 下载并安装。
2. 安装数据库，有两种选择：
   1. - 安装 MySQL，可以从 [MySQL 官网](https://dev.mysql.com/downloads/) 下载并安装。
      - 配置 MySQL 数据库，创建一个名为 `drive_easy_pass` 的数据库，并设置用户名和密码。
      - 在 `application.properties` 文件中配置数据库连接信息，例如：

        ```properties
        spring.datasource.url=jdbc:mysql://localhost:3306/drive_easy_pass
        spring.datasource.username=your_username
        spring.datasource.password=your_password
        ```

      - 执行 backend/src/main/resources/SQL 目录下按编号排序的 SQL 脚本（含 `07_data_mock.sql` 运营演示数据）。

   2. - 安装 Docker ，可以从 [Docker 官网](https://www.docker.com/get-started) 下载并安装。
      - 在项目根目录创建 `.env` 文件（可从 `.env.example` 复制），并配置：
            ```properties
            MYSQL_ROOT_PASSWORD=ChangeThisRootPass123!
            MYSQL_USER=drive_user
            MYSQL_PASSWORD=ChangeThisUserPass123!
            ```

      - 使用以下命令拉取 MySQL 镜像并运行容器：

        ```bash
        docker compose up -d 
        ```

3. 在项目根目录下运行以下命令安装项目依赖：

```bash
cd ./backend
./mvnw install
```

## 考场路线地图（沿真实道路）

打开考场路线地图时，系统会调用**驾车路径规划**（优先高德，与高德导航同类接口），根据起终点（及途经点）生成沿道路的折线，而不是手画直线。

1. 登录 [高德开放平台](https://console.amap.com/dev/key/app) 创建应用，添加 **Web 服务** Key。
2. 在 `backend/application.properties`（由 `application.properties.template` 复制）中配置：

```properties
app.amap.web-key=你的高德Web服务Key
```

或通过环境变量 `APP_AMAP_WEB_KEY`。配置后重启后端。

未配置 Key 时，前端会尝试 OSRM 公开路由（国内可能不稳定）。数据库 `route_path` 中 `routing: "driving"` 表示按道路规划；可含 `waypoints` 途经点。

## 运行项目

在项目根目录下运行以下命令启动开发服务器：

```bash
npm run dev
```
