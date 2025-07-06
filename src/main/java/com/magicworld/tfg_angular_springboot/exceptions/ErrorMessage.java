package com.magicworld.tfg_angular_springboot.exceptions;

import lombok.Getter;

import java.util.Date;

@Getter

public class ErrorMessage {
    private final int status;
    private final Date timestamp;
    private final String code;      // antes "message"
    private final Object[] args;    // parámetros opcionales
    private final String path;

    public ErrorMessage(int status, Date timestamp, String code, Object[] args, String path) {
        this.status    = status;
        this.timestamp = timestamp;
        this.code      = code;
        this.args      = args;
        this.path      = path;
    }

}