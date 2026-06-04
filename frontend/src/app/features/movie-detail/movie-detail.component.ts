import { Component, ChangeDetectionStrategy, DestroyRef, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { CommonModule } from '@angular/common';
import { RouterLink, ActivatedRoute } from '@angular/router';
import { switchMap } from 'rxjs/operators';
import { forkJoin } from 'rxjs';
import { MovieService } from '../../core/services/movie.service';
import { ScreeningService } from '../../core/services/screening.service';
import { Movie, Screening, ScreeningGroup } from '../../core/models/api.models';
import { formatTime, formatDuration } from '../../shared/utils/cinema-format.utils';

@Component({
  selector: 'app-movie-detail',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [CommonModule, RouterLink],
  templateUrl: './movie-detail.component.html',
  styleUrl: './movie-detail.component.css'
})
export class MovieDetailComponent {
  private movieService = inject(MovieService);
  private screeningService = inject(ScreeningService);
  private route = inject(ActivatedRoute);
  private destroyRef = inject(DestroyRef);

  movie = signal<Movie | null>(null);
  screenings = signal<Screening[]>([]);
  groupedScreenings = signal<ScreeningGroup[]>([]);
  loading = signal(true);

  constructor() {
    this.route.params
      .pipe(
        switchMap(params => {
          const id = +params['id'];
          return forkJoin({
            movie: this.movieService.getById(id),
            screenings: this.screeningService.getByMovie(id)
          });
        }),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe({
        next: ({ movie, screenings }) => {
          this.movie.set(movie);
          this.screenings.set(screenings);
          this.groupScreenings(screenings);
          this.loading.set(false);
        },
        error: () => {
          this.movie.set(null);
          this.screenings.set([]);
          this.loading.set(false);
        }
      });
  }

  private groupScreenings(screenings: Screening[]): void {
    const now = new Date();
    const upcoming = screenings.filter(s => new Date(s.startTime) > now);

    const groups = new Map<string, Screening[]>();
    upcoming.forEach(screening => {
      const date = new Date(screening.startTime).toLocaleDateString('en-US', {
        weekday: 'long',
        year: 'numeric',
        month: 'long',
        day: 'numeric'
      });
      if (!groups.has(date)) groups.set(date, []);
      groups.get(date)!.push(screening);
    });

    this.groupedScreenings.set(
      Array.from(groups.entries()).map(([date, scrs]) => ({
        date,
        screenings: scrs.sort((a, b) => new Date(a.startTime).getTime() - new Date(b.startTime).getTime())
      }))
    );
  }

  readonly formatDuration = formatDuration;
  readonly formatTime = formatTime;
}
