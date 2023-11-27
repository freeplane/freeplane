/*
 * Created on 17 Nov 2023
 *
 * author dimitry
 */
package org.freeplane.plugin.codeexplorer.configurator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class CodeExplorerConfigurations {
    private static final String CONFIGURATION_DELIMITER = "\n";
    private List<CodeExplorerConfiguration> configurations;

    public CodeExplorerConfigurations(List<CodeExplorerConfiguration> configurations) {
        this.configurations = configurations;
    }

    public List<CodeExplorerConfiguration> getConfigurations() {
        return configurations;
    }

    public void setConfigurations(List<CodeExplorerConfiguration> configurations) {
        this.configurations = configurations;
    }

    String serialize() {
        return configurations.stream()
                .map(CodeExplorerConfiguration::serialize)
                .collect(Collectors.joining(CONFIGURATION_DELIMITER));
    }

    static CodeExplorerConfigurations deserialize(String serialized) {
        List<CodeExplorerConfiguration> configurations = serialized.isEmpty()
                ? new ArrayList<>()
                : Arrays.stream(serialized.split(CONFIGURATION_DELIMITER))
                .map(CodeExplorerConfiguration::deserialize)
                .collect(Collectors.toCollection(ArrayList::new));
        return new CodeExplorerConfigurations(configurations);
    }
}
