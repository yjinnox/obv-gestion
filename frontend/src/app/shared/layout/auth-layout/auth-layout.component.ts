import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { MatCardModule } from '@angular/material/card';

/** Coquille centrée pour les écrans publics (connexion, activation). */
@Component({
  selector: 'app-auth-layout',
  imports: [RouterOutlet, MatCardModule],
  templateUrl: './auth-layout.component.html',
  styleUrl: './auth-layout.component.scss',
})
export class AuthLayoutComponent {}
