package com.orangeandbronze.coding_practices.leave.repositories;

public class DataAccessException extends RuntimeException {

    public DataAccessException(String message){
        super(message);
    }

    public DataAccessException(String message, Throwable cause){
        super(message, cause);
    }
}
