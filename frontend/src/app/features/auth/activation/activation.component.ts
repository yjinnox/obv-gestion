import { HttpErrorResponse } from '@angular/common/http';
import { Component, computed, inject, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { form, FormField, pattern, required, schema, submit } from '@angular/forms/signals';
import { firstValueFrom } from 'rxjs';
import { toSignal } from '@angular/core/rxjs-interop';
import { map } from 'rxjs';
import { ActivationApiService } from './activation-api.service';
import { estProblemDetail } from '../../../core/http/problem-detail';
import { NotificationService } from '../../../core/ui/notification.service';

interface MotDePasseModele {
  motDePasse: string;
  confirmation: string;
}

interface OtpModele {
  code: string;
}

const MESSAGE_POLITIQUE_MDP =
  'Le mot de passe doit comporter au moins 10 caractères, avec au moins une minuscule, une majuscule et un chiffre.';

const motDePasseSchema = schema<MotDePasseModele>((champ) => {
  required(champ.motDePasse, { message: 'Le mot de passe est requis.' });
  pattern(champ.motDePasse, /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d).{10,}$/, { message: MESSAGE_POLITIQUE_MDP });
  required(champ.confirmation, { message: 'La confirmation est requise.' });
});

const otpSchema = schema<OtpModele>((champ) => {
  required(champ.code, { message: 'Le code est requis.' });
  pattern(champ.code, /^\d{6}$/, { message: 'Le code comporte 6 chiffres.' });
});

type Etape = 'mot-de-passe' | 'otp' | 'termine' | 'lien-invalide';

/** §4.2 — activation de compte : lien reçu par e-mail/SMS (`?token=...`). */
@Component({
  selector: 'app-activation',
  imports: [
    RouterLink,
    FormField,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
  ],
  templateUrl: './activation.component.html',
  styleUrl: './activation.component.scss',
})
export class ActivationComponent {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly api = inject(ActivationApiService);
  private readonly notification = inject(NotificationService);

  private readonly token = toSignal(this.route.queryParamMap.pipe(map((p) => p.get('token'))), {
    initialValue: null,
  });

  protected readonly etape = signal<Etape>('mot-de-passe');
  protected readonly enCours = signal(false);
  protected readonly masquerMotDePasse = signal(true);

  protected readonly mdpModele = signal<MotDePasseModele>({ motDePasse: '', confirmation: '' });
  protected readonly mdpForm = form(this.mdpModele, motDePasseSchema);

  protected readonly otpModele = signal<OtpModele>({ code: '' });
  protected readonly otpForm = form(this.otpModele, otpSchema);

  constructor() {
    if (!this.token()) {
      this.etape.set('lien-invalide');
    }
  }

  protected readonly titre = computed(() => {
    switch (this.etape()) {
      case 'mot-de-passe':
        return 'Choisissez votre mot de passe';
      case 'otp':
        return 'Vérification du code';
      case 'termine':
        return 'Compte activé';
      default:
        return 'Lien invalide';
    }
  });

  protected async onSubmitMotDePasse(event: Event): Promise<void> {
    event.preventDefault();
    if (this.enCours()) {
      return;
    }
    await submit(this.mdpForm, async (f) => {
      const { motDePasse, confirmation } = f().value();
      if (motDePasse !== confirmation) {
        return [{ fieldTree: this.mdpForm.confirmation, kind: 'server', message: 'Les mots de passe ne correspondent pas.' }];
      }
      this.enCours.set(true);
      try {
        await firstValueFrom(this.api.definirMotDePasse(this.token()!, motDePasse, confirmation));
        this.etape.set('otp');
        return undefined;
      } catch (erreur) {
        return [{ fieldTree: this.mdpForm, kind: 'server', message: this.messageErreur(erreur) }];
      } finally {
        this.enCours.set(false);
      }
    });
  }

  protected async onSubmitOtp(event: Event): Promise<void> {
    event.preventDefault();
    if (this.enCours()) {
      return;
    }
    await submit(this.otpForm, async (f) => {
      this.enCours.set(true);
      try {
        await firstValueFrom(this.api.validerOtp(this.token()!, f().value().code));
        this.etape.set('termine');
        return undefined;
      } catch (erreur) {
        return [{ fieldTree: this.otpForm, kind: 'server', message: this.messageErreur(erreur) }];
      } finally {
        this.enCours.set(false);
      }
    });
  }

  protected async renvoyerOtp(): Promise<void> {
    if (this.enCours()) {
      return;
    }
    this.enCours.set(true);
    try {
      await firstValueFrom(this.api.renvoyerOtp(this.token()!));
      this.notification.succes('Un nouveau code a été envoyé.');
    } catch (erreur) {
      this.notification.erreur(this.messageErreur(erreur));
    } finally {
      this.enCours.set(false);
    }
  }

  protected allerConnexion(): void {
    this.router.navigateByUrl('/connexion');
  }

  private messageErreur(erreur: unknown): string {
    return erreur instanceof HttpErrorResponse && estProblemDetail(erreur.error) && erreur.error.detail
      ? erreur.error.detail
      : "Ce lien ou ce code n'est plus valide.";
  }
}
