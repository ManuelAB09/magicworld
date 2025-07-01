package com.magicworld.tfg_angular_springboot.exceptions;

public class ApiException extends RuntimeException {
  public ApiException(String message) {
    super(message);
  }
}
