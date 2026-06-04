import { Component, ChangeDetectionStrategy, DestroyRef, inject, OnInit, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { Router, RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { TicketService } from '../../core/services/ticket.service';
import { AuthService } from '../../core/services/auth.service';
import { Ticket, Screening } from '../../core/models/api.models';

@Component({
  selector: 'app-my-tickets',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [CommonModule, RouterLink],
  templateUrl: './my-tickets.component.html',
  styleUrl: './my-tickets.component.css'
})
export class MyTicketsComponent implements OnInit {
  private ticketService = inject(TicketService);
  private authService = inject(AuthService);
  private router = inject(Router);
  private destroyRef = inject(DestroyRef);

  tickets = signal<Ticket[]>([]);
  loading = signal(true);
  user = this.authService.currentUser;

  ngOnInit() {
    if (!this.authService.isLoggedIn()) {
      this.router.navigate(['/login']);
      return;
    }
    this.loadTickets();
  }

  loadTickets() {
    this.ticketService.getMyTickets().pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: (data) => {
        this.tickets.set(data);
        this.loading.set(false);
      },
      error: () => {
        this.loading.set(false);
      }
    });
  }

  cancelTicket(ticketId: number) {
    if (confirm('Are you sure you want to cancel this ticket?')) {
      this.ticketService.cancel(ticketId).subscribe({
        next: () => {
          this.tickets.update(current => current.filter(t => t.id !== ticketId));
        },
        error: () => {
          alert('Failed to cancel ticket');
        }
      });
    }
  }

  isScreeningStarted(screening: Screening): boolean {
    if (!screening?.startTime) return false;
    return new Date(screening.startTime) < new Date();
  }
}
