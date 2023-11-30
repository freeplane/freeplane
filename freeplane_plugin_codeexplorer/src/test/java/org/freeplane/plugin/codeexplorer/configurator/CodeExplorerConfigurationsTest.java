/*
 * Created on 30 Nov 2023
 *
 * author dimitry
 */
package org.freeplane.plugin.codeexplorer.configurator;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;

import org.assertj.core.api.Assertions;
import org.freeplane.plugin.codeexplorer.task.CodeExplorerConfiguration;
import org.junit.Test;

public class CodeExplorerConfigurationsTest {

    private static String serialize(CodeExplorerConfiguration singleConfiguration) {
        return new CodeExplorerConfigurations(Collections.singletonList(singleConfiguration)).serialize();
    }

    private static CodeExplorerConfiguration deserialize(String string) {
        return CodeExplorerConfigurations.deserialize(string).getConfigurations().get(0);
    }

    @Test
    public void createsConfigurationFromEmptyString() throws Exception {
        Assertions.assertThat(CodeExplorerConfigurations.deserialize("").getConfigurations()).isEmpty();
    }

    @Test
    public void serializesAndDeserializesEmptyConfigurations() throws Exception {
        CodeExplorerConfiguration uut = new CodeExplorerConfiguration("", Collections.emptyList(), "");
        String serialized = serialize(uut);
        Assertions.assertThat(deserialize(serialized))
        .usingRecursiveComparison()
        .isEqualTo(uut);
    }

    @Test
    public void serializesAndDeserializesConfigurationContainingProjectName() throws Exception {
        CodeExplorerConfiguration uut = new CodeExplorerConfiguration("project name", Collections.emptyList(), "");
        String serialized = serialize(uut);
        Assertions.assertThat(deserialize(serialized))
        .usingRecursiveComparison()
        .isEqualTo(uut);
    }

    @Test
    public void serializesAndDeserializesConfigurationContainingProjectNameWithTab() throws Exception {
        CodeExplorerConfiguration uut = new CodeExplorerConfiguration("project\tname", Collections.emptyList(), "");
        String serialized = serialize(uut);
        Assertions.assertThat(deserialize(serialized))
        .usingRecursiveComparison()
        .isEqualTo(uut);
    }

    @Test
    public void serializesAndDeserializesConfigurationContainingRules() throws Exception {
        CodeExplorerConfiguration uut = new CodeExplorerConfiguration("", Collections.emptyList(), " a ->^ b\n b ->v c");
        String serialized = serialize(uut);
        Assertions.assertThat(deserialize(serialized))
        .usingRecursiveComparison()
        .isEqualTo(uut);
    }

    @Test
    public void serializesAndDeserializesConfigurationContainingProjectNameAndRules() throws Exception {
        CodeExplorerConfiguration uut = new CodeExplorerConfiguration("project name", Collections.emptyList(), " a ->^ b\n b ->v c");
        String serialized = serialize(uut);
        Assertions.assertThat(deserialize(serialized))
        .usingRecursiveComparison()
        .isEqualTo(uut);
    }

    @Test
    public void serializesAndDeserializesConfigurationContainingLocations() throws Exception {
        CodeExplorerConfiguration uut = new CodeExplorerConfiguration("",
                Arrays.asList(new File("a"), new File("b")), "");
        String serialized = serialize(uut);
        Assertions.assertThat(deserialize(serialized))
        .usingRecursiveComparison()
        .isEqualTo(uut);
    }

    @Test
    public void serializesAndDeserializesConfigurationContainingLocationsAndRules() throws Exception {
        CodeExplorerConfiguration uut = new CodeExplorerConfiguration("",
                Arrays.asList(new File("a"), new File("b")), "");
        String serialized = serialize(uut);
        Assertions.assertThat(deserialize(serialized))
        .usingRecursiveComparison()
        .isEqualTo(uut);
    }

}
