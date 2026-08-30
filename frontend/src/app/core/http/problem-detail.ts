/** Corps d'erreur RFC 7807 renvoyé par l'API (voir GlobalExceptionHandler). */
export interface ProblemDetail {
  type?: string;
  title?: string;
  status?: number;
  detail?: string;
  instance?: string;
  code?: string;
}

export function estProblemDetail(valeur: unknown): valeur is ProblemDetail {
  return typeof valeur === 'object' && valeur !== null && ('detail' in valeur || 'code' in valeur);
}
