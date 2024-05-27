package com.company.BroBarber.service.impl;

import com.company.BroBarber.dto.ResponseDto;
import com.company.BroBarber.db.domain.Barber;
import com.company.BroBarber.db.repositories.BarberRepository;
import com.company.BroBarber.service.BarberService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Log4j2
public class BarberServiceImpl implements BarberService {
@Autowired
private BarberRepository barberRepository ;
    @Override
    public ResponseDto<Barber> getdraftBarber() {
        try {
            return new ResponseDto<>(true , "Ok" , barberRepository.findByLevel());
        } catch (Exception e) {
            log.error(e);
            return new ResponseDto<>(false , e.getMessage());
        }
    }

    @Override
    public ResponseDto deleteById(Long id) {
        try {
            barberRepository.deleteById(id);
            log.info("success delete barber");
            return new ResponseDto(true ,"Ok");
        } catch (Exception e) {
            log.error(e.getMessage());
            return new ResponseDto(false  , e.getMessage());
        }
    }

    @Override
    public ResponseDto<Barber> findByName(String name) {
        try {
            Barber barber = barberRepository.findByFullName(name);
            if (barber == null) {
                return new ResponseDto<>(false , "Nimagadir barber topilmadi",null);
            }
            return new ResponseDto<>(true , "Ok",barber);
        } catch (Exception e) {
            log.error(e);
            return new ResponseDto<>(false , e.getMessage());
        }
    }


    @Override
    public ResponseDto<List<Barber>> findAll() {
        try {
            return new ResponseDto<>(true , "Ok" , barberRepository.findAll());
        } catch (Exception e) {
            log.error(e);
            return new ResponseDto<>(false , e.getMessage());
        }
    }

    @Override
    public ResponseDto<Barber> findById(Long barberId) {
        try {
            Optional<Barber>bOp = barberRepository.findById(barberId);
            if (bOp.isEmpty()){
                log.error("topilmadi");
                return new ResponseDto<>(false , "topilmadi") ;
            }
            return new ResponseDto<>(true , "Ok" , bOp.get());
        } catch (Exception e) {
            log.error(e);
            return new ResponseDto<>(false,e.getMessage());
        }
    }

    @Override
    public ResponseDto<Barber> findByChatId(Long chatId) {
        try {
            List<Barber>list = barberRepository.findAll();
            for (Barber barber : list) {
                if (barber.getSuccess() && chatId.equals(barber.getChatId()))
                    return new ResponseDto<>(true , "Ok" , barber) ;
            }
            return new ResponseDto<>(false , "Barber topilmadi" );
        } catch (Exception e) {
            return new ResponseDto<>(false , e.getMessage());
        }
    }


    @Override
    public ResponseDto<Barber> save(Barber barber) {
        try {
            barberRepository.save(barber);
            return new ResponseDto<>(true , "Ok");
        } catch (Exception e) {
            return new ResponseDto<>(false , e.getMessage());
        }
    }
}
