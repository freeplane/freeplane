/*
 * Created on 4 Apr 2024
 *
 * author dimitry
 */
package org.freeplane.api;

import java.util.Collection;

/**@since 1.12.1 */
public interface Tags extends TagsRO{
    void setTags(Collection<String> tags);
    void addTag(String tag);
    void addTag(int index, String tag);
    void addAllTags(Collection<String> tags);
    boolean removeTag(String tag);
    String removeTag(int index);
    boolean removeAllTags(Collection<String> tags);
}
