import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TranslatePipe } from '@ngx-translate/core';
import { AttractionStatus } from '../monitoring.service';

@Component({
  selector: 'app-attraction-queue-list',
  standalone: true,
  imports: [CommonModule, TranslatePipe],
  templateUrl: './attraction-queue-list.html',
  styleUrls: ['./attraction-queue-list.css']
})
export class AttractionQueueListComponent {
  @Input() attractions: AttractionStatus[] = [];

  get sortedAttractions(): AttractionStatus[] {
    return [...this.attractions].sort((a, b) => b.queueSize - a.queueSize);
  }

  getQueuePercentage(attraction: AttractionStatus): number {
    const maxQueue = 100;
    return Math.min(100, (attraction.queueSize / maxQueue) * 100);
  }
}
