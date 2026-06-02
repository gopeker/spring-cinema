import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { MovieService } from '../../core/services/movie.service';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './home.component.html',
  styleUrl: './home.component.css'
})
export class HomeComponent implements OnInit {
  private movieService = inject(MovieService);

  movies = signal<any[]>([]);
  loading = signal(true);

  ngOnInit(): void {
    this.loadMovies();
  }

  private loadMovies(): void {
    this.movieService.getAll().subscribe({
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
