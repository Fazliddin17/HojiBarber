package com.company.BroBarber.service;

import com.company.BroBarber.db.domain.Barber;
import com.company.BroBarber.dto.ResponseDto;

import java.util.List;

public interface BarberService {
    ResponseDto<List<Barber>>findAll() ;
    ResponseDto<Barber>findById(Long barberId) ;
    ResponseDto<Barber>findByChatId(Long chatId) ;
    ResponseDto<Barber>save(Barber barber) ;
    ResponseDto deleteById(Long id) ;
    ResponseDto<Barber>findByName(String name) ;
    ResponseDto<Barber> getdraftBarber() ;
}
