import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { shareReplay } from 'rxjs/operators';
import { Movie } from '../models/api.models';

@Injectable({ providedIn: 'root' })
export class MovieService {
  private http = inject(HttpClient);
  private baseUrl = '/api/movies';

  getAll(): Observable<Movie[]> {
    return this.http.get<Movie[]>(this.baseUrl).pipe(shareReplay(1));
  }

  getById(id: number): Observable<Movie> {
    return this.http.get<Movie>(`${this.baseUrl}/${id}`);
  }
}
