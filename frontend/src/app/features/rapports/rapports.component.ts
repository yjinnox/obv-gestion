import { KeyValuePipe } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { rxResource } from '@angular/core/rxjs-interop';
import { MatButtonModule } from '@angular/material/button';
import { MatButtonToggleModule } from '@angular/material/button-toggle';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSelectModule } from '@angular/material/select';
import { firstValueFrom, of } from 'rxjs';
import { RapportApiService } from '../../core/api/rapport-api.service';
import { ReferentielApiService } from '../../core/api/referentiel-api.service';
import { LIBELLES_MODE_PAIEMENT } from '../../core/api/models/enums';
import { XofPipe } from '../../shared/pipes/xof.pipe';
import { telechargerFichier } from '../../shared/util/telecharger-fichier';

type Onglet = 'ventes' | 'stock';

/** §7 (P7) — rapports de ventes et de stock valorisé, avec export CSV. */
@Component({
  selector: 'app-rapports',
  imports: [
    KeyValuePipe,
    MatButtonModule,
    MatButtonToggleModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatSelectModule,
    XofPipe,
  ],
  templateUrl: './rapports.component.html',
  styleUrl: './rapports.component.scss',
})
export class RapportsComponent {
  private readonly rapportApi = inject(RapportApiService);
  private readonly referentielApi = inject(ReferentielApiService);

  protected readonly libellesModePaiement: Record<string, string> = LIBELLES_MODE_PAIEMENT;
  protected readonly onglet = signal<Onglet>('ventes');

  protected readonly pointsDeVente = rxResource({
    stream: () => this.referentielApi.pointsDeVente.lister({ actif: true }, { size: 100 }),
  });

  protected readonly pdvVentes = signal<number | null>(null);
  protected readonly du = signal<string>('');
  protected readonly au = signal<string>('');

  protected readonly rapportVentes = rxResource({
    params: () => ({ pdv: this.pdvVentes(), du: this.du(), au: this.au() }),
    stream: ({ params }) =>
      params.pdv === null
        ? of(undefined)
        : this.rapportApi.ventes({
            pdv: params.pdv,
            du: params.du ? new Date(params.du).toISOString() : undefined,
            au: params.au ? new Date(params.au).toISOString() : undefined,
          }),
  });

  protected readonly pdvStock = signal<number | null>(null);
  protected readonly rapportStock = rxResource({
    params: () => this.pdvStock(),
    stream: ({ params }) => this.rapportApi.stockValorise(params === null ? {} : { pdv: params }),
  });

  protected async exporterVentesCsv(): Promise<void> {
    if (this.pdvVentes() === null) return;
    const blob = await firstValueFrom(
      this.rapportApi.exporterVentesCsv({
        pdv: this.pdvVentes()!,
        du: this.du() ? new Date(this.du()).toISOString() : undefined,
        au: this.au() ? new Date(this.au()).toISOString() : undefined,
      }),
    );
    telechargerFichier(blob, 'rapport-ventes.csv');
  }

  protected async exporterStockCsv(): Promise<void> {
    const blob = await firstValueFrom(
      this.rapportApi.exporterStockValoriseCsv(this.pdvStock() === null ? {} : { pdv: this.pdvStock()! }),
    );
    telechargerFichier(blob, 'rapport-stock-valorise.csv');
  }
}
