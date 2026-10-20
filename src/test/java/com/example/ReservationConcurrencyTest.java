package com.example;

import com.example.model.Reservation;
import com.example.service.ReservationService;
import com.example.service.ReservationServiceImpl;
import junit.framework.TestCase;
import javax.persistence.OptimisticLockException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

public class ReservationConcurrencyTest extends TestCase {
    public void testConflitPuisRetryConserveLesModifications() {
        try (ReservationServiceImpl serviceReservations = new ReservationServiceImpl()) {
            LocalDateTime debutPrevu = LocalDateTime.of(2026, 11, 1, 10, 0);
            Reservation reservationInitiale = serviceReservations.creerReservation(
                    new Reservation("Salle A", debutPrevu, debutPrevu.plusHours(2)));
            Reservation premiereCopie = serviceReservations.trouverParId(reservationInitiale.getId());
            Reservation secondeCopie = serviceReservations.trouverParId(reservationInitiale.getId());
            int versionInitiale = reservationInitiale.getVersion();

            premiereCopie.setSalle("Salle B");
            serviceReservations.modifierReservation(premiereCopie);
            secondeCopie.setSalle("Salle C");
            secondeCopie.setDateDebut(debutPrevu.plusHours(1));
            secondeCopie.setDateFin(debutPrevu.plusHours(4));
            try {
                serviceReservations.modifierReservation(secondeCopie);
                fail("Une copie obsolète doit déclencher un conflit.");
            } catch (OptimisticLockException conflitAttendu) {
                Reservation apresConflit = serviceReservations.trouverParId(reservationInitiale.getId());
                assertEquals("Salle B", apresConflit.getSalle());
                assertEquals(versionInitiale + 1, apresConflit.getVersion());
            }

            new ReservationRetryHandler(serviceReservations).modifierAvecRetry(secondeCopie);
            Reservation apresRetry = serviceReservations.trouverParId(reservationInitiale.getId());
            assertEquals("Salle C", apresRetry.getSalle());
            assertEquals(debutPrevu.plusHours(1), apresRetry.getDateDebut());
            assertEquals(debutPrevu.plusHours(4), apresRetry.getDateFin());
            assertEquals(versionInitiale + 2, apresRetry.getVersion());
            assertTrue(serviceReservations.listerReservations().stream()
                    .anyMatch(element -> element.getId().equals(reservationInitiale.getId())));
        }
    }

    public void testRetryLimiteATroisTentatives() {
        final int[] nombreAppels = {0};
        ReservationService serviceEnConflit = new ReservationService() {
            public Reservation creerReservation(Reservation nouvelleReservation) {
                return nouvelleReservation;
            }
            public Reservation modifierReservation(Reservation reservationModifiee) {
                nombreAppels[0]++;
                throw new OptimisticLockException("Conflit simulé");
            }
            public Reservation trouverParId(Long identifiantReservation) {
                return new Reservation("Salle B", LocalDateTime.now(), LocalDateTime.now().plusHours(2));
            }
            public List<Reservation> listerReservations() {
                return Collections.emptyList();
            }
        };
        try {
            new ReservationRetryHandler(serviceEnConflit).modifierAvecRetry(
                    new Reservation("Salle C", LocalDateTime.now(), LocalDateTime.now().plusHours(2)));
            fail("Les conflits répétés doivent être signalés.");
        } catch (OptimisticLockException conflitAttendu) {
            assertEquals(3, nombreAppels[0]);
        }
    }
}
