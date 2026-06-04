import { Component, ChangeDetectionStrategy, DestroyRef, OnInit, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { MovieService } from '../../core/services/movie.service';
import { Movie } from '../../core/models/api.models';

@Component({
  selector: 'app-home',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [CommonModule, RouterLink],
  templateUrl: './home.component.html',
  styleUrl: './home.component.css'
})
export class HomeComponent implements OnInit {
  private movieService = inject(MovieService);
  private destroyRef = inject(DestroyRef);

  movies = signal<Movie[]>([]);
  loading = signal(true);

  ngOnInit(): void {
    this.loadMovies();
  }

  private loadMovies(): void {
    this.movieService.getAll().pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: (movies) => {
        this.movies.set(movies);
        this.loading.set(false);
      },
      error: () => {
        this.loading.set(false);
      }
    });
  }
}
