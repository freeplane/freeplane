/*
 * Created on 17 Nov 2023
 *
 * author dimitry
 */
package org.freeplane.plugin.codeexplorer.configurator;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.freeplane.core.util.Compat;
import org.freeplane.core.util.LogUtils;
import org.freeplane.plugin.codeexplorer.task.CodeExplorerConfiguration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class CodeExplorerConfigurations {
    private static final File DEFAULT_CONFIGURATION_FILE = new File(Compat.getApplicationUserDirectory(), "codeExplorer.json");
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

    void saveConfiguration() {
        saveConfiguration(DEFAULT_CONFIGURATION_FILE);
    }

    void saveConfiguration(File file) {
        try {
            OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValue(file, configurations);
        } catch (IOException e) {
            LogUtils.severe(e);
        }
    }

    static CodeExplorerConfigurations loadConfigurations() {
        File configurationFile = DEFAULT_CONFIGURATION_FILE;
        return loadConfigurations(configurationFile);
    }

    static CodeExplorerConfigurations loadConfigurations(File configurationFile) {
        List<CodeExplorerConfiguration> configurations;
        try {
           configurations = !configurationFile.exists()
                    ? new ArrayList<>()
                    : OBJECT_MAPPER.readValue(configurationFile, new TypeReference<List<CodeExplorerConfiguration>>() {/**/});
        } catch (IOException e) {
            LogUtils.severe(e);
            configurations = new ArrayList<>();
        }
        return new CodeExplorerConfigurations(configurations);
    }
}
