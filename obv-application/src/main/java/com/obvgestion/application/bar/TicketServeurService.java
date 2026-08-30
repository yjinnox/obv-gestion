package com.obvgestion.application.bar;

import com.obvgestion.application.audit.Journalisateur;
import com.obvgestion.application.referentiel.ProduitRepository;
import com.obvgestion.application.referentiel.ServeurRepository;
import com.obvgestion.application.referentiel.TarifRepository;
import com.obvgestion.application.stock.StockRepository;
import com.obvgestion.application.stock.StockService;
import com.obvgestion.application.utilisateur.UtilisateurRepository;
import com.obvgestion.application.vente.SessionVenteRepository;
import com.obvgestion.domain.audit.TypeActionJournal;
import com.obvgestion.domain.bar.LigneTicketServeur;
import com.obvgestion.domain.bar.StatutTicketServeur;
import com.obvgestion.domain.bar.TicketServeur;
import com.obvgestion.domain.bar.TicketServeurInvalideException;
import com.obvgestion.domain.commun.Montant;
import com.obvgestion.domain.commun.SeparationDesTachesException;
import com.obvgestion.domain.referentiel.NatureTarif;
import com.obvgestion.domain.referentiel.Produit;
import com.obvgestion.domain.referentiel.Serveur;
import com.obvgestion.domain.referentiel.UniteVente;
import com.obvgestion.domain.stock.StockInsuffisantException;
import com.obvgestion.domain.stock.TypeMouvementStock;
import com.obvgestion.domain.utilisateur.Permission;
import com.obvgestion.domain.utilisateur.Utilisateur;
import com.obvgestion.domain.vente.ModePaiement;
import com.obvgestion.domain.vente.SessionVente;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.Function;
import java.util.stream.Collectors;

/** §10 — cycle de vie du ticket serveur au bar (RG-01, RG-33, RG-34 par réutilisation de {@link SessionVente}). */
@Service
public class TicketServeurService {

    private static final String TYPE_DOCUMENT = "TICKET_SERVEUR";

    private final TicketServeurRepository ticketServeurRepository;
    private final SessionVenteRepository sessionVenteRepository;
    private final ServeurRepository serveurRepository;
    private final ProduitRepository produitRepository;
    private final TarifRepository tarifRepository;
    private final StockRepository stockRepository;
    private final StockService stockService;
    private final UtilisateurRepository utilisateurRepository;
    private final Journalisateur journalisateur;

    public TicketServeurService(TicketServeurRepository ticketServeurRepository,
                                 SessionVenteRepository sessionVenteRepository, ServeurRepository serveurRepository,
                                 ProduitRepository produitRepository, TarifRepository tarifRepository,
                                 StockRepository stockRepository, StockService stockService,
                                 UtilisateurRepository utilisateurRepository, Journalisateur journalisateur) {
        this.ticketServeurRepository = ticketServeurRepository;
        this.sessionVenteRepository = sessionVenteRepository;
        this.serveurRepository = serveurRepository;
        this.produitRepository = produitRepository;
        this.tarifRepository = tarifRepository;
        this.stockRepository = stockRepository;
        this.stockService = stockService;
        this.utilisateurRepository = utilisateurRepository;
        this.journalisateur = journalisateur;
    }

    /** §10 — le gérant ouvre une ligne de vente par serveur. RG-23 : rejeté hors session ouverte. */
    @Transactional
    public TicketServeur creer(Long sessionVenteId, Long serveurId) {
        SessionVente session = sessionVenteRepository.parId(sessionVenteId)
                .orElseThrow(() -> new NoSuchElementException("Session de vente introuvable : " + sessionVenteId));
        Serveur serveur = serveurRepository.parId(serveurId)
                .orElseThrow(() -> new NoSuchElementException("Serveur introuvable : " + serveurId));
        return ticketServeurRepository.enregistrer(new TicketServeur(session, serveur));
    }

    /** {@code prixVenteBouteilleXof} nullable : pré-rempli depuis le tarif VENTE en vigueur si omis. */
    @Transactional
    public LigneTicketServeur ajouterLigne(Long ticketId, Long produitId, long quantiteBouteilles,
                                            Long prixVenteBouteilleXof) {
        TicketServeur ticket = trouver(ticketId);
        Produit produit = produitRepository.parId(produitId)
                .orElseThrow(() -> new NoSuchElementException("Produit introuvable : " + produitId));
        Montant prix = prixVenteBouteilleXof != null
                ? new Montant(prixVenteBouteilleXof)
                : prixVenteEnVigueur(ticket.getSessionVente().getPointDeVente().getId(), produitId);

        LigneTicketServeur ligne = ticket.ajouterLigne(produit, quantiteBouteilles, prix);
        ticketServeurRepository.enregistrer(ticket);
        return ligne;
    }

    private Montant prixVenteEnVigueur(Long pointDeVenteId, Long produitId) {
        return tarifRepository.tarifOuvert(pointDeVenteId, produitId, UniteVente.BOUTEILLE, NatureTarif.VENTE)
                .map(tarif -> tarif.getMontant())
                .orElseThrow(() -> new TicketServeurInvalideException(
                        "Aucun tarif de vente en vigueur pour ce produit : veuillez saisir un prix."));
    }

    /** §10 — le gérant encaisse et remet les bouteilles. RG-24 (par analogie) : décrément bloquant du stock au comptoir. */
    @Transactional
    public TicketServeur encaisser(Long ticketId, ModePaiement modePaiement, Long acteurId) {
        TicketServeur ticket = trouver(ticketId);
        Utilisateur acteur = utilisateur(acteurId);
        verifierDisponibilite(ticket);

        ticket.encaisser(modePaiement, acteurId.toString(), Instant.now());
        ticket = ticketServeurRepository.enregistrer(ticket);

        for (LigneTicketServeur ligne : ticket.getLignes()) {
            stockService.appliquer(ticket.getSessionVente().getPointDeVente(), ligne.getProduit(),
                    TypeMouvementStock.SORTIE_VENTE, -ligne.getQuantiteBouteilles(), TYPE_DOCUMENT, ticket.getId(),
                    acteur);
        }
        return ticket;
    }

    /** RG-24 (par analogie) — contrôle bloquant anticipé : évite de décrémenter partiellement un ticket multi-lignes. */
    private void verifierDisponibilite(TicketServeur ticket) {
        Long pointDeVenteId = ticket.getSessionVente().getPointDeVente().getId();
        for (LigneTicketServeur ligne : ticket.getLignes()) {
            long disponible = stockRepository.parPointDeVenteEtProduit(pointDeVenteId, ligne.getProduit().getId())
                    .map(stock -> stock.getQuantite())
                    .orElse(0L);
            if (disponible < ligne.getQuantiteBouteilles()) {
                throw new StockInsuffisantException(
                        ligne.getProduit().getMarque().getLibelle() + " " + ligne.getProduit().getVolume().getLibelle(),
                        ligne.getQuantiteBouteilles(), disponible);
            }
        }
    }

    /**
     * RG-29 (par analogie) — correction d'une quantité vendue par le
     * SUPER_ADMINISTRATEUR pendant la modification de session : ajuste le
     * stock de l'écart et journalise.
     */
    @Transactional
    public void modifierQuantiteLigne(Long ticketId, Long ligneId, long quantiteBouteilles, Long acteurId,
                                       String adresseIp) {
        TicketServeur ticket = trouver(ticketId);
        Utilisateur acteur = utilisateur(acteurId);
        if (!acteur.permissions().contains(Permission.MODIFICATION_POST_CLOTURE)) {
            throw new SeparationDesTachesException(
                    "Seul un SUPER_ADMINISTRATEUR peut modifier un ticket pendant la modification de session.");
        }
        LigneTicketServeur ligne = ticket.getLignes().stream().filter(l -> l.getId().equals(ligneId)).findFirst()
                .orElseThrow(() -> new NoSuchElementException("Ligne introuvable : " + ligneId));
        long avant = ligne.getQuantiteBouteilles();
        String descriptionAvant = "quantiteBouteilles=" + avant;

        ticket.modifierQuantiteLigne(ligneId, quantiteBouteilles);
        ticketServeurRepository.enregistrer(ticket);

        long delta = quantiteBouteilles - avant;
        if (delta != 0) {
            stockService.appliquer(ticket.getSessionVente().getPointDeVente(), ligne.getProduit(),
                    TypeMouvementStock.AJUSTEMENT, -delta, TYPE_DOCUMENT, ticket.getId(), acteur);
        }
        journalisateur.journaliser(acteurId.toString(), TypeActionJournal.MODIFICATION_POST_CLOTURE,
                "LigneTicketServeur", ligneId.toString(), descriptionAvant, "quantiteBouteilles=" + quantiteBouteilles,
                adresseIp);
    }

    /** RG-33 — total par serveur, par marque/volume, total général. */
    @Transactional(readOnly = true)
    public RecapitulatifSessionBar recapitulatif(Long sessionVenteId) {
        SessionVente session = sessionVenteRepository.parId(sessionVenteId)
                .orElseThrow(() -> new NoSuchElementException("Session de vente introuvable : " + sessionVenteId));
        List<TicketServeur> tickets = ticketServeurRepository.parSession(sessionVenteId).stream()
                .filter(t -> t.getStatut() == StatutTicketServeur.ENCAISSE)
                .toList();
        List<LigneTicketServeur> lignes = tickets.stream().flatMap(t -> t.getLignes().stream()).toList();

        Map<String, Long> parServeur = tickets.stream()
                .flatMap(t -> t.getLignes().stream()
                        .map(l -> Map.entry(t.getServeur().getNom() + " " + t.getServeur().getPrenoms(),
                                l.getQuantiteBouteilles())))
                .collect(Collectors.groupingBy(Map.Entry::getKey, LinkedHashMap::new,
                        Collectors.summingLong(Map.Entry::getValue)));
        Map<String, Long> parMarque = totauxParBouteilles(lignes, l -> l.getProduit().getMarque().getLibelle());
        Map<String, Long> parVolume = totauxParBouteilles(lignes, l -> l.getProduit().getVolume().getLibelle());
        long quantiteTotale = lignes.stream().mapToLong(LigneTicketServeur::getQuantiteBouteilles).sum();
        long recetteTotale = tickets.stream().mapToLong(t -> t.getMontantTotal().valeurXof()).sum();

        return new RecapitulatifSessionBar(session, parServeur, parMarque, parVolume, quantiteTotale, recetteTotale);
    }

    private static Map<String, Long> totauxParBouteilles(List<LigneTicketServeur> lignes,
                                                           Function<LigneTicketServeur, String> cle) {
        return lignes.stream().collect(Collectors.groupingBy(cle, LinkedHashMap::new,
                Collectors.summingLong(LigneTicketServeur::getQuantiteBouteilles)));
    }

    @Transactional(readOnly = true)
    public TicketServeur trouver(Long id) {
        return ticketServeurRepository.parId(id)
                .orElseThrow(() -> new NoSuchElementException("Ticket serveur introuvable : " + id));
    }

    @Transactional(readOnly = true)
    public Page<TicketServeur> rechercher(Long sessionVenteId, Long serveurId, StatutTicketServeur statut,
                                           Pageable pageable) {
        return ticketServeurRepository.rechercher(sessionVenteId, serveurId, statut, pageable);
    }

    private Utilisateur utilisateur(Long id) {
        return utilisateurRepository.parId(id)
                .orElseThrow(() -> new NoSuchElementException("Utilisateur introuvable : " + id));
    }
}
