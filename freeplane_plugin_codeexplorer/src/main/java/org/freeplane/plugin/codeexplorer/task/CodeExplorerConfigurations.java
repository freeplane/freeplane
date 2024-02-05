/*
 * Created on 17 Nov 2023
 *
 * author dimitry
 */
package org.freeplane.plugin.codeexplorer.task;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.util.LogUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;

public class CodeExplorerConfigurations {
    private static final String CODE_EXPLORER_CONFIGURATION_FILE_PROPERTY = "code.explorer_configuration_file";

    private List<CodeExplorerConfiguration> configurations;
    private final static Gson OBJECT_MAPPER = new GsonBuilder()
            .registerTypeAdapter(File.class, new FileTypeAdapter())
            .registerTypeAdapterFactory(OptionalTypeAdapter.FACTORY)
            .registerTypeHierarchyAdapter(Collection.class, new CollectionAdapter())
            .registerTypeHierarchyAdapter(Map.class, new MapAdapter())
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

    public void saveConfiguration() {
        saveConfiguration(getConfigurationFile());
    }

    private static File getConfigurationFile() {
        return ResourceController.getResourceController().getFile(CODE_EXPLORER_CONFIGURATION_FILE_PROPERTY);
    }

    void saveConfiguration(File file) {
        try (FileWriter writer = new FileWriter(file)){
            OBJECT_MAPPER.toJson(configurations, writer);
        } catch (IOException|JsonIOException e) {
            LogUtils.severe(e);
        }
    }

    public static CodeExplorerConfigurations loadConfigurations() {
        return loadConfigurations(getConfigurationFile());
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
            List<CodeExplorerConfiguration> configurations = OBJECT_MAPPER.fromJson(reader, CONFIGURATIONS_TYPE);
            if(configurations != null) {
                configurations.forEach(CodeExplorerConfiguration::initialize);
                return configurations;
            }
        } catch (Exception e) {
            LogUtils.severe(e);
        }
        return Collections.emptyList();
    }
}
