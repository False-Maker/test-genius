import { existsSync, readdirSync } from 'node:fs'
import { join } from 'node:path'
import { defineConfig } from '@playwright/test'

const findCachedChromiumExecutable = () => {
  const homeDir = process.env.HOME
  if (!homeDir) {
    return undefined
  }

  const cacheDir = join(homeDir, '.cache', 'ms-playwright')
  if (!existsSync(cacheDir)) {
    return undefined
  }

  const candidates = readdirSync(cacheDir)
    .filter((entry) => entry.startsWith('chromium_headless_shell-') || entry.startsWith('chromium-'))
    .sort((left, right) => right.localeCompare(left))
  const headlessShellCandidates = candidates
    .filter((entry) => entry.startsWith('chromium_headless_shell-'))
    .map((entry) => join(cacheDir, entry, 'chrome-headless-shell-linux64', 'chrome-headless-shell'))
  const chromiumCandidates = candidates
    .filter((entry) => entry.startsWith('chromium-'))
    .map((entry) => join(cacheDir, entry, 'chrome-linux64', 'chrome'))

  return [...headlessShellCandidates, ...chromiumCandidates].find((candidate) => existsSync(candidate))
}

const executablePath = process.env.PLAYWRIGHT_EXECUTABLE_PATH || findCachedChromiumExecutable()

export default defineConfig({
  testDir: './e2e',
  testMatch: /.*\.e2e\.ts/,
  timeout: 60_000,
  expect: {
    timeout: 10_000
  },
  fullyParallel: false,
  workers: 1,
  reporter: 'list',
  use: {
    baseURL: process.env.E2E_BASE_URL || 'http://127.0.0.1:3000',
    headless: true,
    launchOptions: executablePath
      ? {
          executablePath
        }
      : undefined,
    viewport: {
      width: 1440,
      height: 900
    },
    actionTimeout: 15_000,
    navigationTimeout: 20_000,
    trace: 'retain-on-failure',
    screenshot: 'only-on-failure'
  }
})
