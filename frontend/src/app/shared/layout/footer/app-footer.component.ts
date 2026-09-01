import { ChangeDetectionStrategy, Component } from '@angular/core';

/** Mention de copyright affichée en pied de toutes les pages. */
@Component({
  selector: 'app-footer',
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `<footer class="pied-page">Copyright ASSOMA Technology - {{ annee }}</footer>`,
  styles: `
    /* Orange de la charte (§15.1) : lisible aussi bien sur le fond clair des
       écrans internes que sur le vert des écrans publics. */
    .pied-page {
      padding: 16px 8px;
      text-align: center;
      font-size: 1.3125rem;
      color: var(--orange-primaire);
    }
  `,
})
export class AppFooterComponent {
  /** Recalculée à chaque chargement de l'application : l'année n'est jamais figée dans le code. */
  protected readonly annee = new Date().getFullYear();
}
