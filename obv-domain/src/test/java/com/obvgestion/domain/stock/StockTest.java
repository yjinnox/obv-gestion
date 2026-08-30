package com.obvgestion.domain.stock;

import com.obvgestion.domain.referentiel.Marque;
import com.obvgestion.domain.referentiel.PointDeVente;
import com.obvgestion.domain.referentiel.Produit;
import com.obvgestion.domain.referentiel.TypePointDeVente;
import com.obvgestion.domain.referentiel.Volume;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StockTest {

    private final PointDeVente depot = new PointDeVente("Dépôt central", TypePointDeVente.DEPOT, "Abidjan");
    private final Produit produit = new Produit(new Marque("Flag"), new Volume("33 cl", 330));

    /** RG-14 — une entrée incrémente le solde. */
    @Test
    void uneEntreeIncrementeLeSolde() {
        Stock stock = new Stock(depot, produit, 10);
        stock.appliquer(4);
        assertThat(stock.getQuantite()).isEqualTo(14);
    }

    /** RG-14 — une sortie décrémente le solde. */
    @Test
    void uneSortieDecrementeLeSolde() {
        Stock stock = new Stock(depot, produit, 10);
        stock.appliquer(-4);
        assertThat(stock.getQuantite()).isEqualTo(6);
    }

    /** RG-15 — le stock ne peut jamais devenir négatif. */
    @Test
    void uneSortieQuiRendraitLeStockNegatifEstRejetee() {
        Stock stock = new Stock(depot, produit, 3);
        assertThatThrownBy(() -> stock.appliquer(-4)).isInstanceOf(StockInsuffisantException.class);
        assertThat(stock.getQuantite()).isEqualTo(3);
    }

    @Test
    void uneSortieExactementEgaleAuSoldeAmeneAZero() {
        Stock stock = new Stock(depot, produit, 5);
        stock.appliquer(-5);
        assertThat(stock.getQuantite()).isZero();
    }
}
