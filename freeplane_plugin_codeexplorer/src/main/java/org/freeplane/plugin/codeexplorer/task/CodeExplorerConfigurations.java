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
import com.google.gson.reflect.TypeToken;

public class CodeExplorerConfigurations {
    private static final String CODE_EXPLORER_CONFIGURATION_FILE_PROPERTY = "code.explorer_configuration_file";
    private static File configurationFile;

    private List<UserDefinedCodeExplorerConfiguration> configurations;
    private final static Gson OBJECT_MAPPER = new GsonBuilder()
            .registerTypeAdapter(File.class, new FileTypeAdapter())
            .registerTypeAdapterFactory(OptionalTypeAdapter.FACTORY)
            .registerTypeHierarchyAdapter(Collection.class, new CollectionAdapter())
            .registerTypeHierarchyAdapter(Map.class, new MapAdapter())
            .setPrettyPrinting()
            .create();
    private final static Type CONFIGURATIONS_TYPE = new TypeToken<List<UserDefinedCodeExplorerConfiguration>>() {/**/}.getType();


    public CodeExplorerConfigurations(List<UserDefinedCodeExplorerConfiguration> configurations) {
        this.configurations = configurations;
    }

    public List<UserDefinedCodeExplorerConfiguration> getConfigurations() {
        return configurations;
    }

    public void setConfigurations(List<UserDefinedCodeExplorerConfiguration> configurations) {
        this.configurations = configurations;
    }

    public void saveConfiguration() {
        if(configurationFile != null)
            saveConfiguration(configurationFile);
    }

    private static File getConfigurationFile() {
        if(configurationFile == null)
            configurationFile = ResourceController.getResourceController().getFile(CODE_EXPLORER_CONFIGURATION_FILE_PROPERTY);
        return configurationFile;
    }

    void saveConfiguration(File file) {
        try (FileWriter writer = new FileWriter(file)){
            if(! configurations.isEmpty())
                OBJECT_MAPPER.toJson(configurations, writer);
        } catch (IOException|JsonIOException e) {
            LogUtils.severe(e);
        }
    }

    public static CodeExplorerConfigurations loadConfigurations() {
        return loadConfigurations(getConfigurationFile());
    }

    static CodeExplorerConfigurations loadConfigurations(File configurationFile) {
        List<UserDefinedCodeExplorerConfiguration> configurations;
        configurations = !configurationFile.exists()
                ? new ArrayList<>()
                        : fromJsonFile(configurationFile);
        return new CodeExplorerConfigurations(configurations);
    }

    private static List<UserDefinedCodeExplorerConfiguration> fromJsonFile(File configurationFile){
        try (FileReader reader = new FileReader(configurationFile)) {
            List<UserDefinedCodeExplorerConfiguration> configurations = OBJECT_MAPPER.fromJson(reader, CONFIGURATIONS_TYPE);
            if(configurations != null) {
                configurations.forEach(UserDefinedCodeExplorerConfiguration::initialize);
                return configurations;
            }
        } catch (Exception e) {
            LogUtils.severe(e);
        }
        return Collections.emptyList();
    }
}
