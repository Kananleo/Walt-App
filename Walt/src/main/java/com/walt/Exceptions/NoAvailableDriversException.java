package com.walt.Exceptions;
import com.walt.ErrorMessages;

public class NoAvailableDriversException extends Exception {
    public NoAvailableDriversException() {
        super(ErrorMessages.NO_AVAILABLE_DRIVERS);
    }
}
