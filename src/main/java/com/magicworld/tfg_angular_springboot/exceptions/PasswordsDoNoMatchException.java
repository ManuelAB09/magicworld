package com.magicworld.tfg_angular_springboot.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class PasswordsDoNoMatchException extends ApiException {
    public PasswordsDoNoMatchException() {
        super("error.password.do.not.match");
    }
}
