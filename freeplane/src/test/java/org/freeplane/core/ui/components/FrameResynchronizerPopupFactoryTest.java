package org.freeplane.core.ui.components;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assume.assumeFalse;

import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Window;
import java.util.Arrays;
import java.util.Collection;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPopupMenu;
import javax.swing.LookAndFeel;
import javax.swing.PopupFactory;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * Run under Xvfb to exercise the X11 regression; other toolkits also verify that
 * frame installation preserves the look-and-feel's popup behavior.
 */
@RunWith(Parameterized.class)
public class FrameResynchronizerPopupFactoryTest {
    @Parameters(name = "{0}, lightweight={1}")
    public static Collection<Object[]> lookAndFeelsAndPopupWeights() {
        return Arrays.asList(new Object[][] {
            { "javax.swing.plaf.metal.MetalLookAndFeel", false },
            { "javax.swing.plaf.metal.MetalLookAndFeel", true },
            { "com.formdev.flatlaf.FlatLightLaf", false },
            { "com.formdev.flatlaf.FlatLightLaf", true }
        });
    }

    private final String lookAndFeelClassName;
    private final boolean lightweightEnabled;

    public FrameResynchronizerPopupFactoryTest(final String lookAndFeelClassName,
            final boolean lightweightEnabled) {
        this.lookAndFeelClassName = lookAndFeelClassName;
        this.lightweightEnabled = lightweightEnabled;
    }

    @Test
    public void install_preservesLookAndFeelPopupSelectionAndCoordinates() throws Exception {
        assumeFalse("Popup placement requires a display", GraphicsEnvironment.isHeadless());
        SwingUtilities.invokeAndWait(() -> {
            final LookAndFeel previousLookAndFeel = UIManager.getLookAndFeel();
            final PopupFactory previousFactory = PopupFactory.getSharedInstance();
            try {
                UIManager.setLookAndFeel(lookAndFeelClassName);
                final PopupFactory lookAndFeelFactory = PopupFactory.getSharedInstance();
                final Class<? extends Window> originalPopupWindow = showMenu(false);
                final Class<? extends Window> resynchronizedPopupWindow = showMenu(true);

                assertThat(resynchronizedPopupWindow).isEqualTo(originalPopupWindow);
                assertThat(PopupFactory.getSharedInstance()).isSameAs(lookAndFeelFactory);
            }
            catch (Exception exception) {
                throw new AssertionError(exception);
            }
            finally {
                try {
                    UIManager.setLookAndFeel(previousLookAndFeel);
                }
                catch (Exception exception) {
                    throw new AssertionError(exception);
                }
                finally {
                    PopupFactory.setSharedInstance(previousFactory);
                }
            }
        });
    }

    private Class<? extends Window> showMenu(final boolean installResynchronizer) {
        final JFrame frame = new JFrame("Frame resynchronizer popup test");
        final JPopupMenu menu = new JPopupMenu();
        try {
            final JButton owner = new JButton("Popup owner");
            frame.add(owner);
            frame.setBounds(100, 100, 640, 480);
            if (installResynchronizer) {
                FrameResynchronizer.install(frame);
            }
            frame.setVisible(true);
            menu.setLightWeightPopupEnabled(lightweightEnabled);
            menu.add("Popup item");
            final Point expectedLocation = owner.getLocationOnScreen();
            expectedLocation.translate(30, 30);
            menu.show(owner, 30, 30);

            assertThat(menu.isShowing()).isTrue();
            assertThat(menu.getLocationOnScreen()).isEqualTo(expectedLocation);
            final Window popupWindow = SwingUtilities.getWindowAncestor(menu);
            assertThat(popupWindow).isNotNull();
            if (!lightweightEnabled) {
                assertThat(popupWindow).isNotSameAs(frame);
            }
            return popupWindow.getClass();
        }
        finally {
            menu.setVisible(false);
            frame.dispose();
        }
    }
}
