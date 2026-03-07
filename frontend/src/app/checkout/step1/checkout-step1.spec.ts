import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { TranslateModule } from '@ngx-translate/core';
import { CheckoutStep1Component } from './checkout-step1';
import { TicketAvailability } from '../services/checkout.service';

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
});

