package com.obvgestion.domain.stock;

import com.obvgestion.domain.commun.RegleGestionException;

/** RG-15/RG-19 — le stock ne peut jamais devenir négatif. */
public final class StockInsuffisantException extends RegleGestionException {
    public StockInsuffisantException(String produitLibelle, long quantiteDemandee, long quantiteDisponible) {
        super("STOCK_INSUFFISANT", "Stock insuffisant pour " + produitLibelle + " : demandé " + quantiteDemandee
                + ", disponible " + quantiteDisponible + ".");
    }
}
