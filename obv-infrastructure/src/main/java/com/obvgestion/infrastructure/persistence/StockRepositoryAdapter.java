package com.obvgestion.infrastructure.persistence;

import com.obvgestion.application.stock.StockRepository;
import com.obvgestion.domain.stock.Stock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
class StockRepositoryAdapter implements StockRepository {

    private final StockJpaRepository jpaRepository;

    StockRepositoryAdapter(StockJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Stock enregistrer(Stock stock) {
        return jpaRepository.save(stock);
    }

    @Override
    public Optional<Stock> parPointDeVenteEtProduit(Long pointDeVenteId, Long produitId) {
        return jpaRepository.parPointDeVenteEtProduit(pointDeVenteId, produitId);
    }

    @Override
    public Page<Stock> rechercher(Long pointDeVenteId, Pageable pageable) {
        return jpaRepository.rechercher(pointDeVenteId, pageable);
    }
}
