package com.obvgestion.application.rapport;

import com.obvgestion.application.bar.TicketServeurRepository;
import com.obvgestion.application.referentiel.PointDeVenteRepository;
import com.obvgestion.application.referentiel.TarifRepository;
import com.obvgestion.application.stock.StockRepository;
import com.obvgestion.application.vente.VenteRepository;
import com.obvgestion.domain.bar.LigneTicketServeur;
import com.obvgestion.domain.bar.StatutTicketServeur;
import com.obvgestion.domain.bar.TicketServeur;
import com.obvgestion.domain.referentiel.NatureTarif;
import com.obvgestion.domain.referentiel.PointDeVente;
import com.obvgestion.domain.referentiel.TypePointDeVente;
import com.obvgestion.domain.referentiel.UniteVente;
import com.obvgestion.domain.stock.Stock;
import com.obvgestion.domain.vente.LigneVente;
import com.obvgestion.domain.vente.Vente;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.ZoneId;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.Function;
import java.util.stream.Collectors;

/** §13 — rapport de ventes sur une période et stock valorisé (phase P7). */
@Service
public class RapportService {

    private final PointDeVenteRepository pointDeVenteRepository;
    private final VenteRepository venteRepository;
    private final TicketServeurRepository ticketServeurRepository;
    private final StockRepository stockRepository;
    private final TarifRepository tarifRepository;
    private final ZoneId fuseauHoraireMetier;

    public RapportService(PointDeVenteRepository pointDeVenteRepository, VenteRepository venteRepository,
                           TicketServeurRepository ticketServeurRepository, StockRepository stockRepository,
                           TarifRepository tarifRepository, @Value("${app.timezone}") String fuseauHoraireMetier) {
        this.pointDeVenteRepository = pointDeVenteRepository;
        this.venteRepository = venteRepository;
        this.ticketServeurRepository = ticketServeurRepository;
        this.stockRepository = stockRepository;
        this.tarifRepository = tarifRepository;
        this.fuseauHoraireMetier = ZoneId.of(fuseauHoraireMetier);
    }

    /** §13 — quantités et recette d'un point de vente sur une période, ventilées selon son type (dépôt ou bar). */
    @Transactional(readOnly = true)
    public RapportVentes rapportVentes(Long pointDeVenteId, Instant du, Instant au) {
        PointDeVente pointDeVente = pointDeVenteRepository.parId(pointDeVenteId)
                .orElseThrow(() -> new NoSuchElementException("Point de vente introuvable : " + pointDeVenteId));

        return pointDeVente.getType() == TypePointDeVente.DEPOT
                ? rapportVentesDepot(pointDeVente, du, au)
                : rapportVentesBar(pointDeVente, du, au);
    }

    private RapportVentes rapportVentesDepot(PointDeVente pointDeVente, Instant du, Instant au) {
        List<Vente> ventes = venteRepository.parPointDeVenteEtPeriode(pointDeVente.getId(), du, au);
        List<LigneVente> lignes = ventes.stream().flatMap(v -> v.getLignes().stream()).toList();

        Map<String, Long> parMarque = totauxLong(lignes, l -> l.getProduit().getMarque().getLibelle(),
                LigneVente::quantiteDemiCasiers);
        Map<String, Long> parVolume = totauxLong(lignes, l -> l.getProduit().getVolume().getLibelle(),
                LigneVente::quantiteDemiCasiers);
        long quantiteTotale = lignes.stream().mapToLong(LigneVente::quantiteDemiCasiers).sum();

        Map<String, Long> parModePaiement = ventes.stream()
                .collect(Collectors.groupingBy(v -> v.getModePaiement().name(), LinkedHashMap::new,
                        Collectors.summingLong(v -> v.getMontantTotal().valeurXof())));
        Map<String, Long> parJour = ventes.stream()
                .collect(Collectors.groupingBy(v -> jourDe(v.getDateHeure()), LinkedHashMap::new,
                        Collectors.summingLong(v -> v.getMontantTotal().valeurXof())));
        long recetteTotale = ventes.stream().mapToLong(v -> v.getMontantTotal().valeurXof()).sum();

        return new RapportVentes(pointDeVente.getId(), pointDeVente.getLibelle(), pointDeVente.getType(), du, au,
                quantiteTotale, parMarque, parVolume, parModePaiement, Map.of(), parJour, recetteTotale);
    }

    private RapportVentes rapportVentesBar(PointDeVente pointDeVente, Instant du, Instant au) {
        List<TicketServeur> tickets = ticketServeurRepository.parPointDeVenteEtPeriode(pointDeVente.getId(), du, au)
                .stream()
                .filter(t -> t.getStatut() == StatutTicketServeur.ENCAISSE)
                .toList();
        List<LigneTicketServeur> lignes = tickets.stream().flatMap(t -> t.getLignes().stream()).toList();

        Map<String, Long> parMarque = totauxLong(lignes, l -> l.getProduit().getMarque().getLibelle(),
                LigneTicketServeur::getQuantiteBouteilles);
        Map<String, Long> parVolume = totauxLong(lignes, l -> l.getProduit().getVolume().getLibelle(),
                LigneTicketServeur::getQuantiteBouteilles);
        long quantiteTotale = lignes.stream().mapToLong(LigneTicketServeur::getQuantiteBouteilles).sum();

        Map<String, Long> parServeur = tickets.stream()
                .flatMap(t -> t.getLignes().stream()
                        .map(l -> Map.entry(t.getServeur().getNom() + " " + t.getServeur().getPrenoms(),
                                l.getQuantiteBouteilles())))
                .collect(Collectors.groupingBy(Map.Entry::getKey, LinkedHashMap::new,
                        Collectors.summingLong(Map.Entry::getValue)));
        Map<String, Long> parJour = tickets.stream()
                .collect(Collectors.groupingBy(t -> jourDe(t.getDateEncaissement()), LinkedHashMap::new,
                        Collectors.summingLong(t -> t.getMontantTotal().valeurXof())));
        long recetteTotale = tickets.stream().mapToLong(t -> t.getMontantTotal().valeurXof()).sum();

        return new RapportVentes(pointDeVente.getId(), pointDeVente.getLibelle(), pointDeVente.getType(), du, au,
                quantiteTotale, parMarque, parVolume, Map.of(), parServeur, parJour, recetteTotale);
    }

    private static <T> Map<String, Long> totauxLong(List<T> lignes, Function<T, String> cle,
                                                      java.util.function.ToLongFunction<T> valeur) {
        return lignes.stream().collect(Collectors.groupingBy(cle, LinkedHashMap::new,
                Collectors.summingLong(valeur::applyAsLong)));
    }

    private String jourDe(Instant instant) {
        return instant.atZone(fuseauHoraireMetier).toLocalDate().toString();
    }

    /**
     * §13 — stock valorisé. Le dépôt est valorisé au coût d'achat (tarif ACHAT
     * en vigueur, RG-08) ; le bar n'a aucun coût par bouteille fiable (seul le
     * prix de cession du casier transféré est connu, non historisé par
     * bouteille) et n'est donc pas valorisé, seulement listé en quantité.
     */
    @Transactional(readOnly = true)
    public RapportStockValorise rapportStockValorise(Long pointDeVenteId) {
        List<Stock> stocks = stockRepository.rechercher(pointDeVenteId, Pageable.unpaged()).getContent();
        List<LigneStockValorise> lignes = stocks.stream().map(this::valoriser).toList();
        long valeurTotale = lignes.stream()
                .filter(ligne -> ligne.valeurLigneXof() != null)
                .mapToLong(LigneStockValorise::valeurLigneXof)
                .sum();
        return new RapportStockValorise(lignes, valeurTotale);
    }

    private LigneStockValorise valoriser(Stock stock) {
        PointDeVente pointDeVente = stock.getPointDeVente();
        Long prixAchatCasierXof = null;
        Long valeurLigneXof = null;
        if (pointDeVente.getType() == TypePointDeVente.DEPOT) {
            var prixAchat = tarifRepository.tarifOuvert(pointDeVente.getId(), stock.getProduit().getId(),
                    UniteVente.CASIER, NatureTarif.ACHAT);
            if (prixAchat.isPresent()) {
                prixAchatCasierXof = prixAchat.get().getMontant().valeurXof();
                valeurLigneXof = prixAchatCasierXof * stock.getQuantite() / 2;
            }
        }
        return new LigneStockValorise(pointDeVente.getId(), pointDeVente.getLibelle(), stock.getProduit().getId(),
                stock.getProduit().getMarque().getLibelle(), stock.getProduit().getVolume().getLibelle(),
                stock.getQuantite(), prixAchatCasierXof, valeurLigneXof);
    }
}
