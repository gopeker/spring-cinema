import { Component, inject } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { NavbarComponent } from './shared/components/navbar.component';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, NavbarComponent],
  template: `
    <app-navbar></app-navbar>
    <main class="container-fluid py-4">
      <router-outlet></router-outlet>
    </main>
    <footer class="footer-cinema text-center">
      <div class="container">
        <p class="mb-0 text-white-50">
          <i class="bi bi-film me-2"></i>
          Spring Cinema &mdash; Built with Spring Boot &amp; Angular
        </p>
      </div>
    </footer>
  `,
  styles: [`
    main {
      min-height: calc(100vh - 200px);
    }
  `]
})
export class AppComponent {}
