package org.freeplane.features.styles.mindmapmode.styleeditorpanel;

import org.freeplane.core.resources.components.IPropertyControl;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.styles.MapStyleModel;

class StylePropertyAdjuster {
    static void adjustPropertyControl(NodeModel node, IPropertyControl control) {
        if(!MapStyleModel.isStyleNode(node))
            return;
        final boolean enable;
        if(MapStyleModel.isPredefinedStyleNode(node)) {
            Object style = node.getUserObject();
            String propertyName = control.getName();
            if(style.equals(MapStyleModel.DEFAULT_STYLE)) {
                if(propertyName.equals(RevertingProperty.NAME)) {
                    enable = false;
                    RevertingProperty property = (RevertingProperty) control;
                    property.setValue(true);
                } else
                    enable = ! propertyName.contains("cloud");
            }
            else if(propertyName.equals(RevertingProperty.NAME)
                    || propertyName.equals(NodeBackgroundColorControlGroup.REVERT_BACKGROUND)) {
                enable = true;
            }
            else if(style.equals(MapStyleModel.DETAILS_STYLE)
                    || style.equals(MapStyleModel.NOTE_STYLE)) {
                enable = propertyName.equals(NodeBackgroundColorControlGroup.NODE_BACKGROUND_COLOR)
                        || propertyName.equals(NodeColorControlGroup.NODE_COLOR)
                        || propertyName.equals(FontBoldControlGroup.NODE_FONT_BOLD)
                        || propertyName.equals(FontItalicControlGroup.NODE_FONT_ITALIC)
                        || propertyName.equals(FontStrikeThroughControlGroup.NODE_FONT_STRIKE_THROUGH)
                        || propertyName.equals(FontNameControlGroup.NODE_FONT_NAME)
                        || propertyName.equals(FontSizeControlGroup.NODE_FONT_SIZE)
                        || propertyName.equals(NodeHorizontalTextAlignmentControlGroup.TEXT_ALIGNMENT);
            }
            else if(style.equals(MapStyleModel.ATTRIBUTE_STYLE)) {
                enable = propertyName.equals(NodeBackgroundColorControlGroup.NODE_BACKGROUND_COLOR)
                        || propertyName.equals(NodeColorControlGroup.NODE_COLOR)
                        || propertyName.equals(FontBoldControlGroup.NODE_FONT_BOLD)
                        || propertyName.equals(FontItalicControlGroup.NODE_FONT_ITALIC)
                        || propertyName.equals(FontStrikeThroughControlGroup.NODE_FONT_STRIKE_THROUGH)
                        || propertyName.equals(FontNameControlGroup.NODE_FONT_NAME)
                        || propertyName.equals(FontSizeControlGroup.NODE_FONT_SIZE);
            }
            else if(style.equals(MapStyleModel.SELECTION_STYLE)
                    || style.equals(MapStyleModel.FLOATING_STYLE)) {
                enable = ! (propertyName.equals(NoteContentTypeControlGroup.NAME)
                        || propertyName.equals(DetailContentTypeControlGroup.NAME)
                        || propertyName.equals(FormatControlGroup.NODE_FORMAT)
                        || propertyName.equals(NodeNumberingControlGroup.NODE_NUMBERING));
            }
            else 
                enable = true;
        }
        else 
            enable = true;
        control.setEnabled(enable);
    }
}
