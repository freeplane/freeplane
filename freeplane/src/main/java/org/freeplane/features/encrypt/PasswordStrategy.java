package org.freeplane.features.encrypt;

import org.freeplane.features.map.NodeModel;

/** not a clean strategy since it is allowed to be stateful. */
public interface PasswordStrategy {

    StringBuilder getPassword(NodeModel node);
    
    StringBuilder getPasswordWithConfirmation(NodeModel node);

    void onWrongPassword();

    boolean isCancelled();
}
