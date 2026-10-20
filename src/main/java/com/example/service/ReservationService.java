package com.example.service;

import com.example.model.Reservation;
import java.util.List;

public interface ReservationService {
    Reservation creerReservation(Reservation nouvelleReservation);
    Reservation modifierReservation(Reservation reservationModifiee);
    Reservation trouverParId(Long identifiantReservation);
    List<Reservation> listerReservations();
}
