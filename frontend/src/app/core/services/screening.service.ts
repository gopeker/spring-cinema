import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Screening, Seat } from '../models/api.models';

@Injectable({ providedIn: 'root' })
export class ScreeningService {
  private http = inject(HttpClient);
  private baseUrl = '/api/screenings';

  getAll(): Observable<Screening[]> {
    return this.http.get<Screening[]>(this.baseUrl);
  }

  getByMovie(movieId: number): Observable<Screening[]> {
    return this.http.get<Screening[]>(`${this.baseUrl}?movieId=${movieId}`);
  }

  getByDate(date: string): Observable<Screening[]> {
    return this.http.get<Screening[]>(`${this.baseUrl}?date=${date}`);
  }

  getById(id: number): Observable<Screening> {
    return this.http.get<Screening>(`${this.baseUrl}/${id}`);
  }

  getSeats(screeningId: number): Observable<Seat[]> {
    return this.http.get<Seat[]>(`${this.baseUrl}/${screeningId}/seats`);
  }
}
