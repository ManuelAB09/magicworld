/** @type {import('@stryker-mutator/api/core').PartialStrykerOptions} */
const config = {
  // Mutation target: all TypeScript files in src/app (excluding spec/config files)
  mutate: [
    'src/app/**/*.ts',
    '!src/app/**/*.spec.ts',
    '!src/app/**/*.module.ts',
    '!src/app/app.config.ts',
    '!src/app/app.routes.ts',
    '!src/app/translate-loader.ts',
  ],

  // Test runner: Jest
  testRunner: 'jest',
  jest: {
    projectType: 'custom',
    config: {
      preset: 'jest-preset-angular',
      testEnvironment: 'jsdom',
      setupFilesAfterEnv: ['<rootDir>/setup-jest.stryker.ts'],
      transformIgnorePatterns: [
        'node_modules/(?!.*\\.mjs$|three/.*)',
      ],
    },
  },

  // No TypeScript checker — Jest handles TS compilation via jest-preset-angular
  checkers: [],

  // Coverage analysis for faster mutation testing
  coverageAnalysis: 'perTest',

  // Reporter configuration
  reporters: ['html', 'clear-text', 'progress'],
  htmlReporter: {
    fileName: 'reports/mutation/mutation-report.html',
  },

  // Thresholds
  thresholds: {
    high: 80,
    low: 60,
    break: null,
  },

  // Timeout settings
  timeoutMS: 30000,
  timeoutFactor: 1.5,

  // Concurrency
  concurrency: 4,

  // Temp dir
  tempDirName: '.stryker-tmp',

  // Clean temp dir after run
  cleanTempDir: true,
};

export default config;
