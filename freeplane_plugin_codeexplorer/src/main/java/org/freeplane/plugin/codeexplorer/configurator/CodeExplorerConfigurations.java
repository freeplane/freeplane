/*
 * Created on 17 Nov 2023
 *
 * author dimitry
 */
package org.freeplane.plugin.codeexplorer.configurator;

import java.util.ArrayList;
import java.util.List;

import org.freeplane.core.util.LogUtils;
import org.freeplane.plugin.codeexplorer.task.CodeExplorerConfiguration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class CodeExplorerConfigurations {
    private List<CodeExplorerConfiguration> configurations;
    private final static ObjectMapper OBJECT_MAPPER = new ObjectMapper();


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
        try {
            return OBJECT_MAPPER.writeValueAsString(configurations);
        } catch (JsonProcessingException e) {
            LogUtils.severe(e);
            return "";
        }
    }

    static CodeExplorerConfigurations deserialize(String serialized) {
        List<CodeExplorerConfiguration> configurations;
        try {
           configurations = serialized.trim().isEmpty()
                    ? new ArrayList<>()
                    : OBJECT_MAPPER.readValue(serialized, new TypeReference<List<CodeExplorerConfiguration>>() {
                    });
        } catch (JsonProcessingException e) {
            LogUtils.severe(e);
            configurations = new ArrayList<>();
        }
        return new CodeExplorerConfigurations(configurations);
    }
}
