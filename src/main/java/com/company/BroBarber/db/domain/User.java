package com.company.BroBarber.db.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Data
@Entity
@Table(name = ("users"))
@Getter
@Setter
@Builder
@NoArgsConstructor
@ToString
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id ;
    private Long chatId;
    private String username;
    private String nickname;
    private String phone;
    private String eventCode ;
    private String role ;
    private Long helper ;
    private LocalDate day ;
    private String lang ;
    private Long barberId ;
    private Boolean active ;
    private String time ;
    private String reklamaType ;
    private String reklamaText ;
    private String reklamaUrl ;
}

