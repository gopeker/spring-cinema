import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { AdminService } from './admin.service';
import {
  Page, MovieDto, ScreeningDto, UserDto,
  MovieCreateRequest, ScreeningCreateRequest, UserCreateRequest, UserUpdateRequest, UserSearchRequest
} from '../models/api.models';

const mockPageMovie: Page<MovieDto> = {
  content: [
    { id: 1, title: 'Inception', description: 'Thriller', duration: 148, posterUrl: 'poster.jpg' },
  ],
  totalElements: 1,
  totalPages: 1,
  size: 10,
  number: 0,
};

const mockPageScreening: Page<ScreeningDto> = {
  content: [
    {
      id: 1,
      movie: { id: 1, title: 'Inception', duration: 148 },
      showroom: { id: 1, name: 'Hall A', rows: 10, seatsPerRow: 20, totalSeats: 200 },
      startTime: '2026-06-15T20:00:00',
      basePrice: 15,
    },
  ],
  totalElements: 1,
  totalPages: 1,
  size: 10,
  number: 0,
};

const mockPageUser: Page<UserDto> = {
  content: [
    { id: 1, name: 'John', email: 'john@example.com', role: 'USER', address: { city: 'Springfield' } },
  ],
  totalElements: 1,
  totalPages: 1,
  size: 10,
  number: 0,
};

describe('AdminService', () => {
  let service: AdminService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(AdminService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  // ── Movies ──────────────────────────────────────────────────────────────

  describe('getMovies()', () => {
    it('should fetch movies with default params', () => {
      service.getMovies().subscribe(page => {
        expect(page.content).toHaveLength(1);
        expect(page.content[0].title).toBe('Inception');
      });

      const req = httpMock.expectOne(r => r.url === '/api/admin/movies');
      expect(req.request.method).toBe('GET');
      expect(req.request.params.get('page')).toBe('0');
      expect(req.request.params.get('size')).toBe('10');
      expect(req.request.params.get('sort')).toBe('id,asc');
      req.flush(mockPageMovie);
    });

    it('should include search params when provided', () => {
      service.getMovies({ title: 'Inception', minDuration: 100, maxDuration: 200 }, 0, 5, 'title', 'desc').subscribe();

      const req = httpMock.expectOne(r => r.url === '/api/admin/movies');
      expect(req.request.params.get('title')).toBe('Inception');
      expect(req.request.params.get('minDuration')).toBe('100');
      expect(req.request.params.get('maxDuration')).toBe('200');
      expect(req.request.params.get('page')).toBe('0');
      expect(req.request.params.get('size')).toBe('5');
      expect(req.request.params.get('sort')).toBe('title,desc');
      req.flush(mockPageMovie);
    });

    it('should not include undefined search params', () => {
      service.getMovies({ title: 'Test' }, 0, 10, 'id', 'asc').subscribe();

      const req = httpMock.expectOne(r => r.url === '/api/admin/movies');
      expect(req.request.params.has('description')).toBe(false);
      expect(req.request.params.has('minDuration')).toBe(false);
      req.flush(mockPageMovie);
    });
  });

  describe('searchMovies()', () => {
    it('should search movies by name', () => {
      service.searchMovies('Inception').subscribe(page => {
        expect(page.content).toHaveLength(1);
      });

      const req = httpMock.expectOne(r => r.url === '/api/admin/movies');
      expect(req.request.params.get('search')).toBe('Inception');
      expect(req.request.params.get('sort')).toBe('title,asc');
      req.flush(mockPageMovie);
    });
  });

  describe('createMovie()', () => {
    it('should POST a new movie', () => {
      const createReq: MovieCreateRequest = { title: 'New Movie', duration: 120 };
      service.createMovie(createReq).subscribe(movie => {
        expect(movie.title).toBe('Inception');
      });

      const req = httpMock.expectOne('/api/admin/movies');
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual(createReq);
      req.flush(mockPageMovie.content[0]);
    });
  });

  describe('updateMovie()', () => {
    it('should PUT to update a movie', () => {
      const updateReq: MovieCreateRequest = { title: 'Updated', duration: 130 };
      service.updateMovie(1, updateReq).subscribe();

      const req = httpMock.expectOne('/api/admin/movies/1');
      expect(req.request.method).toBe('PUT');
      expect(req.request.body).toEqual(updateReq);
      req.flush({});
    });
  });

  describe('deleteMovie()', () => {
    it('should DELETE a movie', () => {
      service.deleteMovie(1).subscribe();

      const req = httpMock.expectOne('/api/admin/movies/1');
      expect(req.request.method).toBe('DELETE');
      req.flush(null);
    });
  });

  // ── Screenings ──────────────────────────────────────────────────────────

  describe('getScreenings()', () => {
    it('should fetch screenings with default params', () => {
      service.getScreenings().subscribe(page => {
        expect(page.content).toHaveLength(1);
      });

      const req = httpMock.expectOne(r => r.url === '/api/admin/screenings');
      expect(req.request.method).toBe('GET');
      expect(req.request.params.get('page')).toBe('0');
      expect(req.request.params.get('size')).toBe('10');
      req.flush(mockPageScreening);
    });

    it('should include search params when provided', () => {
      service.getScreenings({ movieId: 1, minPrice: 10, maxPrice: 20 }, 1, 5, 'startTime', 'desc').subscribe();

      const req = httpMock.expectOne(r => r.url === '/api/admin/screenings');
      expect(req.request.params.get('movieId')).toBe('1');
      expect(req.request.params.get('minPrice')).toBe('10');
      expect(req.request.params.get('maxPrice')).toBe('20');
      expect(req.request.params.get('sort')).toBe('startTime,desc');
      req.flush(mockPageScreening);
    });

    it('should include from/to date params', () => {
      service.getScreenings({ from: '2026-06-01T00:00:00', to: '2026-06-30T23:59:59' }).subscribe();

      const req = httpMock.expectOne(r => r.url === '/api/admin/screenings');
      expect(req.request.params.get('from')).toBe('2026-06-01T00:00:00');
      expect(req.request.params.get('to')).toBe('2026-06-30T23:59:59');
      req.flush(mockPageScreening);
    });
  });

  describe('createScreening()', () => {
    it('should POST a new screening', () => {
      const createReq: ScreeningCreateRequest = { movieId: 1, showroomId: 1, startTime: '2026-06-15T20:00:00', basePrice: 15 };
      service.createScreening(createReq).subscribe();

      const req = httpMock.expectOne('/api/admin/screenings');
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual(createReq);
      req.flush({});
    });
  });

  describe('updateScreening()', () => {
    it('should PUT to update a screening', () => {
      const updateReq: ScreeningCreateRequest = { movieId: 1, showroomId: 2, startTime: '2026-06-16T20:00:00', basePrice: 18 };
      service.updateScreening(1, updateReq).subscribe();

      const req = httpMock.expectOne('/api/admin/screenings/1');
      expect(req.request.method).toBe('PUT');
      expect(req.request.body).toEqual(updateReq);
      req.flush({});
    });
  });

  describe('deleteScreening()', () => {
    it('should DELETE a screening', () => {
      service.deleteScreening(1).subscribe();

      const req = httpMock.expectOne('/api/admin/screenings/1');
      expect(req.request.method).toBe('DELETE');
      req.flush(null);
    });
  });

  // ── Showrooms ───────────────────────────────────────────────────────────

  describe('getShowrooms()', () => {
    it('should fetch showrooms from public endpoint', () => {
      service.getShowrooms().subscribe(showrooms => {
        expect(showrooms).toHaveLength(1);
      });

      const req = httpMock.expectOne('/api/showrooms');
      expect(req.request.method).toBe('GET');
      req.flush([{ id: 1, name: 'Hall A', rows: 10, seatsPerRow: 20, totalSeats: 200 }]);
    });
  });

  // ── Users ───────────────────────────────────────────────────────────────

  describe('getUsers()', () => {
    it('should fetch users with default params', () => {
      service.getUsers().subscribe(page => {
        expect(page.content).toHaveLength(1);
        expect(page.content[0].name).toBe('John');
      });

      const req = httpMock.expectOne(r => r.url === '/api/admin/users');
      expect(req.request.method).toBe('GET');
      expect(req.request.params.get('page')).toBe('0');
      expect(req.request.params.get('size')).toBe('10');
      expect(req.request.params.get('sort')).toBe('id,asc');
      req.flush(mockPageUser);
    });

    it('should pass custom paging/sort params', () => {
      service.getUsers(1, 25, 'name', 'desc').subscribe();

      const req = httpMock.expectOne(r => r.url === '/api/admin/users');
      expect(req.request.params.get('page')).toBe('1');
      expect(req.request.params.get('size')).toBe('25');
      expect(req.request.params.get('sort')).toBe('name,desc');
      req.flush(mockPageUser);
    });
  });

  describe('searchUsers()', () => {
    it('should POST search request with paging', () => {
      const searchReq: UserSearchRequest = { name: 'John', city: 'Springfield' };
      service.searchUsers(searchReq, 0, 10, 'name', 'asc').subscribe(page => {
        expect(page.content).toHaveLength(1);
      });

      const req = httpMock.expectOne(r => r.url === '/api/admin/users/search');
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual(searchReq);
      expect(req.request.params.get('page')).toBe('0');
      expect(req.request.params.get('sort')).toBe('name,asc');
      req.flush(mockPageUser);
    });
  });

  describe('getUser()', () => {
    it('should GET a single user by id', () => {
      service.getUser(1).subscribe(user => {
        expect(user.name).toBe('John');
      });

      const req = httpMock.expectOne('/api/admin/users/1');
      expect(req.request.method).toBe('GET');
      req.flush(mockPageUser.content[0]);
    });
  });

  describe('createUser()', () => {
    it('should POST a new user', () => {
      const createReq: UserCreateRequest = { name: 'Jane', email: 'jane@example.com', password: 'pass123' };
      service.createUser(createReq).subscribe();

      const req = httpMock.expectOne('/api/admin/users');
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual(createReq);
      req.flush({});
    });
  });

  describe('updateUser()', () => {
    it('should PUT to update a user', () => {
      const updateReq: UserUpdateRequest = { name: 'John Updated', email: 'john.updated@example.com' };
      service.updateUser(1, updateReq).subscribe();

      const req = httpMock.expectOne('/api/admin/users/1');
      expect(req.request.method).toBe('PUT');
      expect(req.request.body).toEqual(updateReq);
      req.flush({});
    });
  });

  describe('deleteUser()', () => {
    it('should DELETE a user', () => {
      service.deleteUser(1).subscribe();

      const req = httpMock.expectOne('/api/admin/users/1');
      expect(req.request.method).toBe('DELETE');
      req.flush(null);
    });
  });
});
