/*
 * Created on 2 Apr 2024
 *
 * author dimitry
 */
package org.freeplane.features.icon;

import java.util.Arrays;
import java.util.Collections;
import java.util.SortedSet;
import java.util.TreeSet;

import org.freeplane.core.extension.IExtension;
import org.freeplane.features.map.NodeModel;

public class Tags implements IExtension {
    private final SortedSet<Tag> tags;



    public Tags() {
        super();
        this.tags = new TreeSet<>();
    }



    public static SortedSet<Tag> getTags(NodeModel node){
        Tags tags = node.getExtension(Tags.class);
        return (tags == null) ? new TreeSet<>(Arrays.asList(new Tag("tag1"), new Tag("tag12345"))) : tags.tags;
    }
}
