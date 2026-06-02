import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';
import { HomeComponent } from './home.component';
import { Movie } from '../../core/models/api.models';

const mockMovies: Movie[] = [
  { id: 1, title: 'Inception', description: 'A mind-bending thriller', duration: 148, posterUrl: '', genre: 'Sci-Fi' },
  { id: 2, title: 'The Matrix', description: 'Reality is a simulation', duration: 136, posterUrl: '', genre: 'Action' },
];

describe('HomeComponent', () => {
  let httpMock: HttpTestingController;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [HomeComponent],
      providers: [
        provideRouter([]),
        provideHttpClient(),
        provideHttpClientTesting(),
      ],
    }).compileComponents();

    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('should create', () => {
    const fixture = TestBed.createComponent(HomeComponent);
    fixture.detectChanges();
    httpMock.expectOne('/api/movies').flush(mockMovies);
    expect(fixture.componentInstance).toBeTruthy();
  });

  it('should show loading spinner initially', () => {
    const fixture = TestBed.createComponent(HomeComponent);
    fixture.detectChanges();
    const el = fixture.nativeElement as HTMLElement;
    expect(el.querySelector('.loading-spinner')).toBeTruthy();
    httpMock.expectOne('/api/movies').flush([]);
  });

  it('should render movie cards after loading', async () => {
    const fixture = TestBed.createComponent(HomeComponent);
    fixture.detectChanges();

    httpMock.expectOne('/api/movies').flush(mockMovies);
    await fixture.whenStable();
    fixture.detectChanges();

    const el = fixture.nativeElement as HTMLElement;
    const titles = Array.from(el.querySelectorAll('.card-title')).map(e => e.textContent?.trim());
    expect(titles).toContain('Inception');
    expect(titles).toContain('The Matrix');
  });

  it('should hide loading spinner after data loads', async () => {
    const fixture = TestBed.createComponent(HomeComponent);
    fixture.detectChanges();

    httpMock.expectOne('/api/movies').flush(mockMovies);
    await fixture.whenStable();
    fixture.detectChanges();

    const el = fixture.nativeElement as HTMLElement;
    expect(el.querySelector('.loading-spinner')).toBeNull();
  });

  it('should hide loading spinner on error', async () => {
    const fixture = TestBed.createComponent(HomeComponent);
    fixture.detectChanges();

    httpMock.expectOne('/api/movies').flush('Server error', { status: 500, statusText: 'Error' });
    await fixture.whenStable();
    fixture.detectChanges();

    expect(fixture.componentInstance.loading()).toBe(false);
  });
});
