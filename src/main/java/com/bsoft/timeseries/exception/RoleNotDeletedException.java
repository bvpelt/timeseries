package com.bsoft.timeseries.exception;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RoleNotDeletedException extends RuntimeException {
    public RoleNotDeletedException(final String message) {
        super(message);
    }
}