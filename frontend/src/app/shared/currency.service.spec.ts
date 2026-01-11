import { TestBed } from '@angular/core/testing';
import { TranslateService } from '@ngx-translate/core';
import { CurrencyService } from './currency.service';

describe('CurrencyService', () => {
  let service: CurrencyService;
  let mockTranslate: jasmine.SpyObj<TranslateService>;

  beforeEach(() => {
    mockTranslate = jasmine.createSpyObj('TranslateService', [], { currentLang: 'es' });

    TestBed.configureTestingModule({
      providers: [
        CurrencyService,
        { provide: TranslateService, useValue: mockTranslate }
      ]
    });

    service = TestBed.inject(CurrencyService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  describe('getCurrencySymbol', () => {
    it('should return € for Spanish', () => {
      Object.defineProperty(mockTranslate, 'currentLang', { value: 'es' });
      expect(service.getCurrencySymbol()).toBe('€');
    });

    it('should return $ for English', () => {
      Object.defineProperty(mockTranslate, 'currentLang', { value: 'en' });
      expect(service.getCurrencySymbol()).toBe('$');
    });
  });

  describe('getCurrencyCode', () => {
    it('should return EUR for Spanish', () => {
      Object.defineProperty(mockTranslate, 'currentLang', { value: 'es' });
      expect(service.getCurrencyCode()).toBe('EUR');
    });

    it('should return USD for English', () => {
      Object.defineProperty(mockTranslate, 'currentLang', { value: 'en' });
      expect(service.getCurrencyCode()).toBe('USD');
    });
  });

  describe('convertFromEur', () => {
    it('should return same price for Spanish (EUR)', () => {
      Object.defineProperty(mockTranslate, 'currentLang', { value: 'es' });
      expect(service.convertFromEur(100)).toBe(100);
    });

    it('should convert EUR to USD for English', () => {
      Object.defineProperty(mockTranslate, 'currentLang', { value: 'en' });
      const result = service.convertFromEur(100);
      expect(result).toBe(108); // 100 * 1.08
    });

    it('should round to 2 decimal places', () => {
      Object.defineProperty(mockTranslate, 'currentLang', { value: 'en' });
      const result = service.convertFromEur(33.33);
      expect(result).toBe(36);
    });
  });

  describe('convertToEur', () => {
    it('should return same price for Spanish (EUR)', () => {
      Object.defineProperty(mockTranslate, 'currentLang', { value: 'es' });
      expect(service.convertToEur(100)).toBe(100);
    });

    it('should convert USD to EUR for English', () => {
      Object.defineProperty(mockTranslate, 'currentLang', { value: 'en' });
      const result = service.convertToEur(108);
      expect(result).toBe(100);
    });
  });

  describe('formatPrice', () => {
    it('should format price with EUR symbol for Spanish', () => {
      Object.defineProperty(mockTranslate, 'currentLang', { value: 'es' });
      expect(service.formatPrice(100)).toBe('100.00 €');
    });

    it('should format price with USD symbol for English', () => {
      Object.defineProperty(mockTranslate, 'currentLang', { value: 'en' });
      expect(service.formatPrice(100)).toBe('108.00 $');
    });
  });

  describe('fallback to defaultLang', () => {
    it('should use defaultLang when currentLang is empty', () => {
      Object.defineProperty(mockTranslate, 'currentLang', { value: '' });
      Object.defineProperty(mockTranslate, 'defaultLang', { value: 'en' });
      expect(service.getCurrencySymbol()).toBe('$');
      expect(service.convertFromEur(100)).toBe(108);
    });

    it('should use defaultLang when currentLang is undefined', () => {
      Object.defineProperty(mockTranslate, 'currentLang', { value: undefined });
      Object.defineProperty(mockTranslate, 'defaultLang', { value: 'es' });
      expect(service.getCurrencySymbol()).toBe('€');
      expect(service.convertFromEur(100)).toBe(100);
    });

    it('should fallback to en when both are empty', () => {
      Object.defineProperty(mockTranslate, 'currentLang', { value: '' });
      Object.defineProperty(mockTranslate, 'defaultLang', { value: '' });
      expect(service.getCurrencySymbol()).toBe('$');
    });
  });
});

