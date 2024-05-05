/*
 * Created on 4 May 2024
 *
 * author dimitry
 */
package org.freeplane.features.icon.mindmapmode;

import java.util.Collections;
import java.util.List;

import org.freeplane.features.icon.CategorizedTag;
import org.freeplane.features.icon.Tag;

public class UncategorizedTag implements CategorizedTag {
    private static final List<Tag> EMPTY_TAG_LIST = Collections.singletonList(Tag.EMPTY_TAG);
    private final Tag tag;


    public UncategorizedTag(Tag tag) {
        super();
        this.tag = tag;
    }

    @Override
    public Tag tag() {
        return tag;
    }

    @Override
    public List<Tag> categoryTags() {
        return tag.isEmpty() ? EMPTY_TAG_LIST : Collections.singletonList(tag);
    }

    @Override
    public String toString() {
        return "UncategorizedTag [tag=" + tag + "]";
    }
}
