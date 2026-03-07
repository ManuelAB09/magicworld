import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { getBackendBaseUrl } from '../../config/backend';

export interface TicketAvailability {
  id: number;
  typeName: string;
  description: string;
  cost: number;
  adjustedCost: number;
  seasonalMultiplier: number;
  photoUrl: string;
  maxPerDay: number;
  available: number;
}

export interface CartItem {
  ticketTypeName: string;
  quantity: number;
  unitPrice: number;
  totalPrice: number;
  photoUrl: string;
  description: string;
}

export interface PriceCalculationRequest {
  items: { ticketTypeName: string; quantity: number }[];
  discountCodes: string[];
  visitDate: string;
}

export interface PriceCalculationResponse {
  subtotal: number;
  discountAmount: number;
  total: number;
  validDiscountCodes: string[];
  invalidDiscountCodes: string[];
  validButNotApplicableCodes: string[];
  discountPercentages: { [key: string]: number };
  discountAppliesTo: { [key: string]: string[] };
}

export interface PaymentRequest {
  visitDate: string;
  items: { ticketTypeName: string; quantity: number }[];
  discountCodes: string[];
  email: string;
  firstName: string;
  lastName: string;
  stripePaymentMethodId: string;
}

export interface PaymentResponse {
  success: boolean;
  message: string;
  purchaseId?: number;
  stripePaymentIntentId?: string;
  totalAmount?: number;
  discountAmount?: number;
  appliedDiscountCodes?: string[];
}

export interface ParkClosureDay {
  id: number;
  closureDate: string;
  reason: string;
}

export interface SeasonalPricing {
  id: number;
  name: string;
  startDate: string;
  endDate: string;
  multiplier: number;
  applyOnWeekdays: boolean;
  applyOnWeekends: boolean;
}

@Injectable({ providedIn: 'root' })
export class CheckoutService {
  private baseUrl = `${getBackendBaseUrl()}/api/v1/payment`;
  private closuresUrl = `${getBackendBaseUrl()}/api/v1/park-closures`;
  private pricingUrl = `${getBackendBaseUrl()}/api/v1/seasonal-pricing`;

  constructor(private http: HttpClient) {}

  getStripePublicKey(): Observable<{ publicKey: string }> {
    return this.http.get<{ publicKey: string }>(`${this.baseUrl}/stripe-key`, {
      withCredentials: true
    });
  }

  getAvailability(date: string): Observable<TicketAvailability[]> {
    return this.http.get<TicketAvailability[]>(`${this.baseUrl}/availability`, {
      params: { date },
      withCredentials: true
    });
  }

  calculatePrice(request: PriceCalculationRequest): Observable<PriceCalculationResponse> {
    return this.http.post<PriceCalculationResponse>(`${this.baseUrl}/calculate`, request, {
      withCredentials: true
    });
  }

  processPayment(request: PaymentRequest): Observable<PaymentResponse> {
    return this.http.post<PaymentResponse>(`${this.baseUrl}/process`, request, {
      withCredentials: true
    });
  }

  getClosureDays(from: string, to: string): Observable<ParkClosureDay[]> {
    return this.http.get<ParkClosureDay[]>(this.closuresUrl, {
      params: { from, to },
      withCredentials: true
    });
  }

  getAllClosureDays(): Observable<ParkClosureDay[]> {
    return this.http.get<ParkClosureDay[]>(this.closuresUrl, {
      withCredentials: true
    });
  }

  createClosureDay(request: { closureDate: string; reason: string }): Observable<ParkClosureDay> {
    return this.http.post<ParkClosureDay>(this.closuresUrl, request, {
      withCredentials: true
    });
  }

  deleteClosureDay(id: number): Observable<void> {
    return this.http.delete<void>(`${this.closuresUrl}/${id}`, {
      withCredentials: true
    });
  }

  getAllSeasonalPricing(): Observable<SeasonalPricing[]> {
    return this.http.get<SeasonalPricing[]>(this.pricingUrl, {
      withCredentials: true
    });
  }

  createSeasonalPricing(request: any): Observable<SeasonalPricing> {
    return this.http.post<SeasonalPricing>(this.pricingUrl, request, {
      withCredentials: true
    });
  }

  updateSeasonalPricing(id: number, request: any): Observable<SeasonalPricing> {
    return this.http.put<SeasonalPricing>(`${this.pricingUrl}/${id}`, request, {
      withCredentials: true
    });
  }

  deleteSeasonalPricing(id: number): Observable<void> {
    return this.http.delete<void>(`${this.pricingUrl}/${id}`, {
      withCredentials: true
    });
  }
}

