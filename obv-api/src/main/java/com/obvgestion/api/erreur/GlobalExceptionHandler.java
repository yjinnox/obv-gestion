package com.obvgestion.api.erreur;

import com.obvgestion.domain.commun.RegleGestionException;
import com.obvgestion.domain.commun.SeparationDesTachesException;
import com.obvgestion.domain.bar.TicketServeurInvalideException;
import com.obvgestion.domain.reception.ReceptionInvalideException;
import com.obvgestion.domain.stock.StockInsuffisantException;
import com.obvgestion.domain.transfert.TransfertInvalideException;
import com.obvgestion.domain.utilisateur.AutoModificationInterditeException;
import com.obvgestion.domain.utilisateur.CompteVerrouilleException;
import com.obvgestion.domain.utilisateur.DernierSuperAdministrateurException;
import com.obvgestion.domain.utilisateur.EtatUtilisateurInvalideException;
import com.obvgestion.domain.utilisateur.IdentifiantsInvalidesException;
import com.obvgestion.infrastructure.securite.JetonInvalideException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.NoSuchElementException;
import java.util.stream.Collectors;

/**
 * §13 — erreurs au format RFC 7807 ({@link ProblemDetail}), avec un
 * {@code code} métier stable en extension.
 */
@RestControllerAdvice
class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(RegleGestionException.class)
    ProblemDetail gererRegleGestion(RegleGestionException ex) {
        HttpStatus statut = switch (ex) {
            case IdentifiantsInvalidesException ignored -> HttpStatus.UNAUTHORIZED;
            case JetonInvalideException ignored -> HttpStatus.UNAUTHORIZED;
            case CompteVerrouilleException ignored -> HttpStatus.LOCKED;
            case AutoModificationInterditeException ignored -> HttpStatus.FORBIDDEN;
            case EtatUtilisateurInvalideException ignored -> HttpStatus.CONFLICT;
            case DernierSuperAdministrateurException ignored -> HttpStatus.CONFLICT;
            case ReceptionInvalideException ignored -> HttpStatus.CONFLICT;
            case TransfertInvalideException ignored -> HttpStatus.CONFLICT;
            case TicketServeurInvalideException ignored -> HttpStatus.CONFLICT;
            case StockInsuffisantException ignored -> HttpStatus.CONFLICT;
            case SeparationDesTachesException ignored -> HttpStatus.FORBIDDEN;
            default -> HttpStatus.BAD_REQUEST;
        };
        return probleme(statut, ex.getMessage(), ex.code());
    }

    @ExceptionHandler(NoSuchElementException.class)
    ProblemDetail gererIntrouvable(NoSuchElementException ex) {
        return probleme(HttpStatus.NOT_FOUND, ex.getMessage(), "RESSOURCE_INTROUVABLE");
    }

    @ExceptionHandler(AuthorizationDeniedException.class)
    ProblemDetail gererAccesRefuse(AuthorizationDeniedException ex) {
        return probleme(HttpStatus.FORBIDDEN, "Vous n'avez pas les droits nécessaires pour cette action.",
                "ACCES_REFUSE");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ProblemDetail gererValidation(MethodArgumentNotValidException ex) {
        String detail = ex.getBindingResult().getFieldErrors().stream()
                .map(err -> err.getField() + " : " + err.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return probleme(HttpStatus.BAD_REQUEST, detail.isBlank() ? "Requête invalide." : detail, "VALIDATION_ECHOUEE");
    }

    @ExceptionHandler(Exception.class)
    ProblemDetail gererErreurInattendue(Exception ex) {
        log.error("Erreur inattendue non gérée", ex);
        return probleme(HttpStatus.INTERNAL_SERVER_ERROR, "Une erreur inattendue est survenue.", "ERREUR_INTERNE");
    }

    private static ProblemDetail probleme(HttpStatus statut, String message, String code) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(statut, message);
        problemDetail.setProperty("code", code);
        return problemDetail;
    }
}
