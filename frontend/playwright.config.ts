import { defineConfig, devices } from '@playwright/test';

/**
 * §17 — e2e sur le parcours vente dépôt. Utilise le Chromium préinstallé
 * de l'environnement de développement (voir README) ; en CI, Playwright
 * gère son propre navigateur via `playwright install`.
 */
export default defineConfig({
  testDir: './e2e',
  fullyParallel: true,
  forbidOnly: !!process.env.CI,
  retries: process.env.CI ? 1 : 0,
  reporter: process.env.CI ? [['list'], ['html', { open: 'never' }]] : 'list',
  use: {
    baseURL: 'http://localhost:4200',
    trace: 'on-first-retry',
  },
  projects: [
    {
      name: 'chromium',
      use: {
        ...devices['Desktop Chrome'],
        launchOptions: process.env.CI ? {} : { executablePath: '/opt/pw-browsers/chromium' },
      },
    },
  ],
  webServer: {
    command: 'npm start',
    url: 'http://localhost:4200',
    reuseExistingServer: !process.env.CI,
    timeout: 120_000,
  },
});
