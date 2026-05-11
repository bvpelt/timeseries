package com.bsoft.timeseries.entity;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PrivilegeNotDeletedException extends RuntimeException {
    public PrivilegeNotDeletedException(final String message) {
        super(message);
    }
}