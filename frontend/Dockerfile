# 多阶段构建 - 构建阶段
FROM node:18-alpine AS build

WORKDIR /app

# 复制package文件
COPY package*.json ./

# 安装依赖（构建需要开发依赖）
RUN npm install

# 复制源代码
COPY . .

# 构建应用
RUN npm run build

# 运行阶段 - 使用Nginx提供静态文件服务
FROM nginx:alpine

# 复制构建产物到Nginx目录
COPY --from=build /app/dist /usr/share/nginx/html

# 复制Nginx配置（可选，如果需要自定义配置）
# COPY nginx.conf /etc/nginx/conf.d/default.conf

# 暴露端口
EXPOSE 80

# 健康检查
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost/ || exit 1

# 启动Nginx
CMD ["nginx", "-g", "daemon off;"]

