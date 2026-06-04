import { Component, ChangeDetectionStrategy, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { MatDialogModule, MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatDividerModule } from '@angular/material/divider';
import { AdminService } from '../../core/services/admin.service';
import { UserDto } from '../../core/models/api.models';

@Component({
  selector: 'app-admin-user-form',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [CommonModule, FormsModule, ReactiveFormsModule, MatDialogModule, MatFormFieldModule, MatInputModule, MatButtonModule, MatSelectModule, MatSnackBarModule, MatDividerModule],
  templateUrl: './admin-user-form.component.html',
  styleUrl: './admin-user-form.component.css'
})
export class AdminUserFormComponent {
  private readonly fb = inject(FormBuilder);
  private readonly adminService = inject(AdminService);
  private readonly snack = inject(MatSnackBar);
  readonly dialogRef = inject(MatDialogRef<AdminUserFormComponent>);
  readonly data: UserDto | null = inject(MAT_DIALOG_DATA);

  readonly saving = signal(false);

  readonly form = (() => {
    const group = this.fb.group({
      name: [this.data?.name ?? '', Validators.required],
      email: [this.data?.email ?? '', [Validators.required, Validators.email]],
      password: [''],
      role: [this.data?.role ?? 'USER', Validators.required],
      address: this.fb.group({
        street: [this.data?.address?.street ?? ''],
        city: [this.data?.address?.city ?? ''],
        postalCode: [this.data?.address?.postalCode ?? ''],
        country: [this.data?.address?.country ?? '']
      }),
      paymentDetails: this.fb.group({
        cardHolderName: [this.data?.paymentDetails?.cardHolderName ?? ''],
        cardLastFour: [this.data?.paymentDetails?.cardLastFour ?? ''],
        cardExpiry: [this.data?.paymentDetails?.cardExpiry ?? '']
      })
    });
    if (!this.data) {
      group.get('password')?.setValidators(Validators.required);
      group.get('password')?.updateValueAndValidity();
    }
    return group;
  })();

  save() {
    if (this.form.invalid) return;
    this.saving.set(true);
    const val = this.form.value;
    const obs = this.data
      ? this.adminService.updateUser(this.data.id, val as any)
      : this.adminService.createUser(val as any);
    obs.subscribe({
      next: () => {
        this.saving.set(false);
        this.snack.open('Saved!', 'OK', { duration: 2000 });
        this.dialogRef.close(true);
      },
      error: () => {
        this.saving.set(false);
        this.snack.open('Error saving user', 'OK', { duration: 3000 });
      }
    });
  }
}
