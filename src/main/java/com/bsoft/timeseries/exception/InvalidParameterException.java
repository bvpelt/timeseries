package com.bsoft.timeseries.exception;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InvalidParameterException extends RuntimeException {
    public InvalidParameterException(final String message) {
        super(message);
    }
}