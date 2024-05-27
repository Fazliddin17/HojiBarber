package com.company.BroBarber.service;

import com.company.BroBarber.db.domain.User;
import com.company.BroBarber.dto.ResponseDto;

import java.util.List;

public interface UserService {
    ResponseDto<User>findByChatId(Long chatId) ;
    ResponseDto<List<User>>findAll() ;
    ResponseDto save (User user) ;

}
