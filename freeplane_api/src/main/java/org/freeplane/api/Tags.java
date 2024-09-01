// Copyright (C) 2024  Dimitry Polivaev, macmarrum (at) outlook (dot) ie
// SPDX-License-Identifier: GPL-2.0-or-later
package org.freeplane.api;

import java.util.Collection;

/**@since 1.12.1 */
public interface Tags extends TagsRO{
    void setTags(Collection<String> keywords);
    void add(String keyword);
    void add(int index, String keyword);
    void add(Collection<String> keywords);
    boolean remove(String keyword);
    String remove(int index);
    boolean remove(Collection<String> keywords);
}
