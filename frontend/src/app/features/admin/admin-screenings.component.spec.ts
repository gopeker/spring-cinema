import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { AdminScreeningsComponent } from './admin-screenings.component';
import { Page, ScreeningDto, MovieDto } from '../../core/models/api.models';

const mockMovies: Page<MovieDto> = {
  content: [
    { id: 1, title: 'Inception', duration: 148 },
    { id: 2, title: 'The Matrix', duration: 136 },
  ],
  totalElements: 2, totalPages: 1, size: 100, number: 0,
};

const mockScreeningPage: Page<ScreeningDto> = {
  content: [
    {
      id: 1,
      movie: { id: 1, title: 'Inception', duration: 148 },
      showroom: { id: 1, name: 'Hall A', rows: 10, seatsPerRow: 20, totalSeats: 200 },
      startTime: '2026-06-15T20:00:00',
      basePrice: 15,
    },
  ],
  totalElements: 1, totalPages: 1, size: 10, number: 0,
};

describe('AdminScreeningsComponent', () => {
  let httpMock: HttpTestingController;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AdminScreeningsComponent, NoopAnimationsModule],
      providers: [provideRouter([]), provideHttpClient(), provideHttpClientTesting()],
    }).compileComponents();
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => TestBed.resetTestingModule());

  it('should create and load data on init', () => {
    const fixture = TestBed.createComponent(AdminScreeningsComponent);
    fixture.detectChanges();
    httpMock.expectOne(r => r.url === '/api/admin/movies').flush(mockMovies);
    httpMock.expectOne(r => r.url === '/api/admin/screenings').flush(mockScreeningPage);

    const c = fixture.componentInstance;
    expect(c.movies()).toHaveLength(2);
    expect(c.screenings()).toHaveLength(1);
    expect(c.loading()).toBe(false);
  });

  it('filteredMovies should filter by search text', () => {
    const fixture = TestBed.createComponent(AdminScreeningsComponent);
    fixture.detectChanges();
    httpMock.expectOne(r => r.url === '/api/admin/movies').flush(mockMovies);
    httpMock.expectOne(r => r.url === '/api/admin/screenings').flush(mockScreeningPage);

    const c = fixture.componentInstance;
    expect(c.filteredMovies()).toHaveLength(2);

    c.movieSearch.set('Inception');
    expect(c.filteredMovies()).toHaveLength(1);
    expect(c.filteredMovies()[0].title).toBe('Inception');

    c.movieSearch.set('');
    expect(c.filteredMovies()).toHaveLength(2);
  });

  it('onMovieSearchChange should clear searchMovieId when value is empty', () => {
    const fixture = TestBed.createComponent(AdminScreeningsComponent);
    fixture.detectChanges();
    httpMock.expectOne(r => r.url === '/api/admin/movies').flush(mockMovies);
    httpMock.expectOne(r => r.url === '/api/admin/screenings').flush(mockScreeningPage);

    const c = fixture.componentInstance;
    c.onMovieSelected(1);
    expect(c.searchMovieId()).toBe(1);

    c.onMovieSearchChange('');
    expect(c.searchMovieId()).toBeUndefined();
  });

  it('onMovieSelected should set movieId and reload', () => {
    const fixture = TestBed.createComponent(AdminScreeningsComponent);
    fixture.detectChanges();
    httpMock.expectOne(r => r.url === '/api/admin/movies').flush(mockMovies);
    httpMock.expectOne(r => r.url === '/api/admin/screenings').flush(mockScreeningPage);

    fixture.componentInstance.onMovieSelected(1);

    expect(fixture.componentInstance.searchMovieId()).toBe(1);
    expect(fixture.componentInstance.pageIndex()).toBe(0);

    const req = httpMock.expectOne(r => r.url === '/api/admin/screenings');
    expect(req.request.params.get('movieId')).toBe('1');
    req.flush(mockScreeningPage);
  });

  it('clearMovieFilter should reset and reload', () => {
    const fixture = TestBed.createComponent(AdminScreeningsComponent);
    fixture.detectChanges();
    httpMock.expectOne(r => r.url === '/api/admin/movies').flush(mockMovies);
    httpMock.expectOne(r => r.url === '/api/admin/screenings').flush(mockScreeningPage);

    const c = fixture.componentInstance;
    c.searchMovieId.set(1);
    c.movieSearch.set('Inception');
    c.clearMovieFilter();

    expect(c.movieSearch()).toBe('');
    expect(c.searchMovieId()).toBeUndefined();

    httpMock.expectOne(r => r.url === '/api/admin/screenings').flush(mockScreeningPage);
  });

  it('onSortChange should toggle direction correctly', () => {
    const fixture = TestBed.createComponent(AdminScreeningsComponent);
    fixture.detectChanges();
    httpMock.expectOne(r => r.url === '/api/admin/movies').flush(mockMovies);
    httpMock.expectOne(r => r.url === '/api/admin/screenings').flush(mockScreeningPage);

    fixture.componentInstance.onSortChange({ active: 'basePrice', direction: 'desc' });
    expect(fixture.componentInstance.sortDirection()).toBe('desc');
    httpMock.expectOne(r => r.url === '/api/admin/screenings').flush(mockScreeningPage);

    fixture.componentInstance.onSortChange({ active: 'basePrice', direction: '' as any });
    expect(fixture.componentInstance.sortDirection()).toBe('asc');
    httpMock.expectOne(r => r.url === '/api/admin/screenings').flush(mockScreeningPage);
  });

  it('onPageChange should update paging and reload', () => {
    const fixture = TestBed.createComponent(AdminScreeningsComponent);
    fixture.detectChanges();
    httpMock.expectOne(r => r.url === '/api/admin/movies').flush(mockMovies);
    httpMock.expectOne(r => r.url === '/api/admin/screenings').flush(mockScreeningPage);

    fixture.componentInstance.onPageChange({ pageIndex: 2, pageSize: 25, length: 100 });

    expect(fixture.componentInstance.pageIndex()).toBe(2);
    expect(fixture.componentInstance.pageSize()).toBe(25);

    const req = httpMock.expectOne(r => r.url === '/api/admin/screenings');
    expect(req.request.params.get('page')).toBe('2');
    expect(req.request.params.get('size')).toBe('25');
    req.flush(mockScreeningPage);
  });

  it('deleteScreening should call API when confirmed', () => {
    vi.spyOn(window, 'confirm').mockReturnValue(true);
    const fixture = TestBed.createComponent(AdminScreeningsComponent);
    fixture.detectChanges();
    httpMock.expectOne(r => r.url === '/api/admin/movies').flush(mockMovies);
    httpMock.expectOne(r => r.url === '/api/admin/screenings').flush(mockScreeningPage);

    fixture.componentInstance.deleteScreening(1);

    const req = httpMock.expectOne('/api/admin/screenings/1');
    expect(req.request.method).toBe('DELETE');
    req.flush(null);

    httpMock.expectOne(r => r.url === '/api/admin/screenings').flush(mockScreeningPage);
  });

  it('deleteScreening should not call API when cancelled', () => {
    vi.spyOn(window, 'confirm').mockReturnValue(false);
    const fixture = TestBed.createComponent(AdminScreeningsComponent);
    fixture.detectChanges();
    httpMock.expectOne(r => r.url === '/api/admin/movies').flush(mockMovies);
    httpMock.expectOne(r => r.url === '/api/admin/screenings').flush(mockScreeningPage);

    fixture.componentInstance.deleteScreening(1);

    httpMock.expectNone(r => r.url === '/api/admin/screenings/1' && r.method === 'DELETE');
  });

  it('should send price filter params', () => {
    const fixture = TestBed.createComponent(AdminScreeningsComponent);
    fixture.detectChanges();
    httpMock.expectOne(r => r.url === '/api/admin/movies').flush(mockMovies);
    httpMock.expectOne(r => r.url === '/api/admin/screenings').flush(mockScreeningPage);

    fixture.componentInstance.searchMinPrice.set(10);
    fixture.componentInstance.searchMaxPrice.set(20);
    fixture.componentInstance.onSearch();

    const req = httpMock.expectOne(r => r.url === '/api/admin/screenings');
    expect(req.request.params.get('minPrice')).toBe('10');
    expect(req.request.params.get('maxPrice')).toBe('20');
    req.flush(mockScreeningPage);
  });
});
