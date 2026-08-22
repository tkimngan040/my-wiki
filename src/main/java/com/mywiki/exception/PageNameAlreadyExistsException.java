package com.mywiki.exception;
public class PageNameAlreadyExistsException extends RuntimeException {
    public PageNameAlreadyExistsException(String message) { super(message); }
}
