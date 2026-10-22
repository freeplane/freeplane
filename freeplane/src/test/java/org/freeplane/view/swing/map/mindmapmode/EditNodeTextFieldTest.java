package org.freeplane.view.swing.map.mindmapmode;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.Collection;

import javax.swing.ImageIcon;
import javax.swing.JEditorPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import org.freeplane.api.LengthUnit;
import org.freeplane.api.Quantity;
import org.freeplane.api.TextWritingDirection;
import org.freeplane.core.resources.ResourceBundles;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.features.map.MapController;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.freeplane.features.nodestyle.NodeStyleController;
import org.freeplane.features.spellchecker.mindmapmode.SpellCheckerController;
import org.freeplane.features.text.TextController;
import org.freeplane.features.text.mindmapmode.EditNodeBase.EditedComponent;
import org.freeplane.features.text.mindmapmode.EditNodeBase.IEditControl;
import org.freeplane.features.text.mindmapmode.EventBuffer;
import org.freeplane.features.text.mindmapmode.MTextController;
import org.freeplane.features.ui.IMapViewManager;
import org.freeplane.view.swing.map.MainView;
import org.freeplane.view.swing.map.MapView;
import org.freeplane.view.swing.map.NodeView;
import org.freeplane.view.swing.map.ZoomableLabel;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class EditNodeTextFieldTest {
    @Parameters(name = "{0}, icon={1}")
    public static Collection<Object[]> textDirectionsAndIcons() {
        return Arrays.asList(new Object[][] {
            {TextWritingDirection.LEFT_TO_RIGHT, false},
            {TextWritingDirection.LEFT_TO_RIGHT, true},
            {TextWritingDirection.RIGHT_TO_LEFT, false},
            {TextWritingDirection.RIGHT_TO_LEFT, true}
        });
    }

    private final TextWritingDirection textDirection;
    private final boolean withIcon;
    private Controller previousController;
    private MTextController textController;
    private MapView mapView;

    public EditNodeTextFieldTest(TextWritingDirection textDirection, boolean withIcon) {
        this.textDirection = textDirection;
        this.withIcon = withIcon;
    }

    @Before
    public void setUp() {
        previousController = Controller.getCurrentController();
        Controller controller = mock(Controller.class);
        ModeController modeController = mock(ModeController.class);
        ResourceController resourceController = mock(ResourceController.class);
        ResourceBundles resources = mock(ResourceBundles.class);
        IMapViewManager mapViewManager = mock(IMapViewManager.class);
        MapController mapController = mock(MapController.class);
        textController = mock(MTextController.class);
        NodeStyleController nodeStyleController = mock(NodeStyleController.class);

        when(controller.getModeController()).thenReturn(modeController);
        when(controller.getResourceController()).thenReturn(resourceController);
        when(controller.getMapViewManager()).thenReturn(mapViewManager);
        when(modeController.getController()).thenReturn(controller);
        when(modeController.getMapController()).thenReturn(mapController);
        when(modeController.getExtension(TextController.class)).thenReturn(textController);
        when(modeController.getExtension(NodeStyleController.class)).thenReturn(nodeStyleController);
        when(modeController.getExtension(SpellCheckerController.class)).thenReturn(mock(SpellCheckerController.class));
        when(resourceController.getResources()).thenReturn(resources);
        when(resources.getResourceString(anyString())).thenAnswer(invocation -> invocation.getArgument(0));
        when(resourceController.getProperty(anyString())).thenReturn("false");
        when(resourceController.getIntProperty(anyString(), anyInt())).thenAnswer(invocation -> invocation.getArgument(1));
        when(resourceController.getIntProperty("display_scale")).thenReturn(100);
        when(resourceController.getColorProperty(anyString())).thenReturn(Color.BLACK);
        when(resourceController.getBooleanProperty("layout_map_on_text_change")).thenReturn(true);
        when(mapViewManager.getZoom()).thenReturn(1f);
        when(textController.getEventQueue()).thenReturn(mock(EventBuffer.class));
        when(nodeStyleController.getMaxWidth(any(), any())).thenReturn(new Quantity<>(400, LengthUnit.px));

        Controller.setCurrentController(controller);
        mapView = mock(MapView.class);
        when(mapView.getModeController()).thenReturn(modeController);
        when(mapView.getZoomed(anyInt())).thenAnswer(invocation -> invocation.getArgument(0));
        when(mapView.getBackground()).thenReturn(Color.WHITE);
    }

    @After
    public void tearDown() {
        Controller.setCurrentController(previousController);
    }

    @Test
    public void keepsNodeWidthWhenOpeningEditor() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            ZoomableLabel parent = openEditor(new Dimension(100, 15));

            assertThat(parent.getWidth()).isEqualTo(220);
        });
    }

    @Test
    public void growsNodeWidthWhenEditorNeedsMoreSpace() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            ZoomableLabel parent = openEditor(new Dimension(250, 15));

            assertThat(parent.getWidth()).isEqualTo(withIcon ? 281 : 261);
        });
    }

    @Test
    public void growsNodeHeightWhenEditorNeedsMoreSpace() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            ZoomableLabel parent = openEditor(new Dimension(100, 40));

            assertThat(parent.getWidth()).isEqualTo(220);
            assertThat(parent.getHeight()).isEqualTo(40);
        });
    }

    private ZoomableLabel openEditor(Dimension editorPreferredSize) {
        NodeModel node = new NodeModel("example node text", null);
        MainView mainView = mock(MainView.class);
        when(mainView.getComponentOrientation()).thenReturn(textDirection.componentOrientation);
        NodeView nodeView = new NodeView(node, mapView) {
            @Override
            public MainView getMainView() {
                return mainView;
            }
        };
        ZoomableLabel parent = new ZoomableLabel() {
            @Override
            protected float getZoom() {
                return 1f;
            }

            @Override
            protected boolean useFractionalMetrics() {
                return false;
            }

            @Override
            public boolean isValid() {
                return true;
            }
        };
        parent.setFont(new Font(Font.DIALOG, Font.PLAIN, 12));
        parent.setBorder(new EmptyBorder(0, 2, 1, 2));
        parent.setMaximumWidth(220);
        parent.setHorizontalAlignment(SwingConstants.LEFT);
        parent.setHorizontalTextPosition(SwingConstants.TRAILING);
        parent.setVerticalTextPosition(SwingConstants.TOP);
        parent.setComponentOrientation(textDirection.componentOrientation);
        if (withIcon)
            parent.setIcon(new ImageIcon(new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB)));
        parent.setText(node.getText());
        parent.setSize(220, 20);
        nodeView.add(parent);
        JEditorPane textfield = new JEditorPane() {
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(editorPreferredSize);
            }
        };
        when(textController.createEditorPane(MTextController.NODE_TEXT)).thenReturn(textfield);
        IEditControl editControl = mock(IEditControl.class);
        when(editControl.getEditType()).thenReturn(EditedComponent.TEXT);
        EditNodeTextField editor = new EditNodeTextField(node, parent, node.getText(), editControl);

        editor.show(null);

        return parent;
    }
}
