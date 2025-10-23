# 基础镜像：仅含JRE的轻量镜像
FROM eclipse-temurin:17-jre-alpine

# 非root用户运行
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

WORKDIR /app

# 复制本地JAR包到镜像
COPY app.jar .

EXPOSE 8080

# 启动命令
CMD ["java", "-jar", "app.jar"]