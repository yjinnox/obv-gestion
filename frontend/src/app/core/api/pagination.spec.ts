import { versParams } from './pagination';

describe('versParams', () => {
  it('ajoute page, size et sort quand fournis', () => {
    const params = versParams({ page: 2, size: 10, sort: 'nom,asc' });
    expect(params.get('page')).toBe('2');
    expect(params.get('size')).toBe('10');
    expect(params.get('sort')).toBe('nom,asc');
  });

  it('accepte plusieurs valeurs de tri (append, pas set)', () => {
    const params = versParams({ sort: ['nom,asc', 'id,desc'] });
    expect(params.getAll('sort')).toEqual(['nom,asc', 'id,desc']);
  });

  it('omet les filtres undefined, null ou vides (ne les envoie pas comme des chaînes littérales)', () => {
    const params = versParams(undefined, { actif: true, recherche: '', pdv: undefined, statut: null });
    expect(params.get('actif')).toBe('true');
    expect(params.has('recherche')).toBe(false);
    expect(params.has('pdv')).toBe(false);
    expect(params.has('statut')).toBe(false);
  });

  it('ne pose aucun paramètre en l’absence de pageable et de filtres', () => {
    const params = versParams();
    expect(params.keys().length).toBe(0);
  });
});
