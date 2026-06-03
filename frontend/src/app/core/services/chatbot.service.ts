import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface ChatHistoryEntry {
  role: 'user' | 'assistant';
  content: string;
}

@Injectable({ providedIn: 'root' })
export class ChatbotService {
  private http = inject(HttpClient);
  private baseUrl = '/api/chatbot';

  chat(message: string, history: ChatHistoryEntry[]): Observable<{ response: string }> {
    return this.http.post<{ response: string }>(this.baseUrl, { message, history });
  }
}
