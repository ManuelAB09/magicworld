import * as THREE from 'three';
import { ParkEnvironment } from './park-environment';
import { createHorizontalPath, createVerticalPath } from './three-utils';

export interface AttractionZone {
  x: number;
  z: number;
  radius: number;
}

export class ParkBuilder {
  private parkEnvironment = new ParkEnvironment();
  private attractionZones: AttractionZone[] = [];

  getAttractionZones(): AttractionZone[] {
    return this.attractionZones;
  }

  buildParkEnvironment(scene: THREE.Scene): void {
    const ground = this.parkEnvironment.createGround(120);
    scene.add(ground);

    this.addPlazas(scene);
    this.addPaths(scene);
    this.addEntrance(scene);
    this.addLake(scene);
    this.buildParkWalls(scene);
    this.addParkDecorations(scene);
  }

  private addPlazas(scene: THREE.Scene): void {
    const centralPlaza = this.parkEnvironment.createCircularPlaza(12);
    centralPlaza.position.set(0, 0.02, 0);
    scene.add(centralPlaza);

    const entrancePlaza = this.parkEnvironment.createCircularPlaza(8);
    entrancePlaza.position.set(0, 0.02, -45);
    scene.add(entrancePlaza);

    const plazaPositions = [
      { x: 30, z: 25 }, { x: -30, z: 25 },
      { x: 30, z: -20 }, { x: -30, z: -20 }
    ];

    plazaPositions.forEach(pos => {
      const plaza = this.parkEnvironment.createCircularPlaza(5);
      plaza.position.set(pos.x, 0.02, pos.z);
      scene.add(plaza);
    });
  }

  private addPaths(scene: THREE.Scene): void {
    scene.add(createVerticalPath(0, -45, 40, 6));
    scene.add(createHorizontalPath(0, -40, 40, 5));

    scene.add(createVerticalPath(30, 0, 25, 4));
    scene.add(createHorizontalPath(25, 0, 30, 4));
    scene.add(createVerticalPath(-30, 0, 25, 4));
    scene.add(createHorizontalPath(25, -30, 0, 4));
    scene.add(createVerticalPath(30, -20, 0, 4));
    scene.add(createHorizontalPath(-20, 0, 30, 4));
    scene.add(createVerticalPath(-30, -20, 0, 4));
    scene.add(createHorizontalPath(-20, -30, 0, 4));

    scene.add(createHorizontalPath(25, -30, 30, 3));
    scene.add(createHorizontalPath(-20, -30, 30, 3));
    scene.add(createVerticalPath(30, -20, 25, 3));
    scene.add(createVerticalPath(-30, -20, 25, 3));
  }

  private addEntrance(scene: THREE.Scene): void {
    const entrance = this.parkEnvironment.createEntrance();
    entrance.position.set(0, 0, -48);
    scene.add(entrance);
  }

  private addLake(scene: THREE.Scene): void {
    const lake = this.parkEnvironment.createLake(9, 7);
    lake.position.set(-40, 0.05, 40);
    scene.add(lake);
    this.attractionZones.push({ x: -40, z: 40, radius: 12 });
  }

  private buildParkWalls(scene: THREE.Scene): void {
    const wallHeight = 4;
    const wallThickness = 1;
    const parkSize = 52;

    const wallMaterial = new THREE.MeshStandardMaterial({
      color: 0x8b7355,
      roughness: 0.9
    });

    this.createWallSegments(scene, wallMaterial, wallHeight, wallThickness, parkSize);
    this.addCornerTowers(scene, parkSize, wallHeight);
  }

  private createWallSegments(
    scene: THREE.Scene,
    material: THREE.Material,
    height: number,
    thickness: number,
    size: number
  ): void {
    const wallSouthLeft = new THREE.Mesh(new THREE.BoxGeometry(size - 12, height, thickness), material);
    wallSouthLeft.position.set(-30, height / 2, -size);
    wallSouthLeft.castShadow = true;
    scene.add(wallSouthLeft);

    const wallSouthRight = new THREE.Mesh(new THREE.BoxGeometry(size - 12, height, thickness), material);
    wallSouthRight.position.set(30, height / 2, -size);
    wallSouthRight.castShadow = true;
    scene.add(wallSouthRight);

    const wallNorth = new THREE.Mesh(new THREE.BoxGeometry(size * 2, height, thickness), material);
    wallNorth.position.set(0, height / 2, size);
    wallNorth.castShadow = true;
    scene.add(wallNorth);

    const wallEast = new THREE.Mesh(new THREE.BoxGeometry(thickness, height, size * 2), material);
    wallEast.position.set(size, height / 2, 0);
    wallEast.castShadow = true;
    scene.add(wallEast);

    const wallWest = new THREE.Mesh(new THREE.BoxGeometry(thickness, height, size * 2), material);
    wallWest.position.set(-size, height / 2, 0);
    wallWest.castShadow = true;
    scene.add(wallWest);
  }

  private addCornerTowers(scene: THREE.Scene, parkSize: number, wallHeight: number): void {
    const towerGeo = new THREE.CylinderGeometry(2, 2.5, wallHeight + 2, 8);
    const towerMat = new THREE.MeshStandardMaterial({ color: 0x6b5344, roughness: 0.8 });
    const roofGeo = new THREE.ConeGeometry(3, 3, 8);
    const roofMat = new THREE.MeshStandardMaterial({ color: 0x4a0000 });

    const corners = [
      { x: -parkSize, z: -parkSize },
      { x: parkSize, z: -parkSize },
      { x: -parkSize, z: parkSize },
      { x: parkSize, z: parkSize }
    ];

    corners.forEach(corner => {
      const tower = new THREE.Mesh(towerGeo, towerMat);
      tower.position.set(corner.x, (wallHeight + 2) / 2, corner.z);
      tower.castShadow = true;
      scene.add(tower);

      const roof = new THREE.Mesh(roofGeo, roofMat);
      roof.position.set(corner.x, wallHeight + 3.5, corner.z);
      scene.add(roof);
    });

    const entranceTowerPositions = [{ x: -10, z: -48 }, { x: 10, z: -48 }];
    entranceTowerPositions.forEach(pos => {
      const tower = new THREE.Mesh(towerGeo, towerMat);
      tower.position.set(pos.x, (wallHeight + 2) / 2, pos.z);
      tower.castShadow = true;
      scene.add(tower);

      const roof = new THREE.Mesh(roofGeo, roofMat);
      roof.position.set(pos.x, wallHeight + 3.5, pos.z);
      scene.add(roof);
    });
  }

  private addParkDecorations(scene: THREE.Scene): void {
    this.addTrees(scene);
    this.addBenches(scene);
    this.addLamps(scene);
    this.addFountain(scene);
    this.addTrashCans(scene);
    this.addShops(scene);
  }

  private addTrees(scene: THREE.Scene): void {
    const treePositions = [
      { x: -45, z: 45 }, { x: -20, z: 45 }, { x: 20, z: 45 }, { x: 45, z: 45 },
      { x: 45, z: 30 }, { x: 45, z: 10 }, { x: 45, z: -10 }, { x: 45, z: -35 },
      { x: -45, z: 10 }, { x: -45, z: -10 }, { x: -45, z: -35 },
      { x: 15, z: 35 }, { x: -15, z: 35 },
      { x: 15, z: 15 }, { x: -15, z: 15 },
      { x: 40, z: 10 }, { x: -40, z: 10 },
      { x: 15, z: -30 }, { x: -15, z: -30 },
      { x: 40, z: -35 }, { x: -40, z: -35 },
    ];

    treePositions.forEach(pos => {
      if (!this.isInReservedZone(pos.x, pos.z)) {
        const tree = this.parkEnvironment.createTree();
        tree.position.set(pos.x, 0, pos.z);
        scene.add(tree);
      }
    });
  }

  private addBenches(scene: THREE.Scene): void {
    const benchPositions = [
      { x: 12, z: 4, rot: Math.PI },
      { x: -12, z: 4, rot: Math.PI },
      { x: 12, z: -4, rot: 0 },
      { x: -12, z: -4, rot: 0 },
      { x: 4, z: 15, rot: Math.PI / 2 },
      { x: -4, z: 15, rot: -Math.PI / 2 },
      { x: 30, z: 32, rot: Math.PI },
      { x: -30, z: 32, rot: Math.PI },
    ];

    benchPositions.forEach(pos => {
      const bench = this.parkEnvironment.createBench();
      bench.position.set(pos.x, 0, pos.z);
      bench.rotation.y = pos.rot;
      scene.add(bench);
    });
  }

  private addLamps(scene: THREE.Scene): void {
    const lampPositions = [
      { x: 4, z: 20 }, { x: -4, z: 20 },
      { x: 4, z: -25 }, { x: -4, z: -25 },
      { x: 4, z: -38 }, { x: -4, z: -38 },
      { x: 20, z: 4 }, { x: -20, z: 4 },
      { x: 35, z: 4 }, { x: -35, z: 4 },
      { x: 30, z: 18 }, { x: -30, z: 18 },
      { x: 30, z: -13 }, { x: -30, z: -13 },
    ];

    lampPositions.forEach(pos => {
      const lamp = this.parkEnvironment.createStreetLamp();
      lamp.position.set(pos.x, 0, pos.z);
      scene.add(lamp);
    });
  }

  private addFountain(scene: THREE.Scene): void {
    const fountain = this.parkEnvironment.createFountain();
    fountain.position.set(0, 0, 0);
    scene.add(fountain);
  }

  private addTrashCans(scene: THREE.Scene): void {
    const trashPositions = [
      { x: 8, z: 4 }, { x: -8, z: 4 },
      { x: 4, z: 12 }, { x: -4, z: 12 },
      { x: 25, z: 25 }, { x: -25, z: 25 },
      { x: 25, z: -20 }, { x: -25, z: -20 },
    ];

    trashPositions.forEach(pos => {
      const trash = this.parkEnvironment.createTrashCan();
      trash.position.set(pos.x, 0, pos.z);
      scene.add(trash);
    });
  }

  private addShops(scene: THREE.Scene): void {
    const shops: Array<{ type: 'food' | 'souvenirs' | 'icecream'; x: number; z: number; rot: number }> = [
      { type: 'souvenirs', x: -18, z: -38, rot: Math.PI / 6 },
      { type: 'food', x: 40, z: 20, rot: -Math.PI / 2 },
      { type: 'icecream', x: -40, z: -10, rot: Math.PI / 2 },
      { type: 'food', x: 40, z: -35, rot: -Math.PI / 2 },
      { type: 'souvenirs', x: 18, z: 38, rot: Math.PI },
    ];

    shops.forEach(s => {
      const shop = this.parkEnvironment.createShop(s.type);
      shop.position.set(s.x, 0, s.z);
      shop.rotation.y = s.rot;
      scene.add(shop);
    });
  }

  isInReservedZone(x: number, z: number): boolean {
    for (const zone of this.attractionZones) {
      const dist = Math.sqrt((x - zone.x) ** 2 + (z - zone.z) ** 2);
      if (dist < zone.radius) return true;
    }
    return false;
  }

  addAttractionZone(x: number, z: number, radius: number): void {
    this.attractionZones.push({ x, z, radius });
  }
}

