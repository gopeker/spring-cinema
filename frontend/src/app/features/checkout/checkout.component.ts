import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { forkJoin } from 'rxjs';
import { TicketService } from '../../core/services/ticket.service';
import { AuthService } from '../../core/services/auth.service';
import { ScreeningService } from '../../core/services/screening.service';
import { BookingService } from '../../core/services/booking.service';
import { formatDate, formatTime } from '../../shared/utils/cinema-format.utils';

@Component({
  selector: 'app-checkout',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './checkout.component.html',
  styleUrl: './checkout.component.css'
})
export class CheckoutComponent implements OnInit {
  private ticketService = inject(TicketService);
  readonly authService = inject(AuthService);
  private router = inject(Router);
  private screeningService = inject(ScreeningService);
  private bookingService = inject(BookingService);

  // read directly from booking service
  selectedSeats = this.bookingService.selectedSeats;
  screeningId = this.bookingService.screeningId;
  totalPrice = this.bookingService.totalPrice;

  screening = signal<any>(null);
  purchasedTickets = signal<any[]>([]);
  loading = signal(false);
  error = signal('');
  loginError = signal('');

  email = '';
  password = '';

  readonly formatDate = formatDate;
  readonly formatTime = formatTime;

  ngOnInit(): void {
    if (this.screeningId() > 0) {
      this.loadScreening();
    } else {
      this.router.navigate(['/']);
    }
  }

  loadScreening(): void {
    this.screeningService.getById(this.screeningId()).subscribe({
      next: (data) => this.screening.set(data)
    });
  }

  login(): void {
    this.loginError.set('');
    this.authService.login(this.email, this.password).subscribe({
      error: () => this.loginError.set('Invalid email or password')
    });
  }

  confirmPurchase(): void {
    this.loading.set(true);
    this.error.set('');

    const purchaseRequests = this.selectedSeats().map(seat =>
      this.ticketService.purchase(this.screeningId(), seat.seatRow, seat.seatNumber)
    );

    forkJoin(purchaseRequests).subscribe({
      next: (results) => {
        this.purchasedTickets.set(results);
        this.bookingService.clear();
        this.loading.set(false);
      },
      error: () => {
        this.error.set('Failed to purchase tickets. Please try again.');
        this.loading.set(false);
      }
    });
  }
}
