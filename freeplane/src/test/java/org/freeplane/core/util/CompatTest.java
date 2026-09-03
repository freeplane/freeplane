package org.freeplane.core.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class CompatTest {
    private String originalVendor;
    private String originalVmVendor;
    private String originalVendorVersion;

    @Before
    public void setUp() {
        originalVendor = System.getProperty("java.vendor");
        originalVmVendor = System.getProperty("java.vm.vendor");
        originalVendorVersion = System.getProperty("java.vendor.version");
    }

    @After
    public void tearDown() {
        restoreProperty("java.vendor", originalVendor);
        restoreProperty("java.vm.vendor", originalVmVendor);
        restoreProperty("java.vendor.version", originalVendorVersion);
    }

    private void restoreProperty(String key, String value) {
        if (value != null) {
            System.setProperty(key, value);
        } else {
            System.clearProperty(key);
        }
    }

    @Test
    public void isJetBrainsRuntime_withStandardJdk_returnsFalse() {
        System.setProperty("java.vendor", "Azul Systems, Inc.");
        System.setProperty("java.vm.vendor", "Azul Systems, Inc.");
        System.setProperty("java.vendor.version", "Zulu21.38+21-CA");

        assertThat(Compat.isJetBrainsRuntime()).isFalse();
    }

    @Test
    public void isJetBrainsRuntime_withJetBrainsVendor_returnsTrue() {
        System.setProperty("java.vendor", "JetBrains s.r.o.");
        System.setProperty("java.vm.vendor", "OpenJDK 64-Bit Server VM");
        System.clearProperty("java.vendor.version");

        assertThat(Compat.isJetBrainsRuntime()).isTrue();
    }

    @Test
    public void isJetBrainsRuntime_withJetBrainsVmVendor_returnsTrue() {
        System.setProperty("java.vendor", "Oracle Corporation");
        System.setProperty("java.vm.vendor", "JetBrains s.r.o.");
        System.clearProperty("java.vendor.version");

        assertThat(Compat.isJetBrainsRuntime()).isTrue();
    }

    @Test
    public void isJetBrainsRuntime_withJbrVendorVersion_returnsTrue() {
        System.setProperty("java.vendor", "Custom");
        System.setProperty("java.vm.vendor", "Custom VM");
        System.setProperty("java.vendor.version", "JBR-21.0.8+9-1004.2-nomod");

        assertThat(Compat.isJetBrainsRuntime()).isTrue();
    }

    @Test
    public void isJetBrainsRuntime_withNullOrEmptyProperties_returnsFalseWithoutException() {
        System.clearProperty("java.vendor");
        System.clearProperty("java.vm.vendor");
        System.clearProperty("java.vendor.version");

        assertThat(Compat.isJetBrainsRuntime()).isFalse();
    }
}
