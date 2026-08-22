package com.mywiki.exception;

public class FolderNameAlreadyExistsException extends RuntimeException {
    public FolderNameAlreadyExistsException(String message) {
        super(message);
    }
}
