import { Pipe, PipeTransform } from '@angular/core';

const FORMATEUR = new Intl.NumberFormat('fr-FR', {
  style: 'currency',
  currency: 'XOF',
  maximumFractionDigits: 0,
});

/** Montant XOF (entier, sans décimale — H1) formaté « 12 500 F CFA ». */
@Pipe({ name: 'xof' })
export class XofPipe implements PipeTransform {
  transform(valeur: number | null | undefined): string {
    return valeur === null || valeur === undefined ? '—' : FORMATEUR.format(valeur);
  }
}
