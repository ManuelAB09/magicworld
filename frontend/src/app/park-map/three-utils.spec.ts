import * as THREE from 'three';
import {
  isWebGLAvailable,
  createPath,
  createHorizontalPath,
  createVerticalPath,
  addStandardLights,
  addSimpleLights,
  createCobblestoneTexture
} from './three-utils';

describe('ThreeUtils', () => {
  describe('isWebGLAvailable', () => {
    it('should return boolean', () => {
      const result = isWebGLAvailable();
      expect(typeof result).toBe('boolean');
    });
  });

  describe('createPath', () => {
    it('should create a group for vertical path', () => {
      const result = createPath(0, 0, 0, 10, 2, true);
      expect(result).toBeInstanceOf(THREE.Group);
    });

    it('should create a group for horizontal path', () => {
      const result = createPath(0, 0, 10, 0, 2, false);
      expect(result).toBeInstanceOf(THREE.Group);
    });

    it('should contain children (path + borders)', () => {
      const result = createPath(0, 0, 10, 0, 2, false);
      expect(result.children.length).toBeGreaterThan(0);
    });
  });

  describe('createHorizontalPath', () => {
    it('should create a group', () => {
      const result = createHorizontalPath(0, -10, 10);
      expect(result).toBeInstanceOf(THREE.Group);
    });

    it('should have children', () => {
      const result = createHorizontalPath(0, -10, 10);
      expect(result.children.length).toBeGreaterThan(0);
    });
  });

  describe('createVerticalPath', () => {
    it('should create a group', () => {
      const result = createVerticalPath(0, -10, 10);
      expect(result).toBeInstanceOf(THREE.Group);
    });

    it('should have children', () => {
      const result = createVerticalPath(0, -10, 10);
      expect(result.children.length).toBeGreaterThan(0);
    });
  });

  describe('addStandardLights', () => {
    it('should add lights to scene', () => {
      const scene = new THREE.Scene();
      const initialCount = scene.children.length;
      addStandardLights(scene);
      expect(scene.children.length).toBeGreaterThan(initialCount);
    });

    it('should add ambient light', () => {
      const scene = new THREE.Scene();
      addStandardLights(scene);
      const hasAmbient = scene.children.some(child => child instanceof THREE.AmbientLight);
      expect(hasAmbient).toBeTrue();
    });

    it('should add directional light', () => {
      const scene = new THREE.Scene();
      addStandardLights(scene);
      const hasDirectional = scene.children.some(child => child instanceof THREE.DirectionalLight);
      expect(hasDirectional).toBeTrue();
    });
  });

  describe('addSimpleLights', () => {
    it('should add lights to scene', () => {
      const scene = new THREE.Scene();
      const initialCount = scene.children.length;
      addSimpleLights(scene);
      expect(scene.children.length).toBeGreaterThan(initialCount);
    });

    it('should add ambient light', () => {
      const scene = new THREE.Scene();
      addSimpleLights(scene);
      const hasAmbient = scene.children.some(child => child instanceof THREE.AmbientLight);
      expect(hasAmbient).toBeTrue();
    });

    it('should add directional light with shadows', () => {
      const scene = new THREE.Scene();
      addSimpleLights(scene);
      const directional = scene.children.find(child => child instanceof THREE.DirectionalLight) as THREE.DirectionalLight;
      expect(directional).toBeTruthy();
      expect(directional.castShadow).toBeTrue();
    });

    it('should add hemisphere light', () => {
      const scene = new THREE.Scene();
      addSimpleLights(scene);
      const hasHemi = scene.children.some(child => child instanceof THREE.HemisphereLight);
      expect(hasHemi).toBeTrue();
    });
  });

  describe('createCobblestoneTexture', () => {
    it('should return a CanvasTexture', () => {
      const tex = createCobblestoneTexture();
      expect(tex).toBeInstanceOf(THREE.CanvasTexture);
    });

    it('should accept custom size', () => {
      const tex = createCobblestoneTexture(256);
      expect(tex).toBeInstanceOf(THREE.CanvasTexture);
    });

    it('should set repeat wrapping', () => {
      const tex = createCobblestoneTexture();
      expect(tex.wrapS).toBe(THREE.RepeatWrapping);
      expect(tex.wrapT).toBe(THREE.RepeatWrapping);
    });
  });
});
