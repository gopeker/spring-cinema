import { Component, inject, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { ScreeningService } from '../../core/services/screening.service';
import { TicketService } from '../../core/services/ticket.service';
import { AuthService } from '../../core/services/auth.service';

interface Seat {
  seatRow: string;
  seatNumber: number;
  tier: string;
  available: boolean;
  price: number;
}

interface SelectedSeat {
  seatRow: string;
  seatNumber: number;
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
  private ticketService = inject(TicketService);
  private authService = inject(AuthService);
  private router = inject(Router);
  private route = inject(ActivatedRoute);
  private cdr = inject(ChangeDetectorRef);

  screening: any = null;
  seats: Seat[] = [];
  selectedSeats: SelectedSeat[] = [];
  seatRows: string[] = [];
  seatsPerRow = 10;
  loading = true;
  error: string | null = null;

  get totalPrice(): number {
    return this.selectedSeats.reduce((sum, s) => sum + s.price, 0);
  }

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      const screeningId = +id;
      this.loadScreening(screeningId);
      this.loadSeats(screeningId);
    }
  }

  loadScreening(id: number): void {
    this.screeningService.getById(id).subscribe({
      next: (data) => this.screening = data,
      error: (err) => {
        this.error = 'Failed to load screening';
        this.loading = false;
      }
    });
  }

loadSeats(id: number): void {
    this.screeningService.getSeats(id).subscribe({
      next: (data) => {
        this.seats = data;
        this.computeSeatRows();
        this.loading = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.error = 'Failed to load seats';
        this.loading = false;
        this.cdr.detectChanges();
      }
    });
  }

  computeSeatRows(): void {
    const rows = new Set(this.seats.map(s => s.seatRow));
    this.seatRows = Array.from(rows).sort();
    if (this.seatRows.length > 0) {
      const firstRow = this.seats.filter(s => s.seatRow === this.seatRows[0]);
      this.seatsPerRow = firstRow.length;
    }
  }

  getSeatsForRow(row: string): Seat[] {
    return this.seats.filter(s => s.seatRow === row);
  }

  isSelected(seat: Seat): boolean {
    return this.selectedSeats.some(s => s.seatRow === seat.seatRow && s.seatNumber === seat.seatNumber);
  }

  toggleSeat(seat: Seat): void {
    if (!seat.available) return;
    
    const index = this.selectedSeats.findIndex(s => s.seatRow === seat.seatRow && s.seatNumber === seat.seatNumber);
    if (index >= 0) {
      this.selectedSeats.splice(index, 1);
    } else {
      this.selectedSeats.push({
        seatRow: seat.seatRow,
        seatNumber: seat.seatNumber,
        price: seat.price
      });
    }
  }

  proceedToCheckout(): void {
    sessionStorage.setItem('selectedSeats', JSON.stringify(this.selectedSeats));
    sessionStorage.setItem('screeningId', this.screening.id.toString());
    this.router.navigate(['/checkout']);
  }

  cancel(): void {
    this.router.navigate(['/']);
  }

  formatDate(dateStr: string | undefined): string {
    if (!dateStr) return '';
    return new Date(dateStr).toLocaleDateString('en-GB', {
      weekday: 'short',
      day: 'numeric',
      month: 'short',
      year: 'numeric'
    });
  }

  formatTime(dateStr: string | undefined): string {
    if (!dateStr) return '';
    return new Date(dateStr).toLocaleTimeString('en-GB', {
      hour: '2-digit',
      minute: '2-digit',
      hour12: false
    });
  }
}