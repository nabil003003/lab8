package com.example;

import com.example.model.Reservation;
import com.example.service.ReservationService;
import javax.persistence.OptimisticLockException;
import java.time.LocalDateTime;

public class ReservationRetryHandler {
    private static final int NOMBRE_MAX_TENTATIVES = 3;
    private final ReservationService serviceReservations;

    public ReservationRetryHandler(ReservationService serviceReservations) {
        this.serviceReservations = serviceReservations;
    }

    public Reservation modifierAvecRetry(Reservation reservationDemandee) {
        String salleDemandee = reservationDemandee.getSalle();
        LocalDateTime debutDemande = reservationDemandee.getDateDebut();
        LocalDateTime finDemandee = reservationDemandee.getDateFin();
        Reservation reservationATenter = reservationDemandee;
        OptimisticLockException dernierConflit = null;

        for (int numeroTentative = 1; numeroTentative <= NOMBRE_MAX_TENTATIVES; numeroTentative++) {
            try {
                return serviceReservations.modifierReservation(reservationATenter);
            } catch (OptimisticLockException conflit) {
                dernierConflit = conflit;
                if (numeroTentative < NOMBRE_MAX_TENTATIVES) {
                    System.out.println("Conflit détecté, tentative de retry " + numeroTentative);
                    reservationATenter = serviceReservations.trouverParId(reservationDemandee.getId());
                    if (reservationATenter == null) {
                        throw new IllegalArgumentException("Réservation non trouvée");
                    }
                    // Réappliquer les valeurs demandées après le rechargement.
                    reservationATenter.setSalle(salleDemandee);
                    reservationATenter.setDateDebut(debutDemande);
                    reservationATenter.setDateFin(finDemandee);
                }
            }
        }
        throw new OptimisticLockException(
                "Échec après " + NOMBRE_MAX_TENTATIVES + " tentatives", dernierConflit);
    }
}
