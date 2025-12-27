import { TestBed } from '@angular/core/testing';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';
import { ChatbotService, ChatRequest, ChatResponse } from './chatbot.service';
import { AuthService } from '../auth/auth.service';
import { of } from 'rxjs';
import { HttpHeaders } from '@angular/common/http';

describe('ChatbotService', () => {
  let service: ChatbotService;
  let httpMock: HttpTestingController;
  let mockAuthService: jasmine.SpyObj<AuthService>;

  beforeEach(() => {
    mockAuthService = jasmine.createSpyObj('AuthService', ['ensureCsrfToken']);
    mockAuthService.ensureCsrfToken.and.returnValue(of(new HttpHeaders()));

    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        ChatbotService,
        { provide: AuthService, useValue: mockAuthService }
      ]
    });

    service = TestBed.inject(ChatbotService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should send message and receive response', () => {
    const request: ChatRequest = {
      message: 'Create a discount',
      history: []
    };

    const mockResponse: ChatResponse = {
      message: 'Discount created',
      success: true
    };

    service.sendMessage(request).subscribe(response => {
      expect(response.message).toBe('Discount created');
      expect(response.success).toBeTrue();
    });

    const req = httpMock.expectOne(r => r.url.includes('/chatbot/message'));
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(request);
    req.flush(mockResponse);
  });

  it('should send message with pending action', () => {
    const request: ChatRequest = {
      message: 'yes',
      history: [{ role: 'user', content: 'delete discount' }],
      pendingAction: {
        actionType: 'DELETE_DISCOUNT',
        params: { id: 1 },
        confirmationMessage: 'Delete discount?'
      }
    };

    const mockResponse: ChatResponse = {
      message: 'Discount deleted',
      success: true
    };

    service.sendMessage(request).subscribe(response => {
      expect(response.success).toBeTrue();
    });

    const req = httpMock.expectOne(r => r.url.includes('/chatbot/message'));
    expect(req.request.body.pendingAction).toBeDefined();
    req.flush(mockResponse);
  });

  it('should handle response with pending action', () => {
    const request: ChatRequest = { message: 'delete discount 1' };

    const mockResponse: ChatResponse = {
      message: 'Are you sure?',
      success: true,
      pendingAction: {
        actionType: 'DELETE_DISCOUNT',
        params: { id: 1 },
        confirmationMessage: 'Confirm deletion?'
      }
    };

    service.sendMessage(request).subscribe(response => {
      expect(response.pendingAction).toBeDefined();
      expect(response.pendingAction?.actionType).toBe('DELETE_DISCOUNT');
    });

    const req = httpMock.expectOne(r => r.url.includes('/chatbot/message'));
    req.flush(mockResponse);
  });
});

