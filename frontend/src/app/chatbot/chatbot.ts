import { Component, ElementRef, ViewChild, AfterViewChecked } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { TranslateModule } from '@ngx-translate/core';
import { ChatbotService, ChatHistoryEntry, PendingAction, ChatResponse } from './chatbot.service';
import { FormatMarkdownPipe } from './format-markdown.pipe';
import { extractActionFromMessage, interpretError, WELCOME_MESSAGE, RESET_MESSAGE } from './chatbot-utils';

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
  inputMessage = '';
  isLoading = false;
  pendingAction: PendingAction | null = null;
  activityLog: ActivityLogEntry[] = [];

  private readonly MAX_ACTIVITY_LOG = 20;
  private readonly HISTORY_SIZE = 10;

  constructor(private chatbotService: ChatbotService) {
    this.messages.push({ role: 'assistant', content: WELCOME_MESSAGE, timestamp: new Date() });
  }

  ngAfterViewChecked() {
    this.scrollToBottom();
  }

  private scrollToBottom(): void {
    try {
      this.messagesContainer.nativeElement.scrollTop = this.messagesContainer.nativeElement.scrollHeight;
    } catch {}
  }

  sendMessage(): void {
    if (!this.inputMessage.trim() || this.isLoading) return;

    const userMessage = this.inputMessage.trim();
    this.inputMessage = '';

    this.messages.push({ role: 'user', content: userMessage, timestamp: new Date() });
    this.messages.push({ role: 'assistant', content: '', timestamp: new Date(), isLoading: true });
    this.isLoading = true;

    const history: ChatHistoryEntry[] = this.messages
      .filter(m => !m.isLoading)
      .slice(-this.HISTORY_SIZE)
      .map(m => ({ role: m.role, content: m.content }));

    this.chatbotService.sendMessage({
      message: userMessage,
      history: history.slice(0, -1),
      pendingAction: this.pendingAction || undefined
    }).subscribe({
      next: (response) => this.handleResponse(response),
      error: (error) => this.handleError(error)
    });
  }

  private handleResponse(response: ChatResponse): void {
    this.messages = this.messages.filter(m => !m.isLoading);
    this.messages.push({
      role: 'assistant',
      content: response.message,
      timestamp: new Date(),
      pendingAction: response.pendingAction
    });

    this.addToActivityLog(extractActionFromMessage(response), response.success);
    this.pendingAction = response.pendingAction || null;
    this.isLoading = false;
  }

  private handleError(error: any): void {
    this.messages = this.messages.filter(m => !m.isLoading);
    this.messages.push({ role: 'assistant', content: interpretError(error), timestamp: new Date() });
    this.addToActivityLog('❌ Request failed', false);
    this.pendingAction = null;
    this.isLoading = false;
    console.error('Error:', error);
  }

  private addToActivityLog(action: string, success: boolean): void {
    this.activityLog.unshift({ action, success, timestamp: new Date() });
    if (this.activityLog.length > this.MAX_ACTIVITY_LOG) {
      this.activityLog = this.activityLog.slice(0, this.MAX_ACTIVITY_LOG);
    }
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
    this.messages = [{ role: 'assistant', content: RESET_MESSAGE, timestamp: new Date() }];
    this.pendingAction = null;
  }

  clearActivity(): void {
    this.activityLog = [];
  }
}
