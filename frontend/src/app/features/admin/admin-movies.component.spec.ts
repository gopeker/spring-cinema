import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { AdminMoviesComponent } from './admin-movies.component';
import { Page, MovieDto } from '../../core/models/api.models';

const mockMoviePage: Page<MovieDto> = {
  content: [
    { id: 1, title: 'Inception', description: 'Thriller', duration: 148, posterUrl: 'poster.jpg' },
    { id: 2, title: 'The Matrix', description: 'Sci-fi', duration: 136, posterUrl: 'matrix.jpg' },
  ],
  totalElements: 2,
  totalPages: 1,
  size: 10,
  number: 0,
};

describe('AdminMoviesComponent', () => {
  let httpMock: HttpTestingController;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AdminMoviesComponent, NoopAnimationsModule],
      providers: [provideRouter([]), provideHttpClient(), provideHttpClientTesting()],
    }).compileComponents();
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => TestBed.resetTestingModule());

  it('should create and load movies on init', () => {
    const fixture = TestBed.createComponent(AdminMoviesComponent);
    fixture.detectChanges();
    httpMock.expectOne(r => r.url === '/api/admin/movies').flush(mockMoviePage);

    const c = fixture.componentInstance;
    expect(c.movies()).toHaveLength(2);
    expect(c.totalElements()).toBe(2);
    expect(c.loading()).toBe(false);
  });

  it('should load with default paging/sort params', () => {
    const fixture = TestBed.createComponent(AdminMoviesComponent);
    fixture.detectChanges();
    const req = httpMock.expectOne(r => r.url === '/api/admin/movies');
    expect(req.request.params.get('page')).toBe('0');
    expect(req.request.params.get('size')).toBe('10');
    expect(req.request.params.get('sort')).toBe('id,asc');
    req.flush(mockMoviePage);
  });

  it('onSortChange should update sort and reload', () => {
    const fixture = TestBed.createComponent(AdminMoviesComponent);
    fixture.detectChanges();
    httpMock.expectOne(r => r.url === '/api/admin/movies').flush(mockMoviePage);

    fixture.componentInstance.onSortChange({ active: 'title', direction: 'desc' });

    expect(fixture.componentInstance.sortActive()).toBe('title');
    expect(fixture.componentInstance.sortDirection()).toBe('desc');
    expect(fixture.componentInstance.pageIndex()).toBe(0);

    const req = httpMock.expectOne(r => r.url === '/api/admin/movies');
    expect(req.request.params.get('sort')).toBe('title,desc');
    req.flush(mockMoviePage);
  });

  it('onSortChange should toggle direction when MatSort clears', () => {
    const fixture = TestBed.createComponent(AdminMoviesComponent);
    fixture.detectChanges();
    httpMock.expectOne(r => r.url === '/api/admin/movies').flush(mockMoviePage);

    // First: desc
    fixture.componentInstance.onSortChange({ active: 'title', direction: 'desc' });
    expect(fixture.componentInstance.sortDirection()).toBe('desc');
    httpMock.expectOne(r => r.url === '/api/admin/movies').flush(mockMoviePage);

    // Clear → toggle to asc
    fixture.componentInstance.onSortChange({ active: 'title', direction: '' as any });
    expect(fixture.componentInstance.sortDirection()).toBe('asc');
    httpMock.expectOne(r => r.url === '/api/admin/movies').flush(mockMoviePage);

    // Clear again → toggle to desc
    fixture.componentInstance.onSortChange({ active: 'title', direction: '' as any });
    expect(fixture.componentInstance.sortDirection()).toBe('desc');
    httpMock.expectOne(r => r.url === '/api/admin/movies').flush(mockMoviePage);
  });

  it('onSortChange should ignore when active is empty', () => {
    const fixture = TestBed.createComponent(AdminMoviesComponent);
    fixture.detectChanges();
    httpMock.expectOne(r => r.url === '/api/admin/movies').flush(mockMoviePage);

    const before = fixture.componentInstance.sortActive();
    fixture.componentInstance.onSortChange({ active: '', direction: 'asc' });
    expect(fixture.componentInstance.sortActive()).toBe(before);
  });

  it('onPageChange should update paging and reload', () => {
    const fixture = TestBed.createComponent(AdminMoviesComponent);
    fixture.detectChanges();
    httpMock.expectOne(r => r.url === '/api/admin/movies').flush(mockMoviePage);

    fixture.componentInstance.onPageChange({ pageIndex: 1, pageSize: 25, length: 50 });

    expect(fixture.componentInstance.pageIndex()).toBe(1);
    expect(fixture.componentInstance.pageSize()).toBe(25);

    const req = httpMock.expectOne(r => r.url === '/api/admin/movies');
    expect(req.request.params.get('page')).toBe('1');
    expect(req.request.params.get('size')).toBe('25');
    req.flush(mockMoviePage);
  });

  it('onSearch should reset to page 0 and reload', () => {
    const fixture = TestBed.createComponent(AdminMoviesComponent);
    fixture.detectChanges();
    httpMock.expectOne(r => r.url === '/api/admin/movies').flush(mockMoviePage);

    fixture.componentInstance.pageIndex.set(3);
    fixture.componentInstance.searchTitle.set('Inception');
    fixture.componentInstance.onSearch();

    expect(fixture.componentInstance.pageIndex()).toBe(0);

    const req = httpMock.expectOne(r => r.url === '/api/admin/movies');
    expect(req.request.params.get('title')).toBe('Inception');
    expect(req.request.params.get('page')).toBe('0');
    req.flush(mockMoviePage);
  });

  it('deleteMovie should call API when confirmed', () => {
    vi.spyOn(window, 'confirm').mockReturnValue(true);
    const fixture = TestBed.createComponent(AdminMoviesComponent);
    fixture.detectChanges();
    httpMock.expectOne(r => r.url === '/api/admin/movies').flush(mockMoviePage);

    fixture.componentInstance.deleteMovie(1);

    const req = httpMock.expectOne('/api/admin/movies/1');
    expect(req.request.method).toBe('DELETE');
    req.flush(null);

    httpMock.expectOne(r => r.url === '/api/admin/movies').flush(mockMoviePage);
  });

  it('deleteMovie should not call API when cancelled', () => {
    vi.spyOn(window, 'confirm').mockReturnValue(false);
    const fixture = TestBed.createComponent(AdminMoviesComponent);
    fixture.detectChanges();
    httpMock.expectOne(r => r.url === '/api/admin/movies').flush(mockMoviePage);

    fixture.componentInstance.deleteMovie(1);

    httpMock.expectNone(r => r.url === '/api/admin/movies/1' && r.method === 'DELETE');
  });

  it('hasSearchParams should reflect search state', () => {
    const fixture = TestBed.createComponent(AdminMoviesComponent);
    fixture.detectChanges();
    httpMock.expectOne(r => r.url === '/api/admin/movies').flush(mockMoviePage);

    const c = fixture.componentInstance;
    expect(c.hasSearchParams()).toBe(false);
    c.searchTitle.set('Test');
    expect(c.hasSearchParams()).toBe(true);
    c.searchTitle.set('');
    expect(c.hasSearchParams()).toBe(false);
    c.searchMinDuration.set(100);
    expect(c.hasSearchParams()).toBe(true);
  });

  it('should handle error gracefully', () => {
    const fixture = TestBed.createComponent(AdminMoviesComponent);
    fixture.detectChanges();
    httpMock.expectOne(r => r.url === '/api/admin/movies').flush('Error', { status: 500, statusText: 'Server Error' });

    expect(fixture.componentInstance.loading()).toBe(false);
    expect(fixture.componentInstance.movies()).toHaveLength(0);
  });
});
