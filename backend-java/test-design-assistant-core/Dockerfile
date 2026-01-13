# 多阶段构建 - 构建阶段
# 注意：此Dockerfile需要在backend-java目录下执行构建
# 构建命令：docker build -f test-design-assistant-core/Dockerfile -t backend-java:latest backend-java/

FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /app

# 复制父POM文件
COPY pom.xml ./pom.xml

# 复制子模块POM和源代码
COPY test-design-assistant-core/pom.xml ./test-design-assistant-core/pom.xml
COPY test-design-assistant-core/src ./test-design-assistant-core/src

# 构建应用
RUN mvn clean package -DskipTests

# 运行阶段
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# 从构建阶段复制JAR文件
COPY --from=build /app/test-design-assistant-core/target/*.jar app.jar

# 创建非root用户
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# 暴露端口
EXPOSE 8080

# 健康检查
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# 启动应用
ENTRYPOINT ["java", "-jar", "app.jar"]

