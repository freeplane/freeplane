package org.freeplane.api;

import java.io.File;

public interface HeadlessMapCreator {
	/** creates an new unsaved map from given template without an associated view.
	 *
	 * This map can be processed by scripts and any other clients of scripting API.
	 * @since 1.6.16 */
	Map newHiddenMapFromTemplate(File templateFile);
}