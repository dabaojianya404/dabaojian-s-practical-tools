# Tab 顶部 TPS / Ping 功能

## 实现内容
- 按 Tab 打开玩家列表时，在顶部标题栏显示 `TPS: xx.x   ping: xxxms`。
- TPS 由服务端测量最近最多 100 个 tick 的平均间隔，并每 20 tick 向在线玩家同步一次。
- Ping 使用客户端当前玩家的连接延迟：小于 100ms 绿色，100–500ms 黄色，大于 500ms 红色。
- TPS 未收到服务端数据时显示 `--`；Ping 暂时不可用时显示 `--`。

## 安装要求
此功能采用方案 B，因此**同一份修改后的模组必须安装在客户端和服务端**（服务器也必须加载此模组）。只安装客户端无法获得服务端真实 TPS。
Minecraft 1.20.1、Fabric、Java 17。

## 构建
在联网且已安装 Java 17 的环境中，在项目根目录运行：
- Windows：`gradlew.bat build`
- macOS/Linux：`./gradlew build`

生成的模组 jar 位于 `build/libs/`。

## 说明
本次只新增服务端 TPS 同步类和客户端 Tab 标题显示 mixin，并在两个入口注册；其他工具逻辑未主动修改。
当前执行环境无法连接 Gradle 分发服务器，因此没有在此处完成 Gradle 编译验证。首次构建需要下载 Gradle 和相关依赖。
