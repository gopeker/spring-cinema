import { Injectable, inject, signal, computed, effect, DestroyRef } from '@angular/core';

export interface SelectedSeat {
  seatRow: string;
  seatNumber: number;
  price: number;
}

interface StoredBooking {
  selectedSeats: SelectedSeat[];
  screeningId: number;
}

const STORAGE_KEY = 'booking';

@Injectable({ providedIn: 'root' })
export class BookingService {
  selectedSeats = signal<SelectedSeat[]>(this._load().selectedSeats);
  screeningId = signal<number>(this._load().screeningId);

  totalPrice = computed(() =>
    this.selectedSeats().reduce((sum, seat) => sum + seat.price, 0)
  );

  private destroyRef = inject(DestroyRef);

  constructor() {
    effect(() => {
      this.selectedSeats();
      this.screeningId();
      this._persist();
    });
  }

  setSeats(seats: SelectedSeat[], screeningId: number): void {
    this.selectedSeats.set(seats);
    this.screeningId.set(screeningId);
    this._persist();
  }

  clear(): void {
    this.selectedSeats.set([]);
    this.screeningId.set(0);
  }

  private _persist(): void {
    const data: StoredBooking = {
      selectedSeats: this.selectedSeats(),
      screeningId: this.screeningId()
    };
    sessionStorage.setItem(STORAGE_KEY, JSON.stringify(data));
  }

  private _load(): StoredBooking {
    const raw = sessionStorage.getItem(STORAGE_KEY);
    if (!raw) return { selectedSeats: [], screeningId: 0 };
    try { return JSON.parse(raw) as StoredBooking; } catch { return { selectedSeats: [], screeningId: 0 }; }
  }
}
