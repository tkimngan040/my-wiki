package com.mywiki.exception;

public class WorkspaceNameAlreadyExistsException extends RuntimeException {
    public WorkspaceNameAlreadyExistsException(String message) {
        super(message);
    }
}
