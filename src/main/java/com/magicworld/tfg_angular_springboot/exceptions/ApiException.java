package com.magicworld.tfg_angular_springboot.exceptions;

import lombok.Getter;

@Getter
public abstract class ApiException extends RuntimeException {
    private final String code;
    private final Object[] args;

    public ApiException(String code, Object... args) {
        super(code);
        this.code = code;
        this.args = args;
    }
}
