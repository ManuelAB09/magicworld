import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { HomeComponent } from './home';
import { TranslateModule } from '@ngx-translate/core';
import { provideRouter } from '@angular/router';

describe('HomeComponent', () => {
  let component: HomeComponent;
  let fixture: ComponentFixture<HomeComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [HomeComponent, TranslateModule.forRoot()],
      providers: [provideRouter([])]
    }).compileComponents();

    fixture = TestBed.createComponent(HomeComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should have promoVideo ViewChild', () => {
    fixture.detectChanges();
    expect(component.promoVideo).toBeDefined();
  });

  it('should handle ngAfterViewInit with valid video', fakeAsync(() => {
    const mockVideo = {
      currentTime: 10,
      muted: false,
      play: jasmine.createSpy('play').and.returnValue(Promise.resolve())
    };
    (component as any).promoVideo = { nativeElement: mockVideo };

    component.ngAfterViewInit();
    tick(10);

    expect(mockVideo.currentTime).toBe(0);
    expect(mockVideo.muted).toBeTrue();
  }));

  it('should handle missing video element gracefully', fakeAsync(() => {
    (component as any).promoVideo = null;

    expect(() => {
      component.ngAfterViewInit();
      tick(10);
    }).not.toThrow();
  }));

  it('should handle undefined nativeElement gracefully', fakeAsync(() => {
    (component as any).promoVideo = { nativeElement: undefined };

    expect(() => {
      component.ngAfterViewInit();
      tick(10);
    }).not.toThrow();
  }));
});

