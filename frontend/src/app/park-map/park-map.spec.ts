import { ComponentFixture, TestBed, fakeAsync, tick, discardPeriodicTasks } from '@angular/core/testing';
import { ParkMapComponent } from './park-map';
import { AttractionApiService, Attraction } from '../attraction/attraction.service';
import { ParkStatusService } from './park-status.service';
import { MapMonitoringService } from './map-monitoring.service';
import { Router } from '@angular/router';
import { TranslateModule } from '@ngx-translate/core';
import { of, throwError } from 'rxjs';
import { ElementRef } from '@angular/core';
import { AttractionStatus } from '../admin-dashboard/monitoring.service';
import * as THREE from 'three';

describe('ParkMapComponent', () => {
  let component: ParkMapComponent;
  let fixture: ComponentFixture<ParkMapComponent>;
  let mockAttractionService: jasmine.SpyObj<AttractionApiService>;
  let mockParkStatusService: jasmine.SpyObj<ParkStatusService>;
  let mockMapMonitoringService: jasmine.SpyObj<MapMonitoringService>;
  let mockRouter: jasmine.SpyObj<Router>;

  const mockAttractions: Attraction[] = [
    {
      id: 1, name: 'Test Ride', intensity: 'HIGH', category: 'ROLLER_COASTER',
      minimumHeight: 140, minimumAge: 12, minimumWeight: 40,
      description: 'A thrilling ride', photoUrl: '/images/ride.jpg',
      isActive: true, mapPositionX: 50, mapPositionY: 50
    }
  ];

  const mockStatus: AttractionStatus = {
    attractionId: 1, name: 'Test Ride', isOpen: true,
    queueSize: 20, estimatedWaitMinutes: 10,
    mapPositionX: 50, mapPositionY: 50, intensity: 'HIGH'
  };

  beforeEach(async () => {
    mockAttractionService = jasmine.createSpyObj('AttractionApiService', ['findAll']);
    mockRouter = jasmine.createSpyObj('Router', ['navigate']);
    mockParkStatusService = jasmine.createSpyObj('ParkStatusService', [
      'getSimulatorStatus', 'getAttractionStatuses'
    ]);
    mockMapMonitoringService = jasmine.createSpyObj('MapMonitoringService', [
      'createHeatCircle', 'updateHeatCircle', 'clearAll', 'createQueueIndicator', 'updateQueueIndicator'
    ]);
    mockAttractionService.findAll.and.returnValue(of(mockAttractions));
    mockParkStatusService.getSimulatorStatus.and.returnValue(of({ running: false, simulatedVisitors: 0, activeQueues: 0, totalInQueues: 0 }));
    mockParkStatusService.getAttractionStatuses.and.returnValue(of([mockStatus]));
    mockMapMonitoringService.createHeatCircle.and.returnValue(new THREE.Mesh());

    await TestBed.configureTestingModule({
      imports: [ParkMapComponent, TranslateModule.forRoot()],
      providers: [
        { provide: AttractionApiService, useValue: mockAttractionService },
        { provide: ParkStatusService, useValue: mockParkStatusService },
        { provide: MapMonitoringService, useValue: mockMapMonitoringService },
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
    expect(mockMapMonitoringService.clearAll).toHaveBeenCalled();
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

  // --- onResize ---
  it('should handle onResize without mapContainer', () => {
    component.mapContainer = undefined as any;
    expect(() => component.onResize()).not.toThrow();
  });

  it('should call handleResize on sceneManager on resize', () => {
    spyOn(component['sceneManager'], 'handleResize');
    const mockDiv = document.createElement('div');
    Object.defineProperty(mockDiv, 'clientWidth', { value: 800 });
    Object.defineProperty(mockDiv, 'clientHeight', { value: 600 });
    component.mapContainer = { nativeElement: mockDiv } as ElementRef<HTMLDivElement>;

    component.onResize();

    expect(component['sceneManager'].handleResize).toHaveBeenCalledWith(800, 600);
  });

  // --- checkSimulatorStatus with running=true ---
  it('should start polling when simulator is running', fakeAsync(() => {
    mockParkStatusService.getSimulatorStatus.and.returnValue(
      of({ running: true, simulatedVisitors: 50, activeQueues: 3, totalInQueues: 120 })
    );

    fixture.detectChanges();

    expect(component.simulatorRunning).toBeTrue();
    expect(mockParkStatusService.getAttractionStatuses).toHaveBeenCalled();

    component.ngOnDestroy();
    discardPeriodicTasks();
  }));

  // --- startPolling ---
  it('should start polling subscription when simulator is running', fakeAsync(() => {
    mockParkStatusService.getSimulatorStatus.and.returnValue(
      of({ running: true, simulatedVisitors: 50, activeQueues: 3, totalInQueues: 120 })
    );

    fixture.detectChanges();
    expect(component.simulatorRunning).toBeTrue();

    // pollSub should be created
    expect(component['pollSub']).toBeTruthy();

    component.ngOnDestroy();
    discardPeriodicTasks();
  }));

  it('should handle simulator status error during polling silently', fakeAsync(() => {
    mockParkStatusService.getSimulatorStatus.and.returnValue(
      of({ running: true, simulatedVisitors: 50, activeQueues: 3, totalInQueues: 120 })
    );

    fixture.detectChanges();

    // Now make subsequent calls return error
    mockParkStatusService.getSimulatorStatus.and.returnValue(throwError(() => new Error('Poll error')));
    tick(3100);

    // Should not throw - errors are silently ignored
    expect(component).toBeTruthy();

    component.ngOnDestroy();
    discardPeriodicTasks();
  }));


  // --- loadAttractionStatuses ---
  it('should populate attractionStatuses map', fakeAsync(() => {
    mockParkStatusService.getSimulatorStatus.and.returnValue(
      of({ running: true, simulatedVisitors: 50, activeQueues: 3, totalInQueues: 120 })
    );

    fixture.detectChanges();

    expect(component.attractionStatuses.get(1)).toBeTruthy();
    expect(component.attractionStatuses.get(1)!.queueSize).toBe(20);

    component.ngOnDestroy();
    discardPeriodicTasks();
  }));

  it('should handle loadAttractionStatuses error silently', fakeAsync(() => {
    mockParkStatusService.getSimulatorStatus.and.returnValue(
      of({ running: true, simulatedVisitors: 50, activeQueues: 3, totalInQueues: 120 })
    );
    mockParkStatusService.getAttractionStatuses.and.returnValue(throwError(() => new Error('err')));

    fixture.detectChanges();

    expect(component).toBeTruthy();

    component.ngOnDestroy();
    discardPeriodicTasks();
  }));

  // --- getQueueStatus ---
  it('should return null from getQueueStatus when no hovered attraction', () => {
    component.hoveredAttraction = null;
    component.simulatorRunning = true;
    expect(component.getQueueStatus()).toBeNull();
  });

  it('should return null from getQueueStatus when simulator not running', () => {
    component.hoveredAttraction = mockAttractions[0];
    component.simulatorRunning = false;
    expect(component.getQueueStatus()).toBeNull();
  });

  it('should return queue status for hovered attraction', () => {
    component.hoveredAttraction = mockAttractions[0];
    component.simulatorRunning = true;
    component.attractionStatuses.set(1, mockStatus);
    const status = component.getQueueStatus();
    expect(status).toBeTruthy();
    expect(status!.queueSize).toBe(20);
  });

  it('should return null when attraction status not found', () => {
    component.hoveredAttraction = { ...mockAttractions[0], id: 999 };
    component.simulatorRunning = true;
    expect(component.getQueueStatus()).toBeNull();
  });

  // --- initScene ---
  it('should set webglError when initialize fails', () => {
    spyOn(component['sceneManager'], 'initialize').and.returnValue(false);
    const mockDiv = document.createElement('div');
    component.mapContainer = { nativeElement: mockDiv } as ElementRef<HTMLDivElement>;

    (component as any).initScene();

    expect(component.webglError).toBeTrue();
    expect(component.loading).toBeFalse();
  });

  it('should not init scene without mapContainer', () => {
    component.mapContainer = undefined as any;
    expect(() => (component as any).initScene()).not.toThrow();
  });

  it('should start animation and add attractions on successful init', () => {
    spyOn(component['sceneManager'], 'initialize').and.returnValue(true);
    spyOn(component['sceneManager'], 'addAttractionsToScene');
    spyOn(component['sceneManager'], 'startAnimation');
    const mockDiv = document.createElement('div');
    component.mapContainer = { nativeElement: mockDiv } as ElementRef<HTMLDivElement>;
    component.attractions = mockAttractions;

    (component as any).initScene();

    expect(component['sceneManager'].addAttractionsToScene).toHaveBeenCalledWith(mockAttractions);
    expect(component['sceneManager'].startAnimation).toHaveBeenCalled();
  });

  it('should not add attractions to scene if none loaded yet', () => {
    spyOn(component['sceneManager'], 'initialize').and.returnValue(true);
    spyOn(component['sceneManager'], 'addAttractionsToScene');
    spyOn(component['sceneManager'], 'startAnimation');
    const mockDiv = document.createElement('div');
    component.mapContainer = { nativeElement: mockDiv } as ElementRef<HTMLDivElement>;
    component.attractions = [];

    (component as any).initScene();

    expect(component['sceneManager'].addAttractionsToScene).not.toHaveBeenCalled();
    expect(component['sceneManager'].startAnimation).toHaveBeenCalled();
  });

  // --- onMouseMove ---
  it('should return early from onMouseMove if scene not initialized', () => {
    spyOn(component['sceneManager'], 'isInitialized').and.returnValue(false);
    const event = new MouseEvent('mousemove');
    expect(() => component.onMouseMove(event)).not.toThrow();
  });

  it('should clear hovered attraction on mouse move with no intersects', () => {
    spyOn(component['sceneManager'], 'isInitialized').and.returnValue(true);
    spyOn(component['sceneManager'], 'getAttractionMeshes').and.returnValue([]);
    spyOn(component['sceneManager'], 'getCamera').and.returnValue(new THREE.PerspectiveCamera());
    spyOn(component['sceneManager'], 'setHoveredAttraction');

    const mockDiv = document.createElement('div');
    Object.defineProperty(mockDiv, 'getBoundingClientRect', {
      value: () => ({ left: 0, top: 0, width: 800, height: 600 })
    });
    component.mapContainer = { nativeElement: mockDiv } as ElementRef<HTMLDivElement>;

    component.hoveredAttraction = mockAttractions[0];
    const event = new MouseEvent('mousemove', { clientX: 400, clientY: 300 });
    component.onMouseMove(event);

    expect(component.hoveredAttraction).toBeNull();
    expect(component['sceneManager'].setHoveredAttraction).toHaveBeenCalledWith(null);
  });

  // --- unsubscribe on destroy ---
  it('should unsubscribe poll subscription on destroy', fakeAsync(() => {
    mockParkStatusService.getSimulatorStatus.and.returnValue(
      of({ running: true, simulatedVisitors: 50, activeQueues: 3, totalInQueues: 120 })
    );

    fixture.detectChanges();
    spyOn(component['sceneManager'], 'dispose');

    component.ngOnDestroy();
    discardPeriodicTasks();

    expect(component['sceneManager'].dispose).toHaveBeenCalled();
  }));
});
