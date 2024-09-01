/*
 * Created on 8 Jun 2024
 *
 * author dimitry
 */
package org.freeplane.features.icon;

import java.awt.Color;

public class TagReference {
    private Tag tag;

    public TagReference(Tag tag) {
        this.tag = tag;
    }

    public Tag getTag() {
        return tag;
    }

    public void setTag(Tag tag) {
        this.tag = tag;
    }

    public boolean isEmpty() {
        return tag.isEmpty();
    }

    public boolean exists() {
        return tag != Tag.REMOVED_TAG;
    }

    public Color getColor() {
        return tag.getColor();
    }

    public String getContent() {
        return tag.getContent();
    }

    public void setColor(Color color) {
        tag.setColor(color);
    }



}
