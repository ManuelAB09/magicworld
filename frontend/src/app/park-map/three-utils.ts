import * as THREE from 'three';
import { secureRandom } from './secure-random';

export function isWebGLAvailable(): boolean {
  try {
    const canvas = document.createElement('canvas');
    const gl = canvas.getContext('webgl') || canvas.getContext('experimental-webgl');
    return gl !== null;
  } catch (e) {
    return false;
  }
}

/* ─── procedural cobblestone canvas texture ─── */
export function createCobblestoneTexture(size = 512): THREE.CanvasTexture {
  const canvas = document.createElement('canvas');
  canvas.width = canvas.height = size;
  const ctx = canvas.getContext('2d')!;

  // base sand colour
  ctx.fillStyle = '#bfaa82';
  ctx.fillRect(0, 0, size, size);

  const stones: { x: number; y: number; w: number; h: number; c: string }[] = [];
  const cols = 8;
  const rows = 8;
  const sw = size / cols;
  const sh = size / rows;

  for (let r = 0; r < rows; r++) {
    for (let c = 0; c < cols; c++) {
      const offsetX = (r % 2 === 0 ? 0 : sw * 0.5);
      const x = c * sw + offsetX + (secureRandom() - 0.5) * 4;
      const y = r * sh + (secureRandom() - 0.5) * 4;
      const w = sw - 6 + (secureRandom() - 0.5) * 6;
      const h = sh - 6 + (secureRandom() - 0.5) * 6;
      const lightness = 62 + Math.floor(secureRandom() * 14);
      const hue = 30 + Math.floor(secureRandom() * 12);
      stones.push({ x, y, w, h, c: `hsl(${hue}, 18%, ${lightness}%)` });
    }
  }

  stones.forEach(s => {
    ctx.fillStyle = s.c;
    ctx.beginPath();
    const rad = 4;
    ctx.roundRect(s.x, s.y, s.w, s.h, rad);
    ctx.fill();
    ctx.strokeStyle = 'rgba(80,60,30,0.35)';
    ctx.lineWidth = 1.5;
    ctx.stroke();
  });

  // noise grain
  const img = ctx.getImageData(0, 0, size, size);
  for (let i = 0; i < img.data.length; i += 4) {
    const v = (secureRandom() - 0.5) * 14;
    img.data[i] = Math.max(0, Math.min(255, img.data[i] + v));
    img.data[i + 1] = Math.max(0, Math.min(255, img.data[i + 1] + v));
    img.data[i + 2] = Math.max(0, Math.min(255, img.data[i + 2] + v));
  }
  ctx.putImageData(img, 0, 0);

  const tex = new THREE.CanvasTexture(canvas);
  tex.wrapS = tex.wrapT = THREE.RepeatWrapping;
  tex.repeat.set(2, 2);
  (tex as any).colorSpace = THREE.SRGBColorSpace;
  return tex;
}

function makePathMaterial(): THREE.MeshStandardMaterial {
  return new THREE.MeshStandardMaterial({
    map: createCobblestoneTexture(),
    roughness: 0.92,
    metalness: 0.0,
  });
}

function makeBorderMaterial(): THREE.MeshStandardMaterial {
  return new THREE.MeshStandardMaterial({
    color: 0x6b5a42,
    roughness: 0.95,
  });
}

export function createPath(
  x1: number, z1: number,
  x2: number, z2: number,
  width: number,
  isVertical: boolean
): THREE.Group {
  const group = new THREE.Group();
  const length = isVertical ? Math.abs(z2 - z1) : Math.abs(x2 - x1);
  const thickness = 0.12;

  // main path surface
  const geometry = isVertical
    ? new THREE.BoxGeometry(width, thickness, length)
    : new THREE.BoxGeometry(length, thickness, width);
  const path = new THREE.Mesh(geometry, makePathMaterial());
  path.position.set((x1 + x2) / 2, thickness / 2 + 0.01, (z1 + z2) / 2);
  path.receiveShadow = true;
  group.add(path);

  // border stones
  const borderH = 0.18;
  const borderW = 0.35;
  const bMat = makeBorderMaterial();

  if (isVertical) {
    const leftBorder = new THREE.Mesh(new THREE.BoxGeometry(borderW, borderH, length), bMat);
    leftBorder.position.set((x1 + x2) / 2 - width / 2 - borderW / 2, borderH / 2 + 0.01, (z1 + z2) / 2);
    leftBorder.receiveShadow = true;
    group.add(leftBorder);

    const rightBorder = new THREE.Mesh(new THREE.BoxGeometry(borderW, borderH, length), bMat);
    rightBorder.position.set((x1 + x2) / 2 + width / 2 + borderW / 2, borderH / 2 + 0.01, (z1 + z2) / 2);
    rightBorder.receiveShadow = true;
    group.add(rightBorder);
  } else {
    const topBorder = new THREE.Mesh(new THREE.BoxGeometry(length, borderH, borderW), bMat);
    topBorder.position.set((x1 + x2) / 2, borderH / 2 + 0.01, (z1 + z2) / 2 - width / 2 - borderW / 2);
    topBorder.receiveShadow = true;
    group.add(topBorder);

    const bottomBorder = new THREE.Mesh(new THREE.BoxGeometry(length, borderH, borderW), bMat);
    bottomBorder.position.set((x1 + x2) / 2, borderH / 2 + 0.01, (z1 + z2) / 2 + width / 2 + borderW / 2);
    bottomBorder.receiveShadow = true;
    group.add(bottomBorder);
  }

  return group;
}

export function createHorizontalPath(z: number, startX: number, endX: number, width = 4): THREE.Group {
  return createPath(startX, z, endX, z, width, false);
}

export function createVerticalPath(x: number, startZ: number, endZ: number, width = 4): THREE.Group {
  return createPath(x, startZ, x, endZ, width, true);
}

export function addStandardLights(scene: THREE.Scene): void {
  // warm ambient
  const ambient = new THREE.AmbientLight(0xffe8cc, 0.35);
  scene.add(ambient);

  // main sun
  const sun = new THREE.DirectionalLight(0xfff2d6, 1.2);
  sun.position.set(60, 100, 40);
  sun.castShadow = true;
  sun.shadow.mapSize.width = 4096;
  sun.shadow.mapSize.height = 4096;
  sun.shadow.camera.near = 0.5;
  sun.shadow.camera.far = 300;
  sun.shadow.camera.left = -120;
  sun.shadow.camera.right = 120;
  sun.shadow.camera.top = 120;
  sun.shadow.camera.bottom = -120;
  sun.shadow.bias = -0.0005;
  sun.shadow.normalBias = 0.02;
  scene.add(sun);

  // warm sky hemisphere
  const hemi = new THREE.HemisphereLight(0x8ec3f0, 0x3a6b35, 0.35);
  scene.add(hemi);

  // fill light from opposite side (bounce)
  const fill = new THREE.DirectionalLight(0xc8d8f0, 0.25);
  fill.position.set(-40, 30, -30);
  scene.add(fill);
}

export function addSimpleLights(scene: THREE.Scene): void {
  scene.add(new THREE.AmbientLight(0xffe8cc, 0.5));
  const directional = new THREE.DirectionalLight(0xfff2d6, 0.9);
  directional.position.set(50, 80, 40);
  directional.castShadow = true;
  directional.shadow.mapSize.width = 2048;
  directional.shadow.mapSize.height = 2048;
  scene.add(directional);
  scene.add(new THREE.HemisphereLight(0x8ec3f0, 0x3a6b35, 0.3));
}
