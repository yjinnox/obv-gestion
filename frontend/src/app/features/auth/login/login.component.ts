import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { form, FormField, required, schema, submit } from '@angular/forms/signals';
import { firstValueFrom } from 'rxjs';
import { AuthService } from '../../../core/auth/auth.service';
import { estProblemDetail } from '../../../core/http/problem-detail';

interface ConnexionModele {
  identifiant: string;
  motDePasse: string;
}

const connexionSchema = schema<ConnexionModele>((champ) => {
  required(champ.identifiant, { message: "L'identifiant est requis." });
  required(champ.motDePasse, { message: 'Le mot de passe est requis.' });
});

/**
 * N'accepte qu'un chemin interne à l'application : une valeur absolue
 * (`https://...`) ou protocole-relative (`//...`) permettrait de rediriger
 * l'utilisateur vers un site tiers après connexion (open redirect).
 */
export function destinationApresConnexion(returnUrl: string | null): string {
  if (!returnUrl || !returnUrl.startsWith('/') || returnUrl.startsWith('//')) {
    return '/tableau-de-bord';
  }
  return returnUrl;
}

/** §4.4 — écran de connexion (identifiant e-mail/téléphone + mot de passe). */
@Component({
  selector: 'app-login',
  imports: [
    FormField,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
  ],
  templateUrl: './login.component.html',
  styleUrl: './login.component.scss',
})
export class LoginComponent {
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);

  protected readonly modele = signal<ConnexionModele>({ identifiant: '', motDePasse: '' });
  protected readonly connexionForm = form(this.modele, connexionSchema);
  protected readonly enCours = signal(false);
  protected readonly masquerMotDePasse = signal(true);

  protected async onSubmit(event: Event): Promise<void> {
    event.preventDefault();
    if (this.enCours()) {
      return;
    }
    await submit(this.connexionForm, async (f) => {
      this.enCours.set(true);
      try {
        const { identifiant, motDePasse } = f().value();
        await firstValueFrom(this.auth.connecter(identifiant, motDePasse));
        await this.router.navigateByUrl(
          destinationApresConnexion(this.route.snapshot.queryParamMap.get('returnUrl')),
        );
        return undefined;
      } catch (erreur) {
        const message =
          erreur instanceof HttpErrorResponse && estProblemDetail(erreur.error) && erreur.error.detail
            ? erreur.error.detail
            : 'Identifiant ou mot de passe incorrect.';
        return [{ fieldTree: this.connexionForm, kind: 'server', message }];
      } finally {
        this.enCours.set(false);
      }
    });
  }
}
