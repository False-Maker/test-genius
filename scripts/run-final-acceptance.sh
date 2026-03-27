#!/usr/bin/env bash

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
KB_ID="${KB_ID:-2}"
START_DEV_STACK="${START_DEV_STACK:-1}"
FRONTEND_DEV_PORT="${FRONTEND_DEV_PORT:-3000}"
WAIT_TIMEOUT_SECONDS="${WAIT_TIMEOUT_SECONDS:-300}"
WAIT_INTERVAL_SECONDS="${WAIT_INTERVAL_SECONDS:-5}"
FINAL_ACCEPTANCE_SUMMARY_FILE="${FINAL_ACCEPTANCE_SUMMARY_FILE:-final-acceptance-summary.json}"
FINAL_ACCEPTANCE_LOG_FILE="${FINAL_ACCEPTANCE_LOG_FILE:-}"
FINAL_ACCEPTANCE_DOCKER_LOG_FILE="${FINAL_ACCEPTANCE_DOCKER_LOG_FILE:-}"
SCRIPT_STARTED_AT="$(date -u +%Y-%m-%dT%H:%M:%SZ)"
declare -a STEP_RECORDS=()

if [ -n "$FINAL_ACCEPTANCE_LOG_FILE" ] && [ -z "${FINAL_ACCEPTANCE_LOGGING_INITIALIZED:-}" ]; then
  mkdir -p "$(dirname "$FINAL_ACCEPTANCE_LOG_FILE")"
  export FINAL_ACCEPTANCE_LOGGING_INITIALIZED=1
  exec > >(tee "$FINAL_ACCEPTANCE_LOG_FILE") 2>&1
fi

json_escape() {
  local value="${1-}"
  value="${value//\\/\\\\}"
  value="${value//\"/\\\"}"
  value="${value//$'\n'/\\n}"
  value="${value//$'\r'/\\r}"
  value="${value//$'\t'/\\t}"
  printf '%s' "$value"
}

record_step() {
  local title="$1"
  local status="$2"
  STEP_RECORDS+=("{\"title\":\"$(json_escape "$title")\",\"status\":\"$status\"}")
}

write_summary() {
  local exit_code="${1:-0}"
  local final_status="passed"
  local script_finished_at
  local i

  if [ "$exit_code" -ne 0 ]; then
    final_status="failed"
  fi

  script_finished_at="$(date -u +%Y-%m-%dT%H:%M:%SZ)"
  mkdir -p "$(dirname "$FINAL_ACCEPTANCE_SUMMARY_FILE")"

  {
    echo "{"
    echo "  \"started_at\": \"$(json_escape "$SCRIPT_STARTED_AT")\","
    echo "  \"finished_at\": \"$(json_escape "$script_finished_at")\","
    echo "  \"final_status\": \"${final_status}\","
    echo "  \"kb_id\": \"$(json_escape "$KB_ID")\","
    echo "  \"start_dev_stack\": \"$(json_escape "$START_DEV_STACK")\","
    echo "  \"steps\": ["
    for i in "${!STEP_RECORDS[@]}"; do
      if [ "$i" -gt 0 ]; then
        echo ","
      fi
      printf '    %s' "${STEP_RECORDS[$i]}"
    done
    echo
    echo "  ]"
    echo "}"
  } > "$FINAL_ACCEPTANCE_SUMMARY_FILE"
}

collect_docker_logs() {
  if [ -z "$FINAL_ACCEPTANCE_DOCKER_LOG_FILE" ]; then
    return
  fi

  mkdir -p "$(dirname "$FINAL_ACCEPTANCE_DOCKER_LOG_FILE")"
  docker compose --profile dev logs --no-color > "$FINAL_ACCEPTANCE_DOCKER_LOG_FILE" || true
}

on_exit() {
  local exit_code="$1"
  collect_docker_logs
  write_summary "$exit_code"
}

trap 'on_exit "$?"' EXIT

run_step() {
  local title="$1"
  shift
  echo
  echo "==> $title"
  if "$@"; then
    record_step "$title" "passed"
  else
    local exit_code=$?
    record_step "$title" "failed"
    return "$exit_code"
  fi
}

is_service_running() {
  local service="$1"
  docker compose ps --services --status running | grep -qx "$service"
}

wait_for_http() {
  local title="$1"
  local url="$2"
  local elapsed=0

  echo
  echo "==> Wait for $title"

  while ! curl -fsS "$url" >/dev/null 2>&1; do
    if [ "$elapsed" -ge "$WAIT_TIMEOUT_SECONDS" ]; then
      record_step "Wait for $title" "failed"
      echo "$title did not become ready within ${WAIT_TIMEOUT_SECONDS}s: $url" >&2
      return 1
    fi
    sleep "$WAIT_INTERVAL_SECONDS"
    elapsed=$((elapsed + WAIT_INTERVAL_SECONDS))
  done

  record_step "Wait for $title" "passed"
}

cd "$ROOT_DIR"

if [ "$START_DEV_STACK" = "1" ]; then
  if is_service_running postgres \
    && is_service_running redis \
    && is_service_running backend-java-dev \
    && is_service_running backend-python-dev \
    && is_service_running frontend-dev; then
    echo
    echo "==> Ensure dev stack"
    echo "Dev stack already running, skip docker compose up -d"
    record_step "Ensure dev stack" "passed"
  else
    run_step "Ensure dev stack" docker compose --profile dev up -d
  fi
fi

run_step "Check dev containers" docker compose ps
wait_for_http "Java liveness" http://127.0.0.1:8081/api/actuator/health/liveness
wait_for_http "Python health" http://127.0.0.1:8001/health
wait_for_http "Frontend login shell" http://127.0.0.1:${FRONTEND_DEV_PORT}/login
run_step "Java liveness" curl -fsS http://127.0.0.1:8081/api/actuator/health/liveness
run_step "Python health" curl -fsS http://127.0.0.1:8001/health
run_step "Frontend login shell" bash -lc "curl -fsS http://127.0.0.1:${FRONTEND_DEV_PORT}/login | sed -n '1,12p'"
run_step "Frontend lint check" bash -lc "cd frontend && npm run lint:check"
run_step "Frontend type-check" bash -lc "cd frontend && npm run type-check"
run_step "Frontend unit tests" bash -lc "cd frontend && npm run test:run"
run_step "Frontend production build" bash -lc "cd frontend && npm run build"
run_step "Frontend smoke" bash -lc "cd frontend && npm run test:smoke"
run_step "Java full test suite" \
  docker exec test-design-backend-java-dev mvn -f /workspace/test-design-assistant-core/pom.xml test
run_step "Python full test suite" \
  docker exec test-design-backend-python-dev sh -lc \
  "cd /app && pytest tests/ -q --no-cov --capture=no"
run_step "Python backfill dry-run regression" \
  docker exec test-design-backend-python-dev sh -lc \
  "cd /app && pytest tests/scripts/test_backfill_knowledge_document_embeddings.py tests/services/test_knowledge_base_service.py -q --no-cov"
run_step "Knowledge-base backfill dry-run" \
  docker exec test-design-backend-python-dev sh -lc \
  "cd /app && python scripts/backfill_knowledge_document_embeddings.py --dry-run --kb-id ${KB_ID}"
run_step "Knowledge documents missing embeddings count" \
  docker exec test-design-postgres psql -U postgres -d test_design_assistant -At -c \
  "SELECT COUNT(*) FROM knowledge_document WHERE is_active = '1' AND embedding IS NULL;"

echo
echo "Final acceptance checks completed."
echo "Summary file: ${FINAL_ACCEPTANCE_SUMMARY_FILE}"
if [ -n "$FINAL_ACCEPTANCE_LOG_FILE" ]; then
  echo "Log file: ${FINAL_ACCEPTANCE_LOG_FILE}"
fi
if [ -n "$FINAL_ACCEPTANCE_DOCKER_LOG_FILE" ]; then
  echo "Docker log file: ${FINAL_ACCEPTANCE_DOCKER_LOG_FILE}"
fi
