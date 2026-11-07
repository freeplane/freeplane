/*
 * Created on 14 Feb 2025
 *
 * author dimitry
 */
package org.freeplane.features.icon.mindmapmode;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.function.ToIntFunction;

import javax.swing.Icon;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

import org.freeplane.core.ui.components.TagIcon;
import org.freeplane.features.icon.Tag;
import org.freeplane.features.icon.TagCategories;

@SuppressWarnings("serial") class TagCellRenderer extends DefaultTreeCellRenderer {
    static final int NO_USAGE_DISPLAYED = -1;
    private static final float UNUSED_TAG_OPACITY = 0.4f;
    private static final int UNUSED_TEXT_ALPHA = 100;

    /** Wraps an existing tag icon so unused tags can be visually de-emphasized. */
    private static class FadedIcon implements Icon {
        private final Icon delegate;

        FadedIcon(Icon delegate) {
            this.delegate = delegate;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D faded = (Graphics2D) g.create();
            faded.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, UNUSED_TAG_OPACITY));
            delegate.paintIcon(c, faded, x, y);
            faded.dispose();
        }

        @Override
        public int getIconWidth() {
            return delegate.getIconWidth();
        }

        @Override
        public int getIconHeight() {
            return delegate.getIconHeight();
        }
    }

    private final TagCategories tagCategories;
    private final ToIntFunction<DefaultMutableTreeNode> usageOf;

    public TagCellRenderer(TagCategories tagCategories) {
        this(tagCategories, node -> NO_USAGE_DISPLAYED);
    }

    public TagCellRenderer(TagCategories tagCategories, ToIntFunction<DefaultMutableTreeNode> usageOf) {
        this.tagCategories = tagCategories;
        this.usageOf = usageOf;
        setHorizontalAlignment(CENTER);
    }

    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel,
            boolean expanded, boolean leaf, int row, boolean hasFocus) {

        super.getTreeCellRendererComponent(tree, null, sel, expanded, leaf, row, hasFocus);
        if (value instanceof DefaultMutableTreeNode) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
            Tag tag = tagCategories.tagWithoutCategories(node);
            if (! tag.isEmpty()) {
                Font font = getFont();
                Icon tagIcon = new TagIcon(tag, font, getFontMetrics(font).getFontRenderContext());
                int usage = usageOf.applyAsInt(node);
                // The default renderer path remains unchanged until a usage provider is installed.
                if (usage == NO_USAGE_DISPLAYED) {
                    setText(null);
                    setIcon(tagIcon);
                } else {
                    setText(" (" + usage + ")");
                    if (usage == 0) {
                        // Fade both icon and text so an unused tag is visible but clearly distinct.
                        setIcon(new FadedIcon(tagIcon));
                        Color textColor = getForeground();
                        setForeground(new Color(textColor.getRed(), textColor.getGreen(), textColor.getBlue(), UNUSED_TEXT_ALPHA));
                    } else {
                        setIcon(tagIcon);
                    }
                }
            } else if (node.getUserObject() != null) {
                setText(node.getUserObject().toString());
            }
        }

        return this;
    }
}
