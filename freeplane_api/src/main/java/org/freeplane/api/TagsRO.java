/*
 * Created on 4 Apr 2024
 *
 * author dimitry
 */
package org.freeplane.api;

import java.util.Collection;
import java.util.List;

import javax.swing.Icon;

/**@since 1.12.1 */
public interface TagsRO {
    List<String> getStrings();
    List<? extends Icon> getIcons();
    boolean contains(String tag);
    boolean containsAny(Collection<String> tags);
    boolean containsAll(Collection<String> tags);
}
