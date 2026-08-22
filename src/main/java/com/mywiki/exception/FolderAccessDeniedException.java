package com.mywiki.exception;

public class FolderAccessDeniedException extends RuntimeException {
    public FolderAccessDeniedException(String message) {
        super(message);
    }
}
