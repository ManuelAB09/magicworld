package com.magicworld.tfg_angular_springboot.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidPasswordPattern extends ApiException {
    public InvalidPasswordPattern() {
        super("validation.password.pattern");;
    }
}