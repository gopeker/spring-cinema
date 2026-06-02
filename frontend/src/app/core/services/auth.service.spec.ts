import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { AuthService } from './auth.service';
import { AuthResponse } from '../models/api.models';

const mockAuthResponse: AuthResponse = {
  token: 'test-token',
  userId: 1,
  name: 'Alice',
  email: 'alice@example.com',
  role: 'USER',
};

describe('AuthService', () => {
  let service: AuthService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    localStorage.clear();
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(AuthService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
    localStorage.clear();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should start with null currentUser when localStorage is empty', () => {
    expect(service.currentUser()).toBeNull();
    expect(service.isLoggedIn()).toBe(false);
    expect(service.isAdmin()).toBe(false);
  });

  describe('login()', () => {
    it('should save token and user on successful login', () => {
      service.login('alice@example.com', 'password').subscribe();

      const req = httpMock.expectOne('/api/auth/login');
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual({ email: 'alice@example.com', password: 'password' });
      req.flush(mockAuthResponse);

      expect(localStorage.getItem('token')).toBe('test-token');
      expect(service.currentUser()?.name).toBe('Alice');
      expect(service.currentUser()?.email).toBe('alice@example.com');
      expect(service.isLoggedIn()).toBe(true);
    });

    it('should not store token field in currentUser', () => {
      service.login('alice@example.com', 'password').subscribe();
      httpMock.expectOne('/api/auth/login').flush(mockAuthResponse);

      const user = service.currentUser();
      expect((user as any)?.token).toBeUndefined();
    });
  });

  describe('logout()', () => {
    it('should clear currentUser and localStorage', () => {
      service.login('alice@example.com', 'password').subscribe();
      httpMock.expectOne('/api/auth/login').flush(mockAuthResponse);

      service.logout();

      expect(service.currentUser()).toBeNull();
      expect(service.isLoggedIn()).toBe(false);
      expect(localStorage.getItem('token')).toBeNull();
      expect(localStorage.getItem('user')).toBeNull();
    });
  });

  describe('isAdmin()', () => {
    it('should return true for ADMIN role', () => {
      service.login('admin@example.com', 'password').subscribe();
      httpMock.expectOne('/api/auth/login').flush({ ...mockAuthResponse, role: 'ADMIN' });

      expect(service.isAdmin()).toBe(true);
    });

    it('should return false for USER role', () => {
      service.login('alice@example.com', 'password').subscribe();
      httpMock.expectOne('/api/auth/login').flush(mockAuthResponse);

      expect(service.isAdmin()).toBe(false);
    });
  });

  describe('register()', () => {
    it('should save token and user on successful registration', () => {
      service.register('Alice', 'alice@example.com', 'password').subscribe();

      const req = httpMock.expectOne('/api/auth/register');
      expect(req.request.method).toBe('POST');
      req.flush(mockAuthResponse);

      expect(service.currentUser()?.name).toBe('Alice');
      expect(service.isLoggedIn()).toBe(true);
    });
  });
});
