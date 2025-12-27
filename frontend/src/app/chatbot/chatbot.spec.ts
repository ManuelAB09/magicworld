import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { ChatbotComponent } from './chatbot';
import { ChatbotService, ChatResponse } from './chatbot.service';
import { TranslateModule } from '@ngx-translate/core';
import { of, throwError } from 'rxjs';
import { FormsModule } from '@angular/forms';

describe('ChatbotComponent', () => {
  let component: ChatbotComponent;
  let fixture: ComponentFixture<ChatbotComponent>;
  let mockChatbotService: jasmine.SpyObj<ChatbotService>;

  const mockResponse: ChatResponse = {
    message: '✅ Discount created successfully',
    success: true
  };

  beforeEach(async () => {
    mockChatbotService = jasmine.createSpyObj('ChatbotService', ['sendMessage']);
    mockChatbotService.sendMessage.and.returnValue(of(mockResponse));

    await TestBed.configureTestingModule({
      imports: [ChatbotComponent, TranslateModule.forRoot(), FormsModule],
      providers: [
        { provide: ChatbotService, useValue: mockChatbotService }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(ChatbotComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should have initial welcome message', () => {
    expect(component.messages.length).toBe(1);
    expect(component.messages[0].role).toBe('assistant');
  });

  it('should initialize with empty input', () => {
    expect(component.inputMessage).toBe('');
  });

  it('should not be loading initially', () => {
    expect(component.isLoading).toBeFalse();
  });

  it('should have no pending action initially', () => {
    expect(component.pendingAction).toBeNull();
  });

  it('should have empty activity log initially', () => {
    expect(component.activityLog.length).toBe(0);
  });

  it('should not send empty message', fakeAsync(() => {
    component.inputMessage = '   ';
    component.sendMessage();
    tick();
    expect(mockChatbotService.sendMessage).not.toHaveBeenCalled();
  }));

  it('should not send message while loading', fakeAsync(() => {
    component.isLoading = true;
    component.inputMessage = 'test message';
    component.sendMessage();
    tick();
    expect(mockChatbotService.sendMessage).not.toHaveBeenCalled();
  }));

  it('should send message and receive response', fakeAsync(() => {
    component.inputMessage = 'Create a discount';
    component.sendMessage();
    tick();

    expect(mockChatbotService.sendMessage).toHaveBeenCalled();
    expect(component.messages.length).toBeGreaterThan(1);
    expect(component.isLoading).toBeFalse();
  }));

  it('should add to activity log on successful response', fakeAsync(() => {
    component.inputMessage = 'Create a discount';
    component.sendMessage();
    tick();

    expect(component.activityLog.length).toBe(1);
    expect(component.activityLog[0].success).toBeTrue();
  }));

  it('should handle response with pending action', fakeAsync(() => {
    const responseWithPendingAction: ChatResponse = {
      message: '⚠️ Please confirm deletion',
      success: true,
      pendingAction: {
        actionType: 'DELETE_DISCOUNT',
        params: { id: 1 },
        confirmationMessage: 'Are you sure?'
      }
    };
    mockChatbotService.sendMessage.and.returnValue(of(responseWithPendingAction));

    component.inputMessage = 'Delete discount 1';
    component.sendMessage();
    tick();

    expect(component.pendingAction).toBeDefined();
    expect(component.pendingAction?.actionType).toBe('DELETE_DISCOUNT');
  }));

  it('should handle error response', fakeAsync(() => {
    mockChatbotService.sendMessage.and.returnValue(throwError(() => ({ status: 500, message: 'Server error' })));

    component.inputMessage = 'Create a discount';
    component.sendMessage();
    tick();

    expect(component.isLoading).toBeFalse();
    expect(component.activityLog[0].success).toBeFalse();
  }));

  it('should handle 401 error', fakeAsync(() => {
    mockChatbotService.sendMessage.and.returnValue(throwError(() => ({ status: 401 })));

    component.inputMessage = 'test';
    component.sendMessage();
    tick();

    const lastMessage = component.messages[component.messages.length - 1];
    expect(lastMessage.content).toContain('permission');
  }));

  it('should handle 403 error', fakeAsync(() => {
    mockChatbotService.sendMessage.and.returnValue(throwError(() => ({ status: 403 })));

    component.inputMessage = 'test';
    component.sendMessage();
    tick();

    const lastMessage = component.messages[component.messages.length - 1];
    expect(lastMessage.content).toContain('permission');
  }));

  it('should handle date format error', fakeAsync(() => {
    mockChatbotService.sendMessage.and.returnValue(throwError(() => ({ error: { message: 'Invalid date format' } })));

    component.inputMessage = 'test';
    component.sendMessage();
    tick();

    const lastMessage = component.messages[component.messages.length - 1];
    expect(lastMessage.content).toContain('date');
  }));

  it('should handle duplicate error', fakeAsync(() => {
    mockChatbotService.sendMessage.and.returnValue(throwError(() => ({ error: { message: 'Item already exists' } })));

    component.inputMessage = 'test';
    component.sendMessage();
    tick();

    const lastMessage = component.messages[component.messages.length - 1];
    expect(lastMessage.content).toContain('already exists');
  }));

  it('should handle not found error', fakeAsync(() => {
    mockChatbotService.sendMessage.and.returnValue(throwError(() => ({ error: { message: 'Item not found' } })));

    component.inputMessage = 'test';
    component.sendMessage();
    tick();

    const lastMessage = component.messages[component.messages.length - 1];
    expect(lastMessage.content).toContain('not found');
  }));

  it('should confirm action', fakeAsync(() => {
    component.pendingAction = {
      actionType: 'DELETE_DISCOUNT',
      params: { id: 1 },
      confirmationMessage: 'Confirm?'
    };

    component.confirmAction();
    tick();

    expect(mockChatbotService.sendMessage).toHaveBeenCalled();
    expect(component.inputMessage).toBe('');
  }));

  it('should not confirm action when no pending action', () => {
    component.pendingAction = null;
    const initialCallCount = mockChatbotService.sendMessage.calls.count();
    component.confirmAction();
    expect(mockChatbotService.sendMessage.calls.count()).toBe(initialCallCount);
  });

  it('should cancel action', fakeAsync(() => {
    component.pendingAction = {
      actionType: 'DELETE_DISCOUNT',
      params: { id: 1 },
      confirmationMessage: 'Confirm?'
    };

    component.cancelAction();
    tick();

    expect(mockChatbotService.sendMessage).toHaveBeenCalled();
  }));

  it('should handle Enter key press', () => {
    spyOn(component, 'sendMessage');
    const event = new KeyboardEvent('keypress', { key: 'Enter', shiftKey: false });
    spyOn(event, 'preventDefault');

    component.onKeyPress(event);

    expect(event.preventDefault).toHaveBeenCalled();
    expect(component.sendMessage).toHaveBeenCalled();
  });

  it('should not send message on Shift+Enter', () => {
    spyOn(component, 'sendMessage');
    const event = new KeyboardEvent('keypress', { key: 'Enter', shiftKey: true });

    component.onKeyPress(event);

    expect(component.sendMessage).not.toHaveBeenCalled();
  });

  it('should not send message on other key press', () => {
    spyOn(component, 'sendMessage');
    const event = new KeyboardEvent('keypress', { key: 'a' });

    component.onKeyPress(event);

    expect(component.sendMessage).not.toHaveBeenCalled();
  });

  it('should clear chat', () => {
    component.messages = [
      { role: 'user', content: 'test', timestamp: new Date() },
      { role: 'assistant', content: 'response', timestamp: new Date() }
    ];
    component.pendingAction = {
      actionType: 'DELETE',
      params: {},
      confirmationMessage: 'test'
    };

    component.clearChat();

    expect(component.messages.length).toBe(1);
    expect(component.messages[0].content).toContain('reiniciado');
    expect(component.pendingAction).toBeNull();
  });

  it('should clear activity log', () => {
    component.activityLog = [
      { action: 'test', success: true, timestamp: new Date() }
    ];

    component.clearActivity();

    expect(component.activityLog.length).toBe(0);
  });

  it('should keep only last 20 activities', fakeAsync(() => {
    for (let i = 0; i < 25; i++) {
      component.inputMessage = 'test ' + i;
      component.sendMessage();
      tick();
    }

    expect(component.activityLog.length).toBeLessThanOrEqual(20);
  }));

  it('should extract discount created action', fakeAsync(() => {
    mockChatbotService.sendMessage.and.returnValue(of({
      message: '✅ Discount created successfully',
      success: true
    }));

    component.inputMessage = 'Create discount';
    component.sendMessage();
    tick();

    expect(component.activityLog[0].action).toContain('Discount created');
  }));

  it('should extract ticket type created action', fakeAsync(() => {
    mockChatbotService.sendMessage.and.returnValue(of({
      message: '✅ Ticket type created',
      success: true
    }));

    component.inputMessage = 'Create ticket type';
    component.sendMessage();
    tick();

    expect(component.activityLog[0].action).toContain('Ticket type created');
  }));

  it('should extract attraction created action', fakeAsync(() => {
    mockChatbotService.sendMessage.and.returnValue(of({
      message: '✅ Attraction created',
      success: true
    }));

    component.inputMessage = 'Create attraction';
    component.sendMessage();
    tick();

    expect(component.activityLog[0].action).toContain('Attraction created');
  }));

  it('should extract discount updated action', fakeAsync(() => {
    mockChatbotService.sendMessage.and.returnValue(of({
      message: '✅ Discount updated',
      success: true
    }));

    component.inputMessage = 'Update discount';
    component.sendMessage();
    tick();

    expect(component.activityLog[0].action).toContain('Discount updated');
  }));

  it('should extract ticket type updated action', fakeAsync(() => {
    mockChatbotService.sendMessage.and.returnValue(of({
      message: '✅ Ticket type updated',
      success: true
    }));

    component.inputMessage = 'Update ticket type';
    component.sendMessage();
    tick();

    expect(component.activityLog[0].action).toContain('Ticket type updated');
  }));

  it('should extract attraction updated action', fakeAsync(() => {
    mockChatbotService.sendMessage.and.returnValue(of({
      message: '✅ Attraction updated',
      success: true
    }));

    component.inputMessage = 'Update attraction';
    component.sendMessage();
    tick();

    expect(component.activityLog[0].action).toContain('Attraction updated');
  }));

  it('should extract discount deleted action', fakeAsync(() => {
    mockChatbotService.sendMessage.and.returnValue(of({
      message: '✅ Discount deleted',
      success: true
    }));

    component.inputMessage = 'Delete discount';
    component.sendMessage();
    tick();

    expect(component.activityLog[0].action).toContain('Discount deleted');
  }));

  it('should extract ticket type deleted action', fakeAsync(() => {
    mockChatbotService.sendMessage.and.returnValue(of({
      message: '✅ Ticket type deleted',
      success: true
    }));

    component.inputMessage = 'Delete ticket type';
    component.sendMessage();
    tick();

    expect(component.activityLog[0].action).toContain('Ticket type deleted');
  }));

  it('should extract attraction deleted action', fakeAsync(() => {
    mockChatbotService.sendMessage.and.returnValue(of({
      message: '✅ Attraction deleted',
      success: true
    }));

    component.inputMessage = 'Delete attraction';
    component.sendMessage();
    tick();

    expect(component.activityLog[0].action).toContain('Attraction deleted');
  }));

  it('should extract listed discounts action', fakeAsync(() => {
    mockChatbotService.sendMessage.and.returnValue(of({
      message: '📋 Here are the discounts',
      success: true
    }));

    component.inputMessage = 'List discounts';
    component.sendMessage();
    tick();

    expect(component.activityLog[0].action).toContain('Listed discounts');
  }));

  it('should extract listed ticket types action', fakeAsync(() => {
    mockChatbotService.sendMessage.and.returnValue(of({
      message: '🎫 Here are the ticket types',
      success: true
    }));

    component.inputMessage = 'List ticket types';
    component.sendMessage();
    tick();

    expect(component.activityLog[0].action).toContain('Listed ticket types');
  }));

  it('should extract listed attractions action', fakeAsync(() => {
    mockChatbotService.sendMessage.and.returnValue(of({
      message: '🎢 Here are the attractions',
      success: true
    }));

    component.inputMessage = 'List attractions';
    component.sendMessage();
    tick();

    expect(component.activityLog[0].action).toContain('Listed attractions');
  }));

  it('should extract confirmation requested action', fakeAsync(() => {
    mockChatbotService.sendMessage.and.returnValue(of({
      message: '⚠️ Please provide confirmation',
      success: true,
      pendingAction: { actionType: 'DELETE', params: {}, confirmationMessage: 'Confirm?' }
    }));

    component.inputMessage = 'Delete something';
    component.sendMessage();
    tick();

    expect(component.activityLog[0].action).toContain('Confirmation requested');
  }));

  it('should extract operation cancelled action', fakeAsync(() => {
    mockChatbotService.sendMessage.and.returnValue(of({
      message: '❌ Operation cancelled',
      success: true
    }));

    component.inputMessage = 'no';
    component.sendMessage();
    tick();

    expect(component.activityLog[0].action).toContain('Operation cancelled');
  }));

  it('should extract error occurred action', fakeAsync(() => {
    mockChatbotService.sendMessage.and.returnValue(of({
      message: '❌ Something went wrong',
      success: false
    }));

    component.inputMessage = 'test';
    component.sendMessage();
    tick();

    expect(component.activityLog[0].action).toContain('Error occurred');
  }));

  it('should handle Spanish discount created action', fakeAsync(() => {
    mockChatbotService.sendMessage.and.returnValue(of({
      message: '✅ Descuento creado',
      success: true
    }));

    component.inputMessage = 'Crear descuento';
    component.sendMessage();
    tick();

    expect(component.activityLog[0].action).toContain('Discount created');
  }));

  it('should handle Spanish ticket type created action', fakeAsync(() => {
    mockChatbotService.sendMessage.and.returnValue(of({
      message: '✅ Tipo de entrada creado',
      success: true
    }));

    component.inputMessage = 'Crear entrada';
    component.sendMessage();
    tick();

    expect(component.activityLog[0].action).toContain('Ticket type created');
  }));

  it('should handle Spanish attraction created action', fakeAsync(() => {
    mockChatbotService.sendMessage.and.returnValue(of({
      message: '✅ Atracción creado',
      success: true
    }));

    component.inputMessage = 'Crear atracción';
    component.sendMessage();
    tick();

    expect(component.activityLog[0].action).toContain('Attraction created');
  }));

  it('should handle generic response', fakeAsync(() => {
    mockChatbotService.sendMessage.and.returnValue(of({
      message: 'Hello, how can I help?',
      success: true
    }));

    component.inputMessage = 'Hello';
    component.sendMessage();
    tick();

    expect(component.activityLog[0].action).toContain('Response received');
  }));
});

