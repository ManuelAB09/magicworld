// Augment Jest matchers with Jasmine-style toBeTrue/toBeFalse
declare namespace jest {
  // eslint-disable-next-line @typescript-eslint/no-unused-vars
  interface Matchers<R, T = unknown> {
    toBeTrue(): R;
    toBeFalse(): R;
  }
}

// Augment the existing jasmine namespace from @types/jest
// Adding SpyObj<T> type and 3-argument createSpyObj overload
declare namespace jasmine {
  type SpyObj<T> = {
    [K in keyof T]: T[K] extends (...args: any[]) => any ? Spy : T[K];
  };
  function createSpyObj<T = any>(baseName: string, methodNames: any[], properties?: Record<string, any>): SpyObj<T>;
}

