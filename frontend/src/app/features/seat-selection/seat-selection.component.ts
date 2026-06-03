import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { ScreeningService } from '../../core/services/screening.service';
import { BookingService, SelectedSeat } from '../../core/services/booking.service';
import { Screening, Seat } from '../../core/models/api.models';
import { formatDate, formatTime } from '../../shared/utils/cinema-format.utils';

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

  screening = signal<Screening | null>(null);
  seats = signal<Seat[]>([]);
  seatRows = signal<string[]>([]);
  loading = signal(true);
  error = signal<string | null>(null);

  selectedSeats = this.bookingService.selectedSeats;
  totalPrice = this.bookingService.totalPrice;

  readonly formatDate = formatDate;
  readonly formatTime = formatTime;

  ngOnInit(): void {
    this.bookingService.clear();
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      const screeningId = +id;
      this.loadSeats(screeningId);
    }
  }

  loadSeats(id: number): void {
    this.screeningService.getSeats(id).subscribe({
      next: (data) => {
        this.screening.set(data.screening);
        this.seats.set(data.seats);
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
    const s = this.screening();
    if (s) this.bookingService.screeningId.set(s.id);
    this.router.navigate(['/checkout']);
  }

  cancel(): void {
    this.router.navigate(['/']);
  }
}
