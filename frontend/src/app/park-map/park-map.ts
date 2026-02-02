import { Component, OnInit, OnDestroy, ElementRef, ViewChild, AfterViewInit, HostListener } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { TranslatePipe } from '@ngx-translate/core';
import * as THREE from 'three';
import { AttractionApiService, Attraction } from '../attraction/attraction.service';
import { getImageUrl } from '../shared/utils';
import { SceneManager, AttractionMesh } from './scene-manager';

@Component({
  selector: 'app-park-map',
  standalone: true,
  imports: [CommonModule, TranslatePipe],
  templateUrl: './park-map.html',
  styleUrls: ['./park-map.css']
})
export class ParkMapComponent implements OnInit, AfterViewInit, OnDestroy {
  @ViewChild('mapContainer') mapContainer!: ElementRef<HTMLDivElement>;
  @ViewChild('tooltipEl') tooltipEl!: ElementRef<HTMLDivElement>;

  loading = true;
  error = false;
  webglError = false;
  attractions: Attraction[] = [];

  hoveredAttraction: Attraction | null = null;
  tooltipX = 0;
  tooltipY = 0;

  private sceneManager = new SceneManager();
  private raycaster = new THREE.Raycaster();
  private mouse = new THREE.Vector2();

  constructor(
    private attractionService: AttractionApiService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadAttractions();
  }

  ngAfterViewInit(): void {
    setTimeout(() => this.initScene(), 100);
  }

  ngOnDestroy(): void {
    this.sceneManager.dispose();
  }

  @HostListener('window:resize')
  onResize(): void {
    if (!this.mapContainer) return;
    const width = this.mapContainer.nativeElement.clientWidth;
    const height = this.mapContainer.nativeElement.clientHeight;
    this.sceneManager.handleResize(width, height);
  }

  private loadAttractions(): void {
    this.attractionService.findAll().subscribe({
      next: (data) => {
        this.attractions = data.filter(a => a.isActive);
        this.loading = false;
        this.sceneManager.addAttractionsToScene(this.attractions);
      },
      error: () => {
        this.error = true;
        this.loading = false;
      }
    });
  }

  private initScene(): void {
    if (!this.mapContainer?.nativeElement) return;
    const container = this.mapContainer.nativeElement;
    const success = this.sceneManager.initialize(container);
    if (!success) {
      this.webglError = true;
      this.loading = false;
      return;
    }
    if (this.attractions.length > 0) {
      this.sceneManager.addAttractionsToScene(this.attractions);
    }
    this.sceneManager.startAnimation();
  }

  onMouseMove(event: MouseEvent): void {
    if (!this.sceneManager.isInitialized()) return;
    const rect = this.mapContainer.nativeElement.getBoundingClientRect();
    this.mouse.x = ((event.clientX - rect.left) / rect.width) * 2 - 1;
    this.mouse.y = -((event.clientY - rect.top) / rect.height) * 2 + 1;

    this.raycaster.setFromCamera(this.mouse, this.sceneManager.getCamera());
    const attractionMeshes = this.sceneManager.getAttractionMeshes();
    const intersects = this.raycaster.intersectObjects(
      attractionMeshes.flatMap(m => m.children),
      true
    );

    if (intersects.length > 0) {
      const intersected = intersects[0].object;
      let parent = intersected.parent;
      while (parent && !attractionMeshes.includes(parent as AttractionMesh)) {
        parent = parent.parent;
      }
      if (parent) {
        const attractionMesh = parent as AttractionMesh;
        this.hoveredAttraction = attractionMesh.userData.attraction;
        this.sceneManager.setHoveredAttraction(this.hoveredAttraction);
        this.tooltipX = event.clientX - rect.left;
        this.tooltipY = event.clientY - rect.top;
        this.mapContainer.nativeElement.style.cursor = 'pointer';
      }
    } else {
      this.hoveredAttraction = null;
      this.sceneManager.setHoveredAttraction(null);
      this.mapContainer.nativeElement.style.cursor = 'grab';
    }
  }

  onClick(): void {
    if (!this.hoveredAttraction) return;
    this.router.navigate(['/attraction', this.hoveredAttraction.id]);
  }

  getImageUrl(url: string): string {
    return getImageUrl(url) || '';
  }

  zoomIn(): void {
    this.sceneManager.zoomIn();
  }

  zoomOut(): void {
    this.sceneManager.zoomOut();
  }

  resetView(): void {
    this.sceneManager.resetView();
  }
}

