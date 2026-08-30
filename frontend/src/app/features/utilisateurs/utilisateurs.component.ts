import { Component, computed, inject, signal } from '@angular/core';
import { rxResource } from '@angular/core/rxjs-interop';
import { MatButtonModule } from '@angular/material/button';
import { MatChipsModule } from '@angular/material/chips';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatMenuModule } from '@angular/material/menu';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSelectModule } from '@angular/material/select';
import { MatTableModule } from '@angular/material/table';
import { form, FormField, required, schema, submit } from '@angular/forms/signals';
import { firstValueFrom } from 'rxjs';
import { UtilisateurApiService } from '../../core/api/utilisateur-api.service';
import { ReferentielApiService } from '../../core/api/referentiel-api.service';
import {
  AffectationRequest,
  LIBELLES_ROLE,
  LIBELLES_STATUT_UTILISATEUR,
  RoleUtilisateur,
  ROLES_AVEC_POINT_DE_VENTE,
  StatutUtilisateur,
  TYPE_POINT_DE_VENTE_REQUIS,
  UtilisateurResponse,
} from '../../core/api/models/utilisateur.model';
import { CanalContact } from '../../core/api/models/utilisateur.model';
import { NotificationService } from '../../core/ui/notification.service';
import { AuthService } from '../../core/auth/auth.service';
import { ConfirmService } from '../../shared/components/confirm-dialog/confirm.service';

interface UtilisateurModele {
  nom: string;
  prenoms: string;
  canalContact: CanalContact;
  email: string;
  telephone: string;
}

const videCreation: UtilisateurModele = { nom: '', prenoms: '', canalContact: 'EMAIL', email: '', telephone: '' };

const utilisateurSchema = schema<UtilisateurModele>((champ) => {
  required(champ.nom, { message: 'Le nom est requis.' });
  required(champ.prenoms, { message: 'Les prénoms sont requis.' });
});

/** §4.1, §4.3 — administration des comptes utilisateurs, rôles et affectations. */
@Component({
  selector: 'app-utilisateurs',
  imports: [
    FormField,
    MatButtonModule,
    MatChipsModule,
    MatFormFieldModule,
    MatInputModule,
    MatIconModule,
    MatMenuModule,
    MatPaginatorModule,
    MatProgressSpinnerModule,
    MatSelectModule,
    MatTableModule,
  ],
  templateUrl: './utilisateurs.component.html',
  styleUrl: './utilisateurs.component.scss',
})
export class UtilisateursComponent {
  private readonly api = inject(UtilisateurApiService);
  private readonly referentielApi = inject(ReferentielApiService);
  private readonly notification = inject(NotificationService);
  private readonly confirm = inject(ConfirmService);
  protected readonly auth = inject(AuthService);

  protected readonly libellesRole: Record<string, string> = LIBELLES_ROLE;
  protected readonly libellesStatut: Record<string, string> = LIBELLES_STATUT_UTILISATEUR;
  protected readonly tousLesRoles = Object.keys(LIBELLES_ROLE) as RoleUtilisateur[];
  protected readonly colonnes = ['nom', 'contact', 'statut', 'affectations', 'actions'];

  protected readonly ligneOuverte = signal<number | null>(null);
  protected readonly enCours = signal(false);
  protected readonly afficherFormulaireCreation = signal(false);

  protected readonly filtreStatut = signal<StatutUtilisateur | null>(null);
  protected readonly filtreRole = signal<RoleUtilisateur | null>(null);
  protected readonly recherche = signal('');
  protected readonly page = signal(0);
  protected readonly taillePage = signal(20);

  protected readonly modele = signal<UtilisateurModele>({ ...videCreation });
  protected readonly formulaire = form(this.modele, utilisateurSchema);
  protected readonly affectationsAAjouter = signal<AffectationRequest[]>([]);
  protected readonly roleAAjouter = signal<RoleUtilisateur>('VENDEUR');
  protected readonly pdvAAjouter = signal<number | null>(null);

  protected readonly pointsDeVente = rxResource({
    stream: () => this.referentielApi.pointsDeVente.lister({ actif: true }, { size: 100 }),
  });

  protected readonly roleAAjouterNecessitePdv = computed(() => ROLES_AVEC_POINT_DE_VENTE.includes(this.roleAAjouter()));
  protected readonly optionsPdvPourRoleAAjouter = computed(() => {
    const typeRequis = TYPE_POINT_DE_VENTE_REQUIS[this.roleAAjouter()];
    const tous = this.pointsDeVente.value()?.content ?? [];
    return typeRequis ? tous.filter((p) => p.type === typeRequis) : tous;
  });

  protected readonly utilisateurs = rxResource({
    params: () => ({
      statut: this.filtreStatut(),
      role: this.filtreRole(),
      recherche: this.recherche(),
      page: this.page(),
      size: this.taillePage(),
    }),
    stream: ({ params }) =>
      this.api.lister(
        {
          statut: params.statut ?? undefined,
          role: params.role ?? undefined,
          recherche: params.recherche || undefined,
        },
        { page: params.page, size: params.size },
      ),
  });

  libellePointDeVente(id: number | null): string {
    if (id === null) return '';
    return this.pointsDeVente.value()?.content.find((p) => p.id === id)?.libelle ?? `#${id}`;
  }

  protected changerPage(event: PageEvent): void {
    this.page.set(event.pageIndex);
    this.taillePage.set(event.pageSize);
  }

  protected basculerLigne(id: number): void {
    this.ligneOuverte.set(this.ligneOuverte() === id ? null : id);
  }

  protected ajouterAffectationALaListe(): void {
    if (this.roleAAjouterNecessitePdv() && this.pdvAAjouter() === null) {
      return;
    }
    this.affectationsAAjouter.update((liste) => [
      ...liste,
      { role: this.roleAAjouter(), pointDeVenteId: this.roleAAjouterNecessitePdv() ? this.pdvAAjouter() : null },
    ]);
    this.pdvAAjouter.set(null);
  }

  protected retirerAffectationDeLaListe(index: number): void {
    this.affectationsAAjouter.update((liste) => liste.filter((_, i) => i !== index));
  }

  protected async onSubmitCreation(event: Event): Promise<void> {
    event.preventDefault();
    if (this.affectationsAAjouter().length === 0) {
      this.notification.erreur('Ajoutez au moins une affectation.');
      return;
    }
    await submit(this.formulaire, async (f) => {
      this.enCours.set(true);
      try {
        const valeur = f().value();
        await firstValueFrom(
          this.api.creer({
            nom: valeur.nom,
            prenoms: valeur.prenoms,
            canalContact: valeur.canalContact,
            email: valeur.canalContact === 'EMAIL' ? valeur.email : undefined,
            telephone: valeur.canalContact === 'TELEPHONE' ? valeur.telephone : undefined,
            affectations: this.affectationsAAjouter(),
          }),
        );
        this.notification.succes('Utilisateur créé et invité.');
        this.annulerCreation();
        this.utilisateurs.reload();
        return undefined;
      } catch {
        return [{ fieldTree: this.formulaire, kind: 'server', message: 'Échec de la création.' }];
      } finally {
        this.enCours.set(false);
      }
    });
  }

  protected annulerCreation(): void {
    this.afficherFormulaireCreation.set(false);
    this.modele.set({ ...videCreation });
    this.affectationsAAjouter.set([]);
  }

  protected async activer(utilisateur: UtilisateurResponse): Promise<void> {
    await firstValueFrom(this.api.activer(utilisateur.id));
    this.notification.succes('Utilisateur activé.');
    this.utilisateurs.reload();
  }

  protected async desactiver(utilisateur: UtilisateurResponse): Promise<void> {
    const confirme = await this.confirm.demander({
      titre: 'Désactiver le compte',
      message: `Désactiver le compte de ${utilisateur.nom} ${utilisateur.prenoms} ? Il ne pourra plus se connecter.`,
      destructif: true,
      libelleConfirmation: 'Désactiver',
    });
    if (!confirme) return;
    await firstValueFrom(this.api.desactiver(utilisateur.id));
    this.notification.succes('Utilisateur désactivé.');
    this.utilisateurs.reload();
  }

  protected async archiver(utilisateur: UtilisateurResponse): Promise<void> {
    const confirme = await this.confirm.demander({
      titre: 'Archiver le compte',
      message: `Archiver le compte de ${utilisateur.nom} ${utilisateur.prenoms} ? Cette action est définitive.`,
      destructif: true,
      libelleConfirmation: 'Archiver',
    });
    if (!confirme) return;
    await firstValueFrom(this.api.archiver(utilisateur.id));
    this.notification.succes('Utilisateur archivé.');
    this.utilisateurs.reload();
  }

  protected async reinitialiserMotDePasse(utilisateur: UtilisateurResponse): Promise<void> {
    const confirme = await this.confirm.demander({
      titre: 'Réinitialiser le mot de passe',
      message: `Envoyer un nouveau lien d'activation à ${utilisateur.nom} ${utilisateur.prenoms} ?`,
      libelleConfirmation: 'Envoyer',
    });
    if (!confirme) return;
    await firstValueFrom(this.api.reinitialiserMotDePasse(utilisateur.id));
    this.notification.succes('Lien de réinitialisation envoyé.');
  }

  protected async retirerAffectation(utilisateur: UtilisateurResponse, affectationId: number): Promise<void> {
    await firstValueFrom(this.api.retirerAffectation(utilisateur.id, affectationId));
    this.notification.succes('Affectation retirée.');
    this.utilisateurs.reload();
  }

  protected async ajouterAffectationExistant(utilisateur: UtilisateurResponse): Promise<void> {
    if (this.roleAAjouterNecessitePdv() && this.pdvAAjouter() === null) {
      return;
    }
    await firstValueFrom(
      this.api.ajouterAffectation(utilisateur.id, {
        role: this.roleAAjouter(),
        pointDeVenteId: this.roleAAjouterNecessitePdv() ? this.pdvAAjouter() : null,
      }),
    );
    this.notification.succes('Affectation ajoutée.');
    this.pdvAAjouter.set(null);
    this.utilisateurs.reload();
  }
}
