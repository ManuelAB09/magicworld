import { ChatResponse } from './chatbot.service';

export function extractActionFromMessage(response: ChatResponse): string {
  const responseMsg = response.message.toLowerCase();

  const actionPatterns = [
    { check: '✅', actions: ['created', 'creado'], icons: { discount: '✨ Discount created', ticket: '✨ Ticket type created', attraction: '✨ Attraction created' } },
    { check: '✅', actions: ['updated', 'actualizado'], icons: { discount: '✏️ Discount updated', ticket: '✏️ Ticket type updated', attraction: '✏️ Attraction updated' } },
    { check: '✅', actions: ['deleted', 'eliminado'], icons: { discount: '🗑️ Discount deleted', ticket: '🗑️ Ticket type deleted', attraction: '🗑️ Attraction deleted' } },
  ];

  for (const pattern of actionPatterns) {
    if (responseMsg.includes(pattern.check) && pattern.actions.some(a => responseMsg.includes(a))) {
      if (responseMsg.includes('discount') || responseMsg.includes('descuento')) return pattern.icons.discount;
      if (responseMsg.includes('ticket') || responseMsg.includes('entrada')) return pattern.icons.ticket;
      if (responseMsg.includes('attraction') || responseMsg.includes('atracción')) return pattern.icons.attraction;
    }
  }

  if (responseMsg.includes('📋') || responseMsg.includes('🎫') || responseMsg.includes('🎢')) {
    if (responseMsg.includes('discount') || responseMsg.includes('descuento')) return '📋 Listed discounts';
    if (responseMsg.includes('ticket') || responseMsg.includes('entrada')) return '📋 Listed ticket types';
    if (responseMsg.includes('attraction') || responseMsg.includes('atracción')) return '📋 Listed attractions';
  }

  if (responseMsg.includes('⚠️') && (responseMsg.includes('confirmation') || responseMsg.includes('confirmación'))) {
    return '⚠️ Confirmation requested';
  }

  if (responseMsg.includes('❌') && (responseMsg.includes('cancelled') || responseMsg.includes('cancelada'))) {
    return '❌ Operation cancelled';
  }

  if (responseMsg.includes('❌')) return '❌ Error occurred';

  return '💬 Response received';
}

export function interpretError(error: any): string {
  const errorMsg = error?.error?.message || error?.message || '';

  const errorMappings = [
    { patterns: ['date', 'fecha'], message: '❌ The date format is invalid. Please use YYYY-MM-DD format (e.g., 2025-12-31).\n\n❌ El formato de fecha no es válido. Por favor, usa el formato AAAA-MM-DD (ej: 2025-12-31).' },
    { patterns: ['duplicate', 'already exists'], message: '❌ This item already exists. Please use a different name or code.\n\n❌ Este elemento ya existe. Por favor, usa un nombre o código diferente.' },
    { patterns: ['not found', 'no encontr'], message: '❌ The requested item was not found. Please verify the ID or name.\n\n❌ El elemento solicitado no fue encontrado. Por favor, verifica el ID o nombre.' },
  ];

  for (const mapping of errorMappings) {
    if (mapping.patterns.some(p => errorMsg.includes(p))) {
      return mapping.message;
    }
  }

  if (error.status === 401 || error.status === 403) {
    return '❌ You don\'t have permission to perform this action.\n\n❌ No tienes permiso para realizar esta acción.';
  }

  if (error.status === 500) {
    return '❌ A server error occurred. Please try again later.\n\n❌ Ha ocurrido un error en el servidor. Por favor, inténtalo más tarde.';
  }

  return '❌ An error occurred while processing your request. Please try again.\n\n❌ Ha ocurrido un error al procesar tu solicitud. Por favor, inténtalo de nuevo.';
}

export const WELCOME_MESSAGE = '¡Hola! 👋 Soy tu asistente de administración de MagicWorld. Puedo ayudarte a gestionar:\n\n• **Descuentos**: crear, editar, eliminar y listar\n• **Tipos de entrada**: crear, editar, eliminar y listar\n• **Atracciones**: crear, editar, eliminar y listar\n\n¿En qué puedo ayudarte hoy?\n\n---\n\nHi! 👋 I\'m your MagicWorld administration assistant. I can help you manage:\n\n• **Discounts**: create, edit, delete and list\n• **Ticket Types**: create, edit, delete and list\n• **Attractions**: create, edit, delete and list\n\nHow can I help you today?';

export const RESET_MESSAGE = '¡Chat reiniciado! 🔄 ¿En qué puedo ayudarte?\n\nChat restarted! 🔄 How can I help you?';

