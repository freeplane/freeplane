/*
 * Created on 2 Apr 2024
 *
 * author dimitry
 */
package org.freeplane.features.icon;

import java.util.Objects;

public class Tag implements Comparable<Tag>{
    private final String content;

    public Tag(String content) {
        super();
        this.content = content;
    }

    public String getContent() {
        return content;
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
        return "Tag [content=" + content + "]";
    }

    @Override
    public int compareTo(Tag o) {
        return content.compareTo(o.content);
    }

}
