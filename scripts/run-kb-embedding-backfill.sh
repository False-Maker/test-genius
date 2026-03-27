#!/usr/bin/env bash

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

cd "$ROOT_DIR"

exec docker compose --profile dev run --rm backend-python-dev \
  python scripts/backfill_knowledge_document_embeddings.py "$@"
