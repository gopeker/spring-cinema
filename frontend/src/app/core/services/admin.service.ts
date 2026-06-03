import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import {
  Page,
  MovieDto, MovieCreateRequest,
  ScreeningDto, ScreeningCreateRequest,
  ShowroomDto,
  UserDto, UserCreateRequest, UserUpdateRequest, UserSearchRequest,
  MovieSearchParams, ScreeningSearchParams
} from '../models/api.models';

@Injectable({ providedIn: 'root' })
export class AdminService {
  private readonly base = '/api/admin';

  constructor(private http: HttpClient) {}

  private static pageParams(page: number, size: number, sort: string, order: 'asc' | 'desc'): HttpParams {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString())
      .set('sort', `${sort},${order}`);
    return params;
  }

  // Movies
  getMovies(params?: MovieSearchParams, page = 0, size = 10, sort = 'id', order: 'asc' | 'desc' = 'asc'): Observable<Page<MovieDto>> {
    let httpParams = AdminService.pageParams(page, size, sort, order);
    if (params?.title) httpParams = httpParams.set('title', params.title);
    if (params?.description) httpParams = httpParams.set('description', params.description);
    if (params?.minDuration != null) httpParams = httpParams.set('minDuration', params.minDuration.toString());
    if (params?.maxDuration != null) httpParams = httpParams.set('maxDuration', params.maxDuration.toString());
    return this.http.get<Page<MovieDto>>(`${this.base}/movies`, { params: httpParams });
  }

  createMovie(movie: MovieCreateRequest): Observable<MovieDto> {
    return this.http.post<MovieDto>(`${this.base}/movies`, movie);
  }

  searchMovies(name: string): Observable<Page<MovieDto>> {
    const httpParams = AdminService.pageParams(0, 20, 'title', 'asc').set('search', name);
    return this.http.get<Page<MovieDto>>(`${this.base}/movies`, { params: httpParams });
  }

  updateMovie(id: number, movie: MovieCreateRequest): Observable<MovieDto> {
    return this.http.put<MovieDto>(`${this.base}/movies/${id}`, movie);
  }

  deleteMovie(id: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/movies/${id}`);
  }

  // Screenings
  getScreenings(params?: ScreeningSearchParams, page = 0, size = 10, sort = 'id', order: 'asc' | 'desc' = 'asc'): Observable<Page<ScreeningDto>> {
    let httpParams = AdminService.pageParams(page, size, sort, order);
    if (params?.movieId != null) httpParams = httpParams.set('movieId', params.movieId.toString());
    if (params?.from) httpParams = httpParams.set('from', params.from);
    if (params?.to) httpParams = httpParams.set('to', params.to);
    if (params?.minPrice != null) httpParams = httpParams.set('minPrice', params.minPrice.toString());
    if (params?.maxPrice != null) httpParams = httpParams.set('maxPrice', params.maxPrice.toString());
    return this.http.get<Page<ScreeningDto>>(`${this.base}/screenings`, { params: httpParams });
  }

  createScreening(screening: ScreeningCreateRequest): Observable<ScreeningDto> {
    return this.http.post<ScreeningDto>(`${this.base}/screenings`, screening);
  }

  updateScreening(id: number, screening: ScreeningCreateRequest): Observable<ScreeningDto> {
    return this.http.put<ScreeningDto>(`${this.base}/screenings/${id}`, screening);
  }

  deleteScreening(id: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/screenings/${id}`);
  }

  // Showrooms (read-only, public endpoint)
  getShowrooms(): Observable<ShowroomDto[]> {
    return this.http.get<ShowroomDto[]>('/api/showrooms');
  }

  // Users
  getUsers(page = 0, size = 10, sort = 'id', order: 'asc' | 'desc' = 'asc'): Observable<Page<UserDto>> {
    const httpParams = AdminService.pageParams(page, size, sort, order);
    return this.http.get<Page<UserDto>>(`${this.base}/users`, { params: httpParams });
  }

  searchUsers(request: UserSearchRequest, page = 0, size = 10, sort = 'id', order: 'asc' | 'desc' = 'asc'): Observable<Page<UserDto>> {
    const httpParams = AdminService.pageParams(page, size, sort, order);
    return this.http.post<Page<UserDto>>(`${this.base}/users/search`, request, { params: httpParams });
  }

  getUser(id: number): Observable<UserDto> {
    return this.http.get<UserDto>(`${this.base}/users/${id}`);
  }

  createUser(user: UserCreateRequest): Observable<UserDto> {
    return this.http.post<UserDto>(`${this.base}/users`, user);
  }

  updateUser(id: number, user: UserUpdateRequest): Observable<UserDto> {
    return this.http.put<UserDto>(`${this.base}/users/${id}`, user);
  }

  deleteUser(id: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/users/${id}`);
  }
}
