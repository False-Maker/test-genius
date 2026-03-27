FROM python:3.10-slim

WORKDIR /app

# 系统依赖：Node.js（Mermaid CLI）、Chromium（mmdc 导出 PNG/PDF）、wget/curl（健康检查）
RUN set -eux; \
    if [ -f /etc/apt/sources.list.d/debian.sources ]; then \
        sed -i 's|http://deb.debian.org/debian|https://mirrors.tuna.tsinghua.edu.cn/debian|g; s|http://deb.debian.org/debian-security|https://mirrors.tuna.tsinghua.edu.cn/debian-security|g' /etc/apt/sources.list.d/debian.sources; \
    fi; \
    if [ -f /etc/apt/sources.list ]; then \
        sed -i 's|http://deb.debian.org/debian|https://mirrors.tuna.tsinghua.edu.cn/debian|g; s|http://deb.debian.org/debian-security|https://mirrors.tuna.tsinghua.edu.cn/debian-security|g' /etc/apt/sources.list; \
    fi; \
    apt-get update -o Acquire::Retries=3 -o Acquire::Check-Valid-Until=false; \
    apt-get install -y --fix-missing --no-install-recommends \
    bash \
    curl \
    ca-certificates \
    wget \
    chromium \
    nodejs \
    npm; \
    rm -rf /var/lib/apt/lists/*

# 使用系统 Chromium，避免 Puppeteer 重复下载
ENV PUPPETEER_SKIP_CHROMIUM_DOWNLOAD=1
ENV PUPPETEER_EXECUTABLE_PATH=/usr/bin/chromium

# 复制并安装 Python 依赖（使用国内镜像并增加超时，避免 Read timed out）
COPY requirements.txt .
ENV PIP_INDEX_URL=https://pypi.tuna.tsinghua.edu.cn/simple
ENV PIP_TRUSTED_HOST=pypi.tuna.tsinghua.edu.cn
RUN pip install --no-cache-dir --timeout 120 -r requirements.txt

# 可选增强依赖：jieba（BM25 中文分词）、python-pptx（PPT 解析）、markdown（Markdown 解析）
RUN pip install --no-cache-dir --timeout 120 jieba python-pptx markdown

# 安装 Mermaid CLI（流程文档导出 PNG/SVG/PDF）
RUN npm install -g --unsafe-perm @mermaid-js/mermaid-cli

# 复制应用代码
COPY app/ ./app/

# 暴露端口
EXPOSE 8000

# 健康检查（使用 GET 方法，wget --spider 使用 HEAD，所以直接使用 Python）
HEALTHCHECK --interval=30s --timeout=3s --start-period=20s --retries=3 \
  CMD python -c "import urllib.request; urllib.request.urlopen('http://127.0.0.1:8000/health')" || exit 1

# 启动命令
CMD ["uvicorn", "app.main:app", "--host", "0.0.0.0", "--port", "8000"]
