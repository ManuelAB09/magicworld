import { Component, ElementRef, ViewChild, AfterViewChecked } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { TranslateModule } from '@ngx-translate/core';
import { ChatbotService, ChatHistoryEntry, PendingAction, ChatResponse } from './chatbot.service';
import { FormatMarkdownPipe } from './format-markdown.pipe';

interface ChatMessage {
  role: 'user' | 'assistant';
  content: string;
  timestamp: Date;
  pendingAction?: PendingAction;
  isLoading?: boolean;
}

interface ActivityLogEntry {
  action: string;
  success: boolean;
  timestamp: Date;
}

@Component({
  selector: 'app-chatbot',
  standalone: true,
  imports: [CommonModule, FormsModule, TranslateModule, FormatMarkdownPipe],
  templateUrl: './chatbot.html',
  styleUrls: ['./chatbot.css', './chatbot-mobile.css']
})
export class ChatbotComponent implements AfterViewChecked {
  @ViewChild('messagesContainer') private messagesContainer!: ElementRef;

  messages: ChatMessage[] = [];
  inputMessage: string = '';
  isLoading: boolean = false;
  pendingAction: PendingAction | null = null;
  activityLog: ActivityLogEntry[] = [];

  constructor(private chatbotService: ChatbotService) {
    this.messages.push({
      role: 'assistant',
      content: '¡Hola! 👋 Soy tu asistente de administración de MagicWorld. Puedo ayudarte a gestionar:\n\n• **Descuentos**: crear, editar, eliminar y listar\n• **Tipos de entrada**: crear, editar, eliminar y listar\n• **Atracciones**: crear, editar, eliminar y listar\n\n¿En qué puedo ayudarte hoy?\n\n---\n\nHi! 👋 I\'m your MagicWorld administration assistant. I can help you manage:\n\n• **Discounts**: create, edit, delete and list\n• **Ticket Types**: create, edit, delete and list\n• **Attractions**: create, edit, delete and list\n\nHow can I help you today?',
      timestamp: new Date()
    });
  }

  ngAfterViewChecked() {
    this.scrollToBottom();
  }

  private scrollToBottom(): void {
    try {
      this.messagesContainer.nativeElement.scrollTop =
        this.messagesContainer.nativeElement.scrollHeight;
    } catch (err) {}
  }

  private extractActionFromMessage(userMessage: string, response: ChatResponse): string {
    const responseMsg = response.message.toLowerCase();

    // Detect operation type from response
    if (responseMsg.includes('✅') && (responseMsg.includes('created') || responseMsg.includes('creado'))) {
      if (responseMsg.includes('discount') || responseMsg.includes('descuento')) {
        return '✨ Discount created';
      } else if (responseMsg.includes('ticket') || responseMsg.includes('entrada')) {
        return '✨ Ticket type created';
      } else if (responseMsg.includes('attraction') || responseMsg.includes('atracción')) {
        return '✨ Attraction created';
      }
    }

    if (responseMsg.includes('✅') && (responseMsg.includes('updated') || responseMsg.includes('actualizado'))) {
      if (responseMsg.includes('discount') || responseMsg.includes('descuento')) {
        return '✏️ Discount updated';
      } else if (responseMsg.includes('ticket') || responseMsg.includes('entrada')) {
        return '✏️ Ticket type updated';
      } else if (responseMsg.includes('attraction') || responseMsg.includes('atracción')) {
        return '✏️ Attraction updated';
      }
    }

    if (responseMsg.includes('✅') && (responseMsg.includes('deleted') || responseMsg.includes('eliminado'))) {
      if (responseMsg.includes('discount') || responseMsg.includes('descuento')) {
        return '🗑️ Discount deleted';
      } else if (responseMsg.includes('ticket') || responseMsg.includes('entrada')) {
        return '🗑️ Ticket type deleted';
      } else if (responseMsg.includes('attraction') || responseMsg.includes('atracción')) {
        return '🗑️ Attraction deleted';
      }
    }

    if (responseMsg.includes('📋') || responseMsg.includes('🎫') || responseMsg.includes('🎢')) {
      if (responseMsg.includes('discount') || responseMsg.includes('descuento')) {
        return '📋 Listed discounts';
      } else if (responseMsg.includes('ticket') || responseMsg.includes('entrada')) {
        return '📋 Listed ticket types';
      } else if (responseMsg.includes('attraction') || responseMsg.includes('atracción')) {
        return '📋 Listed attractions';
      }
    }

    if (responseMsg.includes('⚠️') && (responseMsg.includes('confirmation') || responseMsg.includes('confirmación'))) {
      return '⚠️ Confirmation requested';
    }

    if (responseMsg.includes('❌') && (responseMsg.includes('cancelled') || responseMsg.includes('cancelada'))) {
      return '❌ Operation cancelled';
    }

    if (responseMsg.includes('❌')) {
      return '❌ Error occurred';
    }

    return '💬 Response received';
  }

  sendMessage(): void {
    if (!this.inputMessage.trim() || this.isLoading) return;

    const userMessage = this.inputMessage.trim();
    this.inputMessage = '';

    // Add user message
    this.messages.push({
      role: 'user',
      content: userMessage,
      timestamp: new Date()
    });

    // Add loading message
    this.messages.push({
      role: 'assistant',
      content: '',
      timestamp: new Date(),
      isLoading: true
    });

    this.isLoading = true;

    // Build history for context
    const history: ChatHistoryEntry[] = this.messages
      .filter(m => !m.isLoading)
      .slice(-10)
      .map(m => ({
        role: m.role,
        content: m.content
      }));

    // Send to backend
    this.chatbotService.sendMessage({
      message: userMessage,
      history: history.slice(0, -1),
      pendingAction: this.pendingAction || undefined
    }).subscribe({
      next: (response: ChatResponse) => {
        // Remove loading message
        this.messages = this.messages.filter(m => !m.isLoading);

        // Add assistant response
        this.messages.push({
          role: 'assistant',
          content: response.message,
          timestamp: new Date(),
          pendingAction: response.pendingAction
        });

        // Add to activity log
        const actionDescription = this.extractActionFromMessage(userMessage, response);
        this.activityLog.unshift({
          action: actionDescription,
          success: response.success,
          timestamp: new Date()
        });

        // Keep only last 20 activities
        if (this.activityLog.length > 20) {
          this.activityLog = this.activityLog.slice(0, 20);
        }

        // Save pending action if exists
        this.pendingAction = response.pendingAction || null;
        this.isLoading = false;
      },
      error: (error) => {
        // Remove loading message
        this.messages = this.messages.filter(m => !m.isLoading);

        const errorMessage = this.interpretError(error);
        this.messages.push({
          role: 'assistant',
          content: errorMessage,
          timestamp: new Date()
        });

        // Add to activity log
        this.activityLog.unshift({
          action: '❌ Request failed',
          success: false,
          timestamp: new Date()
        });

        this.pendingAction = null;
        this.isLoading = false;
        console.error('Error:', error);
      }
    });
  }

  private interpretError(error: any): string {
    let errorMsg = error?.error?.message || error?.message || '';

    if (errorMsg.includes('date') || errorMsg.includes('fecha')) {
      return '❌ The date format is invalid. Please use YYYY-MM-DD format (e.g., 2025-12-31).\n\n❌ El formato de fecha no es válido. Por favor, usa el formato AAAA-MM-DD (ej: 2025-12-31).';
    }

    if (errorMsg.includes('duplicate') || errorMsg.includes('already exists')) {
      return '❌ This item already exists. Please use a different name or code.\n\n❌ Este elemento ya existe. Por favor, usa un nombre o código diferente.';
    }

    if (errorMsg.includes('not found') || errorMsg.includes('no encontr')) {
      return '❌ The requested item was not found. Please verify the ID or name.\n\n❌ El elemento solicitado no fue encontrado. Por favor, verifica el ID o nombre.';
    }

    if (error.status === 401 || error.status === 403) {
      return '❌ You don\'t have permission to perform this action.\n\n❌ No tienes permiso para realizar esta acción.';
    }

    if (error.status === 500) {
      return '❌ A server error occurred. Please try again later.\n\n❌ Ha ocurrido un error en el servidor. Por favor, inténtalo más tarde.';
    }

    return '❌ An error occurred while processing your request. Please try again.\n\n❌ Ha ocurrido un error al procesar tu solicitud. Por favor, inténtalo de nuevo.';
  }

  confirmAction(): void {
    if (!this.pendingAction) return;
    this.inputMessage = 'Sí, confirmo / Yes, I confirm';
    this.sendMessage();
  }

  cancelAction(): void {
    this.pendingAction = null;
    this.inputMessage = 'No, cancelar / No, cancel';
    this.sendMessage();
  }

  onKeyPress(event: KeyboardEvent): void {
    if (event.key === 'Enter' && !event.shiftKey) {
      event.preventDefault();
      this.sendMessage();
    }
  }

  clearChat(): void {
    this.messages = [{
      role: 'assistant',
      content: '¡Chat reiniciado! 🔄 ¿En qué puedo ayudarte?\n\nChat restarted! 🔄 How can I help you?',
      timestamp: new Date()
    }];
    this.pendingAction = null;
  }

  clearActivity(): void {
    this.activityLog = [];
  }
}
