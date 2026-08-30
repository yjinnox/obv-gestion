import { Component, computed, inject, signal } from '@angular/core';
import { rxResource } from '@angular/core/rxjs-interop';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSelectModule } from '@angular/material/select';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { MatTableModule } from '@angular/material/table';
import { form, FormField, required, schema, submit } from '@angular/forms/signals';
import { firstValueFrom } from 'rxjs';
import { ReferentielApiService } from '../../../core/api/referentiel-api.service';
import { ClientResponse } from '../../../core/api/models/referentiel.model';
import { TypeClient } from '../../../core/api/models/enums';
import { NotificationService } from '../../../core/ui/notification.service';
import { AuthService } from '../../../core/auth/auth.service';

interface ClientModele {
  type: TypeClient;
  nom: string;
  prenoms: string;
  raisonSociale: string;
  telephone: string;
  email: string;
  adresseFacturation: string;
}

const videCreation: ClientModele = {
  type: 'PARTICULIER',
  nom: '',
  prenoms: '',
  raisonSociale: '',
  telephone: '',
  email: '',
  adresseFacturation: '',
};

const clientSchema = schema<ClientModele>((champ) => {
  required(champ.telephone, { message: 'Le téléphone est requis.' });
});

/** §5, RG-07 — référentiel des clients du dépôt (particuliers ou entreprises). */
@Component({
  selector: 'app-clients',
  imports: [
    FormField,
    MatButtonModule,
    MatFormFieldModule,
    MatInputModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatSelectModule,
    MatSlideToggleModule,
    MatTableModule,
  ],
  templateUrl: './clients.component.html',
  styleUrl: '../referentiel-liste.scss',
})
export class ClientsComponent {
  private readonly api = inject(ReferentielApiService);
  private readonly notification = inject(NotificationService);
  protected readonly auth = inject(AuthService);

  protected readonly colonnes = ['nomAffiche', 'telephone', 'email', 'actif', 'actions'];
  protected readonly enCours = signal(false);
  protected readonly idEnEdition = signal<number | null>(null);

  protected readonly modele = signal<ClientModele>({ ...videCreation });
  protected readonly formulaire = form(this.modele, clientSchema);
  protected readonly estEntreprise = computed(() => this.modele().type === 'ENTREPRISE');

  protected readonly clients = rxResource({
    stream: () => this.api.clients.lister({}, { size: 100 }),
  });

  nomAffiche(client: ClientResponse): string {
    return client.type === 'ENTREPRISE' ? client.raisonSociale : `${client.nom} ${client.prenoms}`;
  }

  protected async onSubmit(event: Event): Promise<void> {
    event.preventDefault();
    await submit(this.formulaire, async (f) => {
      this.enCours.set(true);
      try {
        const valeur = f().value();
        const id = this.idEnEdition();
        if (id === null) {
          await firstValueFrom(this.api.clients.creer(valeur));
          this.notification.succes('Client créé.');
        } else {
          const existant = this.clients.value()?.content.find((x) => x.id === id);
          await firstValueFrom(
            this.api.clients.modifier(id, {
              telephone: valeur.telephone,
              email: valeur.email,
              adresseFacturation: valeur.adresseFacturation,
              actif: existant?.actif ?? true,
            }),
          );
          this.notification.succes('Client modifié.');
        }
        this.annulerEdition();
        this.clients.reload();
        return undefined;
      } catch {
        return [{ fieldTree: this.formulaire, kind: 'server', message: 'Échec de l’enregistrement.' }];
      } finally {
        this.enCours.set(false);
      }
    });
  }

  protected modifier(client: ClientResponse): void {
    this.idEnEdition.set(client.id);
    this.modele.set({
      type: client.type,
      nom: client.nom ?? '',
      prenoms: client.prenoms ?? '',
      raisonSociale: client.raisonSociale ?? '',
      telephone: client.telephone ?? '',
      email: client.email ?? '',
      adresseFacturation: client.adresseFacturation ?? '',
    });
  }

  protected annulerEdition(): void {
    this.idEnEdition.set(null);
    this.modele.set({ ...videCreation });
  }

  protected async basculerActif(client: ClientResponse): Promise<void> {
    await firstValueFrom(
      this.api.clients.modifier(client.id, {
        telephone: client.telephone,
        email: client.email,
        adresseFacturation: client.adresseFacturation,
        actif: !client.actif,
      }),
    );
    this.notification.succes(client.actif ? 'Client désactivé.' : 'Client activé.');
    this.clients.reload();
  }
}
