package org.freeplane.features.encrypt;

/** not a clean strategy since it is allowed to be stateful. */
public interface PasswordStrategy {

    StringBuilder getPassword();
    
    StringBuilder getPasswordWithConfirmation();

    void onWrongPassword();

    boolean isCancelled();
}
