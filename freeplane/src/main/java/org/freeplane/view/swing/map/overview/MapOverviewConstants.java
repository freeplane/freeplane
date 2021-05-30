package org.freeplane.view.swing.map.overview;

import java.awt.Color;

import org.freeplane.api.LengthUnit;
import org.freeplane.api.Quantity;

public class MapOverviewConstants {
    public final static String VISIBLE_PROPERTY = "mapOverviewVisible";
    public final static String VISIBLE_FS_PROPERTY = "mapOverviewVisible.fullscreen";

    public final static String PROPERTY_PREFIX = "map_overview_";
    public final static String ATTACH_POINT_PROPERTY = PROPERTY_PREFIX + "attach_point";
    public final static String BOUNDS_PROPERTY = PROPERTY_PREFIX + "bounds";

    public final static int DEFAULT_SIZE = (int) new Quantity<LengthUnit>(240, LengthUnit.pt).in(LengthUnit.px).value;
    public final static int MIN_SIZE = (int) new Quantity<LengthUnit>(60, LengthUnit.pt).in(LengthUnit.px).value;
    public final static int MAX_SIZE = (int) new Quantity<LengthUnit>(480, LengthUnit.pt).in(LengthUnit.px).value;

    public static final Color VIEWPORT_THUMBNAIL_COLOR = new Color(0x32_00_00_FF, true);
}
