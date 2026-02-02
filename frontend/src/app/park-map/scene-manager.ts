import * as THREE from 'three';
import { OrbitControls } from 'three/examples/jsm/controls/OrbitControls.js';
import { Attraction } from '../attraction/attraction.service';
import { AttractionMeshFactory } from './attraction-mesh-factory';
import { ParkBuilder } from './park-builder';
import { isWebGLAvailable, addStandardLights } from './three-utils';

export interface AttractionMesh extends THREE.Group {
  userData: {
    attraction: Attraction;
    originalY: number;
  };
}

export class SceneManager {
  private scene!: THREE.Scene;
  private camera!: THREE.PerspectiveCamera;
  private renderer!: THREE.WebGLRenderer;
  private controls!: OrbitControls;
  private meshFactory = new AttractionMeshFactory();
  private parkBuilder = new ParkBuilder();
  private attractionMeshes: AttractionMesh[] = [];
  private animationId: number | null = null;
  private hoveredAttraction: Attraction | null = null;
  private initialized = false;
  private clock = new THREE.Clock();

  isInitialized(): boolean {
    return this.initialized;
  }

  initialize(container: HTMLDivElement): boolean {
    if (!isWebGLAvailable()) {
      console.error('WebGL is not available');
      return false;
    }

    const width = container.clientWidth || 800;
    const height = container.clientHeight || 600;

    this.initScene();
    this.initCamera(width, height);
    this.initRenderer(container, width, height);
    this.initControls();

    addStandardLights(this.scene);
    this.parkBuilder.buildParkEnvironment(this.scene);

    this.initialized = true;
    return true;
  }

  private initScene(): void {
    this.scene = new THREE.Scene();
    const skyGeo = new THREE.SphereGeometry(400, 32, 15);
    const skyMat = new THREE.MeshBasicMaterial({ color: 0x87ceeb, side: THREE.BackSide });
    const sky = new THREE.Mesh(skyGeo, skyMat);
    this.scene.add(sky);
    this.scene.fog = new THREE.Fog(0x87ceeb, 80, 180);
  }

  private initCamera(width: number, height: number): void {
    this.camera = new THREE.PerspectiveCamera(60, width / height, 0.1, 1000);
    this.camera.position.set(0, 60, 70);
  }

  private initRenderer(container: HTMLDivElement, width: number, height: number): void {
    this.renderer = new THREE.WebGLRenderer({ antialias: true });
    this.renderer.setSize(width, height);
    this.renderer.setPixelRatio(Math.min(window.devicePixelRatio, 2));
    this.renderer.shadowMap.enabled = true;
    this.renderer.shadowMap.type = THREE.PCFSoftShadowMap;
    this.renderer.outputColorSpace = THREE.SRGBColorSpace;
    this.renderer.toneMapping = THREE.ACESFilmicToneMapping;
    this.renderer.toneMappingExposure = 1.0;
    (this.renderer as any).physicallyCorrectLights = true;
    container.appendChild(this.renderer.domElement);
  }

  private initControls(): void {
    this.controls = new OrbitControls(this.camera, this.renderer.domElement);
    this.controls.enableDamping = true;
    this.controls.dampingFactor = 0.05;
    this.controls.maxPolarAngle = Math.PI / 2.2;
    this.controls.minDistance = 25;
    this.controls.maxDistance = 220;
    this.controls.target.set(0, 0, 0);
  }

  addAttractionsToScene(attractions: Attraction[]): void {
    if (!this.initialized || !this.scene) return;

    this.attractionMeshes.forEach(m => this.scene.remove(m));
    this.attractionMeshes = [];

    const parkSize = 100;

    attractions.forEach((attraction) => {
      const mesh = this.meshFactory.createAttractionMesh(attraction);
      const x = ((attraction.mapPositionX || 50) / 100 - 0.5) * parkSize;
      const z = ((attraction.mapPositionY || 50) / 100 - 0.5) * parkSize;
      mesh.position.set(x, 0, z);

      mesh.userData = { attraction, originalY: mesh.position.y };
      mesh.traverse((o: any) => {
        if (o.isMesh) {
          o.castShadow = o.castShadow ?? true;
          o.receiveShadow = o.receiveShadow ?? true;
        }
      });

      this.scene.add(mesh);
      this.attractionMeshes.push(mesh as AttractionMesh);
      this.parkBuilder.addAttractionZone(mesh.position.x, mesh.position.z, 8);
    });
  }

  startAnimation(): void {
    if (!this.initialized) return;
    this.animate();
  }

  private animate = (): void => {
    this.animationId = requestAnimationFrame(this.animate);
    this.controls.update();
    const elapsed = this.clock.getElapsedTime();

    this.attractionMeshes.forEach(mesh => {
      if (mesh.userData.attraction === this.hoveredAttraction) {
        mesh.position.y = mesh.userData.originalY + Math.sin(Date.now() * 0.003) * 0.35 + 0.5;
      } else {
        mesh.position.y = mesh.userData.originalY;
      }
    });

    this.updateWaterEffects(elapsed);
    this.renderer.render(this.scene, this.camera);
  }

  private updateWaterEffects(elapsed: number): void {
    this.scene.traverse(obj => {
      if ((obj as THREE.Mesh).material && (obj as THREE.Mesh).material instanceof THREE.MeshPhysicalMaterial) {
        const mat = (obj as THREE.Mesh).material as THREE.MeshPhysicalMaterial;
        if ((mat.color && mat.color.g > mat.color.r) && mat.transparent) {
          (mat as any).reflectivity = 0.55 + Math.sin(elapsed * 0.9) * 0.02;
        }
      }
    });
  }

  setHoveredAttraction(attraction: Attraction | null): void {
    this.hoveredAttraction = attraction;
  }

  getAttractionMeshes(): AttractionMesh[] {
    return this.attractionMeshes;
  }

  getCamera(): THREE.PerspectiveCamera {
    return this.camera;
  }

  handleResize(width: number, height: number): void {
    if (!this.initialized || !this.renderer) return;
    this.camera.aspect = width / height;
    this.camera.updateProjectionMatrix();
    this.renderer.setSize(width, height);
  }

  zoomIn(): void {
    if (!this.initialized) return;
    this.camera.position.multiplyScalar(0.8);
  }

  zoomOut(): void {
    if (!this.initialized) return;
    this.camera.position.multiplyScalar(1.2);
  }

  resetView(): void {
    if (!this.initialized) return;
    this.camera.position.set(0, 50, 50);
    this.controls.reset();
  }

  dispose(): void {
    if (this.animationId) cancelAnimationFrame(this.animationId);
    this.renderer?.dispose();
    this.controls?.dispose();
  }
}
