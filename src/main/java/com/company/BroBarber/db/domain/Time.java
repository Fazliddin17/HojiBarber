package com.company.BroBarber.db.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "times")
public class Time {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id ;
    @Column(name = "select_time")
    private String time ;
    @ManyToOne
    @JoinColumn(name = "barber_id")
    private Barber barber;
    public Time(String time) {
        this.time = time;
    }
}
