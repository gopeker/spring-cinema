import { Component, inject, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatTableModule } from '@angular/material/table';
import { MatSortModule, Sort } from '@angular/material/sort';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatDialogModule, MatDialog } from '@angular/material/dialog';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatCardModule } from '@angular/material/card';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { AdminService } from '../../core/services/admin.service';
import { MovieDto, MovieSearchParams, Page } from '../../core/models/api.models';
import { AdminMovieFormComponent } from './admin-movie-form.component';

@Component({
  selector: 'app-admin-movies',
  standalone: true,
  imports: [
    CommonModule, FormsModule,
    MatTableModule, MatSortModule, MatPaginatorModule,
    MatButtonModule, MatIconModule,
    MatInputModule, MatFormFieldModule, MatDialogModule,
    MatSnackBarModule, MatCardModule, MatProgressSpinnerModule
  ],
  templateUrl: './admin-movies.component.html',
  styleUrl: './admin-movies.component.css'
})
export class AdminMoviesComponent {
  private readonly adminService = inject(AdminService);
  private readonly dialog = inject(MatDialog);
  private readonly snack = inject(MatSnackBar);

  readonly movies = signal<MovieDto[]>([]);
  readonly loading = signal(false);
  readonly displayedColumns = ['id', 'title', 'duration', 'actions'];

  readonly pageIndex = signal(0);
  readonly pageSize = signal(10);
  readonly totalElements = signal(0);
  readonly sortActive = signal('id');
  readonly sortDirection = signal<'asc' | 'desc'>('asc');

  searchTitle = signal('');
  searchMinDuration = signal<number | undefined>(undefined);
  searchMaxDuration = signal<number | undefined>(undefined);

  readonly hasSearchParams = computed(() =>
    !!this.searchTitle() || this.searchMinDuration() != null || this.searchMaxDuration() != null
  );

  constructor() {
    this.loadMovies();
  }

  loadMovies() {
    this.loading.set(true);
    const params: MovieSearchParams = {};
    if (this.searchTitle()) params.title = this.searchTitle();
    if (this.searchMinDuration() != null) params.minDuration = this.searchMinDuration();
    if (this.searchMaxDuration() != null) params.maxDuration = this.searchMaxDuration();
    const searchParams = this.hasSearchParams() ? params : undefined;
    this.adminService.getMovies(
      searchParams,
      this.pageIndex(),
      this.pageSize(),
      this.sortActive(),
      this.sortDirection()
    ).subscribe({
      next: page => {
        this.movies.set(page.content);
        this.totalElements.set(page.totalElements);
        this.loading.set(false);
      },
      error: () => this.loading.set(false)
    });
  }

  onSearch() {
    this.pageIndex.set(0);
    this.loadMovies();
  }

  onPageChange(event: PageEvent) {
    this.pageIndex.set(event.pageIndex);
    this.pageSize.set(event.pageSize);
    this.loadMovies();
  }

  onSortChange(sort: Sort) {
    if (!sort.active) return;
    let dir: 'asc' | 'desc';
    if (sort.direction === 'asc' || sort.direction === 'desc') {
      dir = sort.direction;
    } else {
      dir = this.sortDirection() === 'asc' ? 'desc' : 'asc';
    }
    this.sortActive.set(sort.active);
    this.sortDirection.set(dir);
    this.pageIndex.set(0);
    this.loadMovies();
  }

  openCreateDialog() {
    const ref = this.dialog.open(AdminMovieFormComponent, { width: '500px', data: null });
    ref.afterClosed().subscribe(result => { if (result) this.loadMovies(); });
  }

  openEditDialog(movie: MovieDto) {
    const ref = this.dialog.open(AdminMovieFormComponent, { width: '500px', data: movie });
    ref.afterClosed().subscribe(result => { if (result) this.loadMovies(); });
  }

  deleteMovie(id: number) {
    if (!confirm('Delete this movie?')) return;
    this.adminService.deleteMovie(id).subscribe({
      next: () => { this.snack.open('Movie deleted', 'OK', { duration: 3000 }); this.loadMovies(); },
      error: () => this.snack.open('Error deleting movie', 'OK', { duration: 3000 })
    });
  }
}
