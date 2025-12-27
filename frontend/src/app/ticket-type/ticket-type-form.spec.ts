import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { TicketTypeForm } from './ticket-type-form';
import { TicketTypeApiService, TicketType } from './ticket-type.service';
import { ErrorService } from '../error/error-service';
import { TranslateModule } from '@ngx-translate/core';
import { provideRouter, ActivatedRoute } from '@angular/router';
import { of, throwError } from 'rxjs';
import { ReactiveFormsModule } from '@angular/forms';
import { Component } from '@angular/core';

@Component({ template: '', standalone: true })
class DummyComponent {}

describe('TicketTypeForm', () => {
  let component: TicketTypeForm;
  let fixture: ComponentFixture<TicketTypeForm>;
  let mockApiService: jasmine.SpyObj<TicketTypeApiService>;
  let mockErrorService: jasmine.SpyObj<ErrorService>;

  const mockTicketType: TicketType = {
    id: 1,
    typeName: 'Adult',
    description: 'Adult ticket',
    cost: 50,
    currency: 'EUR',
    maxPerDay: 100,
    photoUrl: '/images/adult.jpg'
  };

  beforeEach(async () => {
    mockApiService = jasmine.createSpyObj('TicketTypeApiService', ['findById', 'createMultipart', 'updateMultipart', 'delete']);
    mockErrorService = jasmine.createSpyObj('ErrorService', ['handleError', 'getValidationMessages']);
    mockErrorService.handleError.and.returnValue({ code: 'error.test', args: {} });
    mockErrorService.getValidationMessages.and.returnValue([]);

    await TestBed.configureTestingModule({
      imports: [TicketTypeForm, TranslateModule.forRoot(), ReactiveFormsModule],
      providers: [
        provideRouter([]),
        { provide: TicketTypeApiService, useValue: mockApiService },
        { provide: ErrorService, useValue: mockErrorService },
        {
          provide: ActivatedRoute,
          useValue: {
            snapshot: { paramMap: { get: () => null } }
          }
        }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(TicketTypeForm);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize form with default values for new ticket type', fakeAsync(() => {
    fixture.detectChanges();
    tick();
    expect(component.isEdit).toBeFalse();
    expect(component.form.get('typeName')?.value).toBe('');
    expect(component.form.get('description')?.value).toBe('');
  }));

  it('should have required validators on form fields', () => {
    fixture.detectChanges();
    expect(component.form.get('typeName')?.hasError('required')).toBeTrue();
    expect(component.form.get('description')?.hasError('required')).toBeTrue();
    expect(component.form.get('currency')?.hasError('required')).toBeTrue();
    expect(component.form.get('cost')?.hasError('required')).toBeTrue();
    expect(component.form.get('maxPerDay')?.hasError('required')).toBeTrue();
  });

  it('should validate cost minimum value', () => {
    fixture.detectChanges();
    component.form.patchValue({ cost: 0 });
    expect(component.form.get('cost')?.hasError('min')).toBeTrue();
  });

  it('should validate maxPerDay minimum value', () => {
    fixture.detectChanges();
    component.form.patchValue({ maxPerDay: 0 });
    expect(component.form.get('maxPerDay')?.hasError('min')).toBeTrue();
  });

  it('should not submit if form is invalid', () => {
    fixture.detectChanges();
    component.submit();
    expect(mockApiService.createMultipart).not.toHaveBeenCalled();
  });

  it('should show photo required error when no file selected for new ticket type', () => {
    fixture.detectChanges();
    component.form.patchValue({
      typeName: 'Test',
      description: 'Test desc',
      currency: 'EUR',
      cost: 50,
      maxPerDay: 100
    });
    component.submit();
    expect(component.photoRequiredError).toBeTrue();
  });

  it('should return correct image URL for relative path', () => {
    const result = component.getImageUrl('/images/test.jpg');
    expect(result).toContain('/images/test.jpg');
  });

  it('should return same URL for absolute path', () => {
    const result = component.getImageUrl('https://example.com/image.jpg');
    expect(result).toBe('https://example.com/image.jpg');
  });

  it('should return null for null image URL', () => {
    expect(component.getImageUrl(null)).toBeNull();
  });

  describe('Edit mode', () => {
    beforeEach(async () => {
      await TestBed.resetTestingModule();
      mockApiService = jasmine.createSpyObj('TicketTypeApiService', ['findById', 'createMultipart', 'updateMultipart', 'delete']);
      mockErrorService = jasmine.createSpyObj('ErrorService', ['handleError', 'getValidationMessages']);
      mockApiService.findById.and.returnValue(of(mockTicketType));
      mockErrorService.handleError.and.returnValue({ code: 'error.test', args: {} });
      mockErrorService.getValidationMessages.and.returnValue([]);

      await TestBed.configureTestingModule({
        imports: [TicketTypeForm, TranslateModule.forRoot(), ReactiveFormsModule],
        providers: [
          provideRouter([{ path: 'ticket-types', component: DummyComponent }]),
          { provide: TicketTypeApiService, useValue: mockApiService },
          { provide: ErrorService, useValue: mockErrorService },
          {
            provide: ActivatedRoute,
            useValue: {
              snapshot: { paramMap: { get: () => '1' } }
            }
          }
        ]
      }).compileComponents();

      fixture = TestBed.createComponent(TicketTypeForm);
      component = fixture.componentInstance;
    });

    it('should load existing ticket type in edit mode', fakeAsync(() => {
      fixture.detectChanges();
      tick();
      expect(component.isEdit).toBeTrue();
      expect(component.id).toBe(1);
      expect(component.form.get('typeName')?.value).toBe('Adult');
    }));

    it('should call updateMultipart on submit in edit mode', fakeAsync(() => {
      mockApiService.updateMultipart.and.returnValue(of(mockTicketType));
      fixture.detectChanges();
      tick();
      component.submit();
      tick();
      expect(mockApiService.updateMultipart).toHaveBeenCalled();
    }));

    it('should delete ticket type when confirmed', fakeAsync(() => {
      spyOn(window, 'confirm').and.returnValue(true);
      mockApiService.delete.and.returnValue(of(void 0));
      fixture.detectChanges();
      tick();
      component.delete();
      tick();
      expect(mockApiService.delete).toHaveBeenCalledWith(1);
    }));

    it('should not delete ticket type when cancelled', fakeAsync(() => {
      spyOn(window, 'confirm').and.returnValue(false);
      fixture.detectChanges();
      tick();
      component.delete();
      tick();
      expect(mockApiService.delete).not.toHaveBeenCalled();
    }));

    it('should handle delete error', fakeAsync(() => {
      spyOn(window, 'confirm').and.returnValue(true);
      mockApiService.delete.and.returnValue(throwError(() => ({ error: { code: 'error.delete' } })));
      fixture.detectChanges();
      tick();
      component.delete();
      tick();
      expect(component.errorKey).toBe('error.test');
    }));

    it('should handle error when loading ticket type fails', fakeAsync(() => {
      mockApiService.findById.and.returnValue(throwError(() => ({ error: { code: 'error.load' } })));
      fixture.detectChanges();
      tick();
      expect(component.errorKey).toBe('error.test');
    }));
  });

  describe('File handling', () => {
    beforeEach(() => {
      fixture.detectChanges();
    });

    it('should clear file selection when no files provided', () => {
      component.selectedFile = new File([''], 'test.jpg');
      component.previewUrl = 'test-url';

      const event = { target: { files: null } } as unknown as Event;
      component.onFileChange(event);

      expect(component.selectedFile).toBeNull();
      expect(component.previewUrl).toBeNull();
    });

    it('should clear file selection when empty file list', () => {
      component.selectedFile = new File([''], 'test.jpg');
      component.previewUrl = 'test-url';

      const event = { target: { files: [] } } as unknown as Event;
      component.onFileChange(event);

      expect(component.selectedFile).toBeNull();
      expect(component.previewUrl).toBeNull();
    });

    it('should reject non-image files', () => {
      const file = new File([''], 'test.txt', { type: 'text/plain' });
      const event = { target: { files: [file] } } as unknown as Event;

      component.onFileChange(event);

      expect(component.selectedFile).toBeNull();
      expect(component.photoRequiredError).toBeTrue();
    });

    it('should reject files larger than max size', () => {
      const largeContent = new Array(60 * 1024 * 1024).fill('x').join('');
      const file = new File([largeContent], 'large.jpg', { type: 'image/jpeg' });
      Object.defineProperty(file, 'size', { value: 60 * 1024 * 1024 });
      const event = { target: { files: [file] } } as unknown as Event;

      component.onFileChange(event);

      expect(component.selectedFile).toBeNull();
      expect(component.errorKey).toBe('error.file.size_exceeded');
    });

    it('should accept valid image file', fakeAsync(() => {
      const file = new File(['test'], 'test.jpg', { type: 'image/jpeg' });
      const event = { target: { files: [file] } } as unknown as Event;

      component.onFileChange(event);
      tick();

      expect(component.selectedFile).toBe(file);
      expect(component.photoRequiredError).toBeFalse();
    }));
  });

  describe('Submit with file', () => {
    beforeEach(async () => {
      TestBed.resetTestingModule();
      mockApiService = jasmine.createSpyObj('TicketTypeApiService', ['findById', 'createMultipart', 'updateMultipart', 'delete']);
      mockErrorService = jasmine.createSpyObj('ErrorService', ['handleError', 'getValidationMessages']);
      mockErrorService.handleError.and.returnValue({ code: 'error.test', args: {} });
      mockErrorService.getValidationMessages.and.returnValue([]);

      await TestBed.configureTestingModule({
        imports: [TicketTypeForm, TranslateModule.forRoot(), ReactiveFormsModule],
        providers: [
          provideRouter([{ path: 'ticket-types', component: DummyComponent }]),
          { provide: TicketTypeApiService, useValue: mockApiService },
          { provide: ErrorService, useValue: mockErrorService },
          {
            provide: ActivatedRoute,
            useValue: {
              snapshot: { paramMap: { get: () => null } }
            }
          }
        ]
      }).compileComponents();

      fixture = TestBed.createComponent(TicketTypeForm);
      component = fixture.componentInstance;
      fixture.detectChanges();
    });

    it('should submit with file for new ticket type', fakeAsync(() => {
      mockApiService.createMultipart.and.returnValue(of(mockTicketType));

      component.form.patchValue({
        typeName: 'Test',
        description: 'Test desc',
        currency: 'EUR',
        cost: 50,
        maxPerDay: 100
      });
      component.selectedFile = new File(['test'], 'test.jpg', { type: 'image/jpeg' });

      component.submit();
      tick();

      expect(mockApiService.createMultipart).toHaveBeenCalled();
    }));

    it('should handle submit error', fakeAsync(() => {
      mockApiService.createMultipart.and.returnValue(throwError(() => ({ error: { code: 'error.create' } })));

      component.form.patchValue({
        typeName: 'Test',
        description: 'Test desc',
        currency: 'EUR',
        cost: 50,
        maxPerDay: 100
      });
      component.selectedFile = new File(['test'], 'test.jpg', { type: 'image/jpeg' });

      component.submit();
      tick();

      expect(component.errorKey).toBe('error.test');
      expect(component.loading).toBeFalse();
    }));
  });

  it('should not delete in create mode', () => {
    fixture.detectChanges();
    component.isEdit = false;
    component.delete();
    expect(mockApiService.delete).not.toHaveBeenCalled();
  });

  it('should validate typeName maxLength', () => {
    fixture.detectChanges();
    component.form.patchValue({ typeName: 'a'.repeat(51) });
    expect(component.form.get('typeName')?.hasError('maxlength')).toBeTrue();
  });

  it('should validate description maxLength', () => {
    fixture.detectChanges();
    component.form.patchValue({ description: 'a'.repeat(256) });
    expect(component.form.get('description')?.hasError('maxlength')).toBeTrue();
  });
});

