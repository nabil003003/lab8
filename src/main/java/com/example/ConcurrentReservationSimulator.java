package com.example;

import com.example.model.Reservation;
import com.example.service.ReservationServiceImpl;
import javax.persistence.OptimisticLockException;
import java.time.LocalDateTime;

public class ConcurrentReservationSimulator {
    public static void main(String[] arguments) {
        try (ReservationServiceImpl serviceReservations = new ReservationServiceImpl()) {
            LocalDateTime debutPrevu = LocalDateTime.now().plusDays(1);
            Reservation reservationInitiale = serviceReservations.creerReservation(
                    new Reservation("Salle A", debutPrevu, debutPrevu.plusHours(2)));

            Reservation copiePremierUtilisateur = serviceReservations.trouverParId(reservationInitiale.getId());
            Reservation copieSecondUtilisateur = serviceReservations.trouverParId(reservationInitiale.getId());

            copiePremierUtilisateur.setSalle("Salle B");
            serviceReservations.modifierReservation(copiePremierUtilisateur);
            System.out.println("Transaction 1 : modification réussie.");

            try {
                copieSecondUtilisateur.setSalle("Salle C");
                serviceReservations.modifierReservation(copieSecondUtilisateur);
                System.out.println("Transaction 2 : modification réussie.");
            } catch (OptimisticLockException conflit) {
                System.err.println("Transaction 2 : conflit détecté - " + conflit.getMessage());
            }
        }
    }
}
