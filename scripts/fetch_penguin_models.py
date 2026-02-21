#!/usr/bin/env python3
"""请求 PenguinSAI (New API) 模型列表并打印完整响应。"""
import urllib.request
import json

BASE = "https://api.penguinsaichat.dpdns.org"
KEY = "sk-nYxsxiy6SZucUFXOsfeKpXwP8JF4JN8FwIFyftguXj6Fq3Eg"

def req(path, method="GET", body=None):
    url = BASE + path
    headers = {
        "Authorization": f"Bearer {KEY}",
        "Content-Type": "application/json",
    }
    data = json.dumps(body).encode() if body else None
    r = urllib.request.Request(url, data=data, headers=headers, method=method)
    with urllib.request.urlopen(r, timeout=15) as res:
        return res.getcode(), json.loads(res.read().decode())

if __name__ == "__main__":
    paths = ["/v1/models", "/v1/model/list", "/api/v1/models"]
    for path in paths:
        print(f"\n--- GET {path} ---")
        try:
            code, data = req(path)
            print(f"Status: {code}")
            print(json.dumps(data, ensure_ascii=False, indent=2))
        except Exception as e:
            print(f"Error: {e}")
