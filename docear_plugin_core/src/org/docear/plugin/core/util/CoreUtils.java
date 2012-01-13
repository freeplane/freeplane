package org.docear.plugin.core.util;

import java.io.File;
import java.net.URI;

import org.freeplane.plugin.workspace.WorkspaceUtils;

public class CoreUtils {
	public static File resolveURI(final URI uri) {
		return WorkspaceUtils.resolveURI(uri);
	}
	
}
