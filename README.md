# SyncSkin Backend

SyncSkin Backend 是一个基于 Quarkus 的皮肤同步后端服务，用于接收、缓存、校验和同步 Minecraft Bedrock/Geyser 相关的皮肤数据。项目提供 HTTP 接口和 WebSocket 通道，可用于皮肤上传、皮肤查询、皮肤恢复和多端同步场景。

## 功能特性

- 提供皮肤数据查询与更新接口。
- 支持通过 WebSocket 接收 Geyser/Floodgate 侧的皮肤事件。
- 支持调用外部皮肤 MD5 校验服务。
- 使用 Caffeine 缓存玩家皮肤和皮肤哈希校验结果。
- 支持 Hibernate Reactive 与 MySQL 存储。
- 支持 JVM、Native、Legacy JAR 等 Quarkus 构建方式。

## 技术栈

- Java 17
- Quarkus
- Gradle
- RESTEasy Reactive
- Quarkus WebSockets
- Hibernate Reactive Panache
- Reactive MySQL Client
- Caffeine Cache

## 项目结构

```text
src/main/java/net/mcbjd      核心业务代码
src/main/resources           应用配置与静态资源
src/test/java/net/mcbjd      单元测试与集成测试
src/main/docker              Quarkus Dockerfile 模板
```

## 环境要求

- JDK 17 或更高版本
- MySQL 数据库
- 可访问的皮肤校验服务

## 配置说明

主要配置位于 `src/main/resources/application.properties`。运行前请根据实际环境配置以下内容：

```properties
quarkus.http.port=8081
quarkus.datasource.db-kind=mysql
quarkus.datasource.username=你的数据库用户名
quarkus.datasource.password=你的数据库密码
quarkus.datasource.reactive.url=mysql://数据库地址:3306/数据库名
quarkus.rest-client.skin-check.url=皮肤校验服务地址
```

公开部署或开源分发前，请不要提交真实数据库账号、密码、内网地址或生产接口签名。

## 本地运行

在项目根目录执行：

```shell
./gradlew quarkusDev
```

服务默认监听 `8081` 端口。测试环境端口由 `application.properties` 中的 `%test.quarkus.http.port` 控制。

## 构建

构建 JVM 应用：

```shell
./gradlew build
```

构建完成后，可按 Quarkus 生成的产物运行应用。

## 接口说明

### 查询皮肤

```http
GET /skin/{uuid}
```

返回指定玩家 UUID 对应的皮肤数据。

### 更新皮肤

```http
PUT /skin/{uuid}
Content-Type: application/json
```

请求体示例：

```json
{
  "xuid": "示例XUID",
  "uuid": "00000000-0000-4000-8000-000000000000",
  "username": "示例玩家",
  "skin_data": "Base64皮肤数据",
  "geometry_data": "Base64模型数据",
  "geometry_name": "模型名称",
  "skin_id": "皮肤ID",
  "fashion_name": "装扮名称",
  "fashion_data_name": "装扮数据名称",
  "player_entitys": [
    "00000000-0000-0000-0000-000000000000"
  ],
  "wear_fashion": true,
  "entity_id": 0,
  "hash": "皮肤哈希"
}
```

### 校验皮肤哈希

```http
POST /check
Content-Type: application/json
```

请求体示例：

```json
{
  "md5_list": [
    "示例MD5"
  ]
}
```

### WebSocket

```text
/geyser
/floodgate
```

WebSocket 用于接收和广播皮肤同步事件，具体事件结构以服务端代码中的事件处理逻辑为准。

## 测试

执行测试：

```shell
./gradlew test
```

当前构建脚本中配置了测试排除规则，如需启用测试，请先检查 `build.gradle` 中的 `test` 配置。

## 开源前注意事项

- 将数据库账号、密码和内网地址改为环境变量或部署平台密钥。
- 替换示例请求中的真实 UUID、XUID、hash、皮肤数据和接口签名。
- 检查日志输出，避免记录玩家名称、UUID、皮肤数据等隐私信息。
- 发布前重新扫描仓库和 Git 历史，确认不存在敏感信息。

## 许可证

本项目基于 MIT License 开源，详见 [LICENSE](LICENSE)。
