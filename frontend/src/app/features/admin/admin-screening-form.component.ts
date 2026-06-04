import { Component, ChangeDetectionStrategy, DestroyRef, inject, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { MatDialogModule, MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatSelectModule } from '@angular/material/select';
import { MatAutocompleteModule } from '@angular/material/autocomplete';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { provideNativeDateAdapter } from '@angular/material/core';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { AdminService } from '../../core/services/admin.service';
import { MovieDto, ShowroomDto, ScreeningDto } from '../../core/models/api.models';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';

@Component({
  selector: 'app-admin-screening-form',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    CommonModule, FormsModule, ReactiveFormsModule,
    MatDialogModule, MatFormFieldModule, MatInputModule, MatButtonModule,
    MatSelectModule, MatAutocompleteModule, MatDatepickerModule, MatSnackBarModule
  ],
  providers: [provideNativeDateAdapter()],
  templateUrl: './admin-screening-form.component.html',
  styleUrl: './admin-screening-form.component.css'
})
export class AdminScreeningFormComponent {
  private readonly fb = inject(FormBuilder);
  private readonly adminService = inject(AdminService);
  private readonly snack = inject(MatSnackBar);
  private readonly destroyRef = inject(DestroyRef);
  readonly dialogRef = inject(MatDialogRef<AdminScreeningFormComponent>);
  readonly data: ScreeningDto | null = inject(MAT_DIALOG_DATA);

  readonly saving = signal(false);
  readonly movies = signal<MovieDto[]>([]);
  readonly showrooms = signal<ShowroomDto[]>([]);
  readonly movieSearch = signal(this.data?.movie?.title ?? '');

  readonly filteredMovies = computed(() => {
    const search = this.movieSearch().toLowerCase();
    const list = this.movies();
    if (!search) return list;
    return list.filter(m => m.title.toLowerCase().includes(search));
  });

  readonly minDate = new Date();

  readonly startDate = signal<Date>(this.parseIsoToDate(this.data?.startTime));
  readonly startTimeStr = signal(this.parseIsoToTime(this.data?.startTime));

  readonly form = this.fb.group({
    movieId: [this.data?.movie?.id ?? null, Validators.required],
    showroomId: [this.data?.showroom?.id ?? null, Validators.required],
    basePrice: [this.data?.basePrice ?? null, Validators.required]
  });

  constructor() {
    this.adminService.getMovies(undefined, 0, 100)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(page => this.movies.set(page.content));
    this.adminService.getShowrooms()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(showrooms => this.showrooms.set(showrooms));
  }

  onMovieSearchChange(value: string) {
    this.movieSearch.set(value);
  }

  onMovieSelected(movieId: number) {
    this.form.patchValue({ movieId });
  }

  onDateChange(date: Date) {
    this.startDate.set(date);
  }

  onTimeChange(value: string) {
    this.startTimeStr.set(value);
  }

  readonly isoDateTime = computed(() => {
    const date = this.startDate();
    const time = this.startTimeStr();
    if (!date || !time) return '';
    const y = date.getFullYear();
    const m = String(date.getMonth() + 1).padStart(2, '0');
    const d = String(date.getDate()).padStart(2, '0');
    return `${y}-${m}-${d}T${time}:00`;
  });

  readonly canSave = computed(() => {
    return this.form.valid && !!this.startDate() && !!this.startTimeStr();
  });

  save() {
    if (!this.canSave()) return;
    this.saving.set(true);
    const req = {
      ...this.form.value,
      startTime: this.isoDateTime()
    };
    const obs = this.data
      ? this.adminService.updateScreening(this.data.id, req as any)
      : this.adminService.createScreening(req as any);
    obs.subscribe({
      next: () => {
        this.saving.set(false);
        this.snack.open('Saved!', 'OK', { duration: 2000 });
        this.dialogRef.close(true);
      },
      error: () => {
        this.saving.set(false);
        this.snack.open('Error saving screening', 'OK', { duration: 3000 });
      }
    });
  }

  private parseIsoToDate(iso?: string): Date {
    if (!iso) return new Date();
    return new Date(iso);
  }

  private parseIsoToTime(iso?: string): string {
    if (!iso) return '18:00';
    const d = new Date(iso);
    const h = String(d.getHours()).padStart(2, '0');
    const m = String(d.getMinutes()).padStart(2, '0');
    return `${h}:${m}`;
  }
}
