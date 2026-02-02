import * as THREE from 'three';

export function isWebGLAvailable(): boolean {
  try {
    const canvas = document.createElement('canvas');
    const gl = canvas.getContext('webgl') || canvas.getContext('experimental-webgl');
    return gl !== null;
  } catch (e) {
    return false;
  }
}

export function createPath(
  x1: number, z1: number,
  x2: number, z2: number,
  width: number,
  isVertical: boolean
): THREE.Mesh {
  const length = isVertical ? Math.abs(z2 - z1) : Math.abs(x2 - x1);
  const thickness = 0.08;
  const geometry = isVertical
    ? new THREE.BoxGeometry(width, thickness, length)
    : new THREE.BoxGeometry(length, thickness, width);
  const material = new THREE.MeshStandardMaterial({
    color: 0xc9b896,
    roughness: 0.95,
    metalness: 0.0
  });
  const path = new THREE.Mesh(geometry, material);
  path.position.set((x1 + x2) / 2, thickness / 2 + 0.01, (z1 + z2) / 2);
  path.receiveShadow = true;
  return path;
}

export function createHorizontalPath(z: number, startX: number, endX: number, width = 4): THREE.Mesh {
  const length = Math.abs(endX - startX);
  const thickness = 0.08;
  const geometry = new THREE.BoxGeometry(length, thickness, width);
  const material = new THREE.MeshStandardMaterial({
    color: 0xc9b896,
    roughness: 0.95,
    metalness: 0.0
  });
  const path = new THREE.Mesh(geometry, material);
  path.position.set((startX + endX) / 2, thickness / 2 + 0.01, z);
  path.receiveShadow = true;
  return path;
}

export function createVerticalPath(x: number, startZ: number, endZ: number, width = 4): THREE.Mesh {
  const length = Math.abs(endZ - startZ);
  const thickness = 0.08;
  const geometry = new THREE.BoxGeometry(width, thickness, length);
  const material = new THREE.MeshStandardMaterial({
    color: 0xc9b896,
    roughness: 0.95,
    metalness: 0.0
  });
  const path = new THREE.Mesh(geometry, material);
  path.position.set(x, thickness / 2 + 0.01, (startZ + endZ) / 2);
  path.receiveShadow = true;
  return path;
}

export function addStandardLights(scene: THREE.Scene): void {
  const ambient = new THREE.AmbientLight(0xffffff, 0.35);
  scene.add(ambient);

  const directional = new THREE.DirectionalLight(0xfff6d8, 1.0);
  directional.position.set(60, 100, 40);
  directional.castShadow = true;
  directional.shadow.mapSize.width = 2048;
  directional.shadow.mapSize.height = 2048;
  directional.shadow.camera.near = 0.5;
  directional.shadow.camera.far = 300;
  directional.shadow.camera.left = -120;
  directional.shadow.camera.right = 120;
  directional.shadow.camera.top = 120;
  directional.shadow.camera.bottom = -120;
  scene.add(directional);

  const hemi = new THREE.HemisphereLight(0x87ceeb, 0x4a7c59, 0.25);
  scene.add(hemi);
}

export function addSimpleLights(scene: THREE.Scene): void {
  scene.add(new THREE.AmbientLight(0xffffff, 0.5));
  const directional = new THREE.DirectionalLight(0xffffff, 0.9);
  directional.position.set(50, 80, 40);
  directional.castShadow = true;
  scene.add(directional);
  scene.add(new THREE.HemisphereLight(0x87ceeb, 0x4a7c59, 0.3));
}

