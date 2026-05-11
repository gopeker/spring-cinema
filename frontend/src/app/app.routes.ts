import { Routes } from '@angular/router';
import { authGuard, adminGuard } from './core/guards/auth.guard';

export const routes: Routes = [
  { path: '', loadComponent: () => import('./features/home/home.component').then(m => m.HomeComponent) },
  { path: 'movies/:id', loadComponent: () => import('./features/movie-detail/movie-detail.component').then(m => m.MovieDetailComponent) },
  { path: 'screenings/:id/seats', loadComponent: () => import('./features/seat-selection/seat-selection.component').then(m => m.SeatSelectionComponent) },
  { path: 'chatbot', loadComponent: () => import('./features/chatbot/chatbot.component').then(m => m.ChatbotComponent) },
  { path: 'checkout', loadComponent: () => import('./features/checkout/checkout.component').then(m => m.CheckoutComponent), canActivate: [authGuard] },
  { path: 'tickets', loadComponent: () => import('./features/my-tickets/my-tickets.component').then(m => m.MyTicketsComponent), canActivate: [authGuard] },
  { path: 'login', loadComponent: () => import('./features/auth/login.component').then(m => m.LoginComponent) },
  { path: 'register', loadComponent: () => import('./features/auth/register.component').then(m => m.RegisterComponent) },
  { path: 'admin/movies', loadComponent: () => import('./features/admin/admin-movies.component').then(m => m.AdminMoviesComponent), canActivate: [adminGuard] },
  { path: '**', redirectTo: '' }
];
