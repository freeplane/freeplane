package org.freeplane.view.swing.map;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Insets;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.event.MouseInputListener;

import org.freeplane.core.resources.IFreeplanePropertyListener;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.features.map.IMapChangeListener;
import org.freeplane.features.map.MapChangeEvent;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.ui.ViewController;
import org.freeplane.view.swing.map.overview.MapOverviewImage;
import org.freeplane.view.swing.map.overview.MapOverviewConstants;
import org.freeplane.view.swing.map.overview.MapOverviewUtils;
import org.freeplane.view.swing.map.overview.ResizableBorder;
import org.freeplane.view.swing.map.overview.ResizePanelMouseHandler;

public class MapViewPane extends JLayeredPane implements IFreeplanePropertyListener, IMapChangeListener {
    private static final long serialVersionUID = 8664710783654626093L;

    private final MapOverviewImage mapOverviewImage;
    private final JPanel mapOverviewPanel;
    private final MapView mapView;
    private boolean isMapOverviewVisible;

    public MapViewPane(JScrollPane mapViewScrollPane) {
        this.mapView = (MapView) mapViewScrollPane.getViewport().getView();
        setLayout(new BorderLayout(0, 0) {
            private static final long serialVersionUID = 3702408082745761647L;

            @Override
            public void layoutContainer(Container parent) {
                synchronized (parent.getTreeLock()) {
                    Insets insets = parent.getInsets();
                    int width = parent.getWidth();
                    int height = parent.getHeight();
                    int top = insets.top;
                    int bottom = height - insets.bottom;
                    int left = insets.left;
                    int right = width - insets.right;
                    mapViewScrollPane.setBounds(left, top, right - left, bottom - top);
                    mapOverviewPanel.setBounds(MapOverviewUtils.getMapOverviewBounds(mapView));
                }
            }
        });
        mapOverviewImage = new MapOverviewImage(mapView);
        mapOverviewPanel = new JPanel(new BorderLayout(0, 0)) {
            private static final long serialVersionUID = 1L;

            @Override
            public void updateUI() {
                super.updateUI();
                MouseInputListener handler = new ResizePanelMouseHandler(mapView);
                addMouseListener(handler);
                addMouseMotionListener(handler);
                setBorder(new ResizableBorder(mapView));
            }
        };
        mapOverviewPanel.add(mapOverviewImage);
        final ViewController viewController = Controller.getCurrentController().getViewController();
        isMapOverviewVisible = viewController.isMapOverviewVisible();
        mapOverviewPanel.setVisible(isMapOverviewVisible);
        add(mapOverviewPanel, BorderLayout.EAST);
        add(mapViewScrollPane);

        mapView.addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent e) {
                updateMapOverview();
            };
        });
    }

    @Override
    public void mapChanged(MapChangeEvent event) {
        if (event.getMap() != mapView.getModel()) {
            return;
        }
        updateMapOverview();
    }

    private void updateMapOverview() {
        if (isMapOverviewVisible) {
            mapOverviewImage.resetImage();
            SwingUtilities.invokeLater(mapOverviewPanel::repaint);
        }
    }

    @Override
    public boolean isOptimizedDrawingEnabled() {
        return false; // enable overlap
    }

    @Override
    public void addNotify() {
        super.addNotify();
        Controller.getCurrentModeController().getMapController().addMapChangeListener(this);
        ResourceController.getResourceController().addPropertyChangeListener(this);
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        Controller.getCurrentModeController().getMapController().removeMapChangeListener(this);
        ResourceController.getResourceController().removePropertyChangeListener(this);
    }

    @Override
    public void propertyChanged(String propertyName, String newValue, String oldValue) {
        if (ViewController.FULLSCREEN_ENABLED_PROPERTY.equals(propertyName)
                || MapOverviewConstants.PROP_MAP_OVERVIEW_VISIBLE.equals(propertyName)
                || MapOverviewConstants.PROP_MAP_OVERVIEW_VISIBLE_FS.equals(propertyName)) {
            final ViewController viewController = Controller.getCurrentController().getViewController();
            if (isMapOverviewVisible != viewController.isMapOverviewVisible()) {
                isMapOverviewVisible = ! isMapOverviewVisible;
                mapOverviewPanel.setVisible(isMapOverviewVisible);
                updateMapOverview();
            }
        } else if (propertyName.startsWith(MapOverviewConstants.PROP_MAP_OVERVIEW_PREFIX)) {
            if (MapOverviewConstants.PROP_MAP_OVERVIEW_ATTACH_POINT.equals(propertyName)) {
                ResourceController.getResourceController().setProperty(MapOverviewConstants.PROP_MAP_OVERVIEW_BOUNDS,
                        "");
            }
            revalidate();
            updateMapOverview();
        }
    }
}
