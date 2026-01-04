import { AfterViewInit, Component, ViewChild, ElementRef } from '@angular/core';
import { RouterLink } from '@angular/router';
import { TranslatePipe } from '@ngx-translate/core';

@Component({
  selector: 'app-home',
  imports: [TranslatePipe, RouterLink],
  templateUrl: './home.html',
  styleUrl: './home.css'
})
export class HomeComponent implements AfterViewInit {
  @ViewChild('promoVideo') promoVideo!: ElementRef<HTMLVideoElement>;

  ngAfterViewInit(): void {
    setTimeout(() => {
      if (this.promoVideo?.nativeElement) {
        this.promoVideo.nativeElement.currentTime = 0;
        this.promoVideo.nativeElement.muted = true;
        this.promoVideo.nativeElement.play().catch((err) => {
          console.warn('No se pudo reproducir el video:', err);
        });
      }
    }, 0);
  }
}
