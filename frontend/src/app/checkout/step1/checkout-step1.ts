import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { TranslatePipe, TranslateService } from '@ngx-translate/core';
import { Subject, takeUntil } from 'rxjs';
import { CheckoutService, TicketAvailability, CartItem } from '../services/checkout.service';
import { AvailabilityWebSocketService } from '../services/availability-websocket.service';
import { ErrorService } from '../../error/error-service';

@Component({
  selector: 'app-checkout-step1',
  standalone: true,
  imports: [CommonModule, FormsModule, TranslatePipe],
  templateUrl: './checkout-step1.html',
  styleUrl: './checkout-step1.css'
})
export class CheckoutStep1Component implements OnInit, OnDestroy {
  tickets: TicketAvailability[] = [];
  cart: Map<string, CartItem> = new Map();
  selectedDate = '';
  minDate = '';
  loading = false;
  error: string | null = null;


  currentMonth: Date = new Date();
  calendarDays: (number | null)[] = [];
  weekDays = ['L', 'M', 'X', 'J', 'V', 'S', 'D'];

  private destroy$ = new Subject<void>();

  constructor(
    private checkoutService: CheckoutService,
    private wsService: AvailabilityWebSocketService,
    private router: Router,
    private translate: TranslateService,
    private errorService: ErrorService
  ) {}

  ngOnInit(): void {
    const tomorrow = new Date();
    tomorrow.setDate(tomorrow.getDate() + 1);
    this.minDate = this.formatDate(tomorrow);
    this.selectedDate = this.minDate;
    this.currentMonth = new Date(tomorrow.getFullYear(), tomorrow.getMonth(), 1);
    this.generateCalendar();
    this.loadAvailability();
    this.setupWebSocket();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
    this.wsService.disconnect();
  }

  get cartItems(): CartItem[] {
    return Array.from(this.cart.values());
  }

  private formatDate(date: Date): string {
    return date.toISOString().split('T')[0];
  }

  onDateChange(): void {
    this.loadAvailability();
    this.setupWebSocket();
  }

  private loadAvailability(): void {
    if (!this.selectedDate) return;

    this.loading = true;
    this.error = null;

    this.checkoutService.getAvailability(this.selectedDate)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (availability) => {
          this.tickets = availability;
          this.updateCartAvailability();
          this.loading = false;
        },
        error: (err) => {
          const { code, args } = this.errorService.handleError(err);
          this.error = this.translate.instant(code, args);
          this.loading = false;
        }
      });
  }

  private setupWebSocket(): void {
    this.wsService.disconnect();
    this.wsService.connect(this.selectedDate)
      .pipe(takeUntil(this.destroy$))
      .subscribe((availability) => {
        this.tickets = availability;
        this.updateCartAvailability();
      });
  }

  private updateCartAvailability(): void {
    this.cart.forEach((item, typeName) => {
      const ticket = this.tickets.find(t => t.typeName === typeName);
      if (ticket && item.quantity > ticket.available) {
        item.quantity = ticket.available;
        item.totalPrice = item.quantity * item.unitPrice;
        if (item.quantity === 0) {
          this.cart.delete(typeName);
        }
      }
    });
  }

  getQuantity(typeName: string): number {
    return this.cart.get(typeName)?.quantity || 0;
  }

  addToCart(ticket: TicketAvailability): void {
    const currentQty = this.getQuantity(ticket.typeName);
    if (currentQty < ticket.available) {
      const newQty = currentQty + 1;
      this.cart.set(ticket.typeName, {
        ticketTypeName: ticket.typeName,
        quantity: newQty,
        unitPrice: ticket.cost,
        totalPrice: newQty * ticket.cost,
        photoUrl: ticket.photoUrl,
        description: ticket.description
      });
    }
  }

  removeFromCart(ticket: TicketAvailability): void {
    const currentQty = this.getQuantity(ticket.typeName);
    if (currentQty > 0) {
      const newQty = currentQty - 1;
      if (newQty === 0) {
        this.cart.delete(ticket.typeName);
      } else {
        const item = this.cart.get(ticket.typeName)!;
        item.quantity = newQty;
        item.totalPrice = newQty * item.unitPrice;
      }
    }
  }

  getSubtotal(): number {
    let subtotal = 0;
    this.cart.forEach(item => subtotal += item.totalPrice);
    return subtotal;
  }

  canProceed(): boolean {
    return this.cart.size > 0 && this.selectedDate !== '';
  }

  goToStep2(): void {
    if (!this.canProceed()) return;

    const cartData = {
      items: Array.from(this.cart.values()),
      visitDate: this.selectedDate
    };
    sessionStorage.setItem('checkout_cart', JSON.stringify(cartData));
    this.router.navigate(['/checkout/step2']);
  }


  generateCalendar(): void {
    const year = this.currentMonth.getFullYear();
    const month = this.currentMonth.getMonth();

    const firstDay = new Date(year, month, 1);
    const lastDay = new Date(year, month + 1, 0);


    let startDay = firstDay.getDay() - 1;
    if (startDay < 0) startDay = 6;

    this.calendarDays = [];


    for (let i = 0; i < startDay; i++) {
      this.calendarDays.push(null);
    }


    for (let day = 1; day <= lastDay.getDate(); day++) {
      this.calendarDays.push(day);
    }
  }

  previousMonth(): void {
    const newMonth = new Date(this.currentMonth.getFullYear(), this.currentMonth.getMonth() - 1, 1);
    const today = new Date();
    const minMonth = new Date(today.getFullYear(), today.getMonth(), 1);

    if (newMonth >= minMonth) {
      this.currentMonth = newMonth;
      this.generateCalendar();
    }
  }

  nextMonth(): void {
    this.currentMonth = new Date(this.currentMonth.getFullYear(), this.currentMonth.getMonth() + 1, 1);
    this.generateCalendar();
  }

  selectDay(day: number | null): void {
    if (day === null || !this.isDaySelectable(day)) return;

    const selected = new Date(this.currentMonth.getFullYear(), this.currentMonth.getMonth(), day);
    this.selectedDate = this.formatDate(selected);
    this.onDateChange();
  }

  isDaySelectable(day: number | null): boolean {
    if (day === null) return false;

    const date = new Date(this.currentMonth.getFullYear(), this.currentMonth.getMonth(), day);
    const minDateObj = new Date(this.minDate);
    minDateObj.setHours(0, 0, 0, 0);
    date.setHours(0, 0, 0, 0);

    return date >= minDateObj;
  }

  isDaySelected(day: number | null): boolean {
    if (day === null) return false;

    const date = new Date(this.currentMonth.getFullYear(), this.currentMonth.getMonth(), day);
    return this.formatDate(date) === this.selectedDate;
  }

  getMonthName(): string {
    return this.currentMonth.toLocaleDateString(this.translate.currentLang || 'es', {
      month: 'long',
      year: 'numeric'
    });
  }

  canGoPrevious(): boolean {
    const today = new Date();
    const minMonth = new Date(today.getFullYear(), today.getMonth(), 1);
    const prevMonth = new Date(this.currentMonth.getFullYear(), this.currentMonth.getMonth() - 1, 1);
    return prevMonth >= minMonth;
  }
}

