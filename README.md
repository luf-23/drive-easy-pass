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

      - 执行 backend/resources/SQL 文件中的 SQL 语句来创建数据库表。

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

## 运行项目

在项目根目录下运行以下命令启动开发服务器：

```bash
npm run dev
```
