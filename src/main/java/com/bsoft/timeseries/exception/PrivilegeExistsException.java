package com.bsoft.timeseries.exception;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PrivilegeExistsException extends RuntimeException {
    public PrivilegeExistsException(final String message) {
        super(message);
    }
}