import * as THREE from 'three';

export class ParkEnvironment {

  createGround(size: number): THREE.Mesh {
    const geometry = new THREE.PlaneGeometry(size * 2, size * 2, 64, 64);
    const material = new THREE.MeshStandardMaterial({
      color: 0x4a7c59,
      roughness: 0.9,
      metalness: 0.0
    });
    const ground = new THREE.Mesh(geometry, material);
    ground.rotation.x = -Math.PI / 2;
    ground.receiveShadow = true;

    ground.position.y = 0;
    return ground;
  }


  createMainPath(length: number, width: number): THREE.Mesh {
    const thickness = 0.06;
    const geometry = new THREE.BoxGeometry(length, thickness, width);
    const material = new THREE.MeshStandardMaterial({
      color: 0xc9b896,
      roughness: 0.95,
      metalness: 0.0
    });
    const path = new THREE.Mesh(geometry, material);
    path.position.y = thickness / 2 + 0.01;
    path.receiveShadow = true;
    path.castShadow = false;
    return path;
  }

  createCircularPlaza(radius: number): THREE.Mesh {
    const geometry = new THREE.CylinderGeometry(radius, radius, 0.08, 48);
    const material = new THREE.MeshStandardMaterial({
      color: 0xd4c4a8,
      roughness: 0.9
    });
    const plaza = new THREE.Mesh(geometry, material);
    plaza.receiveShadow = true;
    plaza.position.y = 0.04;
    return plaza;
  }

  createLake(radiusX: number, radiusZ: number): THREE.Group {
    const group = new THREE.Group();


    const waterGeo = new THREE.CircleGeometry(radiusX, 32);
    const waterMat = new THREE.MeshPhysicalMaterial({
      color: 0x1e90ff,
      transparent: true,
      opacity: 0.8,
      roughness: 0.08,
      metalness: 0.2,
    });
    const water = new THREE.Mesh(waterGeo, waterMat);
    water.rotation.x = -Math.PI / 2;
    water.position.y = 0.035;
    group.add(water);


    const borderGeo = new THREE.RingGeometry(radiusX, radiusX + 1.5, 32);
    const borderMat = new THREE.MeshStandardMaterial({
      color: 0x8b7355,
      roughness: 0.9
    });
    const border = new THREE.Mesh(borderGeo, borderMat);
    border.rotation.x = -Math.PI / 2;
    border.position.y = 0.02;
    group.add(border);

    return group;
  }

  createEntrance(): THREE.Group {
    const group = new THREE.Group();


    const archGeo = new THREE.TorusGeometry(6, 0.8, 8, 16, Math.PI);
    const archMat = new THREE.MeshStandardMaterial({ color: 0x8b4513, roughness: 0.7 });
    const arch = new THREE.Mesh(archGeo, archMat);
    arch.rotation.x = Math.PI / 2;
    arch.position.y = 8;
    arch.castShadow = true;
    group.add(arch);

    const pillarGeo = new THREE.CylinderGeometry(0.8, 1, 8, 8);
    const pillarMat = new THREE.MeshStandardMaterial({ color: 0x8b4513, roughness: 0.7 });

    const leftPillar = new THREE.Mesh(pillarGeo, pillarMat);
    leftPillar.position.set(-6, 4, 0);
    leftPillar.castShadow = true;
    group.add(leftPillar);

    const rightPillar = new THREE.Mesh(pillarGeo, pillarMat);
    rightPillar.position.set(6, 4, 0);
    rightPillar.castShadow = true;
    group.add(rightPillar);

    const signGeo = new THREE.BoxGeometry(10, 2, 0.3);
    const signMat = new THREE.MeshStandardMaterial({ color: 0x4a0000 });
    const sign = new THREE.Mesh(signGeo, signMat);
    sign.position.y = 10;
    sign.castShadow = true;
    group.add(sign);


    const starGeo = new THREE.OctahedronGeometry(0.6);
    const starMat = new THREE.MeshStandardMaterial({ color: 0xffd700, metalness: 0.8, roughness: 0.2 });

    for (let i = -3; i <= 3; i += 2) {
      const star = new THREE.Mesh(starGeo, starMat);
      star.position.set(i, 10, 0.3);
      group.add(star);
    }

    return group;
  }

  createTree(): THREE.Group {
    const group = new THREE.Group();

    const trunkGeo = new THREE.CylinderGeometry(0.3, 0.5, 3, 8);
    const trunkMat = new THREE.MeshStandardMaterial({ color: 0x4a3728, roughness: 0.9 });
    const trunk = new THREE.Mesh(trunkGeo, trunkMat);
    trunk.position.y = 1.5;
    trunk.castShadow = true;
    group.add(trunk);

    const leafMat = new THREE.MeshStandardMaterial({ color: 0x228b22, roughness: 0.8 });

    const positions = [
      { x: 0, y: 4.5, z: 0, r: 2 },
      { x: 1, y: 4, z: 0.5, r: 1.3 },
      { x: -0.8, y: 4.2, z: -0.5, r: 1.4 },
      { x: 0, y: 5.5, z: 0, r: 1.2 },
    ];

    positions.forEach(p => {
      const leafGeo = new THREE.SphereGeometry(p.r, 8, 8);
      const leaf = new THREE.Mesh(leafGeo, leafMat);
      leaf.position.set(p.x, p.y, p.z);
      leaf.castShadow = true;
      group.add(leaf);
    });

    return group;
  }

  createBench(): THREE.Group {
    const group = new THREE.Group();

    const seatGeo = new THREE.BoxGeometry(2, 0.15, 0.6);
    const woodMat = new THREE.MeshStandardMaterial({ color: 0x8b4513, roughness: 0.8 });
    const seat = new THREE.Mesh(seatGeo, woodMat);
    seat.position.y = 0.5;
    seat.castShadow = true;
    group.add(seat);

    const backGeo = new THREE.BoxGeometry(2, 0.6, 0.1);
    const back = new THREE.Mesh(backGeo, woodMat);
    back.position.set(0, 0.8, -0.25);
    back.castShadow = true;
    group.add(back);

    const legGeo = new THREE.BoxGeometry(0.1, 0.5, 0.4);
    const metalMat = new THREE.MeshStandardMaterial({ color: 0x333333, metalness: 0.7 });

    const legPositions = [
      { x: -0.8, z: 0 },
      { x: 0.8, z: 0 }
    ];

    legPositions.forEach(pos => {
      const leg = new THREE.Mesh(legGeo, metalMat);
      leg.position.set(pos.x, 0.25, pos.z);
      group.add(leg);
    });

    return group;
  }

  createStreetLamp(): THREE.Group {
    const group = new THREE.Group();

    const poleGeo = new THREE.CylinderGeometry(0.1, 0.15, 5, 8);
    const poleMat = new THREE.MeshStandardMaterial({ color: 0x222222, metalness: 0.7 });
    const pole = new THREE.Mesh(poleGeo, poleMat);
    pole.position.y = 2.5;
    pole.castShadow = true;
    group.add(pole);

    const lampGeo = new THREE.SphereGeometry(0.4, 16, 16);
    const lampMat = new THREE.MeshStandardMaterial({
      color: 0xfffacd,
      emissive: 0xfff8dc,
      emissiveIntensity: 0.3
    });
    const lamp = new THREE.Mesh(lampGeo, lampMat);
    lamp.position.y = 5.2;
    group.add(lamp);

    const light = new THREE.PointLight(0xfff8dc, 0.5, 15);
    light.position.y = 5.2;
    group.add(light);

    return group;
  }

  createFountain(): THREE.Group {
    const group = new THREE.Group();

    const baseGeo = new THREE.CylinderGeometry(4, 4.5, 0.8, 32);
    const stoneMat = new THREE.MeshStandardMaterial({ color: 0x888888, roughness: 0.8 });
    const base = new THREE.Mesh(baseGeo, stoneMat);
    base.position.y = 0.4;
    base.castShadow = true;
    base.receiveShadow = true;
    group.add(base);

    const waterGeo = new THREE.CylinderGeometry(3.5, 3.5, 0.3, 32);
    const waterMat = new THREE.MeshPhysicalMaterial({
      color: 0x1e90ff,
      transparent: true,
      opacity: 0.6,
      roughness: 0.1
    });
    const water = new THREE.Mesh(waterGeo, waterMat);
    water.position.y = 0.7;
    group.add(water);

    const columnGeo = new THREE.CylinderGeometry(0.4, 0.5, 3, 16);
    const column = new THREE.Mesh(columnGeo, stoneMat);
    column.position.y = 2;
    column.castShadow = true;
    group.add(column);

    const topGeo = new THREE.CylinderGeometry(1.2, 0.8, 0.4, 16);
    const top = new THREE.Mesh(topGeo, stoneMat);
    top.position.y = 3.5;
    top.castShadow = true;
    group.add(top);

    return group;
  }

  createTrashCan(): THREE.Group {
    const group = new THREE.Group();

    const canGeo = new THREE.CylinderGeometry(0.3, 0.35, 1, 16);
    const canMat = new THREE.MeshStandardMaterial({ color: 0x2e8b57, roughness: 0.6 });
    const can = new THREE.Mesh(canGeo, canMat);
    can.position.y = 0.5;
    can.castShadow = true;
    group.add(can);

    const lidGeo = new THREE.CylinderGeometry(0.35, 0.35, 0.1, 16);
    const lid = new THREE.Mesh(lidGeo, canMat);
    lid.position.y = 1.05;
    group.add(lid);

    return group;
  }

  createShop(type: 'food' | 'souvenirs' | 'icecream'): THREE.Group {
    const group = new THREE.Group();

    const colors: Record<string, { wall: number; roof: number; awning: number }> = {
      food: { wall: 0xfff8dc, roof: 0xcc0000, awning: 0xcc0000 },
      souvenirs: { wall: 0xe6e6fa, roof: 0x4169e1, awning: 0x4169e1 },
      icecream: { wall: 0xffe4e1, roof: 0xff69b4, awning: 0xff69b4 }
    };
    const color = colors[type];

    const wallMat = new THREE.MeshStandardMaterial({ color: color.wall, roughness: 0.9 });
    const roofMat = new THREE.MeshStandardMaterial({ color: color.roof, roughness: 0.8 });

    const base = new THREE.Mesh(new THREE.BoxGeometry(4, 3, 3), wallMat);
    base.position.y = 1.5;
    base.castShadow = true;
    base.receiveShadow = true;
    group.add(base);

    const roof = new THREE.Mesh(new THREE.ConeGeometry(3.2, 1.5, 4), roofMat);
    roof.position.y = 3.75;
    roof.rotation.y = Math.PI / 4;
    roof.castShadow = true;
    group.add(roof);

    const awning = new THREE.Mesh(new THREE.BoxGeometry(4.2, 0.15, 1.2), roofMat);
    awning.position.set(0, 2.5, 1.6);
    awning.rotation.x = -0.15;
    group.add(awning);

    const counter = new THREE.Mesh(
      new THREE.BoxGeometry(3.2, 0.8, 0.3),
      new THREE.MeshStandardMaterial({ color: 0x8b4513 })
    );
    counter.position.set(0, 1.2, 1.5);
    group.add(counter);

    const windowMat = new THREE.MeshStandardMaterial({ color: 0x87ceeb, transparent: true, opacity: 0.7 });
    const window = new THREE.Mesh(new THREE.PlaneGeometry(2.5, 1.2), windowMat);
    window.position.set(0, 2.2, 1.51);
    group.add(window);

    return group;
  }

  addDecorations(scene: THREE.Scene, paths?: THREE.Mesh[]): void {
    if (paths && paths.length > 0) {
      const margin = 1.4;
      paths.forEach(path => {
        if (path.geometry && (path.geometry as any).boundingBox === undefined) {
          path.geometry.computeBoundingBox?.();
        }
        const bb = path.geometry.boundingBox;
        if (!bb) return;

        // local corners in geometry space (y ignored)
        const corners = [
          new THREE.Vector3(bb.min.x, 0, bb.min.z),
          new THREE.Vector3(bb.max.x, 0, bb.min.z),
          new THREE.Vector3(bb.max.x, 0, bb.max.z),
          new THREE.Vector3(bb.min.x, 0, bb.max.z),
        ];

        // convert to world positions
        corners.forEach(c => path.localToWorld(c));

        // for each edge (corner i -> i+1) place a few decorations offset to the outside
        for (let i = 0; i < 4; i++) {
          const a = corners[i].clone();
          const b = corners[(i + 1) % 4].clone();
          const edge = new THREE.Vector3().subVectors(b, a);
          const length = edge.length();
          if (length < 0.01) continue;
          const dir = edge.normalize();

          // perpendicular to edge on XZ plane
          const perp = new THREE.Vector3(-dir.z, 0, dir.x);

          // sample along edge (avoid corners): 3 samples
          const samples = [0.25, 0.5, 0.75];
          samples.forEach(t => {
            const pos = new THREE.Vector3().lerpVectors(a, b, t);
            // push outward by margin so decoration is next to path not on it
            const out = pos.clone().add(perp.clone().multiplyScalar(margin));

            // choose decoration type by simple distribution
            const r = Math.random();
            if (r < 0.6) {
              const tree = this.createTree();
              tree.position.set(out.x, 0, out.z);
              scene.add(tree);
            } else if (r < 0.85) {
              const lamp = this.createStreetLamp();
              lamp.position.set(out.x, 0, out.z);
              scene.add(lamp);
            } else {
              const bench = this.createBench();
              bench.position.set(out.x, 0, out.z);
              bench.rotation.y = Math.atan2(dir.x, dir.z) + Math.PI / 2;
              scene.add(bench);
            }
          });
        }
      });

      return;
    }

    // Fallback: previous placements but slightly better distributed (corners + grid)
    const cornerPositions = [
      { x: -35, z: 35 },
      { x: 35, z: 35 },
      { x: -35, z: -35 },
      { x: 35, z: -35 },
      { x: -20, z: 25 },
      { x: 20, z: 25 },
      { x: -20, z: -25 },
      { x: 20, z: -25 }
    ];

    cornerPositions.forEach(o => {
      const tree = this.createTree();
      tree.position.set(o.x, 0, o.z);
      scene.add(tree);
    });

    const benchPositions = [
      { x: 15, z: 3, rot: Math.PI },
      { x: -15, z: 3, rot: Math.PI },
      { x: 3, z: 15, rot: Math.PI / 2 },
      { x: -3, z: 15, rot: -Math.PI / 2 }
    ];

    benchPositions.forEach(o => {
      const bench = this.createBench();
      bench.position.set(o.x, 0, o.z);
      bench.rotation.y = o.rot;
      scene.add(bench);
    });

    const lampPositions = [
      { x: 25, z: 3 },
      { x: -25, z: 3 },
      { x: 3, z: 25 },
      { x: -3, z: 25 }
    ];

    lampPositions.forEach(o => {
      const lamp = this.createStreetLamp();
      lamp.position.set(o.x, 0, o.z);
      scene.add(lamp);
    });
  }
}
