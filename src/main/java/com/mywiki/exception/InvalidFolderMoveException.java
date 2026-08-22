package com.mywiki.exception;

public class InvalidFolderMoveException extends RuntimeException {
    public InvalidFolderMoveException(String message) {
        super(message);
    }
}
