package org.freeplane.api;

import java.io.File;
import java.net.URL;

public interface HeadlessMapCreator {
	/** creates an new unsaved map from given template without an associated view.
	 *
	 * This map can be processed by scripts and any other clients of scripting API.
	 * @since 1.6.16 */
	Map newHiddenMapFromTemplate(File template);
	/** creates an new unsaved map from given template without an associated view.
	 *
	 * This map can be processed by scripts and any other clients of scripting API.
	 * @since 1.6.16 */
	Map newHiddenMapFromTemplate(URL template);

	/** loads a map without an associated view, if it was not loaded before.
	 *
	 * This map can be processed by scripts and any other clients of scripting API.
	 * @since 1.6.16 */
	Map hiddenMap(File template);
	/** loads a map without an associated view, if it was not loaded before.
	 *
	 * This map can be processed by scripts and any other clients of scripting API.
	 * @since 1.6.16 */
	Map hiddenMap(URL template);
}