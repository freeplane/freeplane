package org.freeplane.view.swing.map.overview;

import java.awt.Color;

import org.freeplane.api.LengthUnit;
import org.freeplane.api.Quantity;

public class MapOverviewConstants {
    public final static String PROP_MAP_OVERVIEW_VISIBLE = "mapOverviewVisible";
    public final static String PROP_MAP_OVERVIEW_VISIBLE_FS = "mapOverviewVisible.fullscreen";

    public final static String PROP_MAP_OVERVIEW_PREFIX = "map_overview_";
    public final static String PROP_MAP_OVERVIEW_ATTACH_POINT = PROP_MAP_OVERVIEW_PREFIX + "attach_point";
    public final static String PROP_MAP_OVERVIEW_BOUNDS = PROP_MAP_OVERVIEW_PREFIX + "bounds";

    public final static int DEFAULT_SIZE = (int) new Quantity<LengthUnit>(240, LengthUnit.pt).in(LengthUnit.px).value;
    public final static int MIN_SIZE = (int) new Quantity<LengthUnit>(60, LengthUnit.pt).in(LengthUnit.px).value;
    public final static int MAX_SIZE = (int) new Quantity<LengthUnit>(480, LengthUnit.pt).in(LengthUnit.px).value;

    public static final Color VIEWPORT_THUMBNAIL_COLOR = new Color(0x32_00_00_FF, true);
}
