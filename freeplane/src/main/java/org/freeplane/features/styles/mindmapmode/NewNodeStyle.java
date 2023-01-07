/*
 * Created on 4 Sep 2022
 *
 * author dimitry
 */
package org.freeplane.features.styles.mindmapmode;

import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.styles.IStyle;
import org.freeplane.features.styles.LogicalStyleModel;
import org.freeplane.features.styles.MapStyleModel;
import org.freeplane.features.styles.StyleTranslatedObject;

public class NewNodeStyle {

    private static final String STYLE_PREFIX = ":style:";
    static final String NEW_NODE_STYLE_PROPERTY_NAME = "newNodeStyle";

    static String propertyValue(IStyle style) {
        if(style == null)
            return null;
        else
            return STYLE_PREFIX + StyleTranslatedObject.toKeyString(style);
    }
    
    public static void assignStyleToNewNode(NodeModel node) {
        MapModel map = node.getMap();
        MapStyleModel styles = MapStyleModel.getExtension(map);
        String specification = styles.getProperty( NEW_NODE_STYLE_PROPERTY_NAME);
        if(specification != null && specification.startsWith(STYLE_PREFIX)){
            String styleKey = specification.substring(STYLE_PREFIX.length());
            styles.getNodeStyles().stream()
            .filter(style -> StyleTranslatedObject.toKeyString(style).equals(styleKey))
            .findAny()
            .ifPresent(style -> LogicalStyleModel.createExtension(node).setStyle(style));
            
        }
    }

}
