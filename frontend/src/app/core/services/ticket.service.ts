import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Ticket } from '../models/api.models';

@Injectable({ providedIn: 'root' })
export class TicketService {
  private http = inject(HttpClient);
  private baseUrl = '/api/tickets';

  purchase(screeningId: number, seatRow: string, seatNumber: number): Observable<Ticket> {
    return this.http.post<Ticket>(this.baseUrl, { screeningId, seatRow, seatNumber });
  }

  getMyTickets(): Observable<Ticket[]> {
    return this.http.get<Ticket[]>(`${this.baseUrl}/me`);
  }

  cancel(ticketId: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${ticketId}`);
  }
}
