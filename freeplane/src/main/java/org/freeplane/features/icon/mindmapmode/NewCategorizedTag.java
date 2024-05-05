/*
 * Created on 4 May 2024
 *
 * author dimitry
 */
package org.freeplane.features.icon.mindmapmode;

import java.util.List;

import org.freeplane.features.icon.CategorizedTag;
import org.freeplane.features.icon.Tag;

public class NewCategorizedTag implements CategorizedTag {

    private final List<Tag> categoryTags;



    public NewCategorizedTag(List<Tag> categoryTags) {
        super();
        this.categoryTags = categoryTags;
    }

    @Override
    public Tag tag() {
        return categoryTags.get(categoryTags.size()-1);
    }

    @Override
    public List<Tag> categoryTags() {
        return categoryTags;
    }

    @Override
    public String toString() {
        return "NewCategorizedTag [categoryTags=" + categoryTags + "]";
    }

}
