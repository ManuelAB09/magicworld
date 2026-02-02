import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { TranslateModule } from '@ngx-translate/core';
import { ReviewListComponent } from './review-list';
import { ReviewService, ReviewDTO, ReviewPage } from './review.service';
import { ProfileService, PurchaseDTO } from '../profile/profile.service';
import { AuthService, Role } from '../auth/auth.service';
import { ErrorService } from '../error/error-service';
import { of, throwError, Subject } from 'rxjs';

describe('ReviewListComponent', () => {
  let component: ReviewListComponent;
  let fixture: ComponentFixture<ReviewListComponent>;
  let mockReviewService: jasmine.SpyObj<ReviewService>;
  let mockProfileService: jasmine.SpyObj<ProfileService>;
  let mockAuthService: jasmine.SpyObj<AuthService>;
  let mockErrorService: jasmine.SpyObj<ErrorService>;

  const mockReviews: ReviewDTO[] = [
    { id: 1, stars: 4.5, publicationDate: '2026-01-15', visitDate: '2026-01-10', description: 'Great experience', username: 'user1', purchaseId: 1 },
    { id: 2, stars: 5, publicationDate: '2026-01-20', visitDate: '2026-01-18', description: 'Amazing!', username: 'user2', purchaseId: 2 }
  ];

  const mockReviewPage: ReviewPage = {
    content: mockReviews,
    totalElements: 2,
    totalPages: 1,
    number: 0,
    size: 5
  };

  const mockPurchases: PurchaseDTO[] = [
    { id: 1, purchaseDate: '2026-01-01', lines: [{ id: 1, validDate: '2026-01-10', quantity: 2, totalCost: 59.80, ticketTypeName: 'Adult' }] }
  ];

  beforeEach(async () => {
    mockReviewService = jasmine.createSpyObj('ReviewService', ['findAllPaginated', 'getAvailablePurchases', 'create']);
    mockProfileService = jasmine.createSpyObj('ProfileService', ['getMyPurchases']);
    mockAuthService = jasmine.createSpyObj('AuthService', ['checkRoleSecure'], {
      authChanged: new Subject<boolean>()
    });
    mockErrorService = jasmine.createSpyObj('ErrorService', ['handleError', 'getValidationMessages']);

    mockReviewService.findAllPaginated.and.returnValue(of(mockReviewPage));
    mockReviewService.getAvailablePurchases.and.returnValue(of([1]));
    mockProfileService.getMyPurchases.and.returnValue(of(mockPurchases));
    mockAuthService.checkRoleSecure.and.returnValue(of(Role.USER));
    mockErrorService.handleError.and.returnValue({ code: 'error.test', args: {} });
    mockErrorService.getValidationMessages.and.returnValue([]);

    await TestBed.configureTestingModule({
      imports: [
        ReviewListComponent,
        HttpClientTestingModule,
        TranslateModule.forRoot()
      ],
      providers: [
        { provide: ReviewService, useValue: mockReviewService },
        { provide: ProfileService, useValue: mockProfileService },
        { provide: AuthService, useValue: mockAuthService },
        { provide: ErrorService, useValue: mockErrorService }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(ReviewListComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should load reviews on init', fakeAsync(() => {
    fixture.detectChanges();
    tick();
    expect(component.reviews.length).toBe(2);
    expect(component.reviews[0].username).toBe('user1');
  }));

  it('should set isAuthenticated when user is logged in', fakeAsync(() => {
    fixture.detectChanges();
    tick();
    expect(component.isAuthenticated).toBeTrue();
  }));

  it('should load available purchases for authenticated user', fakeAsync(() => {
    fixture.detectChanges();
    tick();
    expect(mockReviewService.getAvailablePurchases).toHaveBeenCalled();
    expect(component.availablePurchases.length).toBe(1);
  }));

  it('should handle pagination correctly', fakeAsync(() => {
    fixture.detectChanges();
    tick();
    expect(component.totalPages).toBe(1);
    expect(component.currentPage).toBe(0);
  }));

  it('should open create form', fakeAsync(() => {
    fixture.detectChanges();
    tick();
    component.openCreateForm();
    expect(component.showForm).toBeTrue();
    expect(component.formStars).toBe(5);
    expect(component.formDescription).toBe('');
  }));

  it('should close form', fakeAsync(() => {
    fixture.detectChanges();
    tick();
    component.showForm = true;
    component.closeForm();
    expect(component.showForm).toBeFalse();
  }));

  it('should set stars correctly', () => {
    component.setStars(3);
    expect(component.formStars).toBe(3);
  });

  it('should return correct stars array', () => {
    expect(component.getStarsArray(4).length).toBe(4);
    expect(component.getEmptyStarsArray(4).length).toBe(1);
  });

  it('should handle goToPage correctly', fakeAsync(() => {
    fixture.detectChanges();
    tick();
    component.totalPages = 3;

    const page1Response: ReviewPage = {
      content: mockReviews,
      totalElements: 2,
      totalPages: 3,
      number: 1,
      size: 5
    };
    mockReviewService.findAllPaginated.and.returnValue(of(page1Response));

    component.goToPage(1);
    tick();
    expect(component.currentPage).toBe(1);
    expect(mockReviewService.findAllPaginated).toHaveBeenCalled();
  }));

  it('should not go to invalid page', fakeAsync(() => {
    fixture.detectChanges();
    tick();
    component.totalPages = 1;
    component.currentPage = 0;
    component.goToPage(-1);
    expect(component.currentPage).toBe(0);
    component.goToPage(5);
    expect(component.currentPage).toBe(0);
  }));

  it('should have page size options', () => {
    expect(component.pageSizeOptions).toEqual([5, 10, 20, 50]);
  });

  it('should change page size and reset to first page', fakeAsync(() => {
    fixture.detectChanges();
    tick();
    component.currentPage = 2;
    component.onPageSizeChange(20);
    tick();
    expect(component.pageSize).toBe(20);
    expect(component.currentPage).toBe(0);
    expect(mockReviewService.findAllPaginated).toHaveBeenCalledWith(0, 20);
  }));

  it('should handle API error gracefully', fakeAsync(() => {
    mockReviewService.findAllPaginated.and.returnValue(throwError(() => new Error('API Error')));
    fixture.detectChanges();
    tick();
    expect(component.reviews.length).toBe(0);
  }));

  it('should not show form when not authenticated', fakeAsync(() => {
    mockAuthService.checkRoleSecure.and.returnValue(throwError(() => new Error('Not authenticated')));
    fixture.detectChanges();
    tick();
    expect(component.isAuthenticated).toBeFalse();
    expect(component.availablePurchases.length).toBe(0);
  }));

  it('should select purchase correctly', fakeAsync(() => {
    fixture.detectChanges();
    tick();
    component.onPurchaseSelect(1);
    expect(component.selectedPurchaseId).toBe(1);
    expect(component.selectedVisitDate).toBe('2026-01-10');
  }));

  it('should submit review successfully', fakeAsync(() => {
    const newReview: ReviewDTO = { id: 3, stars: 5, publicationDate: '2026-02-01', visitDate: '2026-01-25', description: 'Excellent!', username: 'user1', purchaseId: 1 };
    mockReviewService.create.and.returnValue(of(newReview));

    fixture.detectChanges();
    tick();

    component.selectedPurchaseId = 1;
    component.selectedVisitDate = '2026-01-25';
    component.formStars = 5;
    component.formDescription = 'Excellent!';
    component.showForm = true;

    component.submitReview();
    tick();

    expect(mockReviewService.create).toHaveBeenCalled();
    expect(component.showForm).toBeFalse();
  }));

  it('should not submit review without description', fakeAsync(() => {
    fixture.detectChanges();
    tick();

    component.selectedPurchaseId = 1;
    component.selectedVisitDate = '2026-01-25';
    component.formDescription = '';

    component.submitReview();

    expect(mockReviewService.create).not.toHaveBeenCalled();
  }));
});
