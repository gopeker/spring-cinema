import { Component, inject, OnInit } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../core/services/auth.service';
import { MovieService } from '../../core/services/movie.service';

@Component({
  selector: 'app-admin',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="container py-4">
      <div class="page-header mb-4">
        <h1 class="page-title">Admin Dashboard</h1>
        <p class="page-subtitle">Manage movies and screenings</p>
      </div>

      <ul class="nav nav-tabs mb-4">
        <li class="nav-item">
          <button class="nav-link" [class.active]="activeTab === 'movies'" (click)="activeTab = 'movies'">Movies</button>
        </li>
        <li class="nav-item">
          <button class="nav-link" [class.active]="activeTab === 'screenings'" (click)="activeTab = 'screenings'">Screenings</button>
        </li>
      </ul>

      @if (activeTab === 'movies') {
        <div class="mb-3">
          <button class="btn btn-cinema" (click)="showMovieForm = !showMovieForm">
            {{ showMovieForm ? 'Cancel' : 'Add Movie' }}
          </button>
        </div>

        @if (showMovieForm) {
          <div class="card card-cinema mb-4">
            <div class="card-body">
              <h4>{{ editingMovie ? 'Edit Movie' : 'Add New Movie' }}</h4>
              <form (ngSubmit)="saveMovie()">
                <div class="mb-3">
                  <label class="form-label">Title</label>
                  <input type="text" class="form-control form-control-dark" [(ngModel)]="movieForm.title" name="title" required>
                </div>
                <div class="mb-3">
                  <label class="form-label">Description</label>
                  <textarea class="form-control form-control-dark" [(ngModel)]="movieForm.description" name="description" rows="3"></textarea>
                </div>
                <div class="mb-3">
                  <label class="form-label">Duration (minutes)</label>
                  <input type="number" class="form-control form-control-dark" [(ngModel)]="movieForm.duration" name="duration" required>
                </div>
                <div class="mb-3">
                  <label class="form-label">Poster URL</label>
                  <input type="text" class="form-control form-control-dark" [(ngModel)]="movieForm.posterUrl" name="posterUrl">
                </div>
                <button type="submit" class="btn btn-cinema">{{ editingMovie ? 'Update' : 'Save' }}</button>
              </form>
            </div>
          </div>
        }

        <div class="table-container">
          <table class="table table-dark rounded-table">
            <thead>
              <tr>
                <th>ID</th>
                <th>Title</th>
                <th>Duration</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              @for (movie of movies; track movie.id) {
                <tr>
                  <td>{{ movie.id }}</td>
                  <td>{{ movie.title }}</td>
                  <td>{{ movie.duration }} min</td>
                  <td>
                    <button class="btn btn-sm btn-action me-2" (click)="editMovie(movie)">Edit</button>
                    <button class="btn btn-sm btn-delete" (click)="deleteMovie(movie.id)">Delete</button>
                  </td>
                </tr>
              }
            </tbody>
          </table>
        </div>
      } @if (activeTab === 'screenings') {
        <div class="mb-3">
          <button class="btn btn-cinema" (click)="showScreeningForm = !showScreeningForm">
            {{ showScreeningForm ? 'Cancel' : 'Add Screening' }}
          </button>
        </div>

        @if (showScreeningForm) {
          <div class="card card-cinema mb-4">
            <div class="card-body">
              <h4>{{ editingScreening ? 'Edit Screening' : 'Add New Screening' }}</h4>
              <form (ngSubmit)="saveScreening()">
                <div class="mb-3">
                  <label class="form-label">Movie</label>
                  <select class="form-control form-control-dark" [(ngModel)]="screeningForm.movieId" name="movieId" required>
                    <option value="">Select a movie</option>
                    @for (movie of movies; track movie.id) {
                      <option [value]="movie.id">{{ movie.title }}</option>
                    }
                  </select>
                </div>
                <div class="mb-3">
                  <label class="form-label">Showroom</label>
                  <select class="form-control form-control-dark" [(ngModel)]="screeningForm.showroomId" name="showroomId" required>
                    <option value="">Select a showroom</option>
                    <option value="1">Showroom 1</option>
                    <option value="2">Showroom 2</option>
                    <option value="3">Showroom 3</option>
                  </select>
                </div>
                <div class="mb-3">
                  <label class="form-label">Start Time</label>
                  <input type="datetime-local" class="form-control form-control-dark" [(ngModel)]="screeningForm.startTime" name="startTime" required>
                </div>
                <div class="mb-3">
                  <label class="form-label">Base Price</label>
                  <input type="number" class="form-control form-control-dark" [(ngModel)]="screeningForm.basePrice" name="basePrice" required>
                </div>
                <button type="submit" class="btn btn-cinema">{{ editingScreening ? 'Update' : 'Save' }}</button>
              </form>
            </div>
          </div>
        }

        <div class="table-container">
          <table class="table table-dark rounded-table">
            <thead>
              <tr>
                <th>ID</th>
                <th>Movie</th>
                <th>Showroom</th>
                <th>Start Time</th>
                <th>Base Price</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              @for (screening of screenings; track screening.id) {
                <tr>
                  <td>{{ screening.id }}</td>
                  <td>{{ screening.movie?.title }}</td>
                  <td>{{ screening.showroom?.name }}</td>
                  <td>{{ screening.startTime | date:'MMM d, yyyy h:mm a' }}</td>
                  <td>\${{ screening.basePrice }}</td>
                  <td>
                    <button class="btn btn-sm btn-action me-2" (click)="editScreening(screening)">Edit</button>
                    <button class="btn btn-sm btn-delete" (click)="deleteScreening(screening.id)">Delete</button>
                  </td>
                </tr>
              }
            </tbody>
          </table>
        </div>
      }
    </div>
  `,
  styles: [`
    .page-header {
      text-align: center;
      padding: 20px 0;
    }
    .page-title {
      color: #fff;
      font-size: 2.5rem;
      margin-bottom: 0.5rem;
    }
    .page-subtitle {
      color: #aaa;
      font-size: 1.1rem;
    }
    .nav-tabs {
      border-bottom: 1px solid #0f3460;
    }
    .nav-link {
      color: #aaa;
      background: transparent;
      border: none;
      padding: 10px 20px;
      cursor: pointer;
    }
    .nav-link.active {
      color: #e94560;
      border-bottom: 2px solid #e94560;
    }
    .nav-link:hover {
      color: #fff;
    }
    .card-cinema {
      background: #1a1a2e;
      border: none;
      border-radius: 12px;
    }
    .card-cinema h4 {
      color: #fff;
      margin-bottom: 1rem;
    }
    .form-control-dark {
      background: #16213e;
      border: 1px solid #0f3460;
      color: #fff;
    }
    .form-control-dark:focus {
      background: #16213e;
      border-color: #e94560;
      color: #fff;
      box-shadow: none;
    }
    .form-control-dark option {
      background: #16213e;
      color: #fff;
    }
    .table-container {
      background: #1a1a2e;
      border-radius: 12px;
      overflow: hidden;
    }
    .rounded-table {
      border-radius: 12px;
    }
    .table-dark {
      background: transparent;
      color: #fff;
    }
    .table-dark th {
      border-color: #0f3460;
      background: #16213e;
    }
    .table-dark td {
      border-color: #0f3460;
    }
    .btn-cinema {
      background: #e94560;
      border: none;
      color: #fff;
      padding: 10px 20px;
      border-radius: 6px;
    }
    .btn-cinema:hover {
      background: #d63d56;
      color: #fff;
    }
    .btn-action {
      background: #0f3460;
      border: none;
      color: #fff;
    }
    .btn-action:hover {
      background: #1a4a8a;
    }
    .btn-delete {
      background: #5c1a1a;
      border: none;
      color: #e63946;
    }
    .btn-delete:hover {
      background: #7a2a2a;
    }
  `]
})
export class AdminMoviesComponent implements OnInit {
  private http = inject(HttpClient);
  private router = inject(Router);
  private auth = inject(AuthService);
  private movieService = inject(MovieService);

  private baseUrl = 'http://localhost:8080/api/admin';

  activeTab = 'movies';
  movies: any[] = [];
  screenings: any[] = [];

  showMovieForm = false;
  editingMovie: any = null;
  movieForm = { title: '', description: '', duration: 0, posterUrl: '' };

  showScreeningForm = false;
  editingScreening: any = null;
  screeningForm = { movieId: '', showroomId: '', startTime: '', basePrice: 0 };

  ngOnInit() {
    if (!this.auth.isAdmin()) {
      this.router.navigate(['/']);
      return;
    }
    this.loadMovies();
    this.loadScreenings();
  }

  private getHeaders() {
    const token = this.auth.getToken();
    return {
      headers: { Authorization: `Bearer ${token}` }
    };
  }

  loadMovies() {
    this.movieService.getAll().subscribe({
      next: (data) => this.movies = data
    });
  }

  loadScreenings() {
    this.http.get<any[]>(`${this.baseUrl}/screenings`, this.getHeaders()).subscribe({
      next: (data) => this.screenings = data
    });
  }

  saveMovie() {
    const data = {
      title: this.movieForm.title,
      description: this.movieForm.description,
      duration: this.movieForm.duration,
      posterUrl: this.movieForm.posterUrl
    };

    if (this.editingMovie) {
      this.http.put(`${this.baseUrl}/movies/${this.editingMovie.id}`, data, this.getHeaders()).subscribe({
        next: () => {
          this.loadMovies();
          this.resetMovieForm();
        }
      });
    } else {
      this.http.post(`${this.baseUrl}/movies`, data, this.getHeaders()).subscribe({
        next: () => {
          this.loadMovies();
          this.resetMovieForm();
        }
      });
    }
  }

  editMovie(movie: any) {
    this.editingMovie = movie;
    this.movieForm = {
      title: movie.title,
      description: movie.description,
      duration: movie.duration,
      posterUrl: movie.posterUrl
    };
    this.showMovieForm = true;
  }

  deleteMovie(id: number) {
    if (confirm('Are you sure you want to delete this movie?')) {
      this.http.delete(`${this.baseUrl}/movies/${id}`, this.getHeaders()).subscribe({
        next: () => this.loadMovies()
      });
    }
  }

  resetMovieForm() {
    this.showMovieForm = false;
    this.editingMovie = null;
    this.movieForm = { title: '', description: '', duration: 0, posterUrl: '' };
  }

  saveScreening() {
    const data = {
      movieId: parseInt(this.screeningForm.movieId),
      showroomId: parseInt(this.screeningForm.showroomId),
      startTime: this.screeningForm.startTime,
      basePrice: this.screeningForm.basePrice
    };

    if (this.editingScreening) {
      this.http.put(`${this.baseUrl}/screenings/${this.editingScreening.id}`, data, this.getHeaders()).subscribe({
        next: () => {
          this.loadScreenings();
          this.resetScreeningForm();
        }
      });
    } else {
      this.http.post(`${this.baseUrl}/screenings`, data, this.getHeaders()).subscribe({
        next: () => {
          this.loadScreenings();
          this.resetScreeningForm();
        }
      });
    }
  }

  editScreening(screening: any) {
    this.editingScreening = screening;
    const startTime = screening.startTime ? screening.startTime.slice(0, 16) : '';
    this.screeningForm = {
      movieId: screening.movie?.id?.toString() || '',
      showroomId: screening.showroom?.id?.toString() || '',
      startTime: startTime,
      basePrice: screening.basePrice
    };
    this.showScreeningForm = true;
  }

  deleteScreening(id: number) {
    if (confirm('Are you sure you want to delete this screening?')) {
      this.http.delete(`${this.baseUrl}/screenings/${id}`, this.getHeaders()).subscribe({
        next: () => this.loadScreenings()
      });
    }
  }

  resetScreeningForm() {
    this.showScreeningForm = false;
    this.editingScreening = null;
    this.screeningForm = { movieId: '', showroomId: '', startTime: '', basePrice: 0 };
  }
}