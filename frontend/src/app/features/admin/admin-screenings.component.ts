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
import { MatAutocompleteModule } from '@angular/material/autocomplete';
import { MatDialogModule, MatDialog } from '@angular/material/dialog';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatCardModule } from '@angular/material/card';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { AdminService } from '../../core/services/admin.service';
import { MovieDto, ScreeningDto, ScreeningSearchParams } from '../../core/models/api.models';
import { AdminScreeningFormComponent } from './admin-screening-form.component';

@Component({
  selector: 'app-admin-screenings',
  standalone: true,
  imports: [
    CommonModule, FormsModule,
    MatTableModule, MatSortModule, MatPaginatorModule,
    MatButtonModule, MatIconModule,
    MatInputModule, MatFormFieldModule, MatAutocompleteModule,
    MatDialogModule,
    MatSnackBarModule, MatCardModule, MatProgressSpinnerModule
  ],
  templateUrl: './admin-screenings.component.html',
  styleUrl: './admin-screenings.component.css'
})
export class AdminScreeningsComponent {
  private readonly adminService = inject(AdminService);
  private readonly dialog = inject(MatDialog);
  private readonly snack = inject(MatSnackBar);

  readonly screenings = signal<ScreeningDto[]>([]);
  readonly loading = signal(false);
  readonly displayedColumns = ['id', 'movie', 'showroom', 'startTime', 'basePrice', 'actions'];

  readonly pageIndex = signal(0);
  readonly pageSize = signal(10);
  readonly totalElements = signal(0);
  readonly sortActive = signal('id');
  readonly sortDirection = signal<'asc' | 'desc'>('asc');

  readonly movies = signal<MovieDto[]>([]);
  readonly movieSearch = signal('');
  readonly searchMovieId = signal<number | undefined>(undefined);
  searchMinPrice = signal<number | undefined>(undefined);
  searchMaxPrice = signal<number | undefined>(undefined);

  readonly filteredMovies = computed(() => {
    const search = this.movieSearch().toLowerCase();
    const list = this.movies();
    if (!search) return list;
    return list.filter(m => m.title.toLowerCase().includes(search));
  });

  constructor() {
    this.adminService.getMovies(undefined, 0, 100).subscribe(page => this.movies.set(page.content));
    this.loadScreenings();
  }

  loadScreenings() {
    this.loading.set(true);
    const params: ScreeningSearchParams = {};
    if (this.searchMovieId() != null) params.movieId = this.searchMovieId();
    if (this.searchMinPrice() != null) params.minPrice = this.searchMinPrice();
    if (this.searchMaxPrice() != null) params.maxPrice = this.searchMaxPrice();
    const hasParams = this.searchMovieId() != null || this.searchMinPrice() != null || this.searchMaxPrice() != null;
    this.adminService.getScreenings(
      hasParams ? params : undefined,
      this.pageIndex(),
      this.pageSize(),
      this.sortActive(),
      this.sortDirection()
    ).subscribe({
      next: page => {
        this.screenings.set(page.content);
        this.totalElements.set(page.totalElements);
        this.loading.set(false);
      },
      error: () => this.loading.set(false)
    });
  }

  onSearch() {
    this.pageIndex.set(0);
    this.loadScreenings();
  }

  onMovieSearchChange(value: string) {
    this.movieSearch.set(value);
    if (!value) {
      this.searchMovieId.set(undefined);
    }
  }

  onMovieSelected(movieId: number) {
    this.searchMovieId.set(movieId);
    this.onSearch();
  }

  clearMovieFilter() {
    this.movieSearch.set('');
    this.searchMovieId.set(undefined);
    this.onSearch();
  }

  onPageChange(event: PageEvent) {
    this.pageIndex.set(event.pageIndex);
    this.pageSize.set(event.pageSize);
    this.loadScreenings();
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
    this.loadScreenings();
  }

  openCreateDialog() {
    const ref = this.dialog.open(AdminScreeningFormComponent, { width: '500px', data: null });
    ref.afterClosed().subscribe(result => { if (result) this.loadScreenings(); });
  }

  openEditDialog(screening: ScreeningDto) {
    const ref = this.dialog.open(AdminScreeningFormComponent, { width: '500px', data: screening });
    ref.afterClosed().subscribe(result => { if (result) this.loadScreenings(); });
  }

  deleteScreening(id: number) {
    if (!confirm('Delete this screening?')) return;
    this.adminService.deleteScreening(id).subscribe({
      next: () => { this.snack.open('Screening deleted', 'OK', { duration: 3000 }); this.loadScreenings(); },
      error: () => this.snack.open('Error deleting screening', 'OK', { duration: 3000 })
    });
  }
}
