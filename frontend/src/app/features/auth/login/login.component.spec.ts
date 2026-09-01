import { destinationApresConnexion } from './login.component';

describe('destinationApresConnexion', () => {
  it("revient sur l'URL demandée avant la redirection vers la connexion", () => {
    expect(destinationApresConnexion('/receptions/12?token=abc')).toBe('/receptions/12?token=abc');
  });

  it('retombe sur le tableau de bord en l’absence de returnUrl', () => {
    expect(destinationApresConnexion(null)).toBe('/tableau-de-bord');
    expect(destinationApresConnexion('')).toBe('/tableau-de-bord');
  });

  it("refuse une URL externe ou protocole-relative (open redirect) et retombe sur le tableau de bord", () => {
    expect(destinationApresConnexion('https://exemple-malveillant.test/phishing')).toBe('/tableau-de-bord');
    expect(destinationApresConnexion('//exemple-malveillant.test/phishing')).toBe('/tableau-de-bord');
    expect(destinationApresConnexion('receptions/12')).toBe('/tableau-de-bord');
  });
});
