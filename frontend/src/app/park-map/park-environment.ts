import * as THREE from 'three';
import { secureRandom } from './secure-random';

/* ─── Procedural noise (simplex-adjacent value noise) ─── */
function hash2(ix: number, iy: number): number {
  let h = ix * 374761393 + iy * 668265263;
  h = (h ^ (h >> 13)) * 1274126177;
  return ((h ^ (h >> 16)) & 0x7fffffff) / 0x7fffffff;
}

function smoothNoise(x: number, z: number, freq: number): number {
  const sx = x * freq;
  const sz = z * freq;
  const ix = Math.floor(sx);
  const iz = Math.floor(sz);
  const fx = sx - ix;
  const fz = sz - iz;
  const ux = fx * fx * (3 - 2 * fx);
  const uz = fz * fz * (3 - 2 * fz);
  const a = hash2(ix, iz);
  const b = hash2(ix + 1, iz);
  const c = hash2(ix, iz + 1);
  const d = hash2(ix + 1, iz + 1);
  return (a * (1 - ux) + b * ux) * (1 - uz) + (c * (1 - ux) + d * ux) * uz;
}

function fbm(x: number, z: number, octaves = 4, freq = 0.08, amp = 1): number {
  let v = 0;
  let f = freq;
  let a = amp;
  for (let i = 0; i < octaves; i++) {
    v += smoothNoise(x, z, f) * a;
    f *= 2;
    a *= 0.5;
  }
  return v;
}

/* ─── Procedural grass canvas texture ─── */
function createGrassTexture(size = 512): THREE.CanvasTexture {
  const canvas = document.createElement('canvas');
  canvas.width = canvas.height = size;
  const ctx = canvas.getContext('2d')!;

  // base green
  ctx.fillStyle = '#4a7c4a';
  ctx.fillRect(0, 0, size, size);

  // grass variation patches
  for (let i = 0; i < 800; i++) {
    const x = secureRandom() * size;
    const y = secureRandom() * size;
    const r = 6 + secureRandom() * 18;
    const h = 90 + Math.floor(secureRandom() * 40);
    const s = 40 + Math.floor(secureRandom() * 25);
    const l = 28 + Math.floor(secureRandom() * 18);
    ctx.fillStyle = `hsla(${h}, ${s}%, ${l}%, 0.35)`;
    ctx.beginPath();
    ctx.arc(x, y, r, 0, Math.PI * 2);
    ctx.fill();
  }

  // tiny grass blades
  for (let i = 0; i < 2000; i++) {
    const x = secureRandom() * size;
    const y = secureRandom() * size;
    const h = 85 + Math.floor(secureRandom() * 50);
    const l = 24 + Math.floor(secureRandom() * 18);
    ctx.strokeStyle = `hsla(${h}, 45%, ${l}%, 0.5)`;
    ctx.lineWidth = 0.8;
    ctx.beginPath();
    ctx.moveTo(x, y);
    ctx.lineTo(x + (secureRandom() - 0.5) * 4, y - 3 - secureRandom() * 5);
    ctx.stroke();
  }

  // noise overlay
  const img = ctx.getImageData(0, 0, size, size);
  for (let i = 0; i < img.data.length; i += 4) {
    const v = (secureRandom() - 0.5) * 10;
    img.data[i] = Math.max(0, Math.min(255, img.data[i] + v));
    img.data[i + 1] = Math.max(0, Math.min(255, img.data[i + 1] + v));
    img.data[i + 2] = Math.max(0, Math.min(255, img.data[i + 2] + v));
  }
  ctx.putImageData(img, 0, 0);

  const tex = new THREE.CanvasTexture(canvas);
  tex.wrapS = tex.wrapT = THREE.RepeatWrapping;
  tex.repeat.set(8, 8);
  (tex as any).colorSpace = THREE.SRGBColorSpace;
  return tex;
}

/* ─── Procedural stone / plaza texture ─── */
function createPlazaTileTexture(size = 512): THREE.CanvasTexture {
  const canvas = document.createElement('canvas');
  canvas.width = canvas.height = size;
  const ctx = canvas.getContext('2d')!;

  ctx.fillStyle = '#c4b08a';
  ctx.fillRect(0, 0, size, size);

  // radial tile pattern
  const cx = size / 2;
  const cy = size / 2;
  for (let ring = 1; ring <= 6; ring++) {
    const r = ring * (size / 14);
    const segments = ring * 8;
    for (let s = 0; s < segments; s++) {
      const a1 = (s / segments) * Math.PI * 2;
      const a2 = ((s + 1) / segments) * Math.PI * 2;
      const lightness = 68 + Math.floor(secureRandom() * 12);
      const hue = 32 + Math.floor(secureRandom() * 10);
      ctx.fillStyle = `hsl(${hue}, 20%, ${lightness}%)`;
      ctx.beginPath();
      ctx.arc(cx, cy, r, a1, a2);
      ctx.arc(cx, cy, r - size / 14 + 2, a2, a1, true);
      ctx.closePath();
      ctx.fill();
      ctx.strokeStyle = 'rgba(80,60,30,0.25)';
      ctx.lineWidth = 1;
      ctx.stroke();
    }
  }

  const tex = new THREE.CanvasTexture(canvas);
  tex.wrapS = tex.wrapT = THREE.ClampToEdgeWrapping;
  (tex as any).colorSpace = THREE.SRGBColorSpace;
  return tex;
}

/* ─── Procedural stone wall texture ─── */
function createStoneWallTexture(size = 256): THREE.CanvasTexture {
  const canvas = document.createElement('canvas');
  canvas.width = canvas.height = size;
  const ctx = canvas.getContext('2d')!;

  ctx.fillStyle = '#8b7355';
  ctx.fillRect(0, 0, size, size);

  const rows = 8;
  const rowH = size / rows;
  for (let r = 0; r < rows; r++) {
    const cols = 3 + Math.floor(secureRandom() * 2);
    const offset = (r % 2) * (size / cols / 2);
    const colW = size / cols;
    for (let c = 0; c < cols + 1; c++) {
      const x = c * colW + offset + (secureRandom() - 0.5) * 3;
      const y = r * rowH + (secureRandom() - 0.5) * 2;
      const w = colW - 3;
      const h = rowH - 3;
      const l = 40 + Math.floor(secureRandom() * 15);
      ctx.fillStyle = `hsl(30, 18%, ${l}%)`;
      ctx.beginPath();
      ctx.roundRect(x + 1, y + 1, w, h, 2);
      ctx.fill();
      ctx.strokeStyle = 'rgba(40,30,15,0.4)';
      ctx.lineWidth = 1.5;
      ctx.stroke();
    }
  }

  const tex = new THREE.CanvasTexture(canvas);
  tex.wrapS = tex.wrapT = THREE.RepeatWrapping;
  tex.repeat.set(3, 1);
  (tex as any).colorSpace = THREE.SRGBColorSpace;
  return tex;
}

/* ─── Procedural bark texture ─── */
function createBarkTexture(size = 128): THREE.CanvasTexture {
  const canvas = document.createElement('canvas');
  canvas.width = canvas.height = size;
  const ctx = canvas.getContext('2d')!;

  ctx.fillStyle = '#5a3a20';
  ctx.fillRect(0, 0, size, size);

  for (let i = 0; i < 40; i++) {
    const y = secureRandom() * size;
    const x = secureRandom() * size * 0.3;
    const w = size * 0.4 + secureRandom() * size * 0.6;
    const h = 1 + secureRandom() * 3;
    const l = 20 + Math.floor(secureRandom() * 16);
    ctx.fillStyle = `hsl(25, 30%, ${l}%)`;
    ctx.fillRect(x, y, w, h);
  }

  const img = ctx.getImageData(0, 0, size, size);
  for (let i = 0; i < img.data.length; i += 4) {
    const v = (secureRandom() - 0.5) * 16;
    img.data[i] = Math.max(0, Math.min(255, img.data[i] + v));
    img.data[i + 1] = Math.max(0, Math.min(255, img.data[i + 1] + v));
    img.data[i + 2] = Math.max(0, Math.min(255, img.data[i + 2] + v));
  }
  ctx.putImageData(img, 0, 0);

  const tex = new THREE.CanvasTexture(canvas);
  tex.wrapS = tex.wrapT = THREE.RepeatWrapping;
  (tex as any).colorSpace = THREE.SRGBColorSpace;
  return tex;
}

export class ParkEnvironment {

  createGround(size: number): THREE.Group {
    const group = new THREE.Group();

    const segments = 128;
    const geometry = new THREE.PlaneGeometry(size * 2, size * 2, segments, segments);
    geometry.rotateX(-Math.PI / 2);

    // vertex displacement & colour
    const pos = geometry.getAttribute('position');
    const colors = new Float32Array(pos.count * 3);
    for (let i = 0; i < pos.count; i++) {
      const x = pos.getX(i);
      const z = pos.getZ(i);
      const height = fbm(x, z, 3, 0.015, 0.6) - 0.3;
      pos.setY(i, height);

      // colour based on height + noise
      const n = fbm(x + 200, z + 200, 2, 0.04, 1);
      const g = 0.30 + n * 0.12;
      const r = 0.18 + n * 0.08;
      const b = 0.12 + n * 0.04;
      colors[i * 3] = r;
      colors[i * 3 + 1] = g;
      colors[i * 3 + 2] = b;
    }
    geometry.setAttribute('color', new THREE.BufferAttribute(colors, 3));
    geometry.computeVertexNormals();

    const material = new THREE.MeshStandardMaterial({
      map: createGrassTexture(),
      vertexColors: true,
      roughness: 0.92,
      metalness: 0.0,
    });

    const ground = new THREE.Mesh(geometry, material);
    ground.receiveShadow = true;
    group.add(ground);

    // scatter grass tufts
    const grassGeo = new THREE.ConeGeometry(0.12, 0.5, 4);
    const grassMat = new THREE.MeshStandardMaterial({ color: 0x3a7c3a, roughness: 1 });
    const grassInstanced = new THREE.InstancedMesh(grassGeo, grassMat, 600);
    const dummy = new THREE.Object3D();
    for (let i = 0; i < 600; i++) {
      const gx = (secureRandom() - 0.5) * size * 1.6;
      const gz = (secureRandom() - 0.5) * size * 1.6;
      const gy = fbm(gx, gz, 3, 0.015, 0.6) - 0.3;
      dummy.position.set(gx, gy, gz);
      dummy.rotation.y = secureRandom() * Math.PI;
      dummy.scale.setScalar(0.6 + secureRandom() * 0.8);
      dummy.updateMatrix();
      grassInstanced.setMatrixAt(i, dummy.matrix);
    }
    grassInstanced.instanceMatrix.needsUpdate = true;
    grassInstanced.receiveShadow = true;
    group.add(grassInstanced);

    return group;
  }

  createMainPath(length: number, width: number): THREE.Mesh {
    const thickness = 0.10;
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
    const geometry = new THREE.CylinderGeometry(radius, radius, 0.12, 48);
    const material = new THREE.MeshStandardMaterial({
      map: createPlazaTileTexture(),
      roughness: 0.85,
    });
    const plaza = new THREE.Mesh(geometry, material);
    plaza.receiveShadow = true;
    plaza.position.y = 0.06;
    return plaza;
  }

  createLake(radiusX: number, radiusZ: number): THREE.Group {
    const group = new THREE.Group();

    // sandy shore
    const shoreGeo = new THREE.CircleGeometry(radiusX + 2, 48);
    const shoreMat = new THREE.MeshStandardMaterial({
      color: 0xd4c090,
      roughness: 0.95,
    });
    const shore = new THREE.Mesh(shoreGeo, shoreMat);
    shore.rotation.x = -Math.PI / 2;
    shore.position.y = 0.02;
    shore.receiveShadow = true;
    group.add(shore);

    // recessed basin
    const basinGeo = new THREE.CylinderGeometry(radiusX, radiusX - 0.5, 0.8, 48);
    const basinMat = new THREE.MeshStandardMaterial({ color: 0x2a4a3a, roughness: 1 });
    const basin = new THREE.Mesh(basinGeo, basinMat);
    basin.position.y = -0.3;
    group.add(basin);

    // animated water surface
    const waterGeo = new THREE.CircleGeometry(radiusX - 0.3, 64);
    const waterMat = new THREE.MeshPhysicalMaterial({
      color: 0x1e6e9e,
      metalness: 0.15,
      roughness: 0.04,
      clearcoat: 0.6,
      clearcoatRoughness: 0.1,
      transparent: true,
      opacity: 0.88,
      envMapIntensity: 1.5,
    });
    const water = new THREE.Mesh(waterGeo, waterMat);
    water.rotation.x = -Math.PI / 2;
    water.position.y = 0.06;
    water.name = 'lakeWater';
    group.add(water);

    // border rocks
    const rockMat = new THREE.MeshStandardMaterial({ color: 0x6b6b60, roughness: 0.95 });
    for (let i = 0; i < 20; i++) {
      const angle = (i / 20) * Math.PI * 2;
      const rx = Math.cos(angle) * (radiusX + 0.5) + (secureRandom() - 0.5) * 1.2;
      const rz = Math.sin(angle) * (radiusX + 0.5) + (secureRandom() - 0.5) * 1.2;
      const rockSize = 0.4 + secureRandom() * 0.6;
      const rock = new THREE.Mesh(
        new THREE.DodecahedronGeometry(rockSize, 0),
        rockMat
      );
      rock.position.set(rx, 0.1, rz);
      rock.rotation.set(secureRandom(), secureRandom(), secureRandom());
      rock.scale.y = 0.5 + secureRandom() * 0.3;
      rock.castShadow = true;
      group.add(rock);
    }

    // reed plants
    const reedMat = new THREE.MeshStandardMaterial({ color: 0x3a6830, roughness: 1 });
    for (let i = 0; i < 12; i++) {
      const angle = (i / 12) * Math.PI * 2 + secureRandom() * 0.4;
      const rx = Math.cos(angle) * (radiusX - 0.5);
      const rz = Math.sin(angle) * (radiusX - 0.5);
      const reedH = 1.0 + secureRandom() * 0.8;
      const reed = new THREE.Mesh(
        new THREE.CylinderGeometry(0.03, 0.05, reedH, 4),
        reedMat
      );
      reed.position.set(rx, reedH / 2, rz);
      reed.rotation.z = (secureRandom() - 0.5) * 0.15;
      group.add(reed);
    }

    return group;
  }

  createEntrance(): THREE.Group {
    const group = new THREE.Group();
    const stoneMat = new THREE.MeshStandardMaterial({
      map: createStoneWallTexture(),
      roughness: 0.85,
    });
    const goldMat = new THREE.MeshStandardMaterial({
      color: 0xffd700,
      metalness: 0.7,
      roughness: 0.25,
    });
    const darkWood = new THREE.MeshStandardMaterial({ color: 0x3a2515, roughness: 0.9 });
    const redMat = new THREE.MeshStandardMaterial({ color: 0x6b0000, roughness: 0.7 });

    // pillars
    const pillarGeo = new THREE.CylinderGeometry(1.2, 1.5, 10, 8);
    const leftPillar = new THREE.Mesh(pillarGeo, stoneMat);
    leftPillar.position.set(-7, 5, 0);
    leftPillar.castShadow = true;
    group.add(leftPillar);

    const rightPillar = new THREE.Mesh(pillarGeo, stoneMat);
    rightPillar.position.set(7, 5, 0);
    rightPillar.castShadow = true;
    group.add(rightPillar);

    // tower tops
    const towerTopGeo = new THREE.ConeGeometry(1.8, 3, 8);
    const leftTop = new THREE.Mesh(towerTopGeo, redMat);
    leftTop.position.set(-7, 11.5, 0);
    leftTop.castShadow = true;
    group.add(leftTop);

    const rightTop = new THREE.Mesh(towerTopGeo, redMat);
    rightTop.position.set(7, 11.5, 0);
    rightTop.castShadow = true;
    group.add(rightTop);

    // gold rings on pillars
    for (const xp of [-7, 7]) {
      for (let h = 3; h <= 9; h += 3) {
        const ring = new THREE.Mesh(
          new THREE.TorusGeometry(1.3, 0.1, 8, 16),
          goldMat
        );
        ring.position.set(xp, h, 0);
        ring.rotation.x = Math.PI / 2;
        group.add(ring);
      }
    }

    // arch beam
    const archGeo = new THREE.BoxGeometry(16, 2.5, 2);
    const arch = new THREE.Mesh(archGeo, stoneMat);
    arch.position.set(0, 10, 0);
    arch.castShadow = true;
    group.add(arch);

    // banner / sign
    const signGeo = new THREE.BoxGeometry(10, 2, 0.3);
    const sign = new THREE.Mesh(signGeo, redMat);
    sign.position.set(0, 10, 1.2);
    group.add(sign);

    // sign gold border
    const borderGeo = new THREE.BoxGeometry(10.4, 2.4, 0.2);
    const border = new THREE.Mesh(borderGeo, goldMat);
    border.position.set(0, 10, 1.05);
    group.add(border);

    // stars on sign
    const starGeo = new THREE.OctahedronGeometry(0.5);
    for (let i = -3; i <= 3; i += 2) {
      const star = new THREE.Mesh(starGeo, goldMat);
      star.position.set(i, 10, 1.4);
      group.add(star);
    }

    // gate bars
    const gateMat = new THREE.MeshStandardMaterial({ color: 0x1a1a1a, metalness: 0.8, roughness: 0.3 });
    for (let i = -3; i <= 3; i++) {
      const bar = new THREE.Mesh(new THREE.CylinderGeometry(0.06, 0.06, 8), gateMat);
      bar.position.set(i * 1.2, 4, 0);
      group.add(bar);
    }

    // lanterns at entrance
    const lanternGeo = new THREE.BoxGeometry(0.5, 0.7, 0.5);
    const lanternMat = new THREE.MeshStandardMaterial({
      color: 0xffb347,
      emissive: 0xff8c00,
      emissiveIntensity: 0.8,
    });
    for (const xp of [-5, 5]) {
      const lantern = new THREE.Mesh(lanternGeo, lanternMat);
      lantern.position.set(xp, 8, 1.2);
      group.add(lantern);

      const light = new THREE.PointLight(0xff9933, 0.6, 12);
      light.position.set(xp, 8, 1.5);
      group.add(light);
    }

    // flags on tops
    const flagMat = new THREE.MeshStandardMaterial({ color: 0xcc0000, side: THREE.DoubleSide });
    for (const xp of [-7, 7]) {
      const flagPole = new THREE.Mesh(new THREE.CylinderGeometry(0.04, 0.04, 2.5), darkWood);
      flagPole.position.set(xp, 13.5, 0);
      group.add(flagPole);

      const flag = new THREE.Mesh(new THREE.PlaneGeometry(1.2, 0.8), flagMat);
      flag.position.set(xp + 0.6, 14.2, 0);
      group.add(flag);
    }

    return group;
  }

  /* ─── Trees with species variety ─── */
  createTree(species: 'round' | 'cone' | 'palm' | 'oak' = 'round'): THREE.Group {
    const group = new THREE.Group();
    const barkTex = createBarkTexture();
    const trunkMat = new THREE.MeshStandardMaterial({ map: barkTex, roughness: 0.95 });

    switch (species) {
      case 'cone': {
        const trunkH = 2.5;
        const trunk = new THREE.Mesh(new THREE.CylinderGeometry(0.25, 0.4, trunkH, 8), trunkMat);
        trunk.position.y = trunkH / 2;
        trunk.castShadow = true;
        group.add(trunk);

        const leafMat = new THREE.MeshStandardMaterial({ color: 0x1a6b30, roughness: 0.85 });
        for (let i = 0; i < 4; i++) {
          const scale = 1 - i * 0.2;
          const cone = new THREE.Mesh(
            new THREE.ConeGeometry(1.8 * scale, 2.0, 8),
            leafMat
          );
          cone.position.y = trunkH + i * 1.2;
          cone.castShadow = true;
          group.add(cone);
        }
        break;
      }

      case 'palm': {
        const trunkH = 5;
        const trunk = new THREE.Mesh(new THREE.CylinderGeometry(0.2, 0.35, trunkH, 8), trunkMat);
        trunk.position.y = trunkH / 2;
        trunk.rotation.z = 0.08;
        trunk.castShadow = true;
        group.add(trunk);

        const leafMat = new THREE.MeshStandardMaterial({ color: 0x2d8b3a, roughness: 0.85, side: THREE.DoubleSide });
        for (let i = 0; i < 7; i++) {
          const angle = (i / 7) * Math.PI * 2;
          const leaf = new THREE.Mesh(new THREE.PlaneGeometry(0.8, 3.5), leafMat);
          leaf.position.set(Math.cos(angle) * 1.2, trunkH + 0.5, Math.sin(angle) * 1.2);
          leaf.rotation.x = -0.8;
          leaf.rotation.y = angle;
          leaf.castShadow = true;
          group.add(leaf);
        }

        // coconuts
        const cocoMat = new THREE.MeshStandardMaterial({ color: 0x6b4226, roughness: 0.8 });
        for (let i = 0; i < 3; i++) {
          const coco = new THREE.Mesh(new THREE.SphereGeometry(0.18, 8, 8), cocoMat);
          const ca = (i / 3) * Math.PI * 2;
          coco.position.set(Math.cos(ca) * 0.3, trunkH - 0.3, Math.sin(ca) * 0.3);
          group.add(coco);
        }
        break;
      }

      case 'oak': {
        const trunkH = 3;
        const trunk = new THREE.Mesh(new THREE.CylinderGeometry(0.35, 0.55, trunkH, 8), trunkMat);
        trunk.position.y = trunkH / 2;
        trunk.castShadow = true;
        group.add(trunk);

        // branches
        for (let b = 0; b < 3; b++) {
          const bAngle = (b / 3) * Math.PI * 2 + secureRandom() * 0.5;
          const branch = new THREE.Mesh(
            new THREE.CylinderGeometry(0.08, 0.12, 2, 6),
            trunkMat
          );
          branch.position.set(
            Math.cos(bAngle) * 0.6,
            trunkH * 0.7 + b * 0.3,
            Math.sin(bAngle) * 0.6
          );
          branch.rotation.z = Math.cos(bAngle) * 0.7;
          branch.rotation.x = Math.sin(bAngle) * 0.4;
          branch.castShadow = true;
          group.add(branch);
        }

        // wide round canopy
        const leafMat = new THREE.MeshStandardMaterial({ color: 0x2a7e30, roughness: 0.85 });
        const canopyPositions = [
          { x: 0, y: trunkH + 1.5, z: 0, r: 2.5 },
          { x: 1.2, y: trunkH + 1.0, z: 0.8, r: 1.8 },
          { x: -1.0, y: trunkH + 1.2, z: -0.6, r: 1.6 },
          { x: 0.4, y: trunkH + 2.5, z: -0.3, r: 1.5 },
          { x: -0.5, y: trunkH + 0.8, z: 1.0, r: 1.4 },
        ];
        canopyPositions.forEach(p => {
          const leaf = new THREE.Mesh(new THREE.SphereGeometry(p.r, 10, 8), leafMat);
          leaf.position.set(p.x, p.y, p.z);
          leaf.castShadow = true;
          group.add(leaf);
        });
        break;
      }

      default: {
        // round tree (default)
        const trunkH = 2.8;
        const trunk = new THREE.Mesh(new THREE.CylinderGeometry(0.3, 0.5, trunkH, 8), trunkMat);
        trunk.position.y = trunkH / 2;
        trunk.castShadow = true;
        group.add(trunk);

        const greens = [0x228b22, 0x2e8b40, 0x1e7b2e, 0x34a048];
        const positions = [
          { x: 0, y: trunkH + 1.2, z: 0, r: 2.0 },
          { x: 0.8, y: trunkH + 0.8, z: 0.5, r: 1.4 },
          { x: -0.6, y: trunkH + 1.0, z: -0.4, r: 1.5 },
          { x: 0.2, y: trunkH + 2.0, z: 0, r: 1.2 },
        ];
        positions.forEach((p, idx) => {
          const leafMat = new THREE.MeshStandardMaterial({
            color: greens[idx % greens.length],
            roughness: 0.85,
          });
          const leaf = new THREE.Mesh(new THREE.SphereGeometry(p.r, 10, 8), leafMat);
          leaf.position.set(p.x, p.y, p.z);
          leaf.castShadow = true;
          group.add(leaf);
        });
        break;
      }
    }

    return group;
  }

  createBench(): THREE.Group {
    const group = new THREE.Group();
    const woodMat = new THREE.MeshStandardMaterial({ color: 0x7a3e1a, roughness: 0.85 });
    const metalMat = new THREE.MeshStandardMaterial({ color: 0x2a2a2a, metalness: 0.7, roughness: 0.4 });

    // seat planks
    for (let i = 0; i < 3; i++) {
      const plank = new THREE.Mesh(new THREE.BoxGeometry(2, 0.08, 0.18), woodMat);
      plank.position.set(0, 0.5, -0.15 + i * 0.2);
      plank.castShadow = true;
      group.add(plank);
    }

    // backrest planks
    for (let i = 0; i < 2; i++) {
      const plank = new THREE.Mesh(new THREE.BoxGeometry(2, 0.08, 0.18), woodMat);
      plank.position.set(0, 0.7 + i * 0.22, -0.3);
      plank.rotation.x = 0.1;
      plank.castShadow = true;
      group.add(plank);
    }

    // armrests
    for (const x of [-0.85, 0.85]) {
      const arm = new THREE.Mesh(new THREE.BoxGeometry(0.08, 0.06, 0.5), woodMat);
      arm.position.set(x, 0.65, -0.05);
      group.add(arm);
    }

    // metal legs
    const legGeo = new THREE.BoxGeometry(0.08, 0.5, 0.5);
    for (const x of [-0.75, 0.75]) {
      const leg = new THREE.Mesh(legGeo, metalMat);
      leg.position.set(x, 0.25, -0.05);
      group.add(leg);
    }

    return group;
  }

  createStreetLamp(): THREE.Group {
    const group = new THREE.Group();
    const poleMat = new THREE.MeshStandardMaterial({ color: 0x1a1a1a, metalness: 0.6, roughness: 0.5 });

    // decorative base
    const base = new THREE.Mesh(new THREE.CylinderGeometry(0.35, 0.45, 0.4, 8), poleMat);
    base.position.y = 0.2;
    group.add(base);

    // main pole
    const pole = new THREE.Mesh(new THREE.CylinderGeometry(0.08, 0.12, 5, 8), poleMat);
    pole.position.y = 2.7;
    pole.castShadow = true;
    group.add(pole);

    // decorative ring
    const ring = new THREE.Mesh(new THREE.TorusGeometry(0.15, 0.03, 6, 12), poleMat);
    ring.position.y = 4.2;
    ring.rotation.x = Math.PI / 2;
    group.add(ring);

    // lamp arm
    const arm = new THREE.Mesh(new THREE.BoxGeometry(0.8, 0.06, 0.06), poleMat);
    arm.position.set(0.35, 5.1, 0);
    group.add(arm);

    // glass housing
    const glassMat = new THREE.MeshStandardMaterial({
      color: 0xfff0c0,
      emissive: 0xffcc44,
      emissiveIntensity: 0.6,
      transparent: true,
      opacity: 0.85,
    });
    const lampGeo = new THREE.SphereGeometry(0.3, 12, 12);
    const lamp = new THREE.Mesh(lampGeo, glassMat);
    lamp.position.set(0.7, 5.0, 0);
    group.add(lamp);

    // warm cone light
    const light = new THREE.PointLight(0xffd27f, 0.5, 15);
    light.position.set(0.7, 4.8, 0);
    group.add(light);

    return group;
  }

  createFountain(): THREE.Group {
    const group = new THREE.Group();
    const stoneMat = new THREE.MeshStandardMaterial({ color: 0x808080, roughness: 0.85 });

    // outer basin
    const outerBasin = new THREE.Mesh(new THREE.CylinderGeometry(4, 4.5, 1.0, 32), stoneMat);
    outerBasin.position.y = 0.5;
    outerBasin.castShadow = true;
    outerBasin.receiveShadow = true;
    group.add(outerBasin);

    // inner basin lip
    const lip = new THREE.Mesh(new THREE.TorusGeometry(3.8, 0.25, 8, 32), stoneMat);
    lip.position.y = 1.0;
    lip.rotation.x = Math.PI / 2;
    group.add(lip);

    // water
    const waterMat = new THREE.MeshPhysicalMaterial({
      color: 0x1e6e9e,
      metalness: 0.1,
      roughness: 0.05,
      clearcoat: 0.5,
      transparent: true,
      opacity: 0.75,
    });
    const water = new THREE.Mesh(new THREE.CylinderGeometry(3.5, 3.5, 0.3, 32), waterMat);
    water.position.y = 0.85;
    water.name = 'fountainWater';
    group.add(water);

    // central column
    const column = new THREE.Mesh(new THREE.CylinderGeometry(0.35, 0.45, 3.5, 12), stoneMat);
    column.position.y = 2.25;
    column.castShadow = true;
    group.add(column);

    // top bowl
    const bowl = new THREE.Mesh(new THREE.CylinderGeometry(1.2, 0.8, 0.5, 16), stoneMat);
    bowl.position.y = 4.0;
    bowl.castShadow = true;
    group.add(bowl);

    // top water
    const topWater = new THREE.Mesh(new THREE.CylinderGeometry(1.0, 1.0, 0.15, 16), waterMat);
    topWater.position.y = 4.2;
    topWater.name = 'fountainWater';
    group.add(topWater);

    // decorative finial
    const finial = new THREE.Mesh(new THREE.SphereGeometry(0.35, 12, 12), stoneMat);
    finial.position.y = 4.6;
    group.add(finial);

    // ornamental detail at base
    for (let i = 0; i < 8; i++) {
      const angle = (i / 8) * Math.PI * 2;
      const orn = new THREE.Mesh(new THREE.BoxGeometry(0.3, 0.8, 0.15), stoneMat);
      orn.position.set(Math.cos(angle) * 4.2, 0.6, Math.sin(angle) * 4.2);
      orn.rotation.y = angle;
      group.add(orn);
    }

    return group;
  }

  createTrashCan(): THREE.Group {
    const group = new THREE.Group();
    const canMat = new THREE.MeshStandardMaterial({ color: 0x2e7d32, roughness: 0.7, metalness: 0.2 });

    const can = new THREE.Mesh(new THREE.CylinderGeometry(0.28, 0.32, 1, 16), canMat);
    can.position.y = 0.5;
    can.castShadow = true;
    group.add(can);

    // lid
    const lid = new THREE.Mesh(new THREE.CylinderGeometry(0.32, 0.32, 0.08, 16), canMat);
    lid.position.y = 1.04;
    group.add(lid);

    // handle
    const handle = new THREE.Mesh(new THREE.TorusGeometry(0.12, 0.02, 6, 8, Math.PI), canMat);
    handle.position.y = 1.12;
    handle.rotation.x = Math.PI;
    group.add(handle);

    return group;
  }

  createShop(type: 'food' | 'souvenirs' | 'icecream'): THREE.Group {
    const group = new THREE.Group();

    const colors: Record<string, { wall: number; roof: number; awning: number }> = {
      food: { wall: 0xfff5d6, roof: 0xb30000, awning: 0xcc0000 },
      souvenirs: { wall: 0xe8e0f0, roof: 0x2850a0, awning: 0x4169e1 },
      icecream: { wall: 0xffe8e8, roof: 0xe04090, awning: 0xff69b4 }
    };
    const color = colors[type];

    const wallMat = new THREE.MeshStandardMaterial({ color: color.wall, roughness: 0.9 });
    const roofMat = new THREE.MeshStandardMaterial({ color: color.roof, roughness: 0.8 });
    const awningMat = new THREE.MeshStandardMaterial({ color: color.awning, roughness: 0.7 });

    // building base
    const base = new THREE.Mesh(new THREE.BoxGeometry(4, 3.2, 3), wallMat);
    base.position.y = 1.6;
    base.castShadow = true;
    base.receiveShadow = true;
    group.add(base);

    // pitched roof
    const roof = new THREE.Mesh(new THREE.ConeGeometry(3.2, 1.8, 4), roofMat);
    roof.position.y = 4.1;
    roof.rotation.y = Math.PI / 4;
    roof.castShadow = true;
    group.add(roof);

    // awning with stripes
    const awning = new THREE.Mesh(new THREE.BoxGeometry(4.4, 0.12, 1.4), awningMat);
    awning.position.set(0, 2.7, 1.7);
    awning.rotation.x = -0.15;
    group.add(awning);

    // stripe effect on awning
    const stripeMat = new THREE.MeshStandardMaterial({ color: 0xffffff, roughness: 0.8 });
    for (let i = 0; i < 4; i++) {
      const stripe = new THREE.Mesh(new THREE.BoxGeometry(0.4, 0.13, 1.4), stripeMat);
      stripe.position.set(-1.5 + i * 1.0, 2.71, 1.7);
      stripe.rotation.x = -0.15;
      group.add(stripe);
    }

    // counter
    const counter = new THREE.Mesh(
      new THREE.BoxGeometry(3.2, 0.8, 0.3),
      new THREE.MeshStandardMaterial({ color: 0x7a3e1a, roughness: 0.85 })
    );
    counter.position.set(0, 1.2, 1.5);
    group.add(counter);

    // window
    const windowMat = new THREE.MeshStandardMaterial({
      color: 0x87ceeb,
      transparent: true,
      opacity: 0.65,
      metalness: 0.3,
    });
    const win = new THREE.Mesh(new THREE.PlaneGeometry(2.5, 1.2), windowMat);
    win.position.set(0, 2.2, 1.51);
    group.add(win);

    // small sign light
    const signLight = new THREE.Mesh(
      new THREE.BoxGeometry(0.3, 0.3, 0.2),
      new THREE.MeshStandardMaterial({
        color: 0xffee88,
        emissive: 0xffcc44,
        emissiveIntensity: 0.4,
      })
    );
    signLight.position.set(0, 3.0, 1.55);
    group.add(signLight);

    return group;
  }

  /* ─── Flower bed ─── */
  createFlowerBed(width = 2, depth = 1): THREE.Group {
    const bed = new THREE.Group();
    const flowerColors = [0xff6b6b, 0xffd93d, 0xff8c00, 0xda70d6, 0xee4488, 0x87ceeb];

    const soil = new THREE.Mesh(
      new THREE.BoxGeometry(width, 0.15, depth),
      new THREE.MeshStandardMaterial({ color: 0x3f2a18, roughness: 1 })
    );
    soil.position.y = 0.075;
    soil.receiveShadow = true;
    bed.add(soil);

    // border stones
    const borderMat = new THREE.MeshStandardMaterial({ color: 0x8b8070, roughness: 0.9 });
    for (const z of [-depth / 2, depth / 2]) {
      const border = new THREE.Mesh(new THREE.BoxGeometry(width + 0.1, 0.2, 0.1), borderMat);
      border.position.set(0, 0.1, z);
      bed.add(border);
    }

    for (let i = 0; i < 14; i++) {
      const x = (secureRandom() - 0.5) * (width - 0.2);
      const z = (secureRandom() - 0.5) * (depth - 0.2);
      const color = flowerColors[Math.floor(secureRandom() * flowerColors.length)];

      const stem = new THREE.Mesh(
        new THREE.CylinderGeometry(0.02, 0.02, 0.3),
        new THREE.MeshStandardMaterial({ color: 0x228b22 })
      );
      stem.position.set(x, 0.3, z);
      bed.add(stem);

      const flower = new THREE.Mesh(
        new THREE.SphereGeometry(0.08, 8, 8),
        new THREE.MeshStandardMaterial({ color })
      );
      flower.position.set(x, 0.46, z);
      bed.add(flower);
    }

    return bed;
  }

  /* ─── Hedgerow ─── */
  createHedge(length = 3, height = 1.2): THREE.Mesh {
    const geo = new THREE.BoxGeometry(length, height, 0.8);
    const mat = new THREE.MeshStandardMaterial({ color: 0x1e6b28, roughness: 0.9 });
    const hedge = new THREE.Mesh(geo, mat);
    hedge.position.y = height / 2;
    hedge.castShadow = true;
    hedge.receiveShadow = true;
    return hedge;
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

        const corners = [
          new THREE.Vector3(bb.min.x, 0, bb.min.z),
          new THREE.Vector3(bb.max.x, 0, bb.min.z),
          new THREE.Vector3(bb.max.x, 0, bb.max.z),
          new THREE.Vector3(bb.min.x, 0, bb.max.z),
        ];
        corners.forEach(c => path.localToWorld(c));

        for (let i = 0; i < 4; i++) {
          const a = corners[i].clone();
          const b = corners[(i + 1) % 4].clone();
          const edge = new THREE.Vector3().subVectors(b, a);
          const length = edge.length();
          if (length < 0.01) continue;
          const dir = edge.normalize();
          const perp = new THREE.Vector3(-dir.z, 0, dir.x);

          const samples = [0.25, 0.5, 0.75];
          samples.forEach(t => {
            const pos = new THREE.Vector3().lerpVectors(a, b, t);
            const out = pos.clone().add(perp.clone().multiplyScalar(margin));

            const r = secureRandom();
            if (r < 0.5) {
              const species = (['round', 'cone', 'oak'] as const)[Math.floor(secureRandom() * 3)];
              const tree = this.createTree(species);
              tree.position.set(out.x, 0, out.z);
              scene.add(tree);
            } else if (r < 0.75) {
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

    // Fallback decorations
    const cornerPositions = [
      { x: -35, z: 35 }, { x: 35, z: 35 },
      { x: -35, z: -35 }, { x: 35, z: -35 },
      { x: -20, z: 25 }, { x: 20, z: 25 },
      { x: -20, z: -25 }, { x: 20, z: -25 }
    ];

    const species: ('round' | 'cone' | 'oak')[] = ['round', 'cone', 'oak'];
    cornerPositions.forEach((o, i) => {
      const tree = this.createTree(species[i % species.length]);
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
      { x: 25, z: 3 }, { x: -25, z: 3 },
      { x: 3, z: 25 }, { x: -3, z: 25 }
    ];

    lampPositions.forEach(o => {
      const lamp = this.createStreetLamp();
      lamp.position.set(o.x, 0, o.z);
      scene.add(lamp);
    });
  }
}
