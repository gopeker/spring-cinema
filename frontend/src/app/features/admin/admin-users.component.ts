import { Component, ChangeDetectionStrategy, DestroyRef, inject, signal } from '@angular/core';
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
import { MatExpansionModule } from '@angular/material/expansion';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatChipsModule } from '@angular/material/chips';
import { AdminService } from '../../core/services/admin.service';
import { UserDto, UserSearchRequest } from '../../core/models/api.models';
import { AdminUserFormComponent } from './admin-user-form.component';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';

@Component({
  selector: 'app-admin-users',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    CommonModule, FormsModule,
    MatTableModule, MatSortModule, MatPaginatorModule,
    MatButtonModule, MatIconModule,
    MatInputModule, MatFormFieldModule, MatDialogModule,
    MatSnackBarModule, MatCardModule, MatExpansionModule,
    MatProgressSpinnerModule, MatChipsModule
  ],
  templateUrl: './admin-users.component.html',
  styleUrl: './admin-users.component.css'
})
export class AdminUsersComponent {
  private readonly adminService = inject(AdminService);
  private readonly dialog = inject(MatDialog);
  private readonly snack = inject(MatSnackBar);
  private readonly destroyRef = inject(DestroyRef);

  readonly users = signal<UserDto[]>([]);
  readonly loading = signal(false);
  readonly displayedColumns = ['id', 'name', 'email', 'role', 'city', 'actions'];

  readonly pageIndex = signal(0);
  readonly pageSize = signal(10);
  readonly totalElements = signal(0);
  readonly sortActive = signal('id');
  readonly sortDirection = signal<'asc' | 'desc'>('asc');

  search = signal<UserSearchRequest>({});
  readonly hasSearch = signal(false);

  constructor() {
    this.loadUsers();
  }

  loadUsers() {
    this.loading.set(true);
    if (this.hasSearch()) {
      this.adminService.searchUsers(
        this.search(),
        this.pageIndex(),
        this.pageSize(),
        this.sortActive(),
        this.sortDirection()
      ).subscribe({
        next: page => {
          this.users.set(page.content);
          this.totalElements.set(page.totalElements);
          this.loading.set(false);
        },
        error: () => this.loading.set(false)
      });
    } else {
      this.adminService.getUsers(
        this.pageIndex(),
        this.pageSize(),
        this.sortActive(),
        this.sortDirection()
      ).subscribe({
        next: page => {
          this.users.set(page.content);
          this.totalElements.set(page.totalElements);
          this.loading.set(false);
        },
        error: () => this.loading.set(false)
      });
    }
  }

  doSearch() {
    this.hasSearch.set(true);
    this.pageIndex.set(0);
    this.loadUsers();
  }

  clearSearch() {
    this.search.set({});
    this.hasSearch.set(false);
    this.pageIndex.set(0);
    this.loadUsers();
  }

  updateSearch(patch: Partial<UserSearchRequest>) {
    this.search.update(s => ({ ...s, ...patch }));
  }

  onSearchKeydown(event: KeyboardEvent) {
    if (event.key === 'Enter') {
      this.doSearch();
    }
  }

  onPageChange(event: PageEvent) {
    this.pageIndex.set(event.pageIndex);
    this.pageSize.set(event.pageSize);
    this.loadUsers();
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
    this.loadUsers();
  }

  openCreateDialog() {
    const ref = this.dialog.open(AdminUserFormComponent, { width: '600px', data: null });
    ref.afterClosed()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(result => { if (result) this.loadUsers(); });
  }

  openEditDialog(user: UserDto) {
    const ref = this.dialog.open(AdminUserFormComponent, { width: '600px', data: user });
    ref.afterClosed()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(result => { if (result) this.loadUsers(); });
  }

  deleteUser(id: number) {
    if (!confirm('Delete this user?')) return;
    this.adminService.deleteUser(id).subscribe({
      next: () => { this.snack.open('User deleted', 'OK', { duration: 3000 }); this.loadUsers(); },
      error: () => this.snack.open('Error deleting user', 'OK', { duration: 3000 })
    });
  }
}
