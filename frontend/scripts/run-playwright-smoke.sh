#!/usr/bin/env bash

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
LIB_ROOT="${PLAYWRIGHT_LIB_ROOT:-/tmp/test-genius-playwright-libs}"
DEB_ROOT="${PLAYWRIGHT_DEB_ROOT:-/tmp/test-genius-playwright-debs}"
LIB_DIR="$LIB_ROOT/usr/lib/x86_64-linux-gnu"

ensure_playwright_libs() {
  if [[ -f "$LIB_DIR/libnspr4.so" && -f "$LIB_DIR/libnss3.so" && -f "$LIB_DIR/libasound.so.2" ]]; then
    return
  fi

  if ! command -v apt >/dev/null || ! command -v dpkg-deb >/dev/null; then
    echo "Missing apt or dpkg-deb; cannot prepare Playwright runtime libs." >&2
    exit 1
  fi

  rm -rf "$LIB_ROOT" "$DEB_ROOT"
  mkdir -p "$LIB_ROOT" "$DEB_ROOT"

  (
    cd "$DEB_ROOT"
    apt download libnspr4 libnss3 libasound2t64 >/dev/null
    for deb in ./*.deb; do
      dpkg-deb -x "$deb" "$LIB_ROOT"
    done
  )
}

ensure_playwright_libs

export LD_LIBRARY_PATH="$LIB_DIR${LD_LIBRARY_PATH:+:$LD_LIBRARY_PATH}"

if [[ -z "${E2E_BASE_URL:-}" && -n "${FRONTEND_DEV_PORT:-}" ]]; then
  export E2E_BASE_URL="http://127.0.0.1:${FRONTEND_DEV_PORT}"
fi

cd "$ROOT_DIR"
exec npx playwright test -c playwright.smoke.config.ts "$@"
