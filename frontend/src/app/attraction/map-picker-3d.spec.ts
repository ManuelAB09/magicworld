import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { MapPicker3DComponent } from './map-picker-3d';
import { AttractionApiService } from './attraction.service';
import { TranslateModule } from '@ngx-translate/core';
import { of, throwError } from 'rxjs';
import { SimpleChange } from '@angular/core';

describe('MapPicker3DComponent', () => {
  let component: MapPicker3DComponent;
  let fixture: ComponentFixture<MapPicker3DComponent>;
  let mockAttractionApi: jasmine.SpyObj<AttractionApiService>;

  beforeEach(async () => {
    mockAttractionApi = jasmine.createSpyObj('AttractionApiService', ['findAll']);
    mockAttractionApi.findAll.and.returnValue(of([]));

    await TestBed.configureTestingModule({
      imports: [MapPicker3DComponent, TranslateModule.forRoot()],
      providers: [
        { provide: AttractionApiService, useValue: mockAttractionApi }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(MapPicker3DComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should have default positionX of 50', () => {
    expect(component.positionX).toBe(50);
  });

  it('should have default positionY of 50', () => {
    expect(component.positionY).toBe(50);
  });

  it('should have default category of OTHER', () => {
    expect(component.category).toBe('OTHER');
  });

  it('should initialize with webglError false', () => {
    expect(component.webglError).toBeFalse();
  });

  it('should initialize with markerVisible false', () => {
    expect(component.markerVisible).toBeFalse();
  });

  it('should not throw on ngOnChanges before initialization', () => {
    expect(() => {
      component.ngOnChanges({
        positionX: new SimpleChange(50, 60, false)
      });
    }).not.toThrow();
  });

  it('should not throw on ngOnDestroy before initialization', () => {
    expect(() => component.ngOnDestroy()).not.toThrow();
  });

  it('should have positionChange as EventEmitter', () => {
    expect(component.positionChange).toBeTruthy();
    expect(component.positionChange.subscribe).toBeDefined();
  });

  it('should accept custom positionX input', () => {
    component.positionX = 75;
    expect(component.positionX).toBe(75);
  });

  it('should accept custom positionY input', () => {
    component.positionY = 25;
    expect(component.positionY).toBe(25);
  });

  it('should accept category input', () => {
    component.category = 'ROLLER_COASTER';
    expect(component.category).toBe('ROLLER_COASTER');
  });

  it('should accept excludeId input', () => {
    component.excludeId = 5;
    expect(component.excludeId).toBe(5);
  });

  describe('when WebGL is not available', () => {
    it('should show webgl error template when webglError is true', () => {
      component.webglError = true;
      fixture.detectChanges();

      const el = fixture.nativeElement as HTMLElement;
      const errorDiv = el.querySelector('.map-picker-3d--error');
      expect(errorDiv).toBeTruthy();
    });

    it('should display coordinates in error mode', () => {
      component.webglError = true;
      component.positionX = 30;
      component.positionY = 70;
      fixture.detectChanges();

      const el = fixture.nativeElement as HTMLElement;
      const text = el.textContent || '';
      expect(text).toContain('30');
      expect(text).toContain('70');
    });
  });

  describe('when webglError is false', () => {
    it('should render container div', () => {
      component.webglError = false;
      fixture.detectChanges();

      const el = fixture.nativeElement as HTMLElement;
      const container = el.querySelector('.map-picker-3d');
      expect(container).toBeTruthy();
    });

    it('should display coords section', () => {
      component.webglError = false;
      fixture.detectChanges();

      const el = fixture.nativeElement as HTMLElement;
      const coordsEl = el.querySelector('.map-picker-3d__coords');
      expect(coordsEl).toBeTruthy();
    });
  });
});

