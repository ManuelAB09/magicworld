import { SceneManager } from './scene-manager';
import { Attraction } from '../attraction/attraction.service';

describe('SceneManager', () => {
  let sceneManager: SceneManager;

  const mockAttraction: Attraction = {
    id: 1,
    name: 'Test Ride',
    intensity: 'HIGH',
    category: 'ROLLER_COASTER',
    minimumHeight: 140,
    minimumAge: 12,
    minimumWeight: 40,
    description: 'A thrilling ride',
    photoUrl: '/images/ride.jpg',
    isActive: true,
    mapPositionX: 50,
    mapPositionY: 50
  };

  beforeEach(() => {
    sceneManager = new SceneManager();
  });

  afterEach(() => {
    sceneManager.dispose();
  });

  it('should create', () => {
    expect(sceneManager).toBeTruthy();
  });

  it('should not be initialized initially', () => {
    expect(sceneManager.isInitialized()).toBeFalse();
  });

  it('should return empty meshes before initialization', () => {
    expect(sceneManager.getAttractionMeshes()).toEqual([]);
  });

  it('should set hovered attraction', () => {
    sceneManager.setHoveredAttraction(mockAttraction);
    expect(sceneManager['hoveredAttraction']).toEqual(mockAttraction);
  });

  it('should clear hovered attraction when set to null', () => {
    sceneManager.setHoveredAttraction(mockAttraction);
    sceneManager.setHoveredAttraction(null);
    expect(sceneManager['hoveredAttraction']).toBeNull();
  });

  it('should not add attractions before initialization', () => {
    sceneManager.addAttractionsToScene([mockAttraction]);
    expect(sceneManager.getAttractionMeshes().length).toBe(0);
  });

  it('should not start animation before initialization', () => {
    expect(() => sceneManager.startAnimation()).not.toThrow();
  });

  it('should not zoom in before initialization', () => {
    expect(() => sceneManager.zoomIn()).not.toThrow();
  });

  it('should not zoom out before initialization', () => {
    expect(() => sceneManager.zoomOut()).not.toThrow();
  });

  it('should not reset view before initialization', () => {
    expect(() => sceneManager.resetView()).not.toThrow();
  });

  it('should not handle resize before initialization', () => {
    expect(() => sceneManager.handleResize(800, 600)).not.toThrow();
  });

  it('should dispose without error', () => {
    expect(() => sceneManager.dispose()).not.toThrow();
  });

  it('should dispose after initialization attempt', () => {
    const container = document.createElement('div');
    container.style.width = '800px';
    container.style.height = '600px';
    document.body.appendChild(container);

    try {
      sceneManager.initialize(container);
    } catch (e) {
    }

    expect(() => sceneManager.dispose()).not.toThrow();

    document.body.removeChild(container);
  });
});
