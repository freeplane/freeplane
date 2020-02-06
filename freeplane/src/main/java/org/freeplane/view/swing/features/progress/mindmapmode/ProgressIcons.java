package org.freeplane.view.swing.features.progress.mindmapmode;

import java.util.List;

import org.freeplane.features.icon.IconController;
import org.freeplane.features.icon.MindIcon;
import org.freeplane.features.icon.mindmapmode.MIconController;
import org.freeplane.features.map.NodeModel;

/**
 * @author Stefan Ott
 * 
 * This class holds the static method to update the progress icons
 */
public class ProgressIcons {
	public static final String EXTENDED_PROGRESS_ICON_IDENTIFIER = ".*[Pp]rogress_(tenth|quarter)_[0-9]{2}\\.[a-zA-Z0-9]*";
	private final static String[] iconNames = new String[] { "0%", "25%", "50%", "75%", "100%" };
	private final static MindIcon[] progressIcons = new MindIcon[] { new MindIcon(iconNames[0], iconNames[0] + ".png"),
	        new MindIcon(iconNames[1], iconNames[1] + ".png"), new MindIcon(iconNames[2], iconNames[2] + ".png"),
	        new MindIcon(iconNames[3], iconNames[3] + ".png"), new MindIcon(iconNames[4], iconNames[4] + ".png") };
	private final static MindIcon OKIcon = new MindIcon("button_ok", "button_ok.png");

	/**
	 * This method increases/ decreases the progress icons.
	 * If none is present then the 0% icon is set.
	 * At 100% the OK-icon is additionally added
	 * 
	 * @param node : the node to update the icons
	 * @param up : true if the progress is increased (0% -> 25% -> 50%...)
	 * 				if false the progress is decreased
	 */
	public static void updateProgressIcons(final NodeModel node, final boolean up) {
		final ProgressUtilities progUtil = new ProgressUtilities();
		final MIconController iconController = (MIconController) IconController.getController();
		String activeIcon = null;
		final List<MindIcon> icons = node.getIcons();
		//get active progress icon and remove it
		if (progUtil.hasProgressIcons(node)) {
			for (int i = 0; i < icons.size(); i++) {
				for (int j = 0; j < iconNames.length; j++) {
					if (icons.get(i).getName().equals(iconNames[j])) {
						activeIcon = iconNames[j];
						break;
					}
				}
			}
			ProgressIcons.removeProgressIcons(node);
		}
		// set initial progress icon always 0%
		if (activeIcon == null) {
			ProgressIcons.removeProgressIcons(node);
			iconController.addIcon(node, progressIcons[0], 0);
		}
		else {
			final int iActiveIcon = Integer.parseInt(activeIcon.substring(0, activeIcon.length() - 1));
			//progress is increased
			if (up) {
				switch (iActiveIcon) {
					case 0:
						iconController.addIcon(node, progressIcons[1], 0);
						break;
					case 25:
						iconController.addIcon(node, progressIcons[2], 0);
						break;
					case 50:
						iconController.addIcon(node, progressIcons[3], 0);
						break;
					case 75:
						iconController.addIcon(node, progressIcons[4], 0);
						if (!progUtil.hasOKIcon(node)) {
							iconController.addIcon(node, OKIcon, 0);
						}
						break;
					//at 100% draw an extra OK-icon
					case 100:
						iconController.addIcon(node, progressIcons[4], 0);
						iconController.addIcon(node, OKIcon, 0);
						break;
					default:
						break;
				}
			}
			//progress is decreased
			else {
				switch (iActiveIcon) {
					case 25:
						iconController.addIcon(node, progressIcons[0], 0);
						break;
					case 50:
						iconController.addIcon(node, progressIcons[1], 0);
						break;
					case 75:
						iconController.addIcon(node, progressIcons[2], 0);
						break;
					case 100:
						iconController.addIcon(node, progressIcons[3], 0);
						break;
					case 0:
					default:
						break;
				}
			}
		}
	}

	/**
	 * This method updates the progress icons dependent of the added external object (svg file)
	 * The file has a distinct naming scheme from which the progress and the icons to be painted 
	 * are derived.
	 * 
	 * @param node : the node to update the icons
	 * @param sFile : the name of the added file.
	 */
	public static void updateExtendedProgressIcons(final NodeModel node, final String sFile) {
		if (sFile.matches(EXTENDED_PROGRESS_ICON_IDENTIFIER)) {
			final MIconController iconController = (MIconController) IconController.getController();
			ProgressIcons.removeProgressIcons(node);
			//add the right progress icon
			if (sFile.matches(".*_quarter_.*")) {
				final int fileNum = Integer.parseInt(sFile.substring(sFile.lastIndexOf("_") + 1,
				    sFile.lastIndexOf("_") + 3));
				switch (fileNum) {
					case 0:
						iconController.addIcon(node, progressIcons[0], 0);
						break;
					case 1:
						iconController.addIcon(node, progressIcons[1], 0);
						break;
					case 2:
						iconController.addIcon(node, progressIcons[2], 0);
						break;
					case 3:
						iconController.addIcon(node, progressIcons[3], 0);
						break;
					case 4:
						iconController.addIcon(node, progressIcons[4], 0);
						iconController.addIcon(node, OKIcon, 0);
						break;
					default:
						iconController.addIcon(node, progressIcons[0], 0);
						break;
				}
			}
			else if (sFile.matches(".*_tenth_.*")) {
				final int fileNum = Integer.parseInt(sFile.substring(sFile.lastIndexOf("_") + 1,
				    sFile.lastIndexOf("_") + 3));
				switch (fileNum) {
					case 0:
					case 1:
						iconController.addIcon(node, progressIcons[0], 0);
						break;
					case 2:
					case 3:
						iconController.addIcon(node, progressIcons[1], 0);
						break;
					case 4:
					case 5:
					case 6:
						iconController.addIcon(node, progressIcons[2], 0);
						break;
					case 7:
					case 8:
					case 9:
						iconController.addIcon(node, progressIcons[3], 0);
						break;
					case 10:
						iconController.addIcon(node, progressIcons[4], 0);
						iconController.addIcon(node, OKIcon, 0);
						break;
					default:
						iconController.addIcon(node, progressIcons[0], 0);
						break;
				}
			}
		}
	}

	/**
	 * Removes the progress icons (0%, 25%, 50%, 75%, 100%) from the node
	 * 
	 * @param node : the node from which the progress icons are removed
	 */
	public static void removeProgressIcons(final NodeModel node) {
		final ProgressUtilities progUtil = new ProgressUtilities();
		if (progUtil.hasProgressIcons(node) || progUtil.hasOKIcon(node)) {
			final MIconController iconController = (MIconController) IconController.getController();
			final String[] progressIconNames = new String[] { "0%", "25%", "50%", "75%", "100%", "button_ok" };
			final List<MindIcon> icons = node.getIcons();
			//	remove progress icons
			for (int i = 0; i < icons.size(); i++) {
				String iconName = icons.get(i).getName();
				for (int j = 0; j < progressIconNames.length; j++) {
					if (iconName.equals(progressIconNames[j])) {
						iconController.removeIcon(node, i);
						i--;
						break;
					}
				}
			}
		}
	}
}
