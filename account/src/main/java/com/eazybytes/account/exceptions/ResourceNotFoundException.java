package com.eazybytes.account.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends RuntimeException{

    public ResourceNotFoundException(String resource,String filedName,String fieldValue) {
        super(String.format("%s with field %s : %s not found",resource,filedName,fieldValue ));
    }
}
