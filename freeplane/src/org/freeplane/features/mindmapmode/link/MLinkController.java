/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2008 Joerg Mueller, Daniel Polansky, Christian Foltin, Dimitry Polivaev
 *
 *  This file is created by Dimitry Polivaev in 2008.
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
package org.freeplane.features.mindmapmode.link;

import java.awt.Color;
import java.awt.Point;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JLabel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JTextField;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.modecontroller.IMapChangeListener;
import org.freeplane.core.modecontroller.MapChangeEvent;
import org.freeplane.core.modecontroller.ModeController;
import org.freeplane.core.model.MapModel;
import org.freeplane.core.model.NodeModel;
import org.freeplane.core.resources.ResourceBundles;
import org.freeplane.core.undo.IActor;
import org.freeplane.features.common.link.ArrowLinkModel;
import org.freeplane.features.common.link.LinkController;
import org.freeplane.features.common.link.LinkModel;
import org.freeplane.features.common.link.MapLinks;
import org.freeplane.features.mindmapmode.MModeController;

/**
 * @author Dimitry Polivaev
 */
public class MLinkController extends LinkController {
	/**
	 * @author Dimitry Polivaev
	 */
	private final class NodeDeletionListener implements IMapChangeListener {
		public void mapChanged(final MapChangeEvent event) {
			// TODO Auto-generated method stub
		}

		public void onNodeDeleted(final NodeModel parent, final NodeModel child, final int index) {
		}

		public void onNodeInserted(final NodeModel parent, final NodeModel child, final int newIndex) {
		}

		public void onNodeMoved(final NodeModel oldParent, final int oldIndex, final NodeModel newParent,
		                        final NodeModel child, final int newIndex) {
		}

		public void onPreNodeDelete(final NodeModel oldParent, final NodeModel model, final int oldIndex) {
			final MapModel map = model.getMap();
			final MModeController modeController = (MModeController) getModeController();
			if (modeController.isUndoAction()) {
				return;
			}
			final MapLinks links = (MapLinks) map.getExtension(MapLinks.class);
			if (links == null) {
				return;
			}
			final String id = model.getID();
			final Set<LinkModel> linkModels = links.get(id);
			if (linkModels == null || linkModels.isEmpty()) {
				return;
			}
			final IActor actor = new IActor() {
				public void act() {
					links.set(id, Collections.EMPTY_SET);
				}

				public String getDescription() {
					return null;
				}

				public void undo() {
					links.set(id, linkModels);
				}
			};
			getModeController().execute(actor, map);
		}
	}

	static private ColorArrowLinkAction colorArrowLinkAction;
	static private Pattern mailPattern;
	static final Pattern nonLinkCharacter = Pattern.compile("[ \n()'\",;]");
	static private SetLinkByFileChooserAction setLinkByFileChooser;
	static private SetLinkByTextFieldAction setLinkByTextField;

	public MLinkController(final MModeController modeController) {
		super(modeController);
		createActions(modeController);
		(modeController.getMapController()).addMapChangeListener(new NodeDeletionListener());
	}

	public void addLink(final NodeModel source, final NodeModel target) {
		((AddArrowLinkAction) getModeController().getAction("AddArrowLinkAction")).addLink(source, target);
	}

	public void changeArrowsOfArrowLink(final ArrowLinkModel arrowLink, final boolean hasStartArrow,
	                                    final boolean hasEndArrow) {
		((ChangeArrowsInArrowLinkAction) getModeController().getAction("ChangeArrowsInArrowLinkAction"))
		    .changeArrowsOfArrowLink(arrowLink, hasStartArrow, hasEndArrow);
	}

	/**
	 *
	 */
	private void createActions(final ModeController modeController) {
		final Controller controller = modeController.getController();
		setLinkByFileChooser = new SetLinkByFileChooserAction(controller);
		modeController.addAction(setLinkByFileChooser);
		final AddArrowLinkAction addArrowLinkAction = new AddArrowLinkAction(controller);
		modeController.addAction(addArrowLinkAction);
		modeController.addAction(new RemoveArrowLinkAction(this, null));
		colorArrowLinkAction = new ColorArrowLinkAction(this, null);
		modeController.addAction(colorArrowLinkAction);
		modeController.addAction(new ChangeArrowsInArrowLinkAction(this, "none", null, true, true));
		setLinkByTextField = new SetLinkByTextFieldAction(controller);
		modeController.addAction(setLinkByTextField);
		modeController.addAction(new AddLocalLinkAction(controller));
		modeController.addAction(new ExtractLinkFromTextAction(controller));
	}

	@Override
	protected void createArrowLinkPopup(final ArrowLinkModel link, final JPopupMenu arrowLinkPopup) {
		super.createArrowLinkPopup(link, arrowLinkPopup);
		((RemoveArrowLinkAction) getModeController().getAction("RemoveArrowLinkAction")).setArrowLink(link);
		arrowLinkPopup.add(new RemoveArrowLinkAction(this, link));
		arrowLinkPopup.add(new ColorArrowLinkAction(this, link));
		arrowLinkPopup.addSeparator();
		arrowLinkPopup.add(new JLabel(ResourceBundles.getText("edit_source_label")));
		final JTextField sourceLabelEditor = new JTextField(link.getSourceLabel());
		arrowLinkPopup.add(sourceLabelEditor);
		arrowLinkPopup.addSeparator();
		arrowLinkPopup.add(new JLabel(ResourceBundles.getText("edit_middle_label")));
		final JTextField middleLabelEditor = new JTextField(link.getMiddleLabel());
		arrowLinkPopup.add(middleLabelEditor);
		arrowLinkPopup.addSeparator();
		arrowLinkPopup.add(new JLabel(ResourceBundles.getText("edit_target_label")));
		final JTextField targetLabelEditor = new JTextField(link.getTargetLabel());
		arrowLinkPopup.add(targetLabelEditor);
		arrowLinkPopup.addSeparator();
		arrowLinkPopup.addPopupMenuListener(new PopupMenuListener() {
			public void popupMenuCanceled(final PopupMenuEvent e) {
			}

			public void popupMenuWillBecomeInvisible(final PopupMenuEvent e) {
				setSourceLabel(link, sourceLabelEditor.getText());
				setMiddleLabel(link, middleLabelEditor.getText());
				setTargetLabel(link, targetLabelEditor.getText());
			}

			public void popupMenuWillBecomeVisible(final PopupMenuEvent e) {
			}
		});
		final boolean a = !link.getStartArrow().equals("None");
		final boolean b = !link.getEndArrow().equals("None");
		final JRadioButtonMenuItem itemnn = new JRadioButtonMenuItem(new ChangeArrowsInArrowLinkAction(this, "none",
		    link, false, false));
		arrowLinkPopup.add(itemnn);
		itemnn.setSelected(!a && !b);
		final JRadioButtonMenuItem itemnt = new JRadioButtonMenuItem(new ChangeArrowsInArrowLinkAction(this, "forward",
		    link, false, true));
		arrowLinkPopup.add(itemnt);
		itemnt.setSelected(!a && b);
		final JRadioButtonMenuItem itemtn = new JRadioButtonMenuItem(new ChangeArrowsInArrowLinkAction(this,
		    "backward", link, true, false));
		arrowLinkPopup.add(itemtn);
		itemtn.setSelected(a && !b);
		final JRadioButtonMenuItem itemtt = new JRadioButtonMenuItem(new ChangeArrowsInArrowLinkAction(this, "both",
		    link, true, true));
		arrowLinkPopup.add(itemtt);
		itemtt.setSelected(a && b);
	}

	private void createMailPattern() {
		if (mailPattern == null) {
			mailPattern = Pattern.compile("([^@ <>\\*']+@[^@ <>\\*']+)");
		}
	}

	public String findLink(final String text) {
		createMailPattern();
		final Matcher mailMatcher = mailPattern.matcher(text);
		String link = null;
		final String[] linkPrefixes = { "http://", "ftp://", "https://" };
		for (int j = 0; j < linkPrefixes.length; j++) {
			final int linkStart = text.indexOf(linkPrefixes[j]);
			if (linkStart != -1) {
				int linkEnd = linkStart;
				while (linkEnd < text.length()
				        && !nonLinkCharacter.matcher(text.substring(linkEnd, linkEnd + 1)).matches()) {
					linkEnd++;
				}
				link = text.substring(linkStart, linkEnd);
			}
		}
		if (link == null && mailMatcher.find()) {
			link = "mailto:" + mailMatcher.group();
		}
		return link;
	}

	public void setArrowLinkColor(final ArrowLinkModel arrowLink, final Color color) {
		colorArrowLinkAction.setArrowLinkColor(arrowLink, color);
	}

	public void setArrowLinkEndPoints(final ArrowLinkModel link, final Point startPoint, final Point endPoint) {
		final IActor actor = new IActor() {
			final private Point oldEndPoint = link.getEndInclination();
			final private Point oldStartPoint = link.getStartInclination();

			public void act() {
				link.setStartInclination(startPoint);
				link.setEndInclination(endPoint);
				getModeController().getMapController().nodeChanged(link.getSource());
			}

			public String getDescription() {
				return "setArrowLinkEndPoints";
			}

			public void undo() {
				link.setStartInclination(oldStartPoint);
				link.setEndInclination(oldEndPoint);
				getModeController().getMapController().nodeChanged(link.getSource());
			}
		};
		getModeController().execute(actor, link.getSource().getMap());
	}

	public void setLink(final NodeModel node, final String link) {
		if (link != null) {
			try {
				final URI uri = new URI(link);
				setLink(node, uri);
			}
			catch (final URISyntaxException e) {
				e.printStackTrace();
			}
			return;
		}
		setLinkByTextField.setLink(node, null);
	}

	public void setLink(final NodeModel node, final URI uri) {
		setLinkByTextField.setLink(node, uri);
	}

	public void setLinkByFileChooser() {
		setLinkByFileChooser.setLinkByFileChooser();
	}

	public void setMiddleLabel(final ArrowLinkModel model, final String label) {
		final String oldLabel = model.getMiddleLabel();
		if (label == oldLabel || label != null && label.equals(oldLabel)) {
			return;
		}
		final IActor actor = new IActor() {
			public void act() {
				model.setMiddleLabel(label);
				getModeController().getMapController().nodeChanged(model.getSource());
			}

			public String getDescription() {
				return "setMiddleLabel";
			}

			public void undo() {
				model.setMiddleLabel(oldLabel);
				getModeController().getMapController().nodeChanged(model.getSource());
			}
		};
		getModeController().execute(actor, model.getSource().getMap());
	}

	public void setSourceLabel(final ArrowLinkModel model, final String label) {
		final String oldLabel = model.getSourceLabel();
		if (label == oldLabel || label != null && label.equals(oldLabel)) {
			return;
		}
		final IActor actor = new IActor() {
			public void act() {
				model.setSourceLabel(label);
				getModeController().getMapController().nodeChanged(model.getSource());
			}

			public String getDescription() {
				return "setSourceLabel";
			}

			public void undo() {
				model.setSourceLabel(oldLabel);
				getModeController().getMapController().nodeChanged(model.getSource());
			}
		};
		getModeController().execute(actor, model.getSource().getMap());
	}

	public void setTargetLabel(final ArrowLinkModel model, final String label) {
		final String oldLabel = model.getTargetLabel();
		if (label == oldLabel || label != null && label.equals(oldLabel)) {
			return;
		}
		final IActor actor = new IActor() {
			public void act() {
				model.setTargetLabel(label);
				getModeController().getMapController().nodeChanged(model.getSource());
			}

			public String getDescription() {
				return "setTargetLabel";
			}

			public void undo() {
				model.setTargetLabel(oldLabel);
				getModeController().getMapController().nodeChanged(model.getSource());
			}
		};
		getModeController().execute(actor, model.getSource().getMap());
	}
}
