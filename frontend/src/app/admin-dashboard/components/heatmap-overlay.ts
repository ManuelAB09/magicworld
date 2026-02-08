import { Component, Input, OnChanges, SimpleChanges } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TranslatePipe } from '@ngx-translate/core';
import { AttractionStatus } from '../monitoring.service';

@Component({
  selector: 'app-heatmap-overlay',
  standalone: true,
  imports: [CommonModule, TranslatePipe],
  templateUrl: './heatmap-overlay.html',
  styleUrls: ['./heatmap-overlay.css']
})
export class HeatmapOverlayComponent implements OnChanges {
  @Input() attractions: AttractionStatus[] = [];

  topQueues: AttractionStatus[] = [];
  hoveredId: number | null = null;

  get hoveredAttraction(): AttractionStatus | null {
    return this.attractions.find(a => a.attractionId === this.hoveredId) || null;
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['attractions']) {
      this.updateTopQueues();
    }
  }

  private updateTopQueues(): void {
    this.topQueues = [...this.attractions]
      .filter(a => a.isOpen && a.queueSize > 0)
      .sort((a, b) => b.queueSize - a.queueSize)
      .slice(0, 5);
  }

  getHeatRadius(attraction: AttractionStatus): number {
    if (!attraction.isOpen) return 3;
    const base = 4;
    const intensity = Math.min(attraction.queueSize / 40, 2);
    return base + intensity * 5;
  }

  getHeatColor(attraction: AttractionStatus): string {
    if (!attraction.isOpen) return '#9ca3af';
    if (attraction.queueSize < 20) return '#10b981';
    if (attraction.queueSize < 50) return '#f59e0b';
    return '#ef4444';
  }
}
