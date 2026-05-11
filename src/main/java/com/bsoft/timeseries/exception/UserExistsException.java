package com.bsoft.timeseries.exception;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserExistsException extends RuntimeException {
    public UserExistsException(final String message) {
        super(message);
    }
}
