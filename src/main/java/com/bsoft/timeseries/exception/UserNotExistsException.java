package com.bsoft.timeseries.exception;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserNotExistsException extends RuntimeException {
    public UserNotExistsException(final String message) {
        super(message);
    }
}
