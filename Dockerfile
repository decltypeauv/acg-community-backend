# 1. 基础镜像：使用 Java 17 环境
FROM eclipse-temurin:17-jdk-alpine

# 2. 作者信息 (可选)
LABEL maintainer="limsuig"

# 3. 创建挂载点：用于存放上传的文件 (非常重要！)
# 这样容器里的 /app/uploads 就会对应到外面，防止文件丢失
VOLUME /tmp

# 4. 把打包好的 jar 复制进容器，并改名为 app.jar
# 注意：target/*.jar 可能会匹配多个，确保 target 下只有一个主 jar 包
COPY target/*.jar app.jar

# 5. 暴露端口 (Spring Boot 默认端口)
EXPOSE 8080

# 6. 启动命令
ENTRYPOINT ["java","-jar","/app.jar"]