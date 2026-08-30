import { XofPipe } from './xof.pipe';

describe('XofPipe', () => {
  const pipe = new XofPipe();

  it('formate un montant entier avec séparateur de milliers et devise (H1 : jamais de décimale)', () => {
    expect(pipe.transform(12500)).toContain('12');
    expect(pipe.transform(12500)).toContain('500');
    expect(pipe.transform(12500)).not.toMatch(/[,.]\d{2}\b/);
  });

  it('formate zéro correctement', () => {
    expect(pipe.transform(0)).toMatch(/0/);
  });

  it('affiche un tiret pour une valeur absente (montant non renseigné, ex. stock sans tarif ACHAT connu)', () => {
    expect(pipe.transform(null)).toBe('—');
    expect(pipe.transform(undefined)).toBe('—');
  });
});
