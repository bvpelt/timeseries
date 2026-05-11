package com.bsoft.timeseries.exception;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserNotDeletedException extends RuntimeException {
    public UserNotDeletedException(final String message) {
        super(message);
    }
}