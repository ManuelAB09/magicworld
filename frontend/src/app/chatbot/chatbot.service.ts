import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, switchMap } from 'rxjs';
import { AuthService } from '../auth/auth.service';
import { getBackendBaseUrl } from '../config/backend';

export interface ChatHistoryEntry {
  role: 'user' | 'assistant';
  content: string;
}

export interface PendingAction {
  actionType: string;
  params: Record<string, any>;
  confirmationMessage: string;
}

export interface ChatRequest {
  message: string;
  history?: ChatHistoryEntry[];
  pendingAction?: PendingAction;
}

export interface ChatResponse {
  message: string;
  pendingAction?: PendingAction;
  success: boolean;
  data?: any;
}

@Injectable({ providedIn: 'root' })
export class ChatbotService {
  private baseUrl = `${getBackendBaseUrl()}/api/v1/chatbot`;

  constructor(private http: HttpClient, private auth: AuthService) {}

  sendMessage(request: ChatRequest): Observable<ChatResponse> {
    const headers = new HttpHeaders();
    return this.auth.ensureCsrfToken(headers).pipe(
      switchMap(h => this.http.post<ChatResponse>(
        `${this.baseUrl}/message`,
        request,
        { withCredentials: true, headers: h }
      ))
    );
  }
}

