package com.management.user.exception.custom;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.BAD_REQUEST)
public class ExistedEmailException extends RuntimeException {

    public ExistedEmailException(String message) {
        super(message);
    }
}
