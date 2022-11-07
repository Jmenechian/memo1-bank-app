package com.aninfo.exceptions;

public class WithdrawNegativeSumException extends RuntimeException{
    public WithdrawNegativeSumException(String message){
        super(message);
    }
}
