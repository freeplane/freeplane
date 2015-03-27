/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2011 dimitry
 *
 *  This file author is dimitry
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.freeplane.main.application.protocols.freeplaneresource;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

import org.freeplane.core.resources.ResourceController;

/** A {@link URLStreamHandler} that handles resources on the classpath. */
public class Handler extends URLStreamHandler {

    public Handler(ClassLoader classLoader) {
    }

    @Override
    protected URLConnection openConnection(URL u) throws IOException {
        final URL resourceUrl = ResourceController.getResourceController().getResource(u.getPath());
        return resourceUrl.openConnection();
    }
}
