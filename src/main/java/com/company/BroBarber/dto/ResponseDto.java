package com.company.BroBarber.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ResponseDto<T> {
    private boolean success ;
    private String message ;
    private T data ;

    public ResponseDto(Boolean success, String message) {
        this.success = success;
        this.message = message;
    }
}
