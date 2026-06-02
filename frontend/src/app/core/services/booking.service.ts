import { Injectable, signal, computed } from '@angular/core';

export interface SelectedSeat {
  seatRow: string;
  seatNumber: number;
  price: number;
}

@Injectable({ providedIn: 'root' })
export class BookingService {
  selectedSeats = signal<SelectedSeat[]>([]);
  screeningId = signal<number>(0);

  totalPrice = computed(() =>
    this.selectedSeats().reduce((sum, seat) => sum + seat.price, 0)
  );

  setSeats(seats: SelectedSeat[], screeningId: number): void {
    this.selectedSeats.set(seats);
    this.screeningId.set(screeningId);
  }

  clear(): void {
    this.selectedSeats.set([]);
    this.screeningId.set(0);
  }
}
