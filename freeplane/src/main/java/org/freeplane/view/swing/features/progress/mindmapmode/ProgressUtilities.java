package org.freeplane.view.swing.features.progress.mindmapmode;

import java.util.List;

import org.freeplane.features.icon.NamedIcon;
import org.freeplane.features.map.NodeModel;
import org.freeplane.view.swing.features.filepreview.ExternalResource;

/**
 * @author Stefan Ott
 * 
 * This class has methods to get informations about progress icons attached to a NodeModel
 */
public class ProgressUtilities {
	public ProgressUtilities() {
	};

	/**
	 * 
	 * @return : true if the node has an external resource attached.
	 */
	public boolean hasExternalResource(final NodeModel node) {
		final ExternalResource extResource = (ExternalResource) node.getExtension(ExternalResource.class);
		if (extResource == null) {
			return false;
		}
		else {
			return true;
		}
	}

	/**
	 * 
	 * @return : true if the node has an extended progress icon attached.
	 */
	public boolean hasExtendedProgressIcon(final NodeModel node) {
		final ExternalResource extResource = (ExternalResource) node.getExtension(ExternalResource.class);
		if (extResource == null) {
			return false;
		}
		if (extResource.getUri().toString().matches(ProgressIcons.EXTENDED_PROGRESS_ICON_IDENTIFIER)) {
			return true;
		}
		return false;
	}

	/**
	 * 
	 * @return : true if OK icon is attached
	 */
	public boolean hasOKIcon(final NodeModel node) {
		final List<NamedIcon> icons = node.getIcons();
		for (int i = 0; i < icons.size(); i++) {
			if (icons.get(i).getName().equals("button_ok")) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 
	 * @return : true if the node has a progress icon (0%, 25%, 50%, 75%, 100%) attached
	 */
	public boolean hasProgressIcons(final NodeModel node) {
		final String[] iconNames = new String[] { "0%", "25%", "50%", "75%", "100%" };
		final List<NamedIcon> icons = node.getIcons();
		for (int i = 0; i < icons.size(); i++) {
			for (int j = 0; j < iconNames.length; j++) {
				if (icons.get(i).getName().equals(iconNames[j])) {
					return true;
				}
			}
		}
		return false;
	}
}
