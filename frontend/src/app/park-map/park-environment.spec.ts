import * as THREE from 'three';
import { ParkEnvironment } from './park-environment';

describe('ParkEnvironment', () => {
  let env: ParkEnvironment;

  beforeEach(() => {
    env = new ParkEnvironment();
  });

  it('should create', () => {
    expect(env).toBeTruthy();
  });

  describe('createGround', () => {
    it('should return a THREE.Group', () => {
      const ground = env.createGround(120);
      expect(ground).toBeInstanceOf(THREE.Group);
    });

    it('should have children', () => {
      const ground = env.createGround(120);
      expect(ground.children.length).toBeGreaterThan(0);
    });

    it('should work with different sizes', () => {
      const small = env.createGround(50);
      const large = env.createGround(200);
      expect(small).toBeInstanceOf(THREE.Group);
      expect(large).toBeInstanceOf(THREE.Group);
    });
  });

  describe('createCircularPlaza', () => {
    it('should return a THREE.Mesh', () => {
      const plaza = env.createCircularPlaza(12);
      expect(plaza).toBeInstanceOf(THREE.Mesh);
    });

    it('should have a CylinderGeometry', () => {
      const plaza = env.createCircularPlaza(12);
      expect(plaza.geometry).toBeInstanceOf(THREE.CylinderGeometry);
    });

    it('should work with different radii', () => {
      const small = env.createCircularPlaza(5);
      const large = env.createCircularPlaza(20);
      expect(small).toBeInstanceOf(THREE.Mesh);
      expect(large).toBeInstanceOf(THREE.Mesh);
    });
  });

  describe('createMainPath', () => {
    it('should return a THREE.Mesh', () => {
      const path = env.createMainPath(50, 5);
      expect(path).toBeInstanceOf(THREE.Mesh);
    });
  });

  describe('createLake', () => {
    it('should return a THREE.Group', () => {
      const lake = env.createLake(9, 7);
      expect(lake).toBeInstanceOf(THREE.Group);
    });

    it('should have children (water, edge, etc.)', () => {
      const lake = env.createLake(9, 7);
      expect(lake.children.length).toBeGreaterThan(0);
    });
  });

  describe('createEntrance', () => {
    it('should return a THREE.Group', () => {
      const entrance = env.createEntrance();
      expect(entrance).toBeInstanceOf(THREE.Group);
    });

    it('should have children', () => {
      const entrance = env.createEntrance();
      expect(entrance.children.length).toBeGreaterThan(0);
    });
  });

  describe('createTree', () => {
    it('should return a THREE.Group for round tree', () => {
      const tree = env.createTree('round');
      expect(tree).toBeInstanceOf(THREE.Group);
    });

    it('should return a THREE.Group for cone tree', () => {
      const tree = env.createTree('cone');
      expect(tree).toBeInstanceOf(THREE.Group);
    });

    it('should return a THREE.Group for palm tree', () => {
      const tree = env.createTree('palm');
      expect(tree).toBeInstanceOf(THREE.Group);
    });

    it('should return a THREE.Group for oak tree', () => {
      const tree = env.createTree('oak');
      expect(tree).toBeInstanceOf(THREE.Group);
    });

    it('should default to round species', () => {
      const tree = env.createTree();
      expect(tree).toBeInstanceOf(THREE.Group);
      expect(tree.children.length).toBeGreaterThan(0);
    });

    it('should have children (trunk + canopy)', () => {
      const tree = env.createTree('round');
      expect(tree.children.length).toBeGreaterThan(0);
    });
  });

  describe('createBench', () => {
    it('should return a THREE.Group', () => {
      const bench = env.createBench();
      expect(bench).toBeInstanceOf(THREE.Group);
    });

    it('should have children', () => {
      const bench = env.createBench();
      expect(bench.children.length).toBeGreaterThan(0);
    });
  });

  describe('createStreetLamp', () => {
    it('should return a THREE.Group', () => {
      const lamp = env.createStreetLamp();
      expect(lamp).toBeInstanceOf(THREE.Group);
    });

    it('should have children (pole + light)', () => {
      const lamp = env.createStreetLamp();
      expect(lamp.children.length).toBeGreaterThan(0);
    });
  });

  describe('createFountain', () => {
    it('should return a THREE.Group', () => {
      const fountain = env.createFountain();
      expect(fountain).toBeInstanceOf(THREE.Group);
    });

    it('should have children', () => {
      const fountain = env.createFountain();
      expect(fountain.children.length).toBeGreaterThan(0);
    });
  });

  describe('createTrashCan', () => {
    it('should return a THREE.Group', () => {
      const trash = env.createTrashCan();
      expect(trash).toBeInstanceOf(THREE.Group);
    });

    it('should have children', () => {
      const trash = env.createTrashCan();
      expect(trash.children.length).toBeGreaterThan(0);
    });
  });

  describe('createShop', () => {
    it('should create food shop', () => {
      const shop = env.createShop('food');
      expect(shop).toBeInstanceOf(THREE.Group);
      expect(shop.children.length).toBeGreaterThan(0);
    });

    it('should create souvenirs shop', () => {
      const shop = env.createShop('souvenirs');
      expect(shop).toBeInstanceOf(THREE.Group);
      expect(shop.children.length).toBeGreaterThan(0);
    });

    it('should create icecream shop', () => {
      const shop = env.createShop('icecream');
      expect(shop).toBeInstanceOf(THREE.Group);
      expect(shop.children.length).toBeGreaterThan(0);
    });
  });

  describe('createFlowerBed', () => {
    it('should return a THREE.Group', () => {
      const bed = env.createFlowerBed(2.5, 1.2);
      expect(bed).toBeInstanceOf(THREE.Group);
    });

    it('should have children', () => {
      const bed = env.createFlowerBed(2.5, 1.2);
      expect(bed.children.length).toBeGreaterThan(0);
    });

    it('should work with default parameters', () => {
      const bed = env.createFlowerBed();
      expect(bed).toBeInstanceOf(THREE.Group);
    });
  });

  describe('createHedge', () => {
    it('should return a THREE.Mesh', () => {
      const hedge = env.createHedge(4, 1.0);
      expect(hedge).toBeInstanceOf(THREE.Mesh);
    });

    it('should work with default parameters', () => {
      const hedge = env.createHedge();
      expect(hedge).toBeInstanceOf(THREE.Mesh);
    });
  });
});

