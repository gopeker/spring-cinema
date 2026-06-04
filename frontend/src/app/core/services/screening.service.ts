import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { shareReplay } from 'rxjs/operators';
import { Screening, ScreeningSeats, Seat } from '../models/api.models';

@Injectable({ providedIn: 'root' })
export class ScreeningService {
  private http = inject(HttpClient);
  private baseUrl = '/api/screenings';

  getAll(): Observable<Screening[]> {
    return this.http.get<Screening[]>(this.baseUrl).pipe(shareReplay(1));
  }

  getByMovie(movieId: number): Observable<Screening[]> {
    return this.http.get<Screening[]>(`${this.baseUrl}?movieId=${movieId}`).pipe(shareReplay(1));
  }

  getByDate(date: string): Observable<Screening[]> {
    return this.http.get<Screening[]>(`${this.baseUrl}?date=${date}`);
  }

  getById(id: number): Observable<Screening> {
    return this.http.get<Screening>(`${this.baseUrl}/${id}`);
  }

  getSeats(screeningId: number): Observable<ScreeningSeats> {
    return this.http.get<ScreeningSeats>(`${this.baseUrl}/${screeningId}/seats`);
  }
}
