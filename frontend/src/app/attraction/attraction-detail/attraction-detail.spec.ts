import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { AttractionDetailComponent } from './attraction-detail';
import { AttractionApiService, Attraction } from '../attraction.service';
import { ActivatedRoute, Router } from '@angular/router';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { of, throwError } from 'rxjs';

describe('AttractionDetailComponent', () => {
  let component: AttractionDetailComponent;
  let fixture: ComponentFixture<AttractionDetailComponent>;
  let mockAttractionService: jasmine.SpyObj<AttractionApiService>;
  let mockRouter: jasmine.SpyObj<Router>;

  const mockAttraction: Attraction = {
    id: 1,
    name: 'Test Ride',
    intensity: 'HIGH',
    category: 'ROLLER_COASTER',
    minimumHeight: 140,
    minimumAge: 12,
    minimumWeight: 40,
    description: 'A thrilling ride',
    photoUrl: '/images/ride.jpg',
    isActive: true,
    mapPositionX: 50,
    mapPositionY: 50
  };

  beforeEach(async () => {
    mockAttractionService = jasmine.createSpyObj('AttractionApiService', ['findById']);
    mockRouter = jasmine.createSpyObj('Router', ['navigate']);
    mockAttractionService.findById.and.returnValue(of(mockAttraction));

    await TestBed.configureTestingModule({
      imports: [AttractionDetailComponent, TranslateModule.forRoot()],
      providers: [
        { provide: AttractionApiService, useValue: mockAttractionService },
        { provide: Router, useValue: mockRouter },
        {
          provide: ActivatedRoute,
          useValue: { snapshot: { paramMap: { get: () => '1' } } }
        }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(AttractionDetailComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should call findById on init', () => {
    fixture.detectChanges();
    expect(mockAttractionService.findById).toHaveBeenCalledWith(1);
  });

  it('should navigate to park-map on goBack', () => {
    component.goBack();
    expect(mockRouter.navigate).toHaveBeenCalledWith(['/park-map']);
  });

  it('should navigate to attractions on goToAttractions', () => {
    component.goToAttractions();
    expect(mockRouter.navigate).toHaveBeenCalledWith(['/attractions']);
  });

  it('should return intensity-high class for HIGH intensity', () => {
    component.attraction = { ...mockAttraction, intensity: 'HIGH' };
    expect(component.getIntensityClass()).toBe('intensity-high');
  });

  it('should return intensity-medium class for MEDIUM intensity', () => {
    component.attraction = { ...mockAttraction, intensity: 'MEDIUM' };
    expect(component.getIntensityClass()).toBe('intensity-medium');
  });

  it('should return intensity-low class for LOW intensity', () => {
    component.attraction = { ...mockAttraction, intensity: 'LOW' };
    expect(component.getIntensityClass()).toBe('intensity-low');
  });

  it('should return empty string for intensity class when no attraction', () => {
    component.attraction = null;
    expect(component.getIntensityClass()).toBe('');
  });

  it('should return empty for intensity label when no attraction', () => {
    component.attraction = null;
    expect(component.getIntensityLabel()).toBe('');
  });

  it('should return empty for category label when no attraction', () => {
    component.attraction = null;
    expect(component.getCategoryLabel()).toBe('');
  });

  it('should return image url', () => {
    const url = component.getImageUrl('/images/test.jpg');
    expect(url).toBeTruthy();
  });

  it('should return empty string for null image url', () => {
    const url = component.getImageUrl(null as any);
    expect(url).toBe('');
  });

  it('should initialize with loading true', () => {
    expect(component.loading).toBeTrue();
  });

  it('should initialize with error false', () => {
    expect(component.error).toBeFalse();
  });
});
