import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';
import { MyTicketsComponent } from './my-tickets.component';
import { AuthService } from '../../core/services/auth.service';
import { Ticket } from '../../core/models/api.models';

const mockTicket: Ticket = {
  id: 1,
  seatRow: 'A',
  seatNumber: 5,
  price: 12.5,
  status: 'CONFIRMED',
  purchaseTime: '2026-06-01T10:00:00',
  screening: {
    id: 10,
    startTime: '2099-12-31T20:00:00',
    basePrice: 12.5,
    movie: { id: 1, title: 'Inception', description: '', duration: 148, posterUrl: '' },
    showroom: { id: 1, name: 'Hall A', rows: 10, seatsPerRow: 10, totalSeats: 100 },
  },
};

describe('MyTicketsComponent', () => {
  let httpMock: HttpTestingController;
  let authService: AuthService;

  beforeEach(async () => {
    localStorage.clear();

    await TestBed.configureTestingModule({
      imports: [MyTicketsComponent],
      providers: [
        provideRouter([]),
        provideHttpClient(),
        provideHttpClientTesting(),
      ],
    }).compileComponents();

    httpMock = TestBed.inject(HttpTestingController);
    authService = TestBed.inject(AuthService);

    // Simulate a logged-in user
    localStorage.setItem('token', 'test-token');
    localStorage.setItem('user', JSON.stringify({ userId: 1, name: 'Alice', email: 'alice@example.com', role: 'USER' }));
    authService.currentUser.set({ userId: 1, name: 'Alice', email: 'alice@example.com', role: 'USER' });
  });

  afterEach(() => {
    httpMock.verify();
    localStorage.clear();
  });

  it('should create', () => {
    const fixture = TestBed.createComponent(MyTicketsComponent);
    fixture.detectChanges();
    httpMock.expectOne('/api/tickets/me').flush([]);
    expect(fixture.componentInstance).toBeTruthy();
  });

  it('should display the logged-in username', async () => {
    const fixture = TestBed.createComponent(MyTicketsComponent);
    fixture.detectChanges();
    httpMock.expectOne('/api/tickets/me').flush([]);
    await fixture.whenStable();
    fixture.detectChanges();

    const el = fixture.nativeElement as HTMLElement;
    expect(el.querySelector('.page-subtitle')?.textContent).toContain('Alice');
  });

  it('should show empty state when no tickets', async () => {
    const fixture = TestBed.createComponent(MyTicketsComponent);
    fixture.detectChanges();
    httpMock.expectOne('/api/tickets/me').flush([]);
    await fixture.whenStable();
    fixture.detectChanges();

    const el = fixture.nativeElement as HTMLElement;
    expect(el.querySelector('.empty-state')).toBeTruthy();
  });

  it('should render ticket cards', async () => {
    const fixture = TestBed.createComponent(MyTicketsComponent);
    fixture.detectChanges();
    httpMock.expectOne('/api/tickets/me').flush([mockTicket]);
    await fixture.whenStable();
    fixture.detectChanges();

    const el = fixture.nativeElement as HTMLElement;
    expect(el.querySelector('.ticket-movie-title')?.textContent).toContain('Inception');
    expect(el.querySelector('.ticket-seat')?.textContent).toContain('Row A');
    expect(el.querySelector('.ticket-seat')?.textContent).toContain('Seat 5');
  });

  it('should remove ticket from list on cancel', async () => {
    const fixture = TestBed.createComponent(MyTicketsComponent);
    fixture.detectChanges();
    httpMock.expectOne('/api/tickets/me').flush([mockTicket]);
    await fixture.whenStable();
    fixture.detectChanges();

    vi.spyOn(window, 'confirm').mockReturnValue(true);
    fixture.componentInstance.cancelTicket(1);

    httpMock.expectOne('/api/tickets/1').flush(null);
    await fixture.whenStable();
    fixture.detectChanges();

    expect(fixture.componentInstance.tickets()).toHaveLength(0);
  });

  it('isScreeningStarted should return false for future screenings', () => {
    const fixture = TestBed.createComponent(MyTicketsComponent);
    fixture.detectChanges();
    httpMock.expectOne('/api/tickets/me').flush([]);
    const result = fixture.componentInstance.isScreeningStarted(mockTicket.screening);
    expect(result).toBe(false);
  });

  it('isScreeningStarted should return true for past screenings', () => {
    const fixture = TestBed.createComponent(MyTicketsComponent);
    fixture.detectChanges();
    httpMock.expectOne('/api/tickets/me').flush([]);
    const pastScreening = { ...mockTicket.screening, startTime: '2000-01-01T10:00:00' };
    expect(fixture.componentInstance.isScreeningStarted(pastScreening)).toBe(true);
  });
});
