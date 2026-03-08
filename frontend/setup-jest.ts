// NOTE: setupZoneTestEnv() is already called by @angular-builders/jest setup.js
// Do NOT call it again here to avoid "NG0400: A platform with a different configuration"


/**
 * Mock HTMLCanvasElement.getContext for jsdom (no native canvas support).
 */
HTMLCanvasElement.prototype.getContext = ((origGetContext) => {
  return function (this: HTMLCanvasElement, contextId: string, options?: any): any {
    if (contextId === '2d') {
      return {
        fillStyle: '',
        strokeStyle: '',
        lineWidth: 1,
        lineCap: 'butt',
        lineJoin: 'miter',
        font: '10px sans-serif',
        textAlign: 'start',
        textBaseline: 'alphabetic',
        globalAlpha: 1,
        globalCompositeOperation: 'source-over',
        shadowBlur: 0,
        shadowColor: 'rgba(0, 0, 0, 0)',
        shadowOffsetX: 0,
        shadowOffsetY: 0,
        canvas: this,
        fillRect: jest.fn(),
        clearRect: jest.fn(),
        strokeRect: jest.fn(),
        beginPath: jest.fn(),
        closePath: jest.fn(),
        moveTo: jest.fn(),
        lineTo: jest.fn(),
        arc: jest.fn(),
        arcTo: jest.fn(),
        bezierCurveTo: jest.fn(),
        quadraticCurveTo: jest.fn(),
        rect: jest.fn(),
        fill: jest.fn(),
        stroke: jest.fn(),
        clip: jest.fn(),
        save: jest.fn(),
        restore: jest.fn(),
        scale: jest.fn(),
        rotate: jest.fn(),
        translate: jest.fn(),
        transform: jest.fn(),
        setTransform: jest.fn(),
        drawImage: jest.fn(),
        createLinearGradient: jest.fn(() => ({ addColorStop: jest.fn() })),
        createRadialGradient: jest.fn(() => ({ addColorStop: jest.fn() })),
        createPattern: jest.fn(),
        measureText: jest.fn(() => ({ width: 0 })),
        fillText: jest.fn(),
        strokeText: jest.fn(),
        getImageData: jest.fn(() => ({ data: new Uint8ClampedArray(0), width: 0, height: 0 })),
        putImageData: jest.fn(),
        createImageData: jest.fn(() => ({ data: new Uint8ClampedArray(0), width: 0, height: 0 })),
        setLineDash: jest.fn(),
        getLineDash: jest.fn(() => []),
        ellipse: jest.fn(),
        roundRect: jest.fn(),
        isPointInPath: jest.fn(),
      } as any;
    }
    if (contextId === 'webgl' || contextId === 'experimental-webgl' || contextId === 'webgl2') {
      return {
        canvas: this,
        getExtension: jest.fn(),
        getParameter: jest.fn(),
        createShader: jest.fn(),
        createProgram: jest.fn(),
        createBuffer: jest.fn(),
        createFramebuffer: jest.fn(),
        createRenderbuffer: jest.fn(),
        createTexture: jest.fn(),
        bindBuffer: jest.fn(),
        bindFramebuffer: jest.fn(),
        bindRenderbuffer: jest.fn(),
        bindTexture: jest.fn(),
        viewport: jest.fn(),
        clearColor: jest.fn(),
        clear: jest.fn(),
        enable: jest.fn(),
        disable: jest.fn(),
        drawArrays: jest.fn(),
        drawElements: jest.fn(),
        useProgram: jest.fn(),
        getShaderPrecisionFormat: jest.fn(() => ({ precision: 23, rangeMin: 127, rangeMax: 127 })),
      } as any;
    }
    return origGetContext.call(this, contextId, options);
  };
})(HTMLCanvasElement.prototype.getContext);

/**
 * Jasmine-compatible shims for Jest.
 */

/** Adds a `.and` shim (returnValue, callFake, callThrough) to a jest.fn() */
function addAndShim(fn: jest.Mock): jest.Mock & { and: any; calls: any } {
  const enhanced = fn as jest.Mock & { and: any; calls: any };
  enhanced.and = {
    returnValue(val: any) { fn.mockReturnValue(val); return enhanced; },
    callFake(fakeFn: (...args: any[]) => any) { fn.mockImplementation(fakeFn); return enhanced; },
    callThrough() { return enhanced; },
  };
  // Jasmine .calls compat
  enhanced.calls = {
    count: () => fn.mock.calls.length,
    mostRecent: () => {
      const c = fn.mock.calls;
      return c.length ? { args: c[c.length - 1] } : undefined;
    },
    allArgs: () => fn.mock.calls,
    all: () => fn.mock.calls.map((args: any[]) => ({ args })),
    reset: () => fn.mockClear(),
  };
  return enhanced;
}

function createSpyObj<T = any>(_name: string, methods: string[], properties?: Record<string, any>): any {
  const obj: any = {};
  for (const method of methods) {
    obj[method] = addAndShim(jest.fn());
  }
  if (properties) {
    for (const [key, value] of Object.entries(properties)) {
      obj[key] = value;
    }
  }
  return obj as T;
}

function createSpy(name?: string): jest.Mock & { and: any; calls: any } {
  return addAndShim(jest.fn());
}

// Make jasmine globals available
(globalThis as any).jasmine = {
  ...(globalThis as any).jasmine,
  createSpyObj,
  createSpy,
  objectContaining: (expected: any) => expect.objectContaining(expected),
};

// Override global spyOn to add .and / .calls shim for compatibility
const _origSpyOn = jest.spyOn;
(globalThis as any).spyOn = function(object: any, method: string) {
  const spy = _origSpyOn.call(jest, object, method as any) as any;
  spy.and = {
    returnValue(val: any) { spy.mockReturnValue(val); return spy; },
    callFake(fakeFn: (...args: any[]) => any) { spy.mockImplementation(fakeFn); return spy; },
    callThrough() { return spy; },
  };
  spy.calls = {
    count: () => spy.mock.calls.length,
    mostRecent: () => {
      const c = spy.mock.calls;
      return c.length ? { args: c[c.length - 1] } : undefined;
    },
    allArgs: () => spy.mock.calls,
    all: () => spy.mock.calls.map((args: any[]) => ({ args })),
    reset: () => spy.mockClear(),
  };
  return spy;
};

// Add Jasmine-style matchers: toBeTrue() and toBeFalse()
expect.extend({
  toBeTrue(received: any) {
    return {
      pass: received === true,
      message: () => `expected ${received} to be true`,
    };
  },
  toBeFalse(received: any) {
    return {
      pass: received === false,
      message: () => `expected ${received} to be false`,
    };
  },
});


