package org.freeplane.view.swing.map.overview;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;
import javax.swing.event.MouseInputListener;

import org.freeplane.api.LengthUnit;
import org.freeplane.api.Quantity;
import org.freeplane.core.resources.IFreeplanePropertyListener;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.features.map.IMapChangeListener;
import org.freeplane.features.map.MapChangeEvent;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.ui.ViewController;
import org.freeplane.view.swing.map.MapView;
import org.freeplane.view.swing.map.MapViewScrollPane;
import org.freeplane.view.swing.map.MapViewScrollPane.ViewportHiddenAreaSupplier;
import org.freeplane.view.swing.map.overview.resizable.ResizablePanelBorder;
import org.freeplane.view.swing.map.overview.resizable.ResizePanelMouseHandler;

public class MapViewPane extends JPanel implements IFreeplanePropertyListener, IMapChangeListener, ViewportHiddenAreaSupplier {
    private static final long serialVersionUID = 8664710783654626093L;

    private final static String MAP_OVERVIEW_VISIBLE_PROPERTY = "mapOverviewVisible";
    private final static String MAP_OVERVIEW_VISIBLE_FS_PROPERTY = "mapOverviewVisible.fullscreen";

    private final static String MAP_OVERVIEW_PROPERTY_PREFIX = "map_overview_";
    private final static String MAP_OVERVIEW_ATTACH_POINT_PROPERTY = MAP_OVERVIEW_PROPERTY_PREFIX + "attach_point";
    private final static String MAP_OVERVIEW_BOUNDS_PROPERTY = MAP_OVERVIEW_PROPERTY_PREFIX + "bounds";

    public final static int MAP_OVERVIEW_DEFAULT_SIZE = new Quantity<LengthUnit>(240, LengthUnit.pt)
            .toBaseUnitsRounded();
    public final static int MAP_OVERVIEW_MIN_SIZE = new Quantity<LengthUnit>(60, LengthUnit.pt).toBaseUnitsRounded();
    public final static int MAP_OVERVIEW_MAX_SIZE = new Quantity<LengthUnit>(480, LengthUnit.pt).toBaseUnitsRounded();

    public static final int MAP_OVERVIEW_BORDER_SIZE = new Quantity<LengthUnit>(3, LengthUnit.pt).toBaseUnitsRounded();
    public static final int MAP_OVERVIEW_BORDER_EXTENDED_SIZE = MAP_OVERVIEW_BORDER_SIZE
            + new Quantity<LengthUnit>(24, LengthUnit.pt).toBaseUnitsRounded();

    private final JScrollPane mapViewScrollPane;
    private final JPanel mapOverviewPanel;
    private final MapOverviewImage mapOverviewImage;
    private final MapView mapView;
    private boolean isMapOverviewVisible;


    public MapViewPane(JScrollPane mapViewScrollPane) {
        this.mapViewScrollPane = mapViewScrollPane;
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
                    mapViewScrollPane.validate();
                    mapOverviewPanel.setBounds(calculateMapOverviewBounds());
                }
            }
        });
        mapOverviewImage = new MapOverviewImage(mapView, mapViewScrollPane);
        mapOverviewPanel = new JPanel(new BorderLayout(0, 0)) {
            private static final long serialVersionUID = 1L;

            @Override
            public void updateUI() {
                super.updateUI();
                MouseInputListener handler = new ResizePanelMouseHandler();
                addMouseListener(handler);
                addMouseMotionListener(handler);
                setBorder(new ResizablePanelBorder(MAP_OVERVIEW_BORDER_SIZE, MAP_OVERVIEW_BORDER_EXTENDED_SIZE));
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
        if (mapOverviewPanel.isVisible()) {
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
                || MAP_OVERVIEW_VISIBLE_PROPERTY.equals(propertyName)
                || MAP_OVERVIEW_VISIBLE_FS_PROPERTY.equals(propertyName)) {
            final ViewController viewController = Controller.getCurrentController().getViewController();
            if (isMapOverviewVisible != viewController.isMapOverviewVisible()) {
                isMapOverviewVisible = ! isMapOverviewVisible;
                mapOverviewPanel.setVisible(isMapOverviewVisible);
                updateMapOverview();
            }
        } else if (propertyName.startsWith(MAP_OVERVIEW_PROPERTY_PREFIX)) {
            if (MAP_OVERVIEW_ATTACH_POINT_PROPERTY.equals(propertyName)) {
                Rectangle mapOverviewBounds = mapOverviewPanel.getBounds();
                convertOriginByAttachPoint(mapOverviewBounds);
                mapOverviewBounds.setLocation(0, 0);
                setMapOverviewBounds(mapOverviewBounds, true);
            }
            revalidate();
            updateMapOverview();
        }
    }

    public MapOverviewAttachPoint getMapOverviewAttachPoint() {
        ResourceController resourceController = ResourceController.getResourceController();
        String rawAttachPoint = resourceController.getProperty(MAP_OVERVIEW_ATTACH_POINT_PROPERTY);
        if (rawAttachPoint == null) {
            return MapOverviewAttachPoint.SOUTH_EAST;
        }
        try {
            return MapOverviewAttachPoint.valueOf(rawAttachPoint);
        } catch (IllegalArgumentException e) {
            return MapOverviewAttachPoint.SOUTH_EAST;
        }
    }

    public Color getMapViewBackground() {
        return mapView.getBackground();
    }

    public void updateMapOverviewAttachPoint(MapOverviewAttachPoint attachPoint) {
        Rectangle oldBounds = mapOverviewPanel.getBounds();
        Rectangle oldAttachBounds = new Rectangle(oldBounds);
        convertOriginByAttachPoint(oldAttachBounds);

        MapOverviewAttachPoint oldAttachPoint = getMapOverviewAttachPoint();
        if (attachPoint != oldAttachPoint) {
            ResourceController.getResourceController().setProperty(MAP_OVERVIEW_ATTACH_POINT_PROPERTY,
                    attachPoint.name());
        } else if (oldAttachBounds.x == 0 && oldAttachBounds.y == 0) {
            oldAttachBounds.setSize(MAP_OVERVIEW_DEFAULT_SIZE, MAP_OVERVIEW_DEFAULT_SIZE);
        }
        oldAttachBounds.setLocation(0, 0);
        setMapOverviewBounds(oldAttachBounds, true);
    }

    private Rectangle calculateMapOverviewBounds() {
        ResourceController resourceController = ResourceController.getResourceController();
        String rawBoundsValue = resourceController.getProperty(MAP_OVERVIEW_BOUNDS_PROPERTY);
        int[] elements = null;
        if (rawBoundsValue != null) {
            String[] rawElements = rawBoundsValue.split(",");
            if (rawElements.length == 4) {
                elements = new int[4];
                for (int i = 0; i < 4; i++) {
                    try {
                        elements[i] = Integer.parseInt(rawElements[i]);
                    } catch (NumberFormatException e) {
                        elements = null;
                        break;
                    }
                }
            }
        }

        if (elements == null) {
            elements = new int[] { 0, 0, MAP_OVERVIEW_DEFAULT_SIZE, MAP_OVERVIEW_DEFAULT_SIZE };
        }
        int x = elements[0];
        int y = elements[1];
        int width = getBoundedValue(elements[2], MAP_OVERVIEW_MIN_SIZE, MAP_OVERVIEW_MAX_SIZE);
        int height = getBoundedValue(elements[3], MAP_OVERVIEW_MIN_SIZE, MAP_OVERVIEW_MAX_SIZE);
        Rectangle bounds = new Rectangle(x, y, width, height);
        convertOriginByAttachPoint(bounds);
        return bounds;
    }

    private int getBoundedValue(int value, int min, int max) {
        return Math.max(Math.min(value, max), min);
    }

    public void setMapOverviewBounds(Rectangle bounds) {
        setMapOverviewBounds(bounds, false);
    }

    private void setMapOverviewBounds(Rectangle bounds, boolean isConverted) {
        if (! isConverted) {
            convertOriginByAttachPoint(bounds);
        }
        ResourceController.getResourceController().setProperty(MAP_OVERVIEW_BOUNDS_PROPERTY,
                String.format("%s,%s,%s,%s", bounds.x, bounds.y, bounds.width, bounds.height));
    }

    private void convertOriginByAttachPoint(Rectangle bounds) {
        final JViewport viewPort = mapViewScrollPane.getViewport();
        int bottom = viewPort.getHeight();
        int right = viewPort.getWidth();
        Point location;
        switch (getMapOverviewAttachPoint()) {
        case SOUTH_EAST:
        	location = new Point(right - bounds.x - bounds.width, bottom - bounds.y - bounds.height);
            break;
        case SOUTH_WEST:
        	location = new Point(bounds.x, bottom - bounds.y  - bounds.height);
            break;
        case NORTH_EAST:
        	location = new Point(right - bounds.x  - bounds.width, bounds.y);
            break;
        case NORTH_WEST:
        	location = new Point(0, 0);
            break;
        default:
        	throw new RuntimeException("All cases handled above");
        }
        UITools.convertPointToAncestor(viewPort, location, JScrollPane.class);
        bounds.setLocation(location);
    }

	@Override
	public Rectangle getHiddenArea() {
		if (isMapOverviewVisible) {
			return mapOverviewPanel.getBounds();
		} else
			return MapViewScrollPane.EMPTY_RECTANGLE;
	}

}
