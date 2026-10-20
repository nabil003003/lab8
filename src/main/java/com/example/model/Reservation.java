package com.example.model;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "reservations")
public class Reservation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String salle;

    @Column(nullable = false)
    private LocalDateTime dateDebut;

    @Column(nullable = false)
    private LocalDateTime dateFin;

    @Version
    private int version;

    public Reservation() {
    }

    public Reservation(String nomSalle, LocalDateTime debutPrevu, LocalDateTime finPrevue) {
        this.salle = nomSalle;
        this.dateDebut = debutPrevu;
        this.dateFin = finPrevue;
    }

    public Long getId() { return id; }
    public String getSalle() { return salle; }
    public void setSalle(String nomSalle) { this.salle = nomSalle; }
    public LocalDateTime getDateDebut() { return dateDebut; }
    public void setDateDebut(LocalDateTime debutPrevu) { this.dateDebut = debutPrevu; }
    public LocalDateTime getDateFin() { return dateFin; }
    public void setDateFin(LocalDateTime finPrevue) { this.dateFin = finPrevue; }
    public int getVersion() { return version; }
}
