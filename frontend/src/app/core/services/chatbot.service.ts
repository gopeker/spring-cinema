import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class ChatbotService {
  private baseUrl = '/api/chatbot';

  constructor(private http: HttpClient) {}

  chat(message: string): Observable<{ response: string }> {
    return this.http.post<{ response: string }>(this.baseUrl, { message });
  }
}