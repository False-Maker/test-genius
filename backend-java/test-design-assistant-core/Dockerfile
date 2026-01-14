# 多阶段构建 - 构建阶段
# 注意：此Dockerfile需要在backend-java目录下执行构建
# 构建命令：docker build -f test-design-assistant-core/Dockerfile -t backend-java:latest backend-java/

FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /app

# 设置Maven内存参数（避免OOM，Java 17不需要MaxPermSize）
ENV MAVEN_OPTS="-Xmx1024m"

# 配置Maven使用阿里云镜像源（解决网络问题）
RUN mkdir -p /root/.m2 && \
    echo '<?xml version="1.0" encoding="UTF-8"?>' > /root/.m2/settings.xml && \
    echo '<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"' >> /root/.m2/settings.xml && \
    echo '          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"' >> /root/.m2/settings.xml && \
    echo '          xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0 http://maven.apache.org/xsd/settings-1.0.0.xsd">' >> /root/.m2/settings.xml && \
    echo '  <mirrors>' >> /root/.m2/settings.xml && \
    echo '    <mirror>' >> /root/.m2/settings.xml && \
    echo '      <id>aliyunmaven</id>' >> /root/.m2/settings.xml && \
    echo '      <mirrorOf>central</mirrorOf>' >> /root/.m2/settings.xml && \
    echo '      <name>阿里云公共仓库</name>' >> /root/.m2/settings.xml && \
    echo '      <url>https://maven.aliyun.com/repository/public</url>' >> /root/.m2/settings.xml && \
    echo '    </mirror>' >> /root/.m2/settings.xml && \
    echo '  </mirrors>' >> /root/.m2/settings.xml && \
    echo '</settings>' >> /root/.m2/settings.xml

# 复制父POM文件（利用Docker缓存层）
COPY pom.xml ./pom.xml

# 复制子模块POM（利用Docker缓存层）
COPY test-design-assistant-core/pom.xml ./test-design-assistant-core/pom.xml

# 先下载依赖（利用Docker缓存，如果POM未变化则跳过）
RUN mvn dependency:go-offline -B || true

# 复制源代码
COPY test-design-assistant-core/src ./test-design-assistant-core/src

# 构建应用（跳过测试，使用批处理模式减少输出）
RUN mvn clean package -DskipTests -B

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

# 健康检查（使用存活检查端点）
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health/liveness || exit 1

# 启动应用
ENTRYPOINT ["java", "-jar", "app.jar"]

