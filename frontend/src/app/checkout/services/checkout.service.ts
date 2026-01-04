import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { getBackendBaseUrl } from '../../config/backend';

export interface TicketAvailability {
  id: number;
  typeName: string;
  description: string;
  cost: number;
  currency: string;
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

@Injectable({ providedIn: 'root' })
export class CheckoutService {
  private baseUrl = `${getBackendBaseUrl()}/api/v1/payment`;

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
}

