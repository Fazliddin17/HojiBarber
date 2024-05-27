package com.company.BroBarber.db.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Entity
@ToString
@Setter
@Getter
@Builder
@Table(name="barbers")
@AllArgsConstructor
@NoArgsConstructor
public class Barber {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id ;
    private Long chatId ;
    @Column(unique = true)
    private String fullName ;
    private String phone ;
    private String username ;
    private String image ;
    private LocalDate nowDay;
    private String price ;
    private Boolean success;
    private Long level;
    @OneToMany(mappedBy = "barber", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<Time> times;
}
