import { Component, OnInit, inject, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, Router, ActivatedRoute } from '@angular/router';
import { MovieService } from '../../core/services/movie.service';
import { ScreeningService } from '../../core/services/screening.service';

@Component({
  selector: 'app-movie-detail',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './movie-detail.component.html',
  styleUrl: './movie-detail.component.css'
})
export class MovieDetailComponent implements OnInit {
  private movieService = inject(MovieService);
  private screeningService = inject(ScreeningService);
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private cdr = inject(ChangeDetectorRef);

  movie: any = null;
  screenings: any[] = [];
  groupedScreenings: { date: string; screenings: any[] }[] = [];
  loading = true;
  private loadCount = 0;

  ngOnInit(): void {
    this.route.params.subscribe(params => {
      const movieId = +params['id'];
      if (movieId) {
        this.loadMovie(movieId);
        this.loadScreenings(movieId);
      } else {
        this.loading = false;
      }
    });
  }

  private loadMovie(id: number): void {
    this.movieService.getById(id).subscribe({
      next: (movie) => {
        this.movie = movie;
        this.finishLoad();
      },
      error: () => {
        this.movie = null;
        this.finishLoad();
      }
    });
  }

  private loadScreenings(movieId: number): void {
    this.screeningService.getByMovie(movieId).subscribe({
      next: (screenings) => {
        this.screenings = screenings;
        this.groupScreenings(screenings);
        this.finishLoad();
      },
      error: () => {
        this.screenings = [];
        this.finishLoad();
      }
    });
  }

  private finishLoad(): void {
    this.loadCount++;
    if (this.loadCount >= 2) {
      this.loading = false;
      this.cdr.detectChanges();
    }
  }

  private groupScreenings(screenings: any[]): void {
    const now = new Date();
    const upcoming = screenings.filter(s => new Date(s.startTime) > now);
    
    const groups = new Map<string, any[]>();
    upcoming.forEach(screening => {
      const date = new Date(screening.startTime).toLocaleDateString('en-US', {
        weekday: 'long',
        year: 'numeric',
        month: 'long',
        day: 'numeric'
      });
      if (!groups.has(date)) {
        groups.set(date, []);
      }
      groups.get(date)!.push(screening);
    });

    this.groupedScreenings = Array.from(groups.entries()).map(([date, scrs]) => ({
      date,
      screenings: scrs.sort((a, b) => new Date(a.startTime).getTime() - new Date(b.startTime).getTime())
    }));
  }

  formatDuration(minutes: number): string {
    const hours = Math.floor(minutes / 60);
    const mins = minutes % 60;
    if (hours > 0 && mins > 0) {
      return `${hours}h ${mins}min`;
    } else if (hours > 0) {
      return `${hours}h`;
    }
    return `${mins}min`;
  }

  formatTime(dateString: string): string {
    return new Date(dateString).toLocaleTimeString('en-GB', {
      hour: '2-digit',
      minute: '2-digit',
      hour12: false
    });
  }
}