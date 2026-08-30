package com.obvgestion.application.stock;

import com.obvgestion.domain.referentiel.PointDeVente;
import com.obvgestion.domain.referentiel.Produit;
import com.obvgestion.domain.stock.MouvementStock;
import com.obvgestion.domain.stock.Stock;
import com.obvgestion.domain.stock.TypeMouvementStock;
import com.obvgestion.domain.utilisateur.Utilisateur;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Instant;

/** §6.2 — mouvements de stock (RG-14/RG-15/RG-16). */
@Service
public class StockService {

    private static final int TENTATIVES_MAX = 3;

    private final StockRepository stockRepository;
    private final MouvementStockRepository mouvementStockRepository;
    private final TransactionTemplate transactionNouvelle;

    public StockService(StockRepository stockRepository, MouvementStockRepository mouvementStockRepository,
                         PlatformTransactionManager transactionManager) {
        this.stockRepository = stockRepository;
        this.mouvementStockRepository = mouvementStockRepository;
        this.transactionNouvelle = new TransactionTemplate(transactionManager);
        this.transactionNouvelle.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    /**
     * RG-14 — met à jour {@link Stock} et journalise le {@link MouvementStock}
     * dans une même transaction dédiée. RG-16 — en cas de conflit de verrou
     * optimiste, l'opération est rejouée jusqu'à {@value #TENTATIVES_MAX} fois
     * avant de propager l'échec. {@code quantiteSignee} positive pour une
     * entrée, négative pour une sortie (RG-15 : rejetée si elle rend le stock
     * négatif, via {@link com.obvgestion.domain.stock.StockInsuffisantException}
     * — jamais rejouée).
     */
    public Stock appliquer(PointDeVente pointDeVente, Produit produit, TypeMouvementStock type, long quantiteSignee,
                            String documentType, Long documentId, Utilisateur utilisateur) {
        for (int tentative = 1; tentative <= TENTATIVES_MAX; tentative++) {
            try {
                return transactionNouvelle.execute(status -> appliquerDansTransaction(
                        pointDeVente, produit, type, quantiteSignee, documentType, documentId, utilisateur));
            } catch (OptimisticLockingFailureException e) {
                if (tentative == TENTATIVES_MAX) {
                    throw e;
                }
            }
        }
        throw new IllegalStateException("Nombre de tentatives de verrou optimiste dépassé sans résultat.");
    }

    private Stock appliquerDansTransaction(PointDeVente pointDeVente, Produit produit, TypeMouvementStock type,
                                            long quantiteSignee, String documentType, Long documentId,
                                            Utilisateur utilisateur) {
        Stock stock = stockRepository.parPointDeVenteEtProduit(pointDeVente.getId(), produit.getId())
                .orElseGet(() -> new Stock(pointDeVente, produit, 0));
        long stockAvant = stock.getQuantite();
        stock.appliquer(quantiteSignee);
        long stockApres = stock.getQuantite();

        Stock stockEnregistre = stockRepository.enregistrer(stock);
        mouvementStockRepository.enregistrer(new MouvementStock(pointDeVente, produit, type, quantiteSignee,
                stockAvant, stockApres, documentType, documentId, Instant.now(), utilisateur));
        return stockEnregistre;
    }

    @Transactional(readOnly = true)
    public Page<Stock> rechercher(Long pointDeVenteId, Pageable pageable) {
        return stockRepository.rechercher(pointDeVenteId, pageable);
    }

    @Transactional(readOnly = true)
    public Page<MouvementStock> rechercherMouvements(Long pointDeVenteId, Long produitId, Instant du, Instant au,
                                                       Pageable pageable) {
        return mouvementStockRepository.rechercher(pointDeVenteId, produitId, du, au, pageable);
    }
}
