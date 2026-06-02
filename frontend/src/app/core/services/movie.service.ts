import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Movie } from '../models/api.models';

@Injectable({ providedIn: 'root' })
export class MovieService {
  private http = inject(HttpClient);
  private baseUrl = '/api/movies';

  getAll(): Observable<Movie[]> {
    return this.http.get<Movie[]>(this.baseUrl);
  }

  getById(id: number): Observable<Movie> {
    return this.http.get<Movie>(`${this.baseUrl}/${id}`);
  }
}
