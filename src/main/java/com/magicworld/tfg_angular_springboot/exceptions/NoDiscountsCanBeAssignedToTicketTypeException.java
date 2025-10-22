package com.magicworld.tfg_angular_springboot.exceptions;

public class NoDiscountsCanBeAssignedToTicketTypeException extends RuntimeException {
  public NoDiscountsCanBeAssignedToTicketTypeException(String message) {
    super(message);
  }
}
