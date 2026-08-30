import { HttpClient, HttpContext } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { SANS_TOAST_ERREUR } from '../../../core/http/http-context';

const SANS_TOAST = new HttpContext().set(SANS_TOAST_ERREUR, true);

/** §4.2 — activation de compte : mot de passe puis OTP (voir ActivationService). */
@Injectable({ providedIn: 'root' })
export class ActivationApiService {
  private readonly http = inject(HttpClient);

  definirMotDePasse(token: string, motDePasse: string, confirmation: string): Observable<void> {
    return this.http.post<void>(
      `/api/v1/auth/activation/${encodeURIComponent(token)}/mot-de-passe`,
      { motDePasse, confirmation },
      { context: SANS_TOAST },
    );
  }

  validerOtp(token: string, code: string): Observable<void> {
    return this.http.post<void>(
      `/api/v1/auth/activation/${encodeURIComponent(token)}/otp`,
      { code },
      { context: SANS_TOAST },
    );
  }

  renvoyerOtp(token: string): Observable<void> {
    return this.http.post<void>('/api/v1/auth/otp/renvoyer', { token }, { context: SANS_TOAST });
  }
}
