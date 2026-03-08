import * as THREE from 'three';
import { ParkBuilder } from './park-builder';

describe('ParkBuilder', () => {
  let builder: ParkBuilder;

  beforeEach(() => {
    builder = new ParkBuilder();
  });

  it('should create', () => {
    expect(builder).toBeTruthy();
  });

  describe('buildParkEnvironment', () => {
    it('should add children to the scene', () => {
      const scene = new THREE.Scene();
      const initialChildren = scene.children.length;

      builder.buildParkEnvironment(scene);

      expect(scene.children.length).toBeGreaterThan(initialChildren);
    });

    it('should add ground to scene', () => {
      const scene = new THREE.Scene();
      builder.buildParkEnvironment(scene);

      // Ground is a Group added to the scene
      const hasGroup = scene.children.some(c => c instanceof THREE.Group);
      expect(hasGroup).toBeTrue();
    });

    it('should add multiple plazas (cylinders)', () => {
      const scene = new THREE.Scene();
      builder.buildParkEnvironment(scene);

      // Plazas are Mesh with CylinderGeometry
      const cylinderMeshes = scene.children.filter(c =>
        c instanceof THREE.Mesh && (c as THREE.Mesh).geometry instanceof THREE.CylinderGeometry
      );
      expect(cylinderMeshes.length).toBeGreaterThan(0);
    });

    it('should add paths as groups', () => {
      const scene = new THREE.Scene();
      builder.buildParkEnvironment(scene);

      const groups = scene.children.filter(c => c instanceof THREE.Group);
      expect(groups.length).toBeGreaterThan(5); // Many paths + entrance + lake + decorations
    });

    it('should add wall segments', () => {
      const scene = new THREE.Scene();
      builder.buildParkEnvironment(scene);

      // Walls are BoxGeometry meshes with castShadow
      const wallMeshes = scene.children.filter(c =>
        c instanceof THREE.Mesh && (c as THREE.Mesh).geometry instanceof THREE.BoxGeometry && c.castShadow
      );
      expect(wallMeshes.length).toBeGreaterThan(0);
    });

    it('should add corner towers (cylinders)', () => {
      const scene = new THREE.Scene();
      builder.buildParkEnvironment(scene);

      const cylinders = scene.children.filter(c =>
        c instanceof THREE.Mesh && (c as THREE.Mesh).geometry instanceof THREE.CylinderGeometry
      );
      expect(cylinders.length).toBeGreaterThan(0);
    });

    it('should add corner tower roofs (cones)', () => {
      const scene = new THREE.Scene();
      builder.buildParkEnvironment(scene);

      const cones = scene.children.filter(c =>
        c instanceof THREE.Mesh && (c as THREE.Mesh).geometry instanceof THREE.ConeGeometry
      );
      expect(cones.length).toBeGreaterThan(0);
    });
  });

  describe('addAttractionZone', () => {
    it('should add zone to reserved zones', () => {
      builder.addAttractionZone(10, 20, 5);
      expect(builder.isInReservedZone(10, 20)).toBeTrue();
    });

    it('should detect positions inside reserved zone', () => {
      builder.addAttractionZone(10, 20, 5);
      expect(builder.isInReservedZone(12, 22)).toBeTrue();
    });

    it('should not detect positions outside reserved zone', () => {
      builder.addAttractionZone(10, 20, 5);
      expect(builder.isInReservedZone(100, 100)).toBeFalse();
    });

    it('should return false when no zones exist', () => {
      expect(builder.isInReservedZone(10, 20)).toBeFalse();
    });

    it('should handle multiple zones', () => {
      builder.addAttractionZone(10, 20, 5);
      builder.addAttractionZone(50, 60, 10);
      expect(builder.isInReservedZone(10, 20)).toBeTrue();
      expect(builder.isInReservedZone(50, 60)).toBeTrue();
      expect(builder.isInReservedZone(200, 200)).toBeFalse();
    });
  });

  describe('after buildParkEnvironment', () => {
    it('should have the lake zone as a reserved zone', () => {
      const scene = new THREE.Scene();
      builder.buildParkEnvironment(scene);

      // The lake adds a reserved zone at (-40, 40, radius=12)
      expect(builder.isInReservedZone(-40, 40)).toBeTrue();
    });

    it('should exclude trees in reserved lake zone', () => {
      const scene = new THREE.Scene();
      builder.buildParkEnvironment(scene);

      // Position near lake center should be reserved
      expect(builder.isInReservedZone(-38, 42)).toBeTrue();
    });
  });
});

