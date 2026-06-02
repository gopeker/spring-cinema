import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class TicketService {
  private http = inject(HttpClient);
  private baseUrl = '/api/tickets';

  purchase(screeningId: number, seatRow: string, seatNumber: number): Observable<any> {
    return this.http.post<any>(this.baseUrl, { screeningId, seatRow, seatNumber });
  }

  getMyTickets(): Observable<any[]> {
    return this.http.get<any[]>(`${this.baseUrl}/me`);
  }

  cancel(ticketId: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${ticketId}`);
  }
}
