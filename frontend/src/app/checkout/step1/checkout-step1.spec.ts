import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { TranslateModule } from '@ngx-translate/core';
import { CheckoutStep1Component } from './checkout-step1';
import { TicketAvailability } from '../services/checkout.service';
import { Router } from '@angular/router';

describe('CheckoutStep1Component', () => {
  let component: CheckoutStep1Component;
  let fixture: ComponentFixture<CheckoutStep1Component>;

  const mockTicket: TicketAvailability = {
    id: 1,
    typeName: 'ADULT',
    description: 'Adult ticket',
    cost: 50,
    adjustedCost: 50,
    seasonalMultiplier: 1,
    photoUrl: '/img.jpg',
    maxPerDay: 100,
    available: 80
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [
        CheckoutStep1Component,
        TranslateModule.forRoot()
      ],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting()
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(CheckoutStep1Component);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize with tomorrow as min date', () => {
    const tomorrow = new Date();
    tomorrow.setDate(tomorrow.getDate() + 1);
    const expectedDate = formatLocalDate(tomorrow);
    expect(component.minDate).toBe(expectedDate);
  });

  it('should initialize maxDate to 2 months from now', () => {
    const maxDateObj = new Date();
    maxDateObj.setMonth(maxDateObj.getMonth() + 2);
    const expectedMax = formatLocalDate(maxDateObj);
    expect(component.maxDate).toBe(expectedMax);
  });

  it('should not allow proceeding with empty cart', () => {
    expect(component.canProceed()).toBeFalse();
  });

  it('should return empty cartItems when cart is empty', () => {
    expect(component.cartItems).toEqual([]);
  });

  it('should add item to cart', () => {
    component.addToCart(mockTicket);
    expect(component.getQuantity('ADULT')).toBe(1);
  });

  it('should increase quantity when adding same item', () => {
    component.addToCart(mockTicket);
    component.addToCart(mockTicket);
    expect(component.getQuantity('ADULT')).toBe(2);
  });

  it('should not exceed available quantity', () => {
    const limitedTicket = { ...mockTicket, available: 2 };
    component.addToCart(limitedTicket);
    component.addToCart(limitedTicket);
    component.addToCart(limitedTicket);
    expect(component.getQuantity('ADULT')).toBe(2);
  });

  it('should remove item from cart', () => {
    component.addToCart(mockTicket);
    component.addToCart(mockTicket);
    component.removeFromCart(mockTicket);
    expect(component.getQuantity('ADULT')).toBe(1);
  });

  it('should delete item when quantity reaches zero', () => {
    component.addToCart(mockTicket);
    component.removeFromCart(mockTicket);
    expect(component.getQuantity('ADULT')).toBe(0);
  });

  it('should calculate subtotal correctly', () => {
    component.addToCart(mockTicket);
    component.addToCart(mockTicket);
    expect(component.getSubtotal()).toBe(100);
  });

  it('should return zero subtotal for empty cart', () => {
    expect(component.getSubtotal()).toBe(0);
  });

  it('should allow proceeding when cart has items and date selected', () => {
    component.addToCart(mockTicket);
    expect(component.canProceed()).toBeTrue();
  });

  it('should generate calendar days', () => {
    component.generateCalendar();
    expect(component.calendarDays.length).toBeGreaterThan(0);
  });

  it('should navigate to next month if within limit', () => {
    const today = new Date();
    component.currentMonth = new Date(today.getFullYear(), today.getMonth(), 1);
    const currentMonth = component.currentMonth.getMonth();
    if (component.canGoNext()) {
      component.nextMonth();
      const newMonth = component.currentMonth.getMonth();
      expect(newMonth).toBe((currentMonth + 1) % 12);
    } else {
      expect(component.canGoNext()).toBeFalse();
    }
  });

  it('should check if day is selectable', () => {
    const futureDay = new Date();
    futureDay.setDate(futureDay.getDate() + 10);
    component.currentMonth = new Date(futureDay.getFullYear(), futureDay.getMonth(), 1);
    expect(component.isDaySelectable(futureDay.getDate())).toBeTrue();
  });

  it('should return false for null day selectable', () => {
    expect(component.isDaySelectable(null)).toBeFalse();
  });

  it('should return false for null day selected', () => {
    expect(component.isDaySelected(null)).toBeFalse();
  });

  it('should get month name', () => {
    const monthName = component.getMonthName();
    expect(monthName).toBeTruthy();
  });


  it('should return cart items as array', () => {
    component.addToCart(mockTicket);
    expect(component.cartItems.length).toBe(1);
  });

  // Helper to format date same as the component (YYYY-MM-DD local)
  function formatLocalDate(date: Date): string {
    const y = date.getFullYear();
    const m = String(date.getMonth() + 1).padStart(2, '0');
    const d = String(date.getDate()).padStart(2, '0');
    return `${y}-${m}-${d}`;
  }

  it('should detect closed days', () => {
    const tomorrow = new Date();
    tomorrow.setDate(tomorrow.getDate() + 5);
    component.currentMonth = new Date(tomorrow.getFullYear(), tomorrow.getMonth(), 1);
    const dateStr = formatLocalDate(tomorrow);
    component.closureDays.set(dateStr, 'Holiday');
    expect(component.isClosedDay(tomorrow.getDate())).toBeTrue();
  });

  it('should not allow selecting closed day', () => {
    const futureDay = new Date();
    futureDay.setDate(futureDay.getDate() + 5);
    component.currentMonth = new Date(futureDay.getFullYear(), futureDay.getMonth(), 1);
    const dateStr = formatLocalDate(futureDay);
    component.closureDays.set(dateStr, 'Holiday');
    expect(component.isDaySelectable(futureDay.getDate())).toBeFalse();
  });

  it('should return closure reason', () => {
    const futureDay = new Date();
    futureDay.setDate(futureDay.getDate() + 5);
    component.currentMonth = new Date(futureDay.getFullYear(), futureDay.getMonth(), 1);
    const dateStr = formatLocalDate(futureDay);
    component.closureDays.set(dateStr, 'Maintenance');
    expect(component.getClosureReason(futureDay.getDate())).toBe('Maintenance');
  });

  it('should detect days beyond max date', () => {
    const farDay = new Date();
    farDay.setMonth(farDay.getMonth() + 3);
    component.currentMonth = new Date(farDay.getFullYear(), farDay.getMonth(), 1);
    expect(component.isBeyondMaxDate(farDay.getDate())).toBeTrue();
  });

  it('should report no seasonal surcharge when multiplier is 1', () => {
    component.tickets = [{ ...mockTicket, seasonalMultiplier: 1 }];
    expect(component.hasSeasonalSurcharge).toBeFalse();
  });

  it('should report seasonal surcharge when multiplier > 1', () => {
    component.tickets = [{ ...mockTicket, seasonalMultiplier: 1.25 }];
    expect(component.hasSeasonalSurcharge).toBeTrue();
    expect(component.seasonalMultiplier).toBe(1.25);
  });

  it('should use adjustedCost for cart item pricing', () => {
    const seasonalTicket = { ...mockTicket, adjustedCost: 62.5, seasonalMultiplier: 1.25 };
    component.addToCart(seasonalTicket);
    const cartItem = component.cartItems[0];
    expect(cartItem.unitPrice).toBe(62.5);
    expect(cartItem.totalPrice).toBe(62.5);
  });

  it('should not remove from cart when quantity is already 0', () => {
    component.removeFromCart(mockTicket);
    expect(component.getQuantity('ADULT')).toBe(0);
  });

  it('should call onDateChange which triggers load availability', () => {
    spyOn<any>(component, 'loadAvailability');
    spyOn<any>(component, 'setupWebSocket');
    component.onDateChange();
    expect((component as any).loadAvailability).toHaveBeenCalled();
    expect((component as any).setupWebSocket).toHaveBeenCalled();
  });

  it('should clear cart and call onDateChange when selecting a day', () => {
    component.addToCart(mockTicket);
    expect(component.cart.size).toBe(1);

    const futureDay = new Date();
    futureDay.setDate(futureDay.getDate() + 10);
    component.currentMonth = new Date(futureDay.getFullYear(), futureDay.getMonth(), 1);

    spyOn(component, 'onDateChange');
    component.selectDay(futureDay.getDate());

    expect(component.cart.size).toBe(0);
    expect(component.onDateChange).toHaveBeenCalled();
  });

  it('should not select null day', () => {
    spyOn(component, 'onDateChange');
    component.selectDay(null);
    expect(component.onDateChange).not.toHaveBeenCalled();
  });

  it('should not select a non-selectable day', () => {
    spyOn(component, 'onDateChange');
    // Past day: use a day in a far past month
    const pastDate = new Date(2020, 0, 1);
    component.currentMonth = new Date(2020, 0, 1);
    component.selectDay(pastDate.getDate());
    expect(component.onDateChange).not.toHaveBeenCalled();
  });

  it('should go to previous month when valid', () => {
    const today = new Date();
    // Set currentMonth to one month in the future
    component.currentMonth = new Date(today.getFullYear(), today.getMonth() + 1, 1);

    if (component.canGoPrevious()) {
      component.previousMonth();
      expect(component.currentMonth.getMonth()).toBe(today.getMonth());
    }
  });

  it('should not go to previous month when at minimum', () => {
    const today = new Date();
    component.currentMonth = new Date(today.getFullYear(), today.getMonth(), 1);
    const originalMonth = component.currentMonth.getMonth();
    component.previousMonth();
    expect(component.currentMonth.getMonth()).toBe(originalMonth);
  });

  it('canGoPrevious should return false when at current month', () => {
    const today = new Date();
    component.currentMonth = new Date(today.getFullYear(), today.getMonth(), 1);
    expect(component.canGoPrevious()).toBeFalse();
  });

  it('canGoPrevious should return true when ahead of current month', () => {
    const today = new Date();
    component.currentMonth = new Date(today.getFullYear(), today.getMonth() + 2, 1);
    expect(component.canGoPrevious()).toBeTrue();
  });

  it('should navigate to step2 when canProceed is true via goToStep2', () => {
    const router = TestBed.inject(Router);
    spyOn(router, 'navigate');

    component.addToCart(mockTicket);

    component.goToStep2();

    expect(router.navigate).toHaveBeenCalledWith(['/checkout/step2']);
    // Verify sessionStorage was set
    const stored = sessionStorage.getItem('checkout_cart');
    expect(stored).toBeTruthy();
    const parsed = JSON.parse(stored!);
    expect(parsed.items.length).toBe(1);
    expect(parsed.visitDate).toBe(component.selectedDate);
    sessionStorage.removeItem('checkout_cart');
  });

  it('should not navigate to step2 when canProceed is false', () => {
    const router = TestBed.inject(Router);
    spyOn(router, 'navigate');

    component.goToStep2();

    expect(router.navigate).not.toHaveBeenCalled();
  });

  it('should store cart data with items and visitDate in sessionStorage', () => {
    const router = TestBed.inject(Router);
    spyOn(router, 'navigate');

    component.addToCart(mockTicket);
    component.goToStep2();

    const stored = JSON.parse(sessionStorage.getItem('checkout_cart')!);
    expect(stored.items.length).toBe(1);
    expect(stored.visitDate).toBe(component.selectedDate);

    sessionStorage.removeItem('checkout_cart');
  });

  it('should update cart availability when tickets change', () => {
    component.addToCart(mockTicket);
    component.addToCart(mockTicket);
    expect(component.getQuantity('ADULT')).toBe(2);

    // Simulate available dropping to 1
    component.tickets = [{ ...mockTicket, available: 1, adjustedCost: 60 }];
    (component as any).updateCartAvailability();

    expect(component.getQuantity('ADULT')).toBe(1);
    const item = component.cartItems[0];
    expect(item.unitPrice).toBe(60);
  });

  it('should remove item from cart if available drops to 0', () => {
    component.addToCart(mockTicket);
    expect(component.getQuantity('ADULT')).toBe(1);

    component.tickets = [{ ...mockTicket, available: 0 }];
    (component as any).updateCartAvailability();

    expect(component.getQuantity('ADULT')).toBe(0);
    expect(component.cart.size).toBe(0);
  });

  it('should return default seasonalMultiplier of 1 when no tickets', () => {
    component.tickets = [];
    expect(component.seasonalMultiplier).toBe(1);
  });

  it('should return closure reason empty string for null day', () => {
    expect(component.getClosureReason(null)).toBe('');
  });

  it('should return empty closure reason for day without closure', () => {
    const futureDay = new Date();
    futureDay.setDate(futureDay.getDate() + 5);
    component.currentMonth = new Date(futureDay.getFullYear(), futureDay.getMonth(), 1);
    expect(component.getClosureReason(futureDay.getDate())).toBe('');
  });

  it('should not detect closed day for null', () => {
    expect(component.isClosedDay(null)).toBeFalse();
  });

  it('should return false for isBeyondMaxDate with null', () => {
    expect(component.isBeyondMaxDate(null)).toBeFalse();
  });

  it('should not trigger HTTP call in loadAvailability when selectedDate is empty', () => {
    component.selectedDate = '';
    // loadAvailability returns early when selectedDate is empty, leaving loading unchanged
    const loadingBefore = component.loading;
    (component as any).loadAvailability();
    expect(component.loading).toBe(loadingBefore);
  });

  it('should call ngOnDestroy cleanly', () => {
    fixture.detectChanges();
    expect(() => component.ngOnDestroy()).not.toThrow();
  });
});

