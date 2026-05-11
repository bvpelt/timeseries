package com.bsoft.timeseries.exception;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RoleExistsException extends RuntimeException {
    public RoleExistsException(final String message) {
        super(message);
    }
}