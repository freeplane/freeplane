/*
 * Created on 17 Nov 2023
 *
 * author dimitry
 */
package org.freeplane.plugin.codeexplorer.configurator;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.freeplane.core.util.Compat;
import org.freeplane.core.util.LogUtils;
import org.freeplane.plugin.codeexplorer.task.CodeExplorerConfiguration;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

public class CodeExplorerConfigurations {


    private static final String CODE_EXPLORER_JSON_FILE = "codeExplorer.json";
    private static final File DEFAULT_CONFIGURATION_FILE = new File(Compat.getApplicationUserDirectory(), CODE_EXPLORER_JSON_FILE);
    private List<CodeExplorerConfiguration> configurations;
    private final static Gson OBJECT_MAPPER = new GsonBuilder()
            .registerTypeAdapter(File.class, new FileTypeAdapter())
            .setPrettyPrinting()
            .create();
    private final static Type CONFIGURATIONS_TYPE = new TypeToken<List<CodeExplorerConfiguration>>() {/**/}.getType();


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
        try (FileWriter writer = new FileWriter(file)){
            OBJECT_MAPPER.toJson(configurations, writer);
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
        configurations = !configurationFile.exists()
                ? new ArrayList<>()
                        : fromJsonFile(configurationFile);
        return new CodeExplorerConfigurations(configurations);
    }

    private static List<CodeExplorerConfiguration> fromJsonFile(File configurationFile){
        try (FileReader reader = new FileReader(configurationFile)) {
            return OBJECT_MAPPER.fromJson(reader, CONFIGURATIONS_TYPE);
        } catch (IOException e) {
            LogUtils.severe(e);
            return Collections.emptyList();
        }
    }
}
