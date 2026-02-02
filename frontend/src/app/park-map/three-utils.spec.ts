import * as THREE from 'three';
import {
  isWebGLAvailable,
  createPath,
  createHorizontalPath,
  createVerticalPath,
  addStandardLights
} from './three-utils';

describe('ThreeUtils', () => {
  describe('isWebGLAvailable', () => {
    it('should return boolean', () => {
      const result = isWebGLAvailable();
      expect(typeof result).toBe('boolean');
    });
  });

  describe('createPath', () => {
    it('should create a mesh for vertical path', () => {
      const mesh = createPath(0, 0, 0, 10, 2, true);
      expect(mesh).toBeInstanceOf(THREE.Mesh);
    });

    it('should create a mesh for horizontal path', () => {
      const mesh = createPath(0, 0, 10, 0, 2, false);
      expect(mesh).toBeInstanceOf(THREE.Mesh);
    });

    it('should position path correctly', () => {
      const mesh = createPath(0, 0, 10, 10, 2, true);
      expect(mesh.position.x).toBe(5);
      expect(mesh.position.z).toBe(5);
    });

    it('should enable receiveShadow', () => {
      const mesh = createPath(0, 0, 10, 0, 2, false);
      expect(mesh.receiveShadow).toBeTrue();
    });
  });

  describe('createHorizontalPath', () => {
    it('should create a mesh', () => {
      const mesh = createHorizontalPath(0, -10, 10);
      expect(mesh).toBeInstanceOf(THREE.Mesh);
    });

    it('should position at correct z', () => {
      const mesh = createHorizontalPath(5, -10, 10);
      expect(mesh.position.z).toBe(5);
    });

    it('should center x position', () => {
      const mesh = createHorizontalPath(0, -10, 10);
      expect(mesh.position.x).toBe(0);
    });
  });

  describe('createVerticalPath', () => {
    it('should create a mesh', () => {
      const mesh = createVerticalPath(0, -10, 10);
      expect(mesh).toBeInstanceOf(THREE.Mesh);
    });

    it('should position at correct x', () => {
      const mesh = createVerticalPath(5, -10, 10);
      expect(mesh.position.x).toBe(5);
    });

    it('should center z position', () => {
      const mesh = createVerticalPath(0, -10, 10);
      expect(mesh.position.z).toBe(0);
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
});
