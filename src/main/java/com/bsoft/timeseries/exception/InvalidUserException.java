package com.bsoft.timeseries.exception;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InvalidUserException extends RuntimeException {
    public InvalidUserException(final String message) {
        super(message);
    }
}