package com.bazaarready.common;

/** A visitor asked for something BazaarReady does not do, such as writing code or homework. */
public class OffTopicException extends RuntimeException {

    public OffTopicException(String message) {
        super(message);
    }
}
