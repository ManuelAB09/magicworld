import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ParkMapComponent } from './park-map';
import { AttractionApiService, Attraction } from '../attraction/attraction.service';
import { Router } from '@angular/router';
import { TranslateModule } from '@ngx-translate/core';
import { of, throwError } from 'rxjs';
import { ElementRef } from '@angular/core';

describe('ParkMapComponent', () => {
  let component: ParkMapComponent;
  let fixture: ComponentFixture<ParkMapComponent>;
  let mockAttractionService: jasmine.SpyObj<AttractionApiService>;
  let mockRouter: jasmine.SpyObj<Router>;

  const mockAttractions: Attraction[] = [
    {
      id: 1, name: 'Test Ride', intensity: 'HIGH', category: 'ROLLER_COASTER',
      minimumHeight: 140, minimumAge: 12, minimumWeight: 40,
      description: 'A thrilling ride', photoUrl: '/images/ride.jpg',
      isActive: true, mapPositionX: 50, mapPositionY: 50
    }
  ];

  beforeEach(async () => {
    mockAttractionService = jasmine.createSpyObj('AttractionApiService', ['findAll']);
    mockRouter = jasmine.createSpyObj('Router', ['navigate']);
    mockAttractionService.findAll.and.returnValue(of(mockAttractions));

    await TestBed.configureTestingModule({
      imports: [ParkMapComponent, TranslateModule.forRoot()],
      providers: [
        { provide: AttractionApiService, useValue: mockAttractionService },
        { provide: Router, useValue: mockRouter }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(ParkMapComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should load attractions on init', () => {
    fixture.detectChanges();
    expect(mockAttractionService.findAll).toHaveBeenCalled();
    expect(component.attractions.length).toBe(1);
  });

  it('should set loading to false after attractions load', () => {
    fixture.detectChanges();
    expect(component.loading).toBeFalse();
  });

  it('should filter only active attractions', () => {
    const mixedAttractions = [
      { ...mockAttractions[0], isActive: true },
      { ...mockAttractions[0], id: 2, isActive: false }
    ];
    mockAttractionService.findAll.and.returnValue(of(mixedAttractions));

    fixture.detectChanges();

    expect(component.attractions.length).toBe(1);
  });

  it('should set error on service failure', () => {
    mockAttractionService.findAll.and.returnValue(throwError(() => new Error('Network error')));

    fixture.detectChanges();

    expect(component.error).toBeTrue();
    expect(component.loading).toBeFalse();
  });

  it('should navigate to attraction detail on click', () => {
    component.hoveredAttraction = mockAttractions[0];
    component.onClick();
    expect(mockRouter.navigate).toHaveBeenCalledWith(['/attraction', 1]);
  });

  it('should not navigate if no hovered attraction', () => {
    component.hoveredAttraction = null;
    component.onClick();
    expect(mockRouter.navigate).not.toHaveBeenCalled();
  });

  it('should return image url', () => {
    const url = component.getImageUrl('/images/test.jpg');
    expect(url).toBeTruthy();
  });

  it('should return empty string for null image url', () => {
    const url = component.getImageUrl(null as any);
    expect(url).toBe('');
  });

  it('should call zoomIn on sceneManager', () => {
    spyOn(component['sceneManager'], 'zoomIn');
    component.zoomIn();
    expect(component['sceneManager'].zoomIn).toHaveBeenCalled();
  });

  it('should call zoomOut on sceneManager', () => {
    spyOn(component['sceneManager'], 'zoomOut');
    component.zoomOut();
    expect(component['sceneManager'].zoomOut).toHaveBeenCalled();
  });

  it('should call resetView on sceneManager', () => {
    spyOn(component['sceneManager'], 'resetView');
    component.resetView();
    expect(component['sceneManager'].resetView).toHaveBeenCalled();
  });

  it('should dispose sceneManager on destroy', () => {
    spyOn(component['sceneManager'], 'dispose');
    component.ngOnDestroy();
    expect(component['sceneManager'].dispose).toHaveBeenCalled();
  });

  it('should initialize with loading true', () => {
    expect(component.loading).toBeTrue();
  });

  it('should initialize with error false', () => {
    expect(component.error).toBeFalse();
  });

  it('should initialize with webglError false', () => {
    expect(component.webglError).toBeFalse();
  });

  it('should initialize with empty attractions array', () => {
    expect(component.attractions).toEqual([]);
  });
});
