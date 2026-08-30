/** Déclenche le téléchargement navigateur d'un Blob reçu de l'API (PDF, CSV). */
export function telechargerFichier(blob: Blob, nomFichier: string): void {
  const url = URL.createObjectURL(blob);
  const lien = document.createElement('a');
  lien.href = url;
  lien.download = nomFichier;
  lien.click();
  URL.revokeObjectURL(url);
}
