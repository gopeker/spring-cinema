import { Component, inject, OnInit, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { ScreeningService } from '../../core/services/screening.service';
import { BookingService, SelectedSeat } from '../../core/services/booking.service';
import { formatDate, formatTime } from '../../shared/utils/cinema-format.utils';

interface Seat {
  seatRow: string;
  seatNumber: number;
  tier: string;
  available: boolean;
  price: number;
}

@Component({
  selector: 'app-seat-selection',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './seat-selection.component.html',
  styleUrl: './seat-selection.component.css'
})
export class SeatSelectionComponent implements OnInit {
  private screeningService = inject(ScreeningService);
  private bookingService = inject(BookingService);
  private router = inject(Router);
  private route = inject(ActivatedRoute);

  screening = signal<any>(null);
  seats = signal<Seat[]>([]);
  seatRows = signal<string[]>([]);
  loading = signal(true);
  error = signal<string | null>(null);

  // delegate selection state to the booking service
  selectedSeats = this.bookingService.selectedSeats;
  totalPrice = this.bookingService.totalPrice;

  readonly formatDate = formatDate;
  readonly formatTime = formatTime;

  ngOnInit(): void {
    this.bookingService.clear();
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      const screeningId = +id;
      this.loadScreening(screeningId);
      this.loadSeats(screeningId);
    }
  }

  loadScreening(id: number): void {
    this.screeningService.getById(id).subscribe({
      next: (data) => this.screening.set(data),
      error: () => {
        this.error.set('Failed to load screening');
        this.loading.set(false);
      }
    });
  }

  loadSeats(id: number): void {
    this.screeningService.getSeats(id).subscribe({
      next: (data) => {
        this.seats.set(data);
        this.computeSeatRows();
        this.loading.set(false);
      },
      error: () => {
        this.error.set('Failed to load seats');
        this.loading.set(false);
      }
    });
  }

  private computeSeatRows(): void {
    const rows = new Set(this.seats().map(s => s.seatRow));
    this.seatRows.set(Array.from(rows).sort());
  }

  getSeatsForRow(row: string): Seat[] {
    return this.seats().filter(s => s.seatRow === row);
  }

  isSelected(seat: Seat): boolean {
    return this.selectedSeats().some(s => s.seatRow === seat.seatRow && s.seatNumber === seat.seatNumber);
  }

  toggleSeat(seat: Seat): void {
    if (!seat.available) return;
    this.bookingService.selectedSeats.update(current => {
      const index = current.findIndex(s => s.seatRow === seat.seatRow && s.seatNumber === seat.seatNumber);
      if (index >= 0) return current.filter((_, i) => i !== index);
      return [...current, { seatRow: seat.seatRow, seatNumber: seat.seatNumber, price: seat.price }];
    });
  }

  proceedToCheckout(): void {
    this.bookingService.screeningId.set(this.screening().id);
    this.router.navigate(['/checkout']);
  }

  cancel(): void {
    this.router.navigate(['/']);
  }
}
