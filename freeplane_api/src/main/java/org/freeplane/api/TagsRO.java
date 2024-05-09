// Copyright (C) 2024  Dimitry Polivaev, macmarrum (at) outlook (dot) ie
// SPDX-License-Identifier: GPL-2.0-or-later
package org.freeplane.api;

import java.util.Collection;
import java.util.List;

import javax.swing.Icon;

/**@since 1.12.1 */
public interface TagsRO {
    List<String> getKeywords();
    List<? extends Icon> getIcons();
    boolean contains(String keyword);
    boolean containsAny(Collection<String> keywords);
    boolean containsAll(Collection<String> keywords);
}
