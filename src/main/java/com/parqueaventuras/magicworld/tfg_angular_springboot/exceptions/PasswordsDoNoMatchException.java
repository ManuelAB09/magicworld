package com.parqueaventuras.magicworld.tfg_angular_springboot.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class PasswordsDoNoMatchException extends RuntimeException {
    public PasswordsDoNoMatchException(String message) {
        super(message);
    }
}
