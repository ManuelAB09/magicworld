import { Component, ElementRef, ViewChild, AfterViewInit, OnDestroy, Input, Output, EventEmitter, OnChanges, SimpleChanges, inject } from '@angular/core';
import { TranslatePipe } from '@ngx-translate/core';
import * as THREE from 'three';
import { OrbitControls } from 'three/examples/jsm/controls/OrbitControls.js';
import { ParkBuilder } from '../park-map/park-builder';
import { AttractionMeshFactory } from '../park-map/attraction-mesh-factory';
import { AttractionApiService } from './attraction.service';
import { isWebGLAvailable, addSimpleLights } from '../park-map/three-utils';

@Component({
  selector: 'app-map-picker-3d',
  standalone: true,
  imports: [TranslatePipe],
  template: `
    @if (webglError) {
      <div class="map-picker-3d map-picker-3d--error">
        <p>{{ 'MAP_PICKER.WEBGL_ERROR' | translate }}</p>
        <p>{{ 'MAP_PICKER.MANUAL_COORDS' | translate }} (X: {{ positionX }}%, Y: {{ positionY }}%)</p>
      </div>
    } @else {
      <div class="map-picker-3d" #container (click)="onContainerClick($event)">
        @if (markerVisible) {
          <div class="map-picker-3d__marker" [style.left.px]="markerScreenX" [style.top.px]="markerScreenY">📍</div>
        }
      </div>
      <div class="map-picker-3d__coords">
        X: {{ positionX.toFixed(1) }}% | Y: {{ positionY.toFixed(1) }}%
      </div>
    }
  `,
  styles: [`
    .map-picker-3d {
      position: relative;
      width: 100%;
      height: 280px;
      border-radius: 12px;
      overflow: hidden;
      border: 3px solid #2c0d0d;
      cursor: crosshair;
      box-shadow: 0 4px 16px rgba(0,0,0,0.2);
    }
    .map-picker-3d--error {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      background: #f5f5f5;
      cursor: default;
    }
    .map-picker-3d--error p {
      margin: 5px 0;
      color: #666;
      text-align: center;
    }
    .map-picker-3d__marker {
      position: absolute;
      font-size: 32px;
      transform: translate(-50%, -100%);
      filter: drop-shadow(0 3px 6px rgba(0,0,0,0.5));
      pointer-events: none;
      z-index: 10;
      transition: left 0.1s, top 0.1s;
    }
    .map-picker-3d__coords {
      margin-top: 10px;
      font-size: 0.95rem;
      color: #2c0d0d;
      text-align: center;
      font-weight: 600;
    }
  `]
})
export class MapPicker3DComponent implements AfterViewInit, OnDestroy, OnChanges {
  @ViewChild('container') containerRef!: ElementRef<HTMLDivElement>;
  @Input() positionX = 50;
  @Input() positionY = 50;
  @Input() category = 'OTHER';
  @Input() excludeId?: number;
  @Output() positionChange = new EventEmitter<{ x: number; y: number }>();

  markerScreenX = 0;
  markerScreenY = 0;
  markerVisible = false;
  webglError = false;

  private attractionApi = inject(AttractionApiService);

  private scene!: THREE.Scene;
  private camera!: THREE.PerspectiveCamera;
  private renderer!: THREE.WebGLRenderer;
  private controls!: OrbitControls;
  private parkBuilder = new ParkBuilder();
  private meshFactory = new AttractionMeshFactory();
  private animationId: number | null = null;
  private markerMesh: THREE.Group | null = null;
  private raycaster = new THREE.Raycaster();
  private groundPlane!: THREE.Mesh;
  private initialized = false;

  ngAfterViewInit(): void {
    setTimeout(() => this.initScene(), 100);
  }

  ngOnChanges(changes: SimpleChanges): void {
    if ((changes['positionX'] || changes['positionY'] || changes['category']) && this.initialized) {
      this.updateMarkerPosition();
    }
  }

  ngOnDestroy(): void {
    if (this.animationId) cancelAnimationFrame(this.animationId);
    this.renderer?.dispose();
    this.controls?.dispose();
  }

  private initScene(): void {
    if (!this.containerRef?.nativeElement) return;

    if (!isWebGLAvailable()) {
      this.webglError = true;
      return;
    }

    const container = this.containerRef.nativeElement;
    const width = container.clientWidth || 400;
    const height = container.clientHeight || 280;

    this.scene = new THREE.Scene();
    this.scene.background = new THREE.Color(0x87ceeb);
    this.scene.fog = new THREE.Fog(0xc8dde8, 60, 140);

    this.camera = new THREE.PerspectiveCamera(50, width / height, 0.1, 500);
    this.camera.position.set(0, 70, 60);

    this.renderer = new THREE.WebGLRenderer({ antialias: true });
    this.renderer.setSize(width, height);
    this.renderer.setPixelRatio(Math.min(window.devicePixelRatio, 2));
    this.renderer.shadowMap.enabled = true;
    this.renderer.toneMapping = THREE.ACESFilmicToneMapping;
    this.renderer.toneMappingExposure = 1.1;
    container.appendChild(this.renderer.domElement);

    this.controls = new OrbitControls(this.camera, this.renderer.domElement);
    this.controls.enableDamping = true;
    this.controls.dampingFactor = 0.05;
    this.controls.maxPolarAngle = Math.PI / 2.2;
    this.controls.minDistance = 30;
    this.controls.maxDistance = 100;

    addSimpleLights(this.scene);
    this.buildEnvironment();
    this.loadExistingAttractions();
    this.initialized = true;
    this.updateMarkerPosition();
    this.animate();
  }

  private buildEnvironment(): void {
    // Use ParkBuilder for full environment (walls, towers, lake, decorations, etc.)
    this.parkBuilder.buildParkEnvironment(this.scene);

    // Add invisible ground plane for raycasting clicks
    const invisibleGround = new THREE.Mesh(
      new THREE.PlaneGeometry(140, 140),
      new THREE.MeshBasicMaterial({ visible: false })
    );
    invisibleGround.rotation.x = -Math.PI / 2;
    invisibleGround.position.y = 0.1;
    this.groundPlane = invisibleGround;
    this.scene.add(invisibleGround);
  }

  private loadExistingAttractions(): void {
    this.attractionApi.findAll().subscribe({
      next: (attractions) => {
        const parkSize = 100;
        attractions.forEach(attraction => {
          if (attraction.id === this.excludeId) return;
          if (!attraction.isActive) return;

          const mesh = this.meshFactory.createAttractionMesh(attraction);
          const x = ((attraction.mapPositionX || 50) / 100 - 0.5) * parkSize;
          const z = ((attraction.mapPositionY || 50) / 100 - 0.5) * parkSize;
          mesh.position.set(x, 0, z);

          // Make existing attractions semi-transparent to distinguish from the placement marker
          mesh.traverse((child: any) => {
            if (child.isMesh && child.material) {
              const mat = child.material.clone();
              mat.transparent = true;
              mat.opacity = 0.6;
              child.material = mat;
            }
          });

          this.scene.add(mesh);
        });
      },
      error: () => { /* silent – environment still works */ }
    });
  }


  private updateMarkerPosition(): void {
    if (this.markerMesh) {
      this.scene.remove(this.markerMesh);
    }

    const attraction = { category: this.category } as any;
    this.markerMesh = this.meshFactory.createAttractionMesh(attraction);

    const x = ((this.positionX / 100) - 0.5) * 100;
    const z = ((this.positionY / 100) - 0.5) * 100;
    this.markerMesh.position.set(x, 0, z);
    this.markerMesh.scale.setScalar(1.2);
    this.scene.add(this.markerMesh);

    this.updateMarkerScreenPosition();
  }

  private updateMarkerScreenPosition(): void {
    if (!this.markerMesh || !this.camera || !this.renderer) return;

    const vector = new THREE.Vector3();
    this.markerMesh.getWorldPosition(vector);
    vector.y += 5;
    vector.project(this.camera);

    const canvas = this.renderer.domElement;
    this.markerScreenX = (vector.x * 0.5 + 0.5) * canvas.clientWidth;
    this.markerScreenY = (-vector.y * 0.5 + 0.5) * canvas.clientHeight;
    this.markerVisible = vector.z < 1;
  }

  private animate = (): void => {
    this.animationId = requestAnimationFrame(this.animate);
    this.controls.update();
    this.updateMarkerScreenPosition();
    this.renderer.render(this.scene, this.camera);
  }

  onContainerClick(event: MouseEvent): void {
    const container = this.containerRef.nativeElement;
    const rect = container.getBoundingClientRect();
    const mouse = new THREE.Vector2(
      ((event.clientX - rect.left) / rect.width) * 2 - 1,
      -((event.clientY - rect.top) / rect.height) * 2 + 1
    );

    this.raycaster.setFromCamera(mouse, this.camera);
    const intersects = this.raycaster.intersectObject(this.groundPlane);

    if (intersects.length > 0) {
      const point = intersects[0].point;
      const x = Math.max(0, Math.min(100, (point.x / 100 + 0.5) * 100));
      const y = Math.max(0, Math.min(100, (point.z / 100 + 0.5) * 100));

      this.positionX = Math.round(x * 10) / 10;
      this.positionY = Math.round(y * 10) / 10;
      this.positionChange.emit({ x: this.positionX, y: this.positionY });
      this.updateMarkerPosition();
    }
  }
}
