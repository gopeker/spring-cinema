import { TestBed } from '@angular/core/testing';
import { BookingService } from './booking.service';

describe('BookingService', () => {
  let service: BookingService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(BookingService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should start with empty seats and zero screeningId', () => {
    expect(service.selectedSeats()).toEqual([]);
    expect(service.screeningId()).toBe(0);
    expect(service.totalPrice()).toBe(0);
  });

  it('should set seats and screeningId via setSeats()', () => {
    service.setSeats(
      [{ seatRow: 'A', seatNumber: 1, price: 12.5 }],
      42
    );
    expect(service.selectedSeats()).toHaveLength(1);
    expect(service.selectedSeats()[0].seatRow).toBe('A');
    expect(service.screeningId()).toBe(42);
  });

  it('should compute totalPrice from selected seats', () => {
    service.setSeats([
      { seatRow: 'A', seatNumber: 1, price: 10 },
      { seatRow: 'B', seatNumber: 2, price: 15.5 },
    ], 1);
    expect(service.totalPrice()).toBe(25.5);
  });

  it('should clear all state', () => {
    service.setSeats([{ seatRow: 'A', seatNumber: 1, price: 10 }], 5);
    service.clear();
    expect(service.selectedSeats()).toEqual([]);
    expect(service.screeningId()).toBe(0);
    expect(service.totalPrice()).toBe(0);
  });

  it('should update seats reactively', () => {
    service.selectedSeats.update(seats => [
      ...seats,
      { seatRow: 'C', seatNumber: 3, price: 8 },
    ]);
    expect(service.selectedSeats()).toHaveLength(1);
    expect(service.totalPrice()).toBe(8);
  });
});
