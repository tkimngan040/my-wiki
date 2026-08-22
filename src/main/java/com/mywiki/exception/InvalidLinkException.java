package com.mywiki.exception;
public class InvalidLinkException extends RuntimeException {
    public InvalidLinkException(String message) { super(message); }
}
