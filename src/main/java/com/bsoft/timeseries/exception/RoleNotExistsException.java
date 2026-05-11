package com.bsoft.timeseries.exception;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RoleNotExistsException extends RuntimeException {
    public RoleNotExistsException(final String message) {
        super(message);
    }
}