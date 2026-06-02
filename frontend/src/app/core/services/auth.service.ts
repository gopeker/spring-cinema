import { Injectable, inject, signal, computed } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private http = inject(HttpClient);
  private baseUrl = '/api/auth';

  currentUser = signal<any | null>(this._loadUser());
  isLoggedIn = computed(() => !!this.currentUser());
  isAdmin = computed(() => this.currentUser()?.role === 'ADMIN');

  private _loadUser(): any | null {
    const raw = localStorage.getItem('user');
    if (!raw || raw === 'undefined') return null;
    try { return JSON.parse(raw); } catch { return null; }
  }

  register(name: string, email: string, password: string): Observable<any> {
    return this.http.post<any>(`${this.baseUrl}/register`, { name, email, password });
  }

  login(email: string, password: string): Observable<any> {
    return this.http.post<any>(`${this.baseUrl}/login`, { email, password }).pipe(
      tap((res) => {
        this.saveToken(res.token);
        this.saveUser(res.user);
      })
    );
  }

  saveToken(token: string): void {
    localStorage.setItem('token', token);
  }

  getToken(): string | null {
    return localStorage.getItem('token');
  }

  saveUser(user: any): void {
    localStorage.setItem('user', JSON.stringify(user));
    this.currentUser.set(user);
  }

  /** @deprecated Use currentUser() signal instead */
  getUser(): any | null {
    return this.currentUser();
  }

  logout(): void {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    this.currentUser.set(null);
  }
}
