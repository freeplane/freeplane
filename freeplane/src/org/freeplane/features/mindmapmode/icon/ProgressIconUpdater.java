package org.freeplane.features.mindmapmode.icon;

import java.util.List;

import org.freeplane.features.common.icon.IconController;
import org.freeplane.features.common.icon.MindIcon;
import org.freeplane.features.common.map.NodeModel;

/**
 * @author Stefan Ott
 * 
 * This class holds the static method to update the progress icons
 */
class ProgressIconUpdater {
	private static final long serialVersionUID = 1L;

	/**
	 * This method increases/ decreases the progress icons.
	 * If none is present then the 0% icon is set.
	 * At 100% the OK-icon is additionally added
	 * 
	 * @param node : the node to update the icons
	 * @param up : true if the progress is increased (0% -> 25% -> 50%...)
	 * 				if false the progress is decreased
	 */
	public static void update(final NodeModel node, final boolean up) {
		final MIconController Mic = (MIconController) IconController.getController();
		final String[] iconNames = new String[] { "0%", "25%", "50%", "75%", "100%" };
		final MindIcon[] progressIcons = new MindIcon[] { new MindIcon(iconNames[0], iconNames[0] + ".png"),
		        new MindIcon(iconNames[1], iconNames[1] + ".png"), new MindIcon(iconNames[2], iconNames[2] + ".png"),
		        new MindIcon(iconNames[3], iconNames[3] + ".png"), new MindIcon(iconNames[4], iconNames[4] + ".png") };
		final MindIcon OKIcon = new MindIcon("OK", "button_ok.png");
		String ActiveIcon = null;
		boolean OKpresent = false;
		final List<MindIcon> icons = node.getIcons();
		//get active progress icon and remove it
		for (int i = 0; i < icons.size(); i++) {
			for (int j = 0; j < iconNames.length; j++) {
				if (icons.get(i).getName().equals("OK")) {
					OKpresent = true;
				}
				if (icons.get(i).getName().equals(iconNames[j])) {
					ActiveIcon = iconNames[j];
					Mic.removeIcon(node, i);
					i--;
					break;
				}
			}
		}
		// set initial progress icon always 0%
		if (ActiveIcon == null) {
			Mic.addIcon(node, progressIcons[0], 0);
		}
		else {
			final int iActiveIcon = Integer.parseInt(ActiveIcon.substring(0, ActiveIcon.length() - 1));
			//progress is increased
			if (up) {
				switch (iActiveIcon) {
					case 0:
						Mic.addIcon(node, progressIcons[1], 0);
						break;
					case 25:
						Mic.addIcon(node, progressIcons[2], 0);
						break;
					case 50:
						Mic.addIcon(node, progressIcons[3], 0);
						break;
					case 75:
						Mic.addIcon(node, progressIcons[4], 0);
						if (!OKpresent) {
							Mic.addIcon(node, OKIcon, 0);
						}
						break;
					//at 100% draw an extra OK-icon
					case 100:
						if (OKpresent) {
							Mic.addIcon(node, progressIcons[4], 1);
						}
						else {
							Mic.addIcon(node, progressIcons[4], 0);
							Mic.addIcon(node, OKIcon, 0);
						}
						break;
					default:
						break;
				}
			}
			//progress is decreased
			else {
				switch (iActiveIcon) {
					case 0:
						Mic.addIcon(node, progressIcons[0], 0);
						break;
					case 25:
						Mic.addIcon(node, progressIcons[0], 0);
						break;
					case 50:
						Mic.addIcon(node, progressIcons[1], 0);
						break;
					case 75:
						Mic.addIcon(node, progressIcons[2], 0);
						break;
					case 100:
						Mic.addIcon(node, progressIcons[3], 0);
						//remove OK icon
						if (OKpresent) {
							final List<MindIcon> changedIcons = node.getIcons();
							for (int i = 0; i < changedIcons.size(); i++) {
								if (changedIcons.get(i).getName().equals("OK")) {
									Mic.removeIcon(node, i);
								}
							}
						}
						break;
					default:
						break;
				}
			}
		}
	}
}
