import * as THREE from 'three';

export class ParkDecorations {

  createTree(height: number = 3, foliageColor: number = 0x228b22): THREE.Group {
    const tree = new THREE.Group();

    const trunkRadiusTop = Math.max(0.12, height * 0.03);
    const trunkRadiusBottom = Math.max(0.18, height * 0.04);
    const trunkHeight = height * 0.45;
    const trunkGeo = new THREE.CylinderGeometry(trunkRadiusTop, trunkRadiusBottom, trunkHeight, 10);
    const trunkMat = new THREE.MeshStandardMaterial({ color: 0x6b3e1b, roughness: 1 });
    const trunk = new THREE.Mesh(trunkGeo, trunkMat);
    trunk.position.y = trunkHeight / 2;
    trunk.castShadow = true;
    tree.add(trunk);

    const crownMat = new THREE.MeshStandardMaterial({ color: foliageColor, roughness: 0.85 });
    const layerCount = 3;
    for (let i = 0; i < layerCount; i++) {
      const scale = 1 - i * 0.2;
      const h = height * (0.35 + 0.15 * i);
      const geom = new THREE.ConeGeometry(height * 0.28 * scale, height * 0.45 * (1 - i * 0.08), 12);
      const mesh = new THREE.Mesh(geom, crownMat);
      mesh.position.y = trunkHeight + i * (height * 0.18);
      mesh.castShadow = true;
      mesh.rotation.y = (Math.random() - 0.5) * 0.4;
      tree.add(mesh);
    }

    for (let b = 0; b < Math.floor(height); b++) {
      const bx = (Math.random() - 0.5) * 0.5;
      const bz = (Math.random() - 0.5) * 0.5;
      const branch = new THREE.Mesh(new THREE.CylinderGeometry(0.02, 0.03, 0.6, 6), trunkMat);
      branch.position.set(bx, trunkHeight * (0.4 + Math.random() * 0.5), bz);
      branch.rotation.z = Math.random() * 0.8 - 0.4;
      branch.castShadow = true;
      tree.add(branch);
    }

    return tree;
  }

  createBush(size: number = 0.5): THREE.Mesh {
    const geometry = new THREE.SphereGeometry(size, 12, 10);
    const material = new THREE.MeshStandardMaterial({ color: 0x2f8b3f, roughness: 1.0 });
    const bush = new THREE.Mesh(geometry, material);
    bush.position.y = size * 0.5;
    bush.scale.y = 0.9;
    bush.castShadow = true;
    return bush;
  }

  createBench(): THREE.Group {
    const bench = new THREE.Group();
    const wood = new THREE.MeshStandardMaterial({ color: 0x8b4513, roughness: 1 });
    const metal = new THREE.MeshStandardMaterial({ color: 0x333333, roughness: 0.8 });

    const seat = new THREE.Mesh(new THREE.BoxGeometry(1.4, 0.08, 0.45), wood);
    seat.position.y = 0.46;
    seat.castShadow = true;
    bench.add(seat);

    const backrest = new THREE.Mesh(new THREE.BoxGeometry(1.4, 0.45, 0.06), wood);
    backrest.position.set(0, 0.72, -0.18);
    bench.add(backrest);

    const legs = [
      { x: -0.55, z: 0.15 }, { x: 0.55, z: 0.15 },
      { x: -0.55, z: -0.15 }, { x: 0.55, z: -0.15 }
    ];
    legs.forEach(lp => {
      const leg = new THREE.Mesh(new THREE.BoxGeometry(0.07, 0.45, 0.07), metal);
      leg.position.set(lp.x, 0.225, lp.z);
      leg.castShadow = true;
      bench.add(leg);
    });

    return bench;
  }

  createLampPost(height: number = 4): THREE.Group {
    const lamp = new THREE.Group();
    const poleMat = new THREE.MeshStandardMaterial({ color: 0x222222, roughness: 0.9 });
    const pole = new THREE.Mesh(new THREE.CylinderGeometry(0.06, 0.08, height, 8), poleMat);
    pole.position.y = height / 2;
    lamp.add(pole);

    const arm = new THREE.Mesh(new THREE.BoxGeometry(0.5, 0.04, 0.04), poleMat);
    arm.position.set(0.22, height - 0.15, 0);
    arm.castShadow = true;
    lamp.add(arm);

    const lightMat = new THREE.MeshStandardMaterial({ color: 0xffffcc, emissive: 0xfff2c2, emissiveIntensity: 0.8 });
    const bulb = new THREE.Mesh(new THREE.SphereGeometry(0.15, 8, 8), lightMat);
    bulb.position.set(0.5, height - 0.2, 0);
    lamp.add(bulb);
    return lamp;
  }

  createFountain(): THREE.Group {
    const fountain = new THREE.Group();
    const stone = new THREE.MeshStandardMaterial({ color: 0x888888, roughness: 1.0 });
    const waterMat = new THREE.MeshPhysicalMaterial({
      color: 0x3f8fd8,
      metalness: 0.05,
      roughness: 0.05,
      reflectivity: 0.6,
      clearcoat: 0.2,
      transparent: true,
      opacity: 0.92
    });

    const basin = new THREE.Mesh(new THREE.CylinderGeometry(2.0, 2.3, 0.45, 32), stone);
    basin.position.y = 0.225;
    basin.receiveShadow = true;
    fountain.add(basin);

    const water = new THREE.Mesh(new THREE.CylinderGeometry(1.6, 1.6, 0.28, 32), waterMat);
    water.position.y = 0.4;
    fountain.add(water);

    const pillar = new THREE.Mesh(new THREE.CylinderGeometry(0.28, 0.36, 1.2, 12), stone);
    pillar.position.y = 1.05;
    pillar.castShadow = true;
    fountain.add(pillar);
    const bowl = new THREE.Mesh(new THREE.CylinderGeometry(0.5, 0.42, 0.2, 12), stone);
    bowl.position.y = 1.6;
    fountain.add(bowl);

    return fountain;
  }

  createTrashBin(): THREE.Group {
    const bin = new THREE.Group();
    const bodyMat = new THREE.MeshStandardMaterial({ color: 0x2e7d32, roughness: 1.0 });
    const body = new THREE.Mesh(new THREE.CylinderGeometry(0.26, 0.22, 0.8, 12), bodyMat);
    body.position.y = 0.4;
    body.castShadow = true;
    bin.add(body);

    const lid = new THREE.Mesh(new THREE.CylinderGeometry(0.28, 0.28, 0.08, 12), bodyMat);
    lid.position.y = 0.84;
    bin.add(lid);

    return bin;
  }

  createShop(type: 'food' | 'souvenirs' | 'icecream' = 'food'): THREE.Group {
    const shop = new THREE.Group();
    const colors: Record<string, { wall: number; roof: number; stripe: number }> = {
      food: { wall: 0xfff8dc, roof: 0xcc0000, stripe: 0xffffff },
      souvenirs: { wall: 0xe6e6fa, roof: 0x4169e1, stripe: 0xffd700 },
      icecream: { wall: 0xffe4e1, roof: 0xff69b4, stripe: 0x87ceeb }
    };
    const color = colors[type];

    const wallMat = new THREE.MeshStandardMaterial({ color: color.wall, roughness: 1.0 });
    const roofMat = new THREE.MeshStandardMaterial({ color: color.roof, roughness: 1.0 });

    const base = new THREE.Mesh(new THREE.BoxGeometry(3, 2.4, 2.4), wallMat);
    base.position.y = 1.2;
    base.castShadow = true;
    base.receiveShadow = true;
    shop.add(base);

    const roof = new THREE.Mesh(new THREE.ConeGeometry(2.4, 1.0, 4), roofMat);
    roof.position.y = 3.0;
    roof.rotation.y = Math.PI / 4;
    roof.castShadow = true;
    shop.add(roof);

    const awning = new THREE.Mesh(new THREE.BoxGeometry(3.2, 0.12, 1), roofMat);
    awning.position.set(0, 2.05, 1.15);
    awning.rotation.x = -0.15;
    shop.add(awning);

    const counter = new THREE.Mesh(new THREE.BoxGeometry(2.4, 0.7, 0.28), new THREE.MeshStandardMaterial({ color: 0x8b4513 }));
    counter.position.set(0, 0.85, 1.12);
    shop.add(counter);

    return shop;
  }

  createLake(radiusX: number = 8, radiusZ: number = 5): THREE.Group {
    // identical to earlier definition: safe, flat, small vertical offsets
    const lake = new THREE.Group();

    const shoreShape = new THREE.Shape();
    shoreShape.absellipse(0, 0, radiusX + 0.5, radiusZ + 0.5, 0, Math.PI * 2, false, 0);
    const shoreGeo = new THREE.ShapeGeometry(shoreShape);
    const shoreMesh = new THREE.Mesh(shoreGeo, new THREE.MeshStandardMaterial({ color: 0xc2b280 }));
    shoreMesh.rotation.x = -Math.PI / 2;
    shoreMesh.position.y = 0.02;
    lake.add(shoreMesh);

    const waterGeo = new THREE.ShapeGeometry(new THREE.Shape().absellipse(0, 0, radiusX, radiusZ, 0, Math.PI * 2, false, 0));
    const waterMat = new THREE.MeshPhysicalMaterial({
      color: 0x3fa0e0,
      metalness: 0.05,
      roughness: 0.08,
      reflectivity: 0.6,
      clearcoat: 0.2,
      transparent: true,
      opacity: 0.92
    });
    const waterMesh = new THREE.Mesh(waterGeo, waterMat);
    waterMesh.rotation.x = -Math.PI / 2;
    waterMesh.position.y = 0.035;
    lake.add(waterMesh);

    return lake;
  }

  createFlowerBed(width: number = 2, depth: number = 1): THREE.Group {
    const bed = new THREE.Group();
    const flowerColors = [0xff6b6b, 0xffd93d, 0xff8c00, 0xda70d6, 0x87ceeb];

    const soil = new THREE.Mesh(new THREE.BoxGeometry(width, 0.15, depth), new THREE.MeshStandardMaterial({ color: 0x4a3728 }));
    soil.position.y = 0.075;
    bed.add(soil);

    for (let i = 0; i < 12; i++) {
      const x = (Math.random() - 0.5) * (width - 0.2);
      const z = (Math.random() - 0.5) * (depth - 0.2);
      const color = flowerColors[Math.floor(Math.random() * flowerColors.length)];

      const stem = new THREE.Mesh(new THREE.CylinderGeometry(0.02, 0.02, 0.28), new THREE.MeshStandardMaterial({ color: 0x228b22 }));
      stem.position.set(x, 0.28, z);
      bed.add(stem);

      const flower = new THREE.Mesh(new THREE.SphereGeometry(0.07, 8, 8), new THREE.MeshStandardMaterial({ color }));
      flower.position.set(x, 0.44, z);
      bed.add(flower);
    }

    return bed;
  }

  createInfoSign(text?: string): THREE.Group {
    const sign = new THREE.Group();
    const post = new THREE.Mesh(new THREE.CylinderGeometry(0.08, 0.08, 2), new THREE.MeshStandardMaterial({ color: 0x8b4513 }));
    post.position.y = 1;
    sign.add(post);

    const board = new THREE.Mesh(new THREE.BoxGeometry(1.2, 0.8, 0.06), new THREE.MeshStandardMaterial({ color: 0x2e7d32 }));
    board.position.y = 2.2;
    sign.add(board);

    return sign;
  }

  createParkEntrance(): THREE.Group {
    // more decorative entrance (used optionally)
    const entrance = new THREE.Group();
    const goldMat = new THREE.MeshStandardMaterial({ color: 0xffd700, metalness: 0.6, roughness: 0.3 });
    const stoneMat = new THREE.MeshStandardMaterial({ color: 0x8b7355, roughness: 0.9 });
    const darkStoneMat = new THREE.MeshStandardMaterial({ color: 0x5a4a3a, roughness: 0.95 });
    const redMat = new THREE.MeshStandardMaterial({ color: 0x8b0000, roughness: 0.7 });

    const createTower = (x: number): THREE.Group => {
      const tower = new THREE.Group();
      const base = new THREE.Mesh(new THREE.CylinderGeometry(1.8, 2.2, 8, 8), stoneMat);
      base.position.y = 4;
      base.castShadow = true;
      tower.add(base);

      const top = new THREE.Mesh(new THREE.ConeGeometry(2.4, 3, 8), redMat);
      top.position.y = 9.5;
      top.castShadow = true;
      tower.add(top);

      const flagPole = new THREE.Mesh(new THREE.CylinderGeometry(0.05, 0.05, 2), darkStoneMat);
      flagPole.position.y = 11.5;
      tower.add(flagPole);

      const flag = new THREE.Mesh(new THREE.PlaneGeometry(1.2, 0.8), goldMat);
      flag.position.set(0.6, 12, 0);
      tower.add(flag);

      for (let i = 0; i < 3; i++) {
        const ring = new THREE.Mesh(new THREE.TorusGeometry(1.9, 0.12, 8, 16), goldMat);
        ring.position.y = 2 + i * 2.5;
        ring.rotation.x = Math.PI / 2;
        tower.add(ring);
      }

      tower.position.x = x;
      return tower;
    };

    entrance.add(createTower(-6));
    entrance.add(createTower(6));

    const archBase = new THREE.Mesh(new THREE.BoxGeometry(14, 2, 2), stoneMat);
    archBase.position.set(0, 9, 0);
    archBase.castShadow = true;
    entrance.add(archBase);

    const archTop = new THREE.Mesh(new THREE.BoxGeometry(12, 1, 1.5), goldMat);
    archTop.position.set(0, 10.5, 0);
    entrance.add(archTop);

    const signBoard = new THREE.Mesh(new THREE.BoxGeometry(8, 1.8, 0.3), redMat);
    signBoard.position.set(0, 9, 1.2);
    entrance.add(signBoard);

    const signBorder = new THREE.Mesh(new THREE.BoxGeometry(8.4, 2.2, 0.2), goldMat);
    signBorder.position.set(0, 9, 1.05);
    entrance.add(signBorder);

    const gateLeft = new THREE.Mesh(new THREE.BoxGeometry(0.15, 7, 2.5), new THREE.MeshStandardMaterial({ color: 0x1a1a1a, metalness: 0.8 }));
    gateLeft.position.set(-2.5, 3.5, 0);
    entrance.add(gateLeft);
    const gateRight = gateLeft.clone();
    gateRight.position.set(2.5, 3.5, 0);
    entrance.add(gateRight);

    for (let i = -2; i <= 2; i++) {
      const bar = new THREE.Mesh(new THREE.CylinderGeometry(0.06, 0.06, 7), new THREE.MeshStandardMaterial({ color: 0x1a1a1a, metalness: 0.8 }));
      bar.position.set(i * 1.2, 3.5, 0);
      entrance.add(bar);
    }

    return entrance;
  }

  createPerimeterWall(length: number, height: number = 3): THREE.Group {
    const wall = new THREE.Group();
    const stoneMat = new THREE.MeshStandardMaterial({ color: 0x8b7355, roughness: 0.95 });
    const capMat = new THREE.MeshStandardMaterial({ color: 0x6b5a4a, roughness: 0.9 });

    const mainWall = new THREE.Mesh(new THREE.BoxGeometry(length, height, 0.8), stoneMat);
    mainWall.position.y = height / 2;
    mainWall.castShadow = true;
    mainWall.receiveShadow = true;
    wall.add(mainWall);

    const cap = new THREE.Mesh(new THREE.BoxGeometry(length + 0.2, 0.3, 1), capMat);
    cap.position.y = height + 0.15;
    wall.add(cap);

    const pillarSpacing = 10;
    const pillarCount = Math.floor(length / pillarSpacing);
    for (let i = 0; i <= pillarCount; i++) {
      const x = -length / 2 + i * pillarSpacing;
      const pillar = new THREE.Mesh(new THREE.BoxGeometry(1.2, height + 1, 1.2), stoneMat);
      pillar.position.set(x, (height + 1) / 2, 0);
      pillar.castShadow = true;
      wall.add(pillar);

      const pillarCap = new THREE.Mesh(new THREE.ConeGeometry(0.8, 0.8, 4), capMat);
      pillarCap.position.set(x, height + 1.4, 0);
      pillarCap.rotation.y = Math.PI / 4;
      wall.add(pillarCap);
    }

    return wall;
  }

  createCornerTower(): THREE.Group {
    const tower = new THREE.Group();
    const stoneMat = new THREE.MeshStandardMaterial({ color: 0x8b7355, roughness: 0.95 });
    const roofMat = new THREE.MeshStandardMaterial({ color: 0x4a3a2a, roughness: 0.9 });

    const base = new THREE.Mesh(new THREE.CylinderGeometry(2.5, 3, 5, 8), stoneMat);
    base.position.y = 2.5;
    base.castShadow = true;
    tower.add(base);

    const roof = new THREE.Mesh(new THREE.ConeGeometry(3.2, 2.5, 8), roofMat);
    roof.position.y = 6.25;
    roof.castShadow = true;
    tower.add(roof);

    return tower;
  }

  createBridge(length: number = 6): THREE.Group {
    const bridge = new THREE.Group();
    const woodMat = new THREE.MeshStandardMaterial({ color: 0x8b5a2b, roughness: 0.9 });
    const ropeMat = new THREE.MeshStandardMaterial({ color: 0x8b7355 });

    const deck = new THREE.Mesh(new THREE.BoxGeometry(length, 0.2, 2.5), woodMat);
    deck.position.y = 0.4;
    deck.receiveShadow = true;
    bridge.add(deck);

    for (let i = 0; i < length * 2; i++) {
      const plank = new THREE.Mesh(new THREE.BoxGeometry(0.4, 0.08, 2.6), woodMat);
      plank.position.set(-length / 2 + 0.3 + i * 0.5, 0.52, 0);
      bridge.add(plank);
    }

    const createRailing = (z: number): void => {
      const rail = new THREE.Mesh(new THREE.CylinderGeometry(0.08, 0.08, length, 8), ropeMat);
      rail.rotation.z = Math.PI / 2;
      rail.position.set(0, 1.2, z);
      bridge.add(rail);

      for (let i = 0; i <= 6; i++) {
        const post = new THREE.Mesh(new THREE.CylinderGeometry(0.06, 0.06, 0.9), woodMat);
        post.position.set(-length / 2 + i * (length / 6), 0.85, z);
        bridge.add(post);
      }
    };

    createRailing(1.1);
    createRailing(-1.1);

    return bridge;
  }
}
