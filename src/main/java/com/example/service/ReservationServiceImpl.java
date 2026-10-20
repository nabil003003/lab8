package com.example.service;

import com.example.model.Reservation;
import javax.persistence.*;
import java.util.List;

public class ReservationServiceImpl implements ReservationService, AutoCloseable {
    private final EntityManagerFactory fabriqueEntites =
            Persistence.createEntityManagerFactory("reservationPU");

    @Override
    public Reservation creerReservation(Reservation nouvelleReservation) {
        EntityManager gestionnaireEntites = fabriqueEntites.createEntityManager();
        try {
            gestionnaireEntites.getTransaction().begin();
            gestionnaireEntites.persist(nouvelleReservation);
            gestionnaireEntites.getTransaction().commit();
            return nouvelleReservation;
        } catch (RuntimeException erreur) {
            annulerTransaction(gestionnaireEntites);
            throw erreur;
        } finally {
            gestionnaireEntites.close();
        }
    }

    @Override
    public Reservation modifierReservation(Reservation reservationModifiee) {
        EntityManager gestionnaireEntites = fabriqueEntites.createEntityManager();
        try {
            gestionnaireEntites.getTransaction().begin();
            if (reservationModifiee.getId() == null
                    || gestionnaireEntites.find(Reservation.class, reservationModifiee.getId()) == null) {
                throw new IllegalArgumentException("Réservation non trouvée");
            }
            // merge conserve la version de la copie détachée pour détecter le conflit.
            Reservation reservationGeree = gestionnaireEntites.merge(reservationModifiee);
            gestionnaireEntites.getTransaction().commit();
            return reservationGeree;
        } catch (RuntimeException erreur) {
            annulerTransaction(gestionnaireEntites);
            // Au commit, JPA peut envelopper le conflit dans une RollbackException.
            Throwable causeCourante = erreur;
            while (causeCourante != null) {
                if (causeCourante instanceof OptimisticLockException) {
                    throw (OptimisticLockException) causeCourante;
                }
                causeCourante = causeCourante.getCause();
            }
            throw erreur;
        } finally {
            gestionnaireEntites.close();
        }
    }

    @Override
    public Reservation trouverParId(Long identifiantReservation) {
        EntityManager gestionnaireEntites = fabriqueEntites.createEntityManager();
        try {
            return gestionnaireEntites.find(Reservation.class, identifiantReservation);
        } finally {
            gestionnaireEntites.close();
        }
    }

    @Override
    public List<Reservation> listerReservations() {
        EntityManager gestionnaireEntites = fabriqueEntites.createEntityManager();
        try {
            return gestionnaireEntites.createQuery(
                    "SELECT element FROM Reservation element", Reservation.class).getResultList();
        } finally {
            gestionnaireEntites.close();
        }
    }

    private void annulerTransaction(EntityManager gestionnaireEntites) {
        if (gestionnaireEntites.getTransaction().isActive()) {
            gestionnaireEntites.getTransaction().rollback();
        }
    }

    @Override
    public void close() {
        fabriqueEntites.close();
    }
}
