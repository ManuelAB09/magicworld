import {
  extractActionFromMessage,
  interpretError,
  WELCOME_MESSAGE,
  RESET_MESSAGE
} from './chatbot-utils';
import { ChatResponse } from './chatbot.service';

describe('ChatbotUtils', () => {
  describe('extractActionFromMessage', () => {
    it('should detect discount created', () => {
      const response: ChatResponse = { message: '✅ Discount created successfully', success: true };
      expect(extractActionFromMessage(response)).toBe('✨ Discount created');
    });

    it('should detect discount updated', () => {
      const response: ChatResponse = { message: '✅ Discount updated successfully', success: true };
      expect(extractActionFromMessage(response)).toBe('✏️ Discount updated');
    });

    it('should detect discount deleted', () => {
      const response: ChatResponse = { message: '✅ Discount deleted successfully', success: true };
      expect(extractActionFromMessage(response)).toBe('🗑️ Discount deleted');
    });

    it('should detect ticket type created', () => {
      const response: ChatResponse = { message: '✅ Ticket type created', success: true };
      expect(extractActionFromMessage(response)).toBe('✨ Ticket type created');
    });

    it('should detect attraction created', () => {
      const response: ChatResponse = { message: '✅ Attraction created', success: true };
      expect(extractActionFromMessage(response)).toBe('✨ Attraction created');
    });

    it('should detect listed discounts', () => {
      const response: ChatResponse = { message: '📋 Here are the discounts', success: true };
      expect(extractActionFromMessage(response)).toBe('📋 Listed discounts');
    });

    it('should detect listed ticket types', () => {
      const response: ChatResponse = { message: '🎫 Listing ticket types', success: true };
      expect(extractActionFromMessage(response)).toBe('📋 Listed ticket types');
    });

    it('should detect listed attractions', () => {
      const response: ChatResponse = { message: '🎢 Here are the attractions', success: true };
      expect(extractActionFromMessage(response)).toBe('📋 Listed attractions');
    });

    it('should detect confirmation requested', () => {
      const response: ChatResponse = { message: '⚠️ Please provide confirmation', success: true };
      expect(extractActionFromMessage(response)).toBe('⚠️ Confirmation requested');
    });

    it('should detect operation cancelled', () => {
      const response: ChatResponse = { message: '❌ Operation cancelled by user', success: false };
      expect(extractActionFromMessage(response)).toBe('❌ Operation cancelled');
    });

    it('should detect generic error', () => {
      const response: ChatResponse = { message: '❌ Something went wrong', success: false };
      expect(extractActionFromMessage(response)).toBe('❌ Error occurred');
    });

    it('should return default response for unknown message', () => {
      const response: ChatResponse = { message: 'Hello there', success: true };
      expect(extractActionFromMessage(response)).toBe('💬 Response received');
    });

    it('should handle Spanish discount creado', () => {
      const response: ChatResponse = { message: '✅ Descuento creado correctamente', success: true };
      expect(extractActionFromMessage(response)).toBe('✨ Discount created');
    });

    it('should handle Spanish entrada', () => {
      const response: ChatResponse = { message: '📋 Lista de entradas', success: true };
      expect(extractActionFromMessage(response)).toBe('📋 Listed ticket types');
    });
  });

  describe('interpretError', () => {
    it('should return date format error for date errors', () => {
      const error = { error: { message: 'Invalid date format' } };
      const result = interpretError(error);
      expect(result).toContain('date format');
    });

    it('should return duplicate error for already exists', () => {
      const error = { error: { message: 'Item already exists' } };
      const result = interpretError(error);
      expect(result).toContain('already exists');
    });

    it('should return not found error', () => {
      const error = { error: { message: 'Item not found' } };
      const result = interpretError(error);
      expect(result).toContain('not found');
    });

    it('should return permission error for 401', () => {
      const error = { status: 401 };
      const result = interpretError(error);
      expect(result).toContain('permission');
    });

    it('should return permission error for 403', () => {
      const error = { status: 403 };
      const result = interpretError(error);
      expect(result).toContain('permission');
    });

    it('should return server error for 500', () => {
      const error = { status: 500 };
      const result = interpretError(error);
      expect(result).toContain('server error');
    });

    it('should return generic error for unknown error', () => {
      const error = { status: 418 };
      const result = interpretError(error);
      expect(result).toContain('error occurred');
    });
  });

  describe('Constants', () => {
    it('should have WELCOME_MESSAGE defined', () => {
      expect(WELCOME_MESSAGE).toBeTruthy();
      expect(WELCOME_MESSAGE).toContain('MagicWorld');
    });

    it('should have RESET_MESSAGE defined', () => {
      expect(RESET_MESSAGE).toBeTruthy();
      expect(RESET_MESSAGE).toContain('🔄');
    });
  });
});
