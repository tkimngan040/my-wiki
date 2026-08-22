package com.mywiki.exception;
public class PageAccessDeniedException extends RuntimeException {
    public PageAccessDeniedException(String message) { super(message); }
}
