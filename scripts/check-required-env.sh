#!/usr/bin/env bash

set -euo pipefail

COMMAND="${1:-check}"

if [ "$COMMAND" = "check" ]; then
  MODE="${2:-}"
elif [ "$COMMAND" = "list" ]; then
  MODE="${2:-}"
else
  MODE="${1:-}"
  COMMAND="check"
fi

if [ -z "$MODE" ]; then
  echo "Usage: $0 [check|list] <final-acceptance|kb-backfill>" >&2
  exit 1
fi

require_var() {
  local name="$1"
  if [ -z "${!name:-}" ]; then
    echo "Missing required environment variable: ${name}" >&2
    exit 1
  fi
}

case "$MODE" in
  final-acceptance)
    REQUIRED_VARS=(
      BIGMODEL_API_KEY
      BIGMODEL_API_BASE
      BIGMODEL_EMBEDDING_MODEL
      BIGMODEL_EMBEDDING_DIMENSION
    )
    ;;
  kb-backfill)
    REQUIRED_VARS=(DATABASE_URL)
    ;;
  *)
    echo "Unsupported mode: ${MODE}" >&2
    exit 1
    ;;
esac

if [ "$COMMAND" = "list" ]; then
  echo "Required environment variables for ${MODE}:"
  for var_name in "${REQUIRED_VARS[@]}"; do
    echo "- ${var_name}"
  done
  if [ "$MODE" = "kb-backfill" ]; then
    echo "- If EMBEDDING_PROVIDER=openai: EMBEDDING_API_KEY"
    echo "- If EMBEDDING_PROVIDER=openai: OPENAI_EMBEDDING_MODEL"
  fi
  exit 0
fi

for var_name in "${REQUIRED_VARS[@]}"; do
  require_var "$var_name"
done

if [ "$MODE" = "kb-backfill" ]; then
  provider="${EMBEDDING_PROVIDER:-local}"
  if [ "$provider" = "openai" ]; then
    require_var EMBEDDING_API_KEY
    require_var OPENAI_EMBEDDING_MODEL
  fi
fi

echo "Environment validation passed for ${MODE}"
