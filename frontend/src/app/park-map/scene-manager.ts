import * as THREE from 'three';
import { OrbitControls } from 'three/examples/jsm/controls/OrbitControls.js';
import { EffectComposer } from 'three/examples/jsm/postprocessing/EffectComposer.js';
import { RenderPass } from 'three/examples/jsm/postprocessing/RenderPass.js';
import { UnrealBloomPass } from 'three/examples/jsm/postprocessing/UnrealBloomPass.js';
import { SMAAPass } from 'three/examples/jsm/postprocessing/SMAAPass.js';
import { OutputPass } from 'three/examples/jsm/postprocessing/OutputPass.js';
import { Attraction } from '../attraction/attraction.service';
import { AttractionMeshFactory } from './attraction-mesh-factory';
import { ParkBuilder } from './park-builder';
import { isWebGLAvailable, addStandardLights } from './three-utils';
import { secureRandom } from './secure-random';

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
  private composer!: EffectComposer;
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
    this.initPostProcessing(width, height);

    addStandardLights(this.scene);
    this.parkBuilder.buildParkEnvironment(this.scene);

    this.initialized = true;
    return true;
  }

  private initScene(): void {
    this.scene = new THREE.Scene();

    // gradient sky sphere
    const skyGeo = new THREE.SphereGeometry(400, 32, 32);
    const skyCanvas = document.createElement('canvas');
    skyCanvas.width = 512;
    skyCanvas.height = 512;
    const skyCtx = skyCanvas.getContext('2d')!;

    // sky gradient: warm horizon → deep blue zenith
    const gradient = skyCtx.createLinearGradient(0, 0, 0, 512);
    gradient.addColorStop(0, '#1a5fa0');   // zenith blue
    gradient.addColorStop(0.25, '#47a0e0'); // mid blue
    gradient.addColorStop(0.55, '#8ecbf0'); // light blue
    gradient.addColorStop(0.75, '#c8e0f0'); // pale
    gradient.addColorStop(0.90, '#f0dcc0'); // warm horizon
    gradient.addColorStop(1.0, '#e8c890');  // golden horizon
    skyCtx.fillStyle = gradient;
    skyCtx.fillRect(0, 0, 512, 512);

    const skyTex = new THREE.CanvasTexture(skyCanvas);
    (skyTex as any).colorSpace = THREE.SRGBColorSpace;
    const skyMat = new THREE.MeshBasicMaterial({ map: skyTex, side: THREE.BackSide });
    const sky = new THREE.Mesh(skyGeo, skyMat);
    this.scene.add(sky);

    // procedural cloud layer
    this.addClouds();

    // warm atmospheric fog
    this.scene.fog = new THREE.Fog(0xc8dde8, 90, 220);
  }

  private addClouds(): void {
    const cloudMat = new THREE.MeshBasicMaterial({
      color: 0xffffff,
      transparent: true,
      opacity: 0.6,
      side: THREE.DoubleSide,
      depthWrite: false,
    });

    for (let i = 0; i < 15; i++) {
      const cloudGroup = new THREE.Group();
      const puffCount = 3 + Math.floor(secureRandom() * 4);
      for (let p = 0; p < puffCount; p++) {
        const puff = new THREE.Mesh(
          new THREE.SphereGeometry(5 + secureRandom() * 8, 8, 6),
          cloudMat
        );
        puff.position.set(
          (secureRandom() - 0.5) * 14,
          (secureRandom() - 0.5) * 3,
          (secureRandom() - 0.5) * 6
        );
        puff.scale.y = 0.4 + secureRandom() * 0.3;
        cloudGroup.add(puff);
      }
      cloudGroup.position.set(
        (secureRandom() - 0.5) * 300,
        60 + secureRandom() * 40,
        (secureRandom() - 0.5) * 300
      );
      cloudGroup.name = 'cloud';
      this.scene.add(cloudGroup);
    }
  }

  private initCamera(width: number, height: number): void {
    this.camera = new THREE.PerspectiveCamera(55, width / height, 0.1, 1000);
    this.camera.position.set(0, 60, 80);
  }

  private initRenderer(container: HTMLDivElement, width: number, height: number): void {
    this.renderer = new THREE.WebGLRenderer({ antialias: true });
    this.renderer.setSize(width, height);
    this.renderer.setPixelRatio(Math.min(window.devicePixelRatio, 2));
    this.renderer.shadowMap.enabled = true;
    this.renderer.shadowMap.type = THREE.PCFSoftShadowMap;
    this.renderer.outputColorSpace = THREE.SRGBColorSpace;
    this.renderer.toneMapping = THREE.ACESFilmicToneMapping;
    this.renderer.toneMappingExposure = 1.1;
    (this.renderer as any).physicallyCorrectLights = true;
    container.appendChild(this.renderer.domElement);
  }

  private initPostProcessing(width: number, height: number): void {
    this.composer = new EffectComposer(this.renderer);

    const renderPass = new RenderPass(this.scene, this.camera);
    this.composer.addPass(renderPass);

    // subtle bloom for emissive objects (lights, water glints)
    const bloomPass = new UnrealBloomPass(
      new THREE.Vector2(width, height),
      0.3,   // strength (subtle)
      0.6,   // radius
      0.85   // threshold
    );
    this.composer.addPass(bloomPass);

    // high-quality anti-aliasing
    const smaaPass = new SMAAPass();
    this.composer.addPass(smaaPass);

    const outputPass = new OutputPass();
    this.composer.addPass(outputPass);
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

    // hover bob animation
    this.attractionMeshes.forEach(mesh => {
      if (mesh.userData.attraction === this.hoveredAttraction) {
        mesh.position.y = mesh.userData.originalY + Math.sin(Date.now() * 0.003) * 0.35 + 0.5;
      } else {
        mesh.position.y = mesh.userData.originalY;
      }
    });

    // water shimmer effects
    this.updateWaterEffects(elapsed);

    // slow cloud drift
    this.scene.traverse(obj => {
      if (obj.name === 'cloud') {
        obj.position.x += 0.02;
        if (obj.position.x > 200) obj.position.x = -200;
      }
    });

    // render via post-processing pipeline
    this.composer.render();
  }

  private updateWaterEffects(elapsed: number): void {
    this.scene.traverse(obj => {
      if ((obj as THREE.Mesh).material && (obj as THREE.Mesh).material instanceof THREE.MeshPhysicalMaterial) {
        const mat = (obj as THREE.Mesh).material as THREE.MeshPhysicalMaterial;
        if ((mat.color && mat.color.g > mat.color.r) && mat.transparent) {
          (mat as any).reflectivity = 0.55 + Math.sin(elapsed * 0.9) * 0.02;
        }
      }
      // gentle water surface oscillation
      if (obj.name === 'lakeWater' || obj.name === 'fountainWater') {
        obj.position.y = 0.06 + Math.sin(elapsed * 1.5) * 0.02;
      }
    });
  }

  setHoveredAttraction(attraction: Attraction | null): void {
    this.hoveredAttraction = attraction;
  }

  addToScene(object: THREE.Object3D): void {
    if (this.initialized && this.scene) {
      this.scene.add(object);
    }
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
    this.composer.setSize(width, height);
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
    this.camera.position.set(0, 60, 80);
    this.controls.reset();
  }

  dispose(): void {
    if (this.animationId) cancelAnimationFrame(this.animationId);
    this.composer?.dispose();
    this.renderer?.dispose();
    this.controls?.dispose();
  }
}
