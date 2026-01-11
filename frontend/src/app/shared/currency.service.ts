import { Injectable } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';

@Injectable({
  providedIn: 'root'
})
export class CurrencyService {

  private readonly EUR_TO_USD_RATE = 1.08;

  constructor(private translate: TranslateService) {}

  private getCurrentLang(): string {
    return this.translate.currentLang || this.translate.defaultLang || 'en';
  }

  private isEnglish(): boolean {
    return this.getCurrentLang() === 'en';
  }

  getCurrencySymbol(): string {
    return this.isEnglish() ? '$' : '€';
  }

  getCurrencyCode(): string {
    return this.isEnglish() ? 'USD' : 'EUR';
  }

  convertFromEur(priceInEur: number): number {
    if (this.isEnglish()) {
      return Math.round(priceInEur * this.EUR_TO_USD_RATE * 100) / 100;
    }
    return priceInEur;
  }

  convertToEur(price: number): number {
    if (this.isEnglish()) {
      return Math.round((price / this.EUR_TO_USD_RATE) * 100) / 100;
    }
    return price;
  }

  formatPrice(priceInEur: number): string {
    const converted = this.convertFromEur(priceInEur);
    const symbol = this.getCurrencySymbol();
    return `${converted.toFixed(2)} ${symbol}`;
  }
}

