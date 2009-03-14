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
import java.util.Collections;
import java.util.Set;

import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.modecontroller.IMapChangeListener;
import org.freeplane.core.modecontroller.MapChangeEvent;
import org.freeplane.core.modecontroller.ModeController;
import org.freeplane.core.model.MapModel;
import org.freeplane.core.model.NodeModel;
import org.freeplane.core.undo.IUndoableActor;
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
			final IUndoableActor actor = new IUndoableActor() {
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
			getModeController().execute(actor);
		}

		public void mapChanged(MapChangeEvent event) {
	        // TODO Auto-generated method stub
	        
        }
	}

	static private ColorArrowLinkAction colorArrowLinkAction;
	static private SetLinkByFileChooserAction setLinkByFileChooser;
	static private SetLinkByTextFieldAction setLinkByTextField;

	public MLinkController(final MModeController modeController) {
		super(modeController);
		createActions(modeController);
		(modeController.getMapController()).addMapChangeListener(new NodeDeletionListener());
	}

	public void addLink(final NodeModel source, final NodeModel target) {
		((AddArrowLinkAction) getModeController().getAction("addArrowLinkAction")).addLink(source, target);
	}

	public void changeArrowsOfArrowLink(final ArrowLinkModel arrowLink, final boolean hasStartArrow,
	                                    final boolean hasEndArrow) {
		((ChangeArrowsInArrowLinkAction) getModeController().getAction("changeArrowsInArrowLinkAction"))
		    .changeArrowsOfArrowLink(arrowLink, hasStartArrow, hasEndArrow);
	}

	/**
	 *
	 */
	private void createActions(final ModeController modeController) {
		final Controller controller = modeController.getController();
		setLinkByFileChooser = new SetLinkByFileChooserAction(controller);
		modeController.addAction("setLinkByFileChooser", setLinkByFileChooser);
		final AddArrowLinkAction addArrowLinkAction = new AddArrowLinkAction(controller);
		modeController.addAction("addArrowLinkAction", addArrowLinkAction);
		modeController.addAction("removeArrowLinkAction", new RemoveArrowLinkAction(this, null));
		colorArrowLinkAction = new ColorArrowLinkAction(this, null);
		modeController.addAction("colorArrowLinkAction", colorArrowLinkAction);
		modeController.addAction("changeArrowsInArrowLinkAction", new ChangeArrowsInArrowLinkAction(this, "none", null,
		    null, true, true));
		setLinkByTextField = new SetLinkByTextFieldAction(controller);
		modeController.addAction("setLinkByTextField", setLinkByTextField);
		modeController.addAction("addLocalLinkAction", new AddLocalLinkAction(controller));
	}

	@Override
	protected void createArrowLinkPopup(final ArrowLinkModel link, final JPopupMenu arrowLinkPopup) {
		super.createArrowLinkPopup(link, arrowLinkPopup);
		((RemoveArrowLinkAction) getModeController().getAction("removeArrowLinkAction")).setArrowLink(link);
		arrowLinkPopup.add(new RemoveArrowLinkAction(this, link));
		arrowLinkPopup.add(new ColorArrowLinkAction(this, link));
		arrowLinkPopup.addSeparator();
		final boolean a = !link.getStartArrow().equals("None");
		final boolean b = !link.getEndArrow().equals("None");
		final JRadioButtonMenuItem itemnn = new JRadioButtonMenuItem(new ChangeArrowsInArrowLinkAction(this, "none",
		    "/images/arrow-mode-none.png", link, false, false));
		itemnn.setText(null);
		arrowLinkPopup.add(itemnn);
		itemnn.setSelected(!a && !b);
		final JRadioButtonMenuItem itemnt = new JRadioButtonMenuItem(new ChangeArrowsInArrowLinkAction(this, "forward",
		    "/images/arrow-mode-forward.png", link, false, true));
		itemnt.setText(null);
		arrowLinkPopup.add(itemnt);
		itemnt.setSelected(!a && b);
		final JRadioButtonMenuItem itemtn = new JRadioButtonMenuItem(new ChangeArrowsInArrowLinkAction(this,
		    "backward", "/images/arrow-mode-backward.png", link, true, false));
		itemtn.setText(null);
		arrowLinkPopup.add(itemtn);
		itemtn.setSelected(a && !b);
		final JRadioButtonMenuItem itemtt = new JRadioButtonMenuItem(new ChangeArrowsInArrowLinkAction(this, "both",
		    "/images/arrow-mode-both.png", link, true, true));
		itemtt.setText(null);
		arrowLinkPopup.add(itemtt);
		itemtt.setSelected(a && b);
	}

	public void setArrowLinkColor(final ArrowLinkModel arrowLink, final Color color) {
		colorArrowLinkAction.setArrowLinkColor(arrowLink, color);
	}

	public void setArrowLinkEndPoints(final ArrowLinkModel link, final Point startPoint, final Point endPoint) {
		final IUndoableActor actor = new IUndoableActor() {
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
		getModeController().execute(actor);
	}

	public void setLink(final NodeModel node, final String link) {
		setLinkByTextField.setLink(node, link);
	}

	public void setLinkByFileChooser() {
		setLinkByFileChooser.setLinkByFileChooser();
	}
}
