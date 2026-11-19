package com.outsera.razzies.dataload;

/** The movie CSV cannot be imported. Startup is aborted so that no partial / wrong data is served. */
public class InvalidMovieCsvException extends RuntimeException {

    public InvalidMovieCsvException(String message) {
        super(message);
    }

    public InvalidMovieCsvException(String message, Throwable cause) {
        super(message, cause);
    }
}
