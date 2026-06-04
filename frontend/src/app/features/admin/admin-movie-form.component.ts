import { Component, ChangeDetectionStrategy, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { MatDialogModule, MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { AdminService } from '../../core/services/admin.service';
import { MovieDto } from '../../core/models/api.models';

@Component({
  selector: 'app-admin-movie-form',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [CommonModule, FormsModule, ReactiveFormsModule, MatDialogModule, MatFormFieldModule, MatInputModule, MatButtonModule, MatSnackBarModule],
  templateUrl: './admin-movie-form.component.html',
  styleUrl: './admin-movie-form.component.css'
})
export class AdminMovieFormComponent {
  private readonly fb = inject(FormBuilder);
  private readonly adminService = inject(AdminService);
  private readonly snack = inject(MatSnackBar);
  readonly dialogRef = inject(MatDialogRef<AdminMovieFormComponent>);
  readonly data: MovieDto | null = inject(MAT_DIALOG_DATA);

  readonly saving = signal(false);

  readonly form = this.fb.group({
    title: [this.data?.title ?? '', Validators.required],
    description: [this.data?.description ?? ''],
    duration: [this.data?.duration ?? null, Validators.required],
    posterUrl: [this.data?.posterUrl ?? '']
  });

  save() {
    if (this.form.invalid) return;
    this.saving.set(true);
    const req = this.form.value;
    const obs = this.data
      ? this.adminService.updateMovie(this.data.id, req as any)
      : this.adminService.createMovie(req as any);
    obs.subscribe({
      next: () => {
        this.saving.set(false);
        this.snack.open('Saved!', 'OK', { duration: 2000 });
        this.dialogRef.close(true);
      },
      error: () => {
        this.saving.set(false);
        this.snack.open('Error saving movie', 'OK', { duration: 3000 });
      }
    });
  }
}
