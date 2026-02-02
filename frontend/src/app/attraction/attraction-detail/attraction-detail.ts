import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { TranslatePipe, TranslateService } from '@ngx-translate/core';
import { AttractionApiService, Attraction } from '../attraction.service';
import { getImageUrl } from '../../shared/utils';

@Component({
  selector: 'app-attraction-detail',
  standalone: true,
  imports: [CommonModule, TranslatePipe, RouterModule],
  templateUrl: './attraction-detail.html',
  styleUrls: ['./attraction-detail.css']
})
export class AttractionDetailComponent implements OnInit {
  attraction: Attraction | null = null;
  loading = true;
  error = false;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private attractionService: AttractionApiService,
    private translate: TranslateService
  ) {}

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.loadAttraction(+id);
    } else {
      this.error = true;
      this.loading = false;
    }
  }

  private loadAttraction(id: number): void {
    this.attractionService.findById(id).subscribe({
      next: (data) => {
        this.attraction = data;
        this.loading = false;
      },
      error: () => {
        this.error = true;
        this.loading = false;
      }
    });
  }

  getImageUrl(url: string): string {
    return getImageUrl(url) || '';
  }

  getIntensityClass(): string {
    if (!this.attraction) return '';
    const intensityMap: Record<string, string> = {
      'LOW': 'intensity-low',
      'MEDIUM': 'intensity-medium',
      'HIGH': 'intensity-high'
    };
    return intensityMap[this.attraction.intensity] || '';
  }

  getIntensityLabel(): string {
    if (!this.attraction) return '';
    return this.translate.instant(`ATTRACTIONS.FILTER.INTENSITY_${this.attraction.intensity}`);
  }

  getCategoryLabel(): string {
    if (!this.attraction) return '';
    return this.translate.instant(`ATTRACTION_CATEGORY.${this.attraction.category}`);
  }

  goBack(): void {
    this.router.navigate(['/park-map']);
  }

  goToAttractions(): void {
    this.router.navigate(['/attractions']);
  }
}

