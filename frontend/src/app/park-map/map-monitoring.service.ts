import { Injectable } from '@angular/core';
import * as THREE from 'three';
import { AttractionStatus } from '../admin-dashboard/monitoring.service';

export interface QueueIndicator {
  attractionId: number;
  sprite: THREE.Sprite;
  label: THREE.Sprite;
}

@Injectable({ providedIn: 'root' })
export class MapMonitoringService {
  private queueIndicators: Map<number, QueueIndicator> = new Map();
  private heatCircles: Map<number, THREE.Mesh> = new Map();

  createQueueIndicator(attractionId: number, position: THREE.Vector3): QueueIndicator {
    const circleTexture = this.createCircleTexture(0);
    const material = new THREE.SpriteMaterial({ map: circleTexture, transparent: true });
    const sprite = new THREE.Sprite(material);
    sprite.position.copy(position);
    sprite.position.y += 12;
    sprite.scale.set(4, 4, 1);

    const labelTexture = this.createLabelTexture('0');
    const labelMaterial = new THREE.SpriteMaterial({ map: labelTexture, transparent: true });
    const label = new THREE.Sprite(labelMaterial);
    label.position.copy(position);
    label.position.y += 12;
    label.scale.set(3, 1.5, 1);

    const indicator: QueueIndicator = { attractionId, sprite, label };
    this.queueIndicators.set(attractionId, indicator);
    return indicator;
  }

  updateQueueIndicator(status: AttractionStatus): void {
    const indicator = this.queueIndicators.get(status.attractionId);
    if (!indicator) return;

    const color = this.getQueueColor(status.queueSize);
    const circleTexture = this.createCircleTexture(status.queueSize, color);
    (indicator.sprite.material as THREE.SpriteMaterial).map?.dispose();
    (indicator.sprite.material as THREE.SpriteMaterial).map = circleTexture;
    (indicator.sprite.material as THREE.SpriteMaterial).needsUpdate = true;

    const labelTexture = this.createLabelTexture(status.queueSize.toString());
    (indicator.label.material as THREE.SpriteMaterial).map?.dispose();
    (indicator.label.material as THREE.SpriteMaterial).map = labelTexture;
    (indicator.label.material as THREE.SpriteMaterial).needsUpdate = true;

    indicator.sprite.material.opacity = status.isOpen ? 1 : 0.3;
  }

  createHeatCircle(attractionId: number, position: THREE.Vector3): THREE.Mesh {
    const geometry = new THREE.CircleGeometry(5, 32);
    const material = new THREE.MeshBasicMaterial({
      color: 0x10b981,
      transparent: true,
      opacity: 0.4,
      side: THREE.DoubleSide
    });
    const circle = new THREE.Mesh(geometry, material);
    circle.rotation.x = -Math.PI / 2;
    circle.position.set(position.x, 0.1, position.z);
    this.heatCircles.set(attractionId, circle);
    return circle;
  }

  updateHeatCircle(status: AttractionStatus): void {
    const circle = this.heatCircles.get(status.attractionId);
    if (!circle) return;

    const intensity = Math.min(status.queueSize / 50, 1);
    const scale = 5 + intensity * 10;
    circle.scale.set(scale / 5, scale / 5, 1);

    const color = this.getQueueColor(status.queueSize);
    (circle.material as THREE.MeshBasicMaterial).color.setStyle(color);
    (circle.material as THREE.MeshBasicMaterial).opacity = status.isOpen ? 0.3 + intensity * 0.3 : 0.1;
  }

  private getQueueColor(queueSize: number): string {
    if (queueSize < 20) return '#10b981';
    if (queueSize < 50) return '#f59e0b';
    return '#ef4444';
  }

  private createCircleTexture(queueSize: number, color = '#10b981'): THREE.CanvasTexture {
    const canvas = document.createElement('canvas');
    canvas.width = 128;
    canvas.height = 128;
    const ctx = canvas.getContext('2d')!;

    const gradient = ctx.createRadialGradient(64, 64, 0, 64, 64, 64);
    gradient.addColorStop(0, color);
    gradient.addColorStop(0.7, color + 'aa');
    gradient.addColorStop(1, color + '00');

    ctx.fillStyle = gradient;
    ctx.beginPath();
    ctx.arc(64, 64, 60, 0, Math.PI * 2);
    ctx.fill();

    return new THREE.CanvasTexture(canvas);
  }

  private createLabelTexture(text: string): THREE.CanvasTexture {
    const canvas = document.createElement('canvas');
    canvas.width = 128;
    canvas.height = 64;
    const ctx = canvas.getContext('2d')!;

    ctx.fillStyle = 'rgba(0, 0, 0, 0.7)';
    ctx.roundRect(4, 4, 120, 56, 8);
    ctx.fill();

    ctx.fillStyle = '#ffffff';
    ctx.font = 'bold 32px Arial';
    ctx.textAlign = 'center';
    ctx.textBaseline = 'middle';
    ctx.fillText(text, 64, 32);

    return new THREE.CanvasTexture(canvas);
  }

  clearAll(): void {
    this.queueIndicators.forEach(indicator => {
      indicator.sprite.material.dispose();
      indicator.label.material.dispose();
    });
    this.queueIndicators.clear();

    this.heatCircles.forEach(circle => {
      circle.geometry.dispose();
      (circle.material as THREE.Material).dispose();
    });
    this.heatCircles.clear();
  }
}
