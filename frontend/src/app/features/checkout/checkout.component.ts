import { Component, inject, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { forkJoin } from 'rxjs';
import { TicketService } from '../../core/services/ticket.service';
import { AuthService } from '../../core/services/auth.service';
import { ScreeningService } from '../../core/services/screening.service';

interface SelectedSeat {
  seatRow: string;
  seatNumber: number;
  price: number;
}

@Component({
  selector: 'app-checkout',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './checkout.component.html',
  styleUrl: './checkout.component.css'
})
export class CheckoutComponent implements OnInit {
  ticketService = inject(TicketService);
  authService = inject(AuthService);
  private router = inject(Router);
  private screeningService = inject(ScreeningService);
  private cdr = inject(ChangeDetectorRef);

  selectedSeats: SelectedSeat[] = [];
  screeningId: number = 0;
  screening: any = null;
  purchasedTickets: any[] = [];
  loading = false;
  error = '';
  loginError = '';

  email = '';
  password = '';

  get totalPrice(): number {
    return this.selectedSeats.reduce((sum, s) => sum + s.price, 0);
  }

  ngOnInit(): void {
    const stored = sessionStorage.getItem('selectedSeats');
    if (stored) {
      this.selectedSeats = JSON.parse(stored);
      const storedScreeningId = sessionStorage.getItem('screeningId');
      if (storedScreeningId) {
        this.screeningId = +storedScreeningId;
        this.loadScreening();
      }
    } else {
      const purchasedScreening = sessionStorage.getItem('purchasedScreening');
      if (purchasedScreening) {
        this.screening = JSON.parse(purchasedScreening);
      } else {
        this.router.navigate(['/']);
      }
    }
  }

  loadScreening(): void {
    this.screeningService.getById(this.screeningId).subscribe({
      next: (data) => {
        this.screening = data;
        this.cdr.detectChanges();
      }
    });
  }

  login(): void {
    this.loginError = '';
    this.authService.login(this.email, this.password).subscribe({
      next: (res) => {
        this.authService.saveToken(res.token);
        this.authService.saveUser(res.user);
      },
      error: () => {
        this.loginError = 'Invalid email or password';
      }
    });
  }

  confirmPurchase(): void {
    this.loading = true;
    this.error = '';

    const purchaseRequests = this.selectedSeats.map(seat => 
      this.ticketService.purchase(this.screeningId, seat.seatRow, seat.seatNumber)
    );

    forkJoin(purchaseRequests).subscribe({
      next: (results) => {
        this.purchasedTickets = results;
        const screeningData = this.screening || { 
          movie: { title: '', posterUrl: '' }, 
          showroom: { name: '' }, 
          startTime: '' 
        };
        sessionStorage.setItem('purchasedScreening', JSON.stringify(screeningData));
        sessionStorage.removeItem('selectedSeats');
        sessionStorage.removeItem('screeningId');
        this.loading = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.error = 'Failed to purchase tickets. Please try again.';
        this.loading = false;
        this.cdr.detectChanges();
      }
    });
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