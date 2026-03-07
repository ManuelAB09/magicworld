import * as THREE from 'three';
import { ParkEnvironment } from './park-environment';
import { createHorizontalPath, createVerticalPath } from './three-utils';
import { secureRandom } from './secure-random';

export interface AttractionZone {
  x: number;
  z: number;
  radius: number;
}

export class ParkBuilder {
  private parkEnvironment = new ParkEnvironment();
  private attractionZones: AttractionZone[] = [];
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
    // Main avenue
    scene.add(createVerticalPath(0, -45, 40, 6));
    scene.add(createHorizontalPath(0, -40, 40, 5));

    // Connections to sub-plazas
    scene.add(createVerticalPath(30, 0, 25, 4));
    scene.add(createHorizontalPath(25, 0, 30, 4));
    scene.add(createVerticalPath(-30, 0, 25, 4));
    scene.add(createHorizontalPath(25, -30, 0, 4));
    scene.add(createVerticalPath(30, -20, 0, 4));
    scene.add(createHorizontalPath(-20, 0, 30, 4));
    scene.add(createVerticalPath(-30, -20, 0, 4));
    scene.add(createHorizontalPath(-20, -30, 0, 4));

    // Outer ring
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
    this.addWallCrenellations(scene, wallHeight, parkSize);
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

  /* ─── Crenellations (merlons) on top of walls ─── */
  private addWallCrenellations(scene: THREE.Scene, wallHeight: number, parkSize: number): void {
    const merlonGeo = new THREE.BoxGeometry(1.5, 1.2, 1.2);
    const merlonMat = new THREE.MeshStandardMaterial({ color: 0x7a6648, roughness: 0.9 });
    const merlonSpacing = 4;
    const topY = wallHeight + 0.6;

    // north wall
    for (let x = -parkSize; x <= parkSize; x += merlonSpacing) {
      const merlon = new THREE.Mesh(merlonGeo, merlonMat);
      merlon.position.set(x, topY, parkSize);
      merlon.castShadow = true;
      scene.add(merlon);
    }

    // south wall (with gap for entrance)
    for (let x = -parkSize; x <= parkSize; x += merlonSpacing) {
      if (Math.abs(x) < 8) continue; // entrance gap
      const merlon = new THREE.Mesh(merlonGeo, merlonMat);
      merlon.position.set(x, topY, -parkSize);
      merlon.castShadow = true;
      scene.add(merlon);
    }

    // east & west walls
    for (let z = -parkSize; z <= parkSize; z += merlonSpacing) {
      const merlonE = new THREE.Mesh(merlonGeo, merlonMat);
      merlonE.position.set(parkSize, topY, z);
      merlonE.castShadow = true;
      scene.add(merlonE);

      const merlonW = new THREE.Mesh(merlonGeo, merlonMat);
      merlonW.position.set(-parkSize, topY, z);
      merlonW.castShadow = true;
      scene.add(merlonW);
    }
  }

  private addCornerTowers(scene: THREE.Scene, parkSize: number, wallHeight: number): void {
    const towerGeo = new THREE.CylinderGeometry(2, 2.5, wallHeight + 3, 8);
    const towerMat = new THREE.MeshStandardMaterial({ color: 0x6b5344, roughness: 0.8 });
    const roofGeo = new THREE.ConeGeometry(3, 3.5, 8);
    const roofMat = new THREE.MeshStandardMaterial({ color: 0x4a0000 });

    const corners = [
      { x: -parkSize, z: -parkSize },
      { x: parkSize, z: -parkSize },
      { x: -parkSize, z: parkSize },
      { x: parkSize, z: parkSize }
    ];

    corners.forEach(corner => {
      const tower = new THREE.Mesh(towerGeo, towerMat);
      tower.position.set(corner.x, (wallHeight + 3) / 2, corner.z);
      tower.castShadow = true;
      scene.add(tower);

      const roof = new THREE.Mesh(roofGeo, roofMat);
      roof.position.set(corner.x, wallHeight + 5, corner.z);
      roof.castShadow = true;
      scene.add(roof);

      // window
      const windowGeo = new THREE.BoxGeometry(0.6, 0.8, 0.1);
      const windowMat = new THREE.MeshStandardMaterial({
        color: 0x1a1a1a,
        emissive: 0x332200,
        emissiveIntensity: 0.3,
      });
      const win = new THREE.Mesh(windowGeo, windowMat);
      win.position.set(corner.x, wallHeight + 1, corner.z + (corner.z > 0 ? -2.2 : 2.2));
      scene.add(win);

      // flag on tower
      const flagPoleGeo = new THREE.CylinderGeometry(0.04, 0.04, 2);
      const flagPole = new THREE.Mesh(flagPoleGeo, new THREE.MeshStandardMaterial({ color: 0x333333 }));
      flagPole.position.set(corner.x, wallHeight + 7, corner.z);
      scene.add(flagPole);

      const flag = new THREE.Mesh(
        new THREE.PlaneGeometry(1.0, 0.6),
        new THREE.MeshStandardMaterial({ color: 0xcc0000, side: THREE.DoubleSide })
      );
      flag.position.set(corner.x + 0.5, wallHeight + 7.5, corner.z);
      scene.add(flag);
    });

    // entrance towers
    const entranceTowerPositions = [{ x: -10, z: -48 }, { x: 10, z: -48 }];
    entranceTowerPositions.forEach(pos => {
      const tower = new THREE.Mesh(towerGeo, towerMat);
      tower.position.set(pos.x, (wallHeight + 3) / 2, pos.z);
      tower.castShadow = true;
      scene.add(tower);

      const roof = new THREE.Mesh(roofGeo, roofMat);
      roof.position.set(pos.x, wallHeight + 5, pos.z);
      roof.castShadow = true;
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
    this.addFlowerBeds(scene);
    this.addHedgeRows(scene);
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
      // extra trees for lushness
      { x: 48, z: 20 }, { x: -48, z: 20 },
      { x: 25, z: 42 }, { x: -25, z: 42 },
      { x: 48, z: -20 }, { x: -48, z: -20 },
      { x: 10, z: 42 }, { x: -10, z: 42 },
    ];

    const species: ('round' | 'cone' | 'palm' | 'oak')[] = ['round', 'cone', 'oak', 'palm'];
    treePositions.forEach((pos, i) => {
      if (!this.isInReservedZone(pos.x, pos.z)) {
        const tree = this.parkEnvironment.createTree(species[i % species.length]);
        const scale = 0.8 + secureRandom() * 0.5;
        tree.scale.setScalar(scale);
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

  private addFlowerBeds(scene: THREE.Scene): void {
    const positions = [
      { x: 8, z: 10, rot: 0 },
      { x: -8, z: 10, rot: 0 },
      { x: 15, z: -8, rot: Math.PI / 2 },
      { x: -15, z: -8, rot: Math.PI / 2 },
      { x: 25, z: 30, rot: 0 },
      { x: -25, z: 30, rot: 0 },
    ];

    positions.forEach(p => {
      const bed = this.parkEnvironment.createFlowerBed(2.5, 1.2);
      bed.position.set(p.x, 0, p.z);
      bed.rotation.y = p.rot;
      scene.add(bed);
    });
  }

  private addHedgeRows(scene: THREE.Scene): void {
    // hedges around central plaza
    const hedgePositions = [
      { x: 14, z: 2, rot: 0, len: 4 },
      { x: -14, z: 2, rot: 0, len: 4 },
      { x: 14, z: -2, rot: 0, len: 4 },
      { x: -14, z: -2, rot: 0, len: 4 },
    ];

    hedgePositions.forEach(p => {
      const hedge = this.parkEnvironment.createHedge(p.len, 1.0);
      hedge.position.set(p.x, 0, p.z);
      hedge.rotation.y = p.rot;
      scene.add(hedge);
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
