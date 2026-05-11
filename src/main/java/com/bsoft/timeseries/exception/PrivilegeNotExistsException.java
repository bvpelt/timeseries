package com.bsoft.timeseries.exception;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PrivilegeNotExistsException extends RuntimeException {
    public PrivilegeNotExistsException(final String message) {
        super(message);
    }
}