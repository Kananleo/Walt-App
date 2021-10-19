package com.walt.Exceptions;
import com.walt.ErrorMessages;

public class NotSameCityException extends Exception{
    public NotSameCityException() {
        super(ErrorMessages.NOT_SAME_CITY);
    }
}
