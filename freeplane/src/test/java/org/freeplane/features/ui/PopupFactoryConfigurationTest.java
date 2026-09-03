package org.freeplane.features.ui;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import javax.swing.PopupFactory;
import javax.swing.UIManager;
import javax.swing.plaf.metal.MetalLookAndFeel;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.util.Compat;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import com.formdev.flatlaf.FlatLightLaf;

public class PopupFactoryConfigurationTest {
    private static PopupFactory initialPopupFactory;
    private MockedStatic<ResourceController> mockedResourceControllerStatic;
    private ResourceController resourceControllerMock;
    private String originalVendor;
    private String originalVmVendor;
    private String originalVendorVersion;

    @BeforeClass
    public static void saveInitialState() {
        try {
            Compat.setIsApplet(false);
        } catch (IllegalStateException ignored) {
        }
        initialPopupFactory = PopupFactory.getSharedInstance();
    }

    @AfterClass
    public static void restoreInitialState() {
        PopupFactory.setSharedInstance(initialPopupFactory);
    }

    @Before
    public void setUp() {
        originalVendor = System.getProperty("java.vendor");
        originalVmVendor = System.getProperty("java.vm.vendor");
        originalVendorVersion = System.getProperty("java.vendor.version");

        mockedResourceControllerStatic = Mockito.mockStatic(ResourceController.class);
        resourceControllerMock = mock(ResourceController.class);
        when(resourceControllerMock.getProperties()).thenReturn(new java.util.Properties());
        mockedResourceControllerStatic.when(ResourceController::getResourceController).thenReturn(resourceControllerMock);
        when(resourceControllerMock.getProperty(eq(FrameController.POPUP_FACTORY_PROPERTY), anyString()))
            .thenReturn(FrameController.POPUP_FACTORY_AUTO);
    }

    @After
    public void tearDown() {
        mockedResourceControllerStatic.close();
        restoreProperty("java.vendor", originalVendor);
        restoreProperty("java.vm.vendor", originalVmVendor);
        restoreProperty("java.vendor.version", originalVendorVersion);
        UIManager.put("Popup.dropShadowPainted", null);
        PopupFactory.setSharedInstance(new PopupFactory());
    }

    private void restoreProperty(String key, String value) {
        if (value != null) {
            System.setProperty(key, value);
        } else {
            System.clearProperty(key);
        }
    }

    @Test
    public void configurePopupFactory_withFlatLafAndAuto_installsFlatPopupFactory() throws Exception {
        when(resourceControllerMock.getProperty(eq(FrameController.POPUP_FACTORY_PROPERTY), anyString()))
            .thenReturn("auto");
        UIManager.setLookAndFeel(new FlatLightLaf());

        FrameController.configurePopupFactory();

        assertThat(PopupFactory.getSharedInstance().getClass().getName())
            .isEqualTo("com.formdev.flatlaf.ui.FlatPopupFactory");
    }

    @Test
    public void configurePopupFactory_withFlatLafAndBasicPref_installsBasicPopupFactory() throws Exception {
        when(resourceControllerMock.getProperty(eq(FrameController.POPUP_FACTORY_PROPERTY), anyString()))
            .thenReturn("basic");
        UIManager.setLookAndFeel(new FlatLightLaf());

        FrameController.configurePopupFactory();

        assertThat(PopupFactory.getSharedInstance().getClass().getName())
            .doesNotContain("FlatPopupFactory");
    }

    @Test
    public void configurePopupFactory_withNonFlatLaf_revertsToBasicPopupFactory() throws Exception {
        when(resourceControllerMock.getProperty(eq(FrameController.POPUP_FACTORY_PROPERTY), anyString()))
            .thenReturn("auto");
        UIManager.setLookAndFeel(new MetalLookAndFeel());

        FrameController.configurePopupFactory();

        assertThat(PopupFactory.getSharedInstance().getClass().getName())
            .doesNotContain("FlatPopupFactory");
    }

    @Test
    public void configurePopupFactory_onJbrRuntime_disablesDropShadowOnAuto() throws Exception {
        System.setProperty("java.vendor", "JetBrains s.r.o.");
        when(resourceControllerMock.getProperty(eq(FrameController.POPUP_FACTORY_PROPERTY), anyString()))
            .thenReturn("auto");
        UIManager.setLookAndFeel(new FlatLightLaf());

        FrameController.configurePopupFactory();

        assertThat(PopupFactory.getSharedInstance().getClass().getName())
            .isEqualTo("com.formdev.flatlaf.ui.FlatPopupFactory");
        assertThat(UIManager.get("Popup.dropShadowPainted")).isEqualTo(Boolean.FALSE);
    }

    @Test
    public void configurePopupFactory_dynamicLafSwitching() throws Exception {
        when(resourceControllerMock.getProperty(eq(FrameController.POPUP_FACTORY_PROPERTY), anyString()))
            .thenReturn("auto");

        // Step A: Set FlatLaf -> verify FlatPopupFactory installed
        UIManager.setLookAndFeel(new FlatLightLaf());
        FrameController.configurePopupFactory();
        assertThat(PopupFactory.getSharedInstance().getClass().getName())
            .isEqualTo("com.formdev.flatlaf.ui.FlatPopupFactory");

        // Step B: Switch to Metal -> verify basic PopupFactory restored
        UIManager.setLookAndFeel(new MetalLookAndFeel());
        FrameController.configurePopupFactory();
        assertThat(PopupFactory.getSharedInstance().getClass().getName())
            .doesNotContain("FlatPopupFactory");

        // Step C: Switch back to FlatLaf -> verify FlatPopupFactory restored
        UIManager.setLookAndFeel(new FlatLightLaf());
        FrameController.configurePopupFactory();
        assertThat(PopupFactory.getSharedInstance().getClass().getName())
            .isEqualTo("com.formdev.flatlaf.ui.FlatPopupFactory");
    }
}
