# 校园新闻 · 后端服务（Ktor）

为「校园新闻」Android 客户端提供 **账号服务** 与 **新闻代理服务**。NewsAPI 密钥仅保存在服务端，客户端不接触密钥。

## 技术栈

- **Ktor 2.3**（Netty 引擎）
- **Exposed ORM + H2** 内嵌文件数据库（零安装，随服务启动自动建表）
- **JWT**（java-jwt）做无状态登录鉴权
- **BCrypt**（jBCrypt）做密码加盐哈希
- **Ktor Client (CIO)** 在服务端反向调用 NewsAPI

## 目录结构

```
backend/
├── build.gradle.kts
├── settings.gradle.kts
└── src/main/
    ├── kotlin/com/example/newsbackend/
    │   ├── Application.kt          入口：插件、JWT、依赖装配、路由
    │   ├── db/                     H2 连接 + Users 表
    │   ├── model/                  请求/响应 DTO（与 App 端契约一致）
    │   ├── security/JwtService.kt  JWT 签发与校验
    │   ├── service/                AuthService / NewsService(+NewsAPI DTO)
    │   └── routes/                 /auth 与 /news 路由
    └── resources/                  application.yaml / logback.xml
```

## 配置（通过环境变量注入，切勿提交密钥）

| 变量 | 说明 | 默认 |
|----|----|----|
| `NEWS_API_KEY` | NewsAPI 密钥（https://newsapi.org/account） | 空（不配则 /news 返回错误） |
| `JWT_SECRET` | JWT 签名密钥 | 内置开发用默认值（生产务必覆盖） |

## 运行

> 本机无需预装环境；在装有 JDK 17 的机器上：

```bash
cd backend
export NEWS_API_KEY=你的NewsAPI密钥
./gradlew run          # 首次会自动下载 Gradle 与依赖
# 或打成可运行 jar：
./gradlew buildFatJar  # 由 ktor 插件提供，产物在 build/libs/*-all.jar
java -jar build/libs/news-backend-all.jar
```

服务默认监听 `http://0.0.0.0:8080`。

## API

| 方法 | 路径 | 说明 | 鉴权 |
|----|----|----|----|
| GET | `/health` | 健康检查 | 否 |
| POST | `/auth/register` | 注册，返回 `{token, user}` | 否 |
| POST | `/auth/login` | 登录，返回 `{token, user}` | 否 |
| GET | `/auth/me` | 当前用户信息 | Bearer Token |
| GET | `/news?category=technology` | 分类新闻列表 | 否 |
| GET | `/news/detail/{id}` | 新闻详情 | 否 |

统一响应格式：`{ "code": 0, "message": "...", "data": ... }`，`code=0` 为成功。

### 请求示例

```bash
# 注册
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"scut2026","nickname":"同学","password":"123456"}'

# 登录
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"scut2026","password":"123456"}'

# 新闻列表
curl "http://localhost:8080/news?category=technology"
```

## 与 Android 客户端联调

App 端在 `NetworkConfig` 配置后端地址（模拟器访问宿主机用 `http://10.0.2.2:8080/`），
并将数据源模式切换为 `REMOTE` 即可走「App → 后端 → NewsAPI」链路；
后端不可用时 App 自动回退本地缓存 / Mock，保证演示不中断。
