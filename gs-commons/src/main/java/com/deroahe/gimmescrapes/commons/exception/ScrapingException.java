package com.deroahe.gimmescrapes.commons.exception;

/**
 * Exception thrown when scraping operations fail.
 */
public class ScrapingException extends Exception {

    public ScrapingException(String message) {
        super(message);
    }

    public ScrapingException(String message, Throwable cause) {
        super(message, cause);
    }

    public ScrapingException(Throwable cause) {
        super(cause);
    }
}
