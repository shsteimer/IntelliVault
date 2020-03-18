package com.razorfish.platforms.intellivault.exceptions;

/**
 * An General Exception class for Errors that occur in IntelliVault.
 *
 * @author Sean Steimer
 */
public class IntelliVaultException extends Exception {

    public IntelliVaultException() {
        super();
    }

    public IntelliVaultException(String message) {
        super(message);
    }

    public IntelliVaultException(String message, Throwable cause) {
        super(message, cause);
    }

    public IntelliVaultException(Throwable cause) {
        super(cause);
    }
}
