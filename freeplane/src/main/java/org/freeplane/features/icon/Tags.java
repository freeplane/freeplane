/*
 * Created on 2 Apr 2024
 *
 * author dimitry
 */
package org.freeplane.features.icon;

import java.util.Collections;
import java.util.List;

import org.freeplane.core.extension.IExtension;
import org.freeplane.features.map.NodeModel;

public class Tags implements IExtension {
    private List<Tag> tags;

    private Tags(List<Tag> tags) {
        super();
        this.tags = tags;
    }

    static List<Tag> getTags(NodeModel node){
        Tags tags = node.getExtension(Tags.class);
        return (tags == null) ? Collections.emptyList() : tags.tags;
    }

    public static void setTags(NodeModel node, List<Tag> newTags) {
        Tags extension = node.getExtension(Tags.class);
        if(extension == null) {
            extension = new Tags(newTags);
            node.addExtension(extension);
        }
        else
            extension.tags = newTags;
    }

    public List<Tag> getTags(){
        return Collections.unmodifiableList(tags);
    }
}
