import * as THREE from 'three';
import { AttractionMeshFactory } from './attraction-mesh-factory';
import { Attraction, AttractionCategory } from '../attraction/attraction.service';

describe('AttractionMeshFactory', () => {
  let factory: AttractionMeshFactory;

  const createMockAttraction = (category: AttractionCategory): Attraction => ({
    id: 1,
    name: 'Test',
    intensity: 'HIGH',
    category: category,
    minimumHeight: 140,
    minimumAge: 12,
    minimumWeight: 40,
    description: 'Test',
    photoUrl: '/test.jpg',
    isActive: true,
    mapPositionX: 50,
    mapPositionY: 50
  });

  beforeEach(() => {
    factory = new AttractionMeshFactory();
  });

  it('should create', () => {
    expect(factory).toBeTruthy();
  });

  it('should create roller coaster mesh', () => {
    const mesh = factory.createAttractionMesh(createMockAttraction('ROLLER_COASTER'));
    expect(mesh).toBeInstanceOf(THREE.Group);
  });

  it('should create ferris wheel mesh', () => {
    const mesh = factory.createAttractionMesh(createMockAttraction('FERRIS_WHEEL'));
    expect(mesh).toBeInstanceOf(THREE.Group);
  });

  it('should create carousel mesh', () => {
    const mesh = factory.createAttractionMesh(createMockAttraction('CAROUSEL'));
    expect(mesh).toBeInstanceOf(THREE.Group);
  });

  it('should create drop tower mesh', () => {
    const mesh = factory.createAttractionMesh(createMockAttraction('DROP_TOWER'));
    expect(mesh).toBeInstanceOf(THREE.Group);
  });

  it('should create haunted house mesh', () => {
    const mesh = factory.createAttractionMesh(createMockAttraction('HAUNTED_HOUSE'));
    expect(mesh).toBeInstanceOf(THREE.Group);
  });

  it('should create water ride mesh', () => {
    const mesh = factory.createAttractionMesh(createMockAttraction('WATER_RIDE'));
    expect(mesh).toBeInstanceOf(THREE.Group);
  });

  it('should create bumper cars mesh', () => {
    const mesh = factory.createAttractionMesh(createMockAttraction('BUMPER_CARS'));
    expect(mesh).toBeInstanceOf(THREE.Group);
  });

  it('should create train ride mesh', () => {
    const mesh = factory.createAttractionMesh(createMockAttraction('TRAIN_RIDE'));
    expect(mesh).toBeInstanceOf(THREE.Group);
  });

  it('should create swing ride mesh', () => {
    const mesh = factory.createAttractionMesh(createMockAttraction('SWING_RIDE'));
    expect(mesh).toBeInstanceOf(THREE.Group);
  });

  it('should create default marker for OTHER category', () => {
    const mesh = factory.createAttractionMesh(createMockAttraction('OTHER'));
    expect(mesh).toBeInstanceOf(THREE.Group);
  });

  it('should return group with children', () => {
    const mesh = factory.createAttractionMesh(createMockAttraction('ROLLER_COASTER'));
    expect(mesh.children.length).toBeGreaterThan(0);
  });
});
