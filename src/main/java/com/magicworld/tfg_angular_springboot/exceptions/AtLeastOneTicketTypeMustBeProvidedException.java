package com.magicworld.tfg_angular_springboot.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class AtLeastOneTicketTypeMustBeProvidedException extends ApiException {
    public AtLeastOneTicketTypeMustBeProvidedException() {
        super("error.at.least.one.ticket.type.must.be.provided");
    }
}
