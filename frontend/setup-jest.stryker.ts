/**
 * Setup file for Stryker Mutator – bridges the gap between
 * @angular-builders/jest (which auto-injects zone.js) and plain Jest.
 */
import 'zone.js';
import 'zone.js/testing';
import { setupZoneTestEnv } from 'jest-preset-angular/setup-env/zone';

setupZoneTestEnv();

// Load the project's own setup (canvas mocks, jasmine shims, etc.)
import './setup-jest';
