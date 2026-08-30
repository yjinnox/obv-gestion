package com.obvgestion.application.transfert;

import com.obvgestion.domain.transfert.BonTransfert;
import com.obvgestion.domain.transfert.StatutTransfert;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

/** Port de persistance des bons de transfert, implémenté en infrastructure. */
public interface BonTransfertRepository {

    BonTransfert enregistrer(BonTransfert transfert);

    Optional<BonTransfert> parId(Long id);

    Page<BonTransfert> rechercher(Long pointDeVenteSourceId, Long pointDeVenteDestinationId, StatutTransfert statut,
                                   Pageable pageable);
}
