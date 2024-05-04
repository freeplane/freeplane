/*
 * Created on 2 Apr 2024
 *
 * author dimitry
 */
package org.freeplane.features.icon;

import java.awt.Color;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Optional;
import java.util.zip.CRC32;

import org.freeplane.core.ui.components.HSLColorConverter;
import org.freeplane.core.util.LineComparator;

public class Tag implements Comparable<Tag>{
    public final static Tag EMPTY_TAG = new Tag("");
    private final String content;
    private Optional<Color> color;

    public Tag(String content) {
        this(content, Optional.empty());
    }

    public Tag(String content, Optional<Color> color) {
        this.content = content;
        this.color = color;
    }

    public String getContent() {
        return content;
    }

    @Override
    public int compareTo(Tag o) {
        return LineComparator.compareLinesParsingNumbers(content, o.content);
    }

    public boolean isEmpty() {
       return content.isEmpty();
    }

    public void setColor(Optional<Color> color) {
        this.color = color;
    }

    public Optional<Color> getColor() {
        return color;
    }

    public Color getIconColor() {
        return color.orElseGet(this::getDefaultColor);
    }

    public Color getDefaultColor() {
         if(content.isEmpty())
            return Color.BLACK;
        long crc = computeCRC32(content);
        return HSLColorConverter.generateColorFromLong(crc);
    }

    public Tag copy() {
        return new Tag(content, color);
    }

    private static long computeCRC32(String input) {
        CRC32 crc = new CRC32();
        crc.update(input.getBytes(StandardCharsets.UTF_8));
        return crc.getValue();
    }

    @Override
    public int hashCode() {
        return Objects.hash(content);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Tag other = (Tag) obj;
        return Objects.equals(content, other.content);
    }

    @Override
    public String toString() {
        return content;
    }
}
