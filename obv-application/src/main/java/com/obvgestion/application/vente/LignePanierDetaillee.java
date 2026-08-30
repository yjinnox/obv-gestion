package com.obvgestion.application.vente;

/** §8.2 étape 3 — ligne de panier enrichie du prix de vente courant (non figé tant que non commandée). */
public record LignePanierDetaillee(int id, Long produitId, String marqueLibelle, String volumeLibelle,
                                    long quantiteDemiCasiers, long prixVenteCasierXof, long montantLigneXof) {
}
