import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class ScreeningService {
  private http = inject(HttpClient);
  private baseUrl = 'http://localhost:8080/api/screenings';

  getAll(): Observable<any[]> {
    return this.http.get<any[]>(this.baseUrl);
  }

  getByMovie(movieId: number): Observable<any[]> {
    return this.http.get<any[]>(`${this.baseUrl}?movieId=${movieId}`);
  }

  getByDate(date: string): Observable<any[]> {
    return this.http.get<any[]>(`${this.baseUrl}?date=${date}`);
  }

  getById(id: number): Observable<any> {
    return this.http.get<any>(`${this.baseUrl}/${id}`);
  }

  getSeats(screeningId: number): Observable<any[]> {
    return this.http.get<any[]>(`${this.baseUrl}/${screeningId}/seats`);
  }
}
