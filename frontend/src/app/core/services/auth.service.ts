import { Injectable, inject, signal, computed } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { AuthResponse, User } from '../models/api.models';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private http = inject(HttpClient);
  private baseUrl = '/api/auth';

  currentUser = signal<User | null>(this._loadUser());
  isLoggedIn = computed(() => !!this.currentUser());
  isAdmin = computed(() => this.currentUser()?.role === 'ADMIN');

  private _loadUser(): User | null {
    const raw = localStorage.getItem('user');
    if (!raw || raw === 'undefined') return null;
    try { return JSON.parse(raw) as User; } catch { return null; }
  }

  register(name: string, email: string, password: string): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.baseUrl}/register`, { name, email, password }).pipe(
      tap((res) => {
        this.saveToken(res.token);
        this.saveUser(res);
      })
    );
  }

  login(email: string, password: string): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.baseUrl}/login`, { email, password }).pipe(
      tap((res) => {
        this.saveToken(res.token);
        this.saveUser(res);
      })
    );
  }

  saveToken(token: string): void {
    localStorage.setItem('token', token);
  }

  getToken(): string | null {
    return localStorage.getItem('token');
  }

  saveUser(res: AuthResponse): void {
    const user: User = { userId: res.userId, name: res.name, email: res.email, role: res.role };
    localStorage.setItem('user', JSON.stringify(user));
    this.currentUser.set(user);
  }

  /** @deprecated Use currentUser() signal instead */
  getUser(): User | null {
    return this.currentUser();
  }

  logout(): void {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    this.currentUser.set(null);
  }
}
