/*
 * Created on 8 Feb 2024
 *
 * author dimitry
 */
package org.freeplane.plugin.codeexplorer.task;

import java.util.Optional;

public interface LocationMatcher {
    String coreLocationPath(String path);

    default Optional<String> coreLocationPath(Optional<String> path ) {
        return path.map(this::coreLocationPath);
    }
}
