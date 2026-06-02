import { Component, inject, OnInit, signal } from '@angular/core';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { TicketService } from '../../core/services/ticket.service';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-my-tickets',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './my-tickets.component.html',
  styleUrl: './my-tickets.component.css'
})
export class MyTicketsComponent implements OnInit {
  private ticketService = inject(TicketService);
  private authService = inject(AuthService);
  private router = inject(Router);

  tickets = signal<any[]>([]);
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
    this.ticketService.getMyTickets().subscribe({
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

  isScreeningStarted(screening: any): boolean {
    if (!screening || !screening.startTime) return false;
    return new Date(screening.startTime) < new Date();
  }
}
