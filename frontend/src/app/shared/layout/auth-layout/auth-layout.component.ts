import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { AppFooterComponent } from '../footer/app-footer.component';

/** Coquille centrée pour les écrans publics (connexion, activation). */
@Component({
  selector: 'app-auth-layout',
  imports: [RouterOutlet, MatCardModule, AppFooterComponent],
  templateUrl: './auth-layout.component.html',
  styleUrl: './auth-layout.component.scss',
})
export class AuthLayoutComponent {}
