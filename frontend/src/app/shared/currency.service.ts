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

  private normalizeCurrencyCode(code: string | null | undefined): string {
    return (code || 'EUR').toUpperCase();
  }

  private roundToCents(value: number): number {
    return Math.round(value * 100) / 100;
  }

  getCurrencySymbol(): string {
    return this.isEnglish() ? '$' : '€';
  }

  getCurrencyCode(): string {
    return this.isEnglish() ? 'USD' : 'EUR';
  }

  convertBetweenCurrencies(amount: number, fromCurrency: string, toCurrency: string): number {
    const from = this.normalizeCurrencyCode(fromCurrency);
    const to = this.normalizeCurrencyCode(toCurrency);

    if (from === to) {
      return amount;
    }

    if (from === 'EUR' && to === 'USD') {
      return this.roundToCents(amount * this.EUR_TO_USD_RATE);
    }

    if (from === 'USD' && to === 'EUR') {
      return this.roundToCents(amount / this.EUR_TO_USD_RATE);
    }

    return amount;
  }

  convertFromCurrency(amount: number, fromCurrency: string): number {
    return this.convertBetweenCurrencies(amount, fromCurrency, this.getCurrencyCode());
  }

  convertFromEur(priceInEur: number): number {
    return this.convertBetweenCurrencies(priceInEur, 'EUR', this.getCurrencyCode());
  }

  convertToEur(price: number): number {
    return this.convertBetweenCurrencies(price, this.getCurrencyCode(), 'EUR');
  }

  formatPrice(priceInEur: number): string {
    const converted = this.convertFromEur(priceInEur);
    const symbol = this.getCurrencySymbol();
    return `${converted.toFixed(2)} ${symbol}`;
  }
}

