/*
 * Created on 30 May 2024
 *
 * author dimitry
 */
package org.freeplane.features.icon;

import javax.swing.tree.TreePath;

public interface TreeTagChangeListener<V> {
    void valueForPathChanged(TreePath path, V newValue);
}
