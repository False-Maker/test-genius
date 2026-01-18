FROM python:3.10-slim

WORKDIR /app

# 复制依赖文件
COPY requirements.txt .

# 安装依赖
RUN pip install --no-cache-dir -r requirements.txt

# 复制应用代码
COPY app/ ./app/

# 暴露端口
EXPOSE 8000

# 安装wget/curl用于健康检查（忽略部分源错误，使用单个主要源）
RUN apt-get update -o Acquire::Check-Valid-Until=false || true && \
    (apt-get install -y --no-install-recommends wget || apt-get install -y --no-install-recommends curl || true) && \
    rm -rf /var/lib/apt/lists/*

# 健康检查（使用 GET 方法，wget --spider 使用 HEAD，所以直接使用 Python）
HEALTHCHECK --interval=30s --timeout=3s --start-period=20s --retries=3 \
  CMD python -c "import urllib.request; urllib.request.urlopen('http://127.0.0.1:8000/health')" || exit 1

# 启动命令
CMD ["uvicorn", "app.main:app", "--host", "0.0.0.0", "--port", "8000"]

