package com.company.BroBarber.service.impl;

import com.company.BroBarber.db.domain.User;
import com.company.BroBarber.dto.ResponseDto;
import com.company.BroBarber.db.repositories.UserRepository;
import com.company.BroBarber.service.UserService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Log4j2
public class UserServiceImpl implements UserService {
    @Autowired
    private UserRepository userRepository ;
    @Override
    public ResponseDto<User> findByChatId(Long chatId) {
        try {
            Optional<User>uOp = userRepository.findByChatId(chatId);
            if (uOp.isEmpty()){
                log.error("Ushbu user topilmadi");
                return new ResponseDto<>(false,"Nimagadir user topilmadi");

            }
            return new ResponseDto<>(true , "Ok" , uOp.get());
        } catch (Exception e) {
            log.error(e);
            return new ResponseDto<>(false , e.getMessage());
        }
    }

    @Override
    public ResponseDto<List<User>> findAll() {
        try {
            return new ResponseDto<>(true , "Ok" , userRepository.findAll());
        } catch (Exception e) {
            log.error(e);
            return new ResponseDto<>(false,e.getMessage());
        }
    }

    @Override
    public ResponseDto save(User user) {
        try {
            userRepository.save(user);
            return new ResponseDto(true , "Ok");
        } catch (Exception e) {
            log.error(e.getMessage());
            return new ResponseDto(false , e.getMessage());
        }
    }
}
