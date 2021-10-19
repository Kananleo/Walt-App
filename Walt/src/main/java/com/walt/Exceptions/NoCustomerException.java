package com.walt.Exceptions;
import com.walt.ErrorMessages;

public class NoCustomerException extends Exception {
    public NoCustomerException() {
        super(ErrorMessages.NO_CUSTOMER);
    }
}
