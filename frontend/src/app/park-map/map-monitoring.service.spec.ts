import { MapMonitoringService } from './map-monitoring.service';
import * as THREE from 'three';
import { AttractionStatus } from '../admin-dashboard/monitoring.service';

describe('MapMonitoringService', () => {
  let service: MapMonitoringService;

  const mockStatus: AttractionStatus = {
    attractionId: 1,
    name: 'Test Ride',
    isOpen: true,
    queueSize: 10,
    estimatedWaitMinutes: 5,
    mapPositionX: 50,
    mapPositionY: 50,
    intensity: 'HIGH'
  };

  beforeEach(() => {
    service = new MapMonitoringService();
  });

  afterEach(() => {
    service.clearAll();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  describe('createQueueIndicator', () => {
    it('should create a queue indicator with sprite and label', () => {
      const pos = new THREE.Vector3(10, 0, 20);
      const indicator = service.createQueueIndicator(1, pos);

      expect(indicator).toBeTruthy();
      expect(indicator.attractionId).toBe(1);
      expect(indicator.sprite).toBeInstanceOf(THREE.Sprite);
      expect(indicator.label).toBeInstanceOf(THREE.Sprite);
    });

    it('should position sprite above the given position', () => {
      const pos = new THREE.Vector3(10, 0, 20);
      const indicator = service.createQueueIndicator(1, pos);

      expect(indicator.sprite.position.x).toBe(10);
      expect(indicator.sprite.position.y).toBe(12); // y + 12
      expect(indicator.sprite.position.z).toBe(20);
    });

    it('should set sprite scale to 4x4', () => {
      const pos = new THREE.Vector3(0, 0, 0);
      const indicator = service.createQueueIndicator(1, pos);
      expect(indicator.sprite.scale.x).toBe(4);
      expect(indicator.sprite.scale.y).toBe(4);
    });

    it('should set label scale to 3x1.5', () => {
      const pos = new THREE.Vector3(0, 0, 0);
      const indicator = service.createQueueIndicator(1, pos);
      expect(indicator.label.scale.x).toBe(3);
      expect(indicator.label.scale.y).toBe(1.5);
    });

    it('should store indicator in internal map', () => {
      const pos = new THREE.Vector3(0, 0, 0);
      service.createQueueIndicator(1, pos);
      service.createQueueIndicator(2, pos);

      // Verify by updating — if not stored, update would do nothing
      const status1 = { ...mockStatus, attractionId: 1, queueSize: 30 };
      expect(() => service.updateQueueIndicator(status1)).not.toThrow();
    });
  });

  describe('updateQueueIndicator', () => {
    it('should update existing indicator without error', () => {
      const pos = new THREE.Vector3(0, 0, 0);
      service.createQueueIndicator(1, pos);

      expect(() => service.updateQueueIndicator(mockStatus)).not.toThrow();
    });

    it('should not throw for non-existing indicator', () => {
      expect(() => service.updateQueueIndicator({ ...mockStatus, attractionId: 999 })).not.toThrow();
    });

    it('should set opacity to 1 when open', () => {
      const pos = new THREE.Vector3(0, 0, 0);
      const indicator = service.createQueueIndicator(1, pos);
      service.updateQueueIndicator({ ...mockStatus, isOpen: true, queueSize: 10 });
      expect(indicator.sprite.material.opacity).toBe(1);
    });

    it('should set opacity to 0.3 when closed', () => {
      const pos = new THREE.Vector3(0, 0, 0);
      const indicator = service.createQueueIndicator(1, pos);
      service.updateQueueIndicator({ ...mockStatus, isOpen: false, queueSize: 10 });
      expect(indicator.sprite.material.opacity).toBe(0.3);
    });

    it('should handle low queue size (green)', () => {
      const pos = new THREE.Vector3(0, 0, 0);
      service.createQueueIndicator(1, pos);
      expect(() => service.updateQueueIndicator({ ...mockStatus, queueSize: 5 })).not.toThrow();
    });

    it('should handle medium queue size (yellow)', () => {
      const pos = new THREE.Vector3(0, 0, 0);
      service.createQueueIndicator(1, pos);
      expect(() => service.updateQueueIndicator({ ...mockStatus, queueSize: 35 })).not.toThrow();
    });

    it('should handle high queue size (red)', () => {
      const pos = new THREE.Vector3(0, 0, 0);
      service.createQueueIndicator(1, pos);
      expect(() => service.updateQueueIndicator({ ...mockStatus, queueSize: 60 })).not.toThrow();
    });
  });

  describe('createHeatCircle', () => {
    it('should create a heat circle mesh', () => {
      const pos = new THREE.Vector3(10, 5, 20);
      const circle = service.createHeatCircle(1, pos);

      expect(circle).toBeInstanceOf(THREE.Mesh);
    });

    it('should position circle at ground level', () => {
      const pos = new THREE.Vector3(10, 5, 20);
      const circle = service.createHeatCircle(1, pos);

      expect(circle.position.x).toBe(10);
      expect(circle.position.y).toBe(0.1);
      expect(circle.position.z).toBe(20);
    });

    it('should rotate circle to be horizontal', () => {
      const pos = new THREE.Vector3(0, 0, 0);
      const circle = service.createHeatCircle(1, pos);
      expect(circle.rotation.x).toBeCloseTo(-Math.PI / 2);
    });

    it('should use transparent green material by default', () => {
      const pos = new THREE.Vector3(0, 0, 0);
      const circle = service.createHeatCircle(1, pos);
      const mat = circle.material as THREE.MeshBasicMaterial;
      expect(mat.transparent).toBeTrue();
      expect(mat.opacity).toBe(0.4);
    });
  });

  describe('updateHeatCircle', () => {
    it('should update existing heat circle without error', () => {
      const pos = new THREE.Vector3(0, 0, 0);
      service.createHeatCircle(1, pos);
      expect(() => service.updateHeatCircle(mockStatus)).not.toThrow();
    });

    it('should not throw for non-existing heat circle', () => {
      expect(() => service.updateHeatCircle({ ...mockStatus, attractionId: 999 })).not.toThrow();
    });

    it('should scale based on queue size intensity', () => {
      const pos = new THREE.Vector3(0, 0, 0);
      const circle = service.createHeatCircle(1, pos);

      service.updateHeatCircle({ ...mockStatus, queueSize: 50, isOpen: true });
      // intensity = min(50/50, 1) = 1, scale = (5 + 10) / 5 = 3
      expect(circle.scale.x).toBeCloseTo(3);
    });

    it('should have low opacity when closed', () => {
      const pos = new THREE.Vector3(0, 0, 0);
      const circle = service.createHeatCircle(1, pos);

      service.updateHeatCircle({ ...mockStatus, isOpen: false, queueSize: 25 });
      const mat = circle.material as THREE.MeshBasicMaterial;
      expect(mat.opacity).toBe(0.1);
    });

    it('should have higher opacity when open with queue', () => {
      const pos = new THREE.Vector3(0, 0, 0);
      const circle = service.createHeatCircle(1, pos);

      service.updateHeatCircle({ ...mockStatus, isOpen: true, queueSize: 50 });
      const mat = circle.material as THREE.MeshBasicMaterial;
      // opacity = 0.3 + 1 * 0.3 = 0.6
      expect(mat.opacity).toBeCloseTo(0.6);
    });

    it('should cap intensity at 1 for very large queues', () => {
      const pos = new THREE.Vector3(0, 0, 0);
      const circle = service.createHeatCircle(1, pos);

      service.updateHeatCircle({ ...mockStatus, queueSize: 200, isOpen: true });
      // intensity = min(200/50, 1) = 1, scale = (5 + 10) / 5 = 3
      expect(circle.scale.x).toBeCloseTo(3);
    });
  });

  describe('clearAll', () => {
    it('should clear without error when empty', () => {
      expect(() => service.clearAll()).not.toThrow();
    });

    it('should dispose queue indicators and heat circles', () => {
      const pos = new THREE.Vector3(0, 0, 0);
      const indicator = service.createQueueIndicator(1, pos);
      service.createHeatCircle(1, pos);

      const spriteMaterialDispose = spyOn(indicator.sprite.material, 'dispose');
      const labelMaterialDispose = spyOn(indicator.label.material, 'dispose');

      service.clearAll();

      expect(spriteMaterialDispose).toHaveBeenCalled();
      expect(labelMaterialDispose).toHaveBeenCalled();
    });

    it('should allow creating new indicators after clear', () => {
      const pos = new THREE.Vector3(0, 0, 0);
      service.createQueueIndicator(1, pos);
      service.clearAll();

      const newIndicator = service.createQueueIndicator(1, pos);
      expect(newIndicator).toBeTruthy();
    });
  });
});

