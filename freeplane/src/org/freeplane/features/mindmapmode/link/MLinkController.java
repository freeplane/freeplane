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
import java.awt.EventQueue;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.ui.components.JAutoRadioButtonMenuItem;
import org.freeplane.core.undo.IActor;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.common.link.ArrowType;
import org.freeplane.features.common.link.ConnectorModel;
import org.freeplane.features.common.link.ConnectorModel.Shape;
import org.freeplane.features.common.link.HyperTextLinkModel;
import org.freeplane.features.common.link.LinkController;
import org.freeplane.features.common.link.LinkModel;
import org.freeplane.features.common.link.MapLinks;
import org.freeplane.features.common.link.NodeLinkModel;
import org.freeplane.features.common.link.NodeLinks;
import org.freeplane.features.common.map.IMapChangeListener;
import org.freeplane.features.common.map.MapChangeEvent;
import org.freeplane.features.common.map.MapModel;
import org.freeplane.features.common.map.ModeController;
import org.freeplane.features.common.map.NodeModel;
import org.freeplane.features.mindmapmode.MModeController;

/**
 * @author Dimitry Polivaev
 */
public class MLinkController extends LinkController {
	private final class CreateArrowLinkActor implements IActor {
		private final String targetID;
		private final NodeModel source;
		private ConnectorModel arrowLink;

		public ConnectorModel getArrowLink() {
			return arrowLink;
		}

		private CreateArrowLinkActor(final String targetID, final NodeModel source) {
			this.targetID = targetID;
			this.source = source;
		}

		public void act() {
			NodeLinks nodeLinks = (NodeLinks) source.getExtension(NodeLinks.class);
			if (nodeLinks == null) {
				nodeLinks = new NodeLinks();
				source.addExtension(nodeLinks);
			}
			arrowLink = new ConnectorModel(source, targetID, 
				getStandardConnectorColor(), getStandardConnectorAlpha(),
				getStandardConnectorShape(), getStandardConnectorWidth());
			nodeLinks.addArrowlink(arrowLink);
			Controller.getCurrentModeController().getMapController().nodeChanged(source);
		}

		public String getDescription() {
			return "addLink";
		}

		public void undo() {
			final NodeLinks nodeLinks = (NodeLinks) source.getExtension(NodeLinks.class);
			nodeLinks.removeArrowlink(arrowLink);
			Controller.getCurrentModeController().getMapController().nodeChanged(source);
		}
	}

	private final class TargetLabelSetter implements IActor {
		private final String oldLabel;
		private final String label;
		private final ConnectorModel model;

		private TargetLabelSetter(final String oldLabel, final String label, final ConnectorModel model) {
			this.oldLabel = oldLabel;
			this.label = label;
			this.model = model;
		}

		public void act() {
			model.setTargetLabel(label);
			Controller.getCurrentModeController().getMapController().nodeChanged(model.getSource());
		}

		public String getDescription() {
			return "setTargetLabel";
		}

		public void undo() {
			model.setTargetLabel(oldLabel);
			Controller.getCurrentModeController().getMapController().nodeChanged(model.getSource());
		}
	}

	private final class SourceLabelSetter implements IActor {
		private final ConnectorModel model;
		private final String label;
		private final String oldLabel;

		private SourceLabelSetter(final ConnectorModel model, final String label, final String oldLabel) {
			this.model = model;
			this.label = label;
			this.oldLabel = oldLabel;
		}

		public void act() {
			model.setSourceLabel(label);
			Controller.getCurrentModeController().getMapController().nodeChanged(model.getSource());
		}

		public String getDescription() {
			return "setSourceLabel";
		}

		public void undo() {
			model.setSourceLabel(oldLabel);
			Controller.getCurrentModeController().getMapController().nodeChanged(model.getSource());
		}
	}

	private final class MiddleLabelSetter implements IActor {
		private final ConnectorModel model;
		private final String oldLabel;
		private final String label;

		private MiddleLabelSetter(final ConnectorModel model, final String oldLabel, final String label) {
			this.model = model;
			this.oldLabel = oldLabel;
			this.label = label;
		}

		public void act() {
			model.setMiddleLabel(label);
			Controller.getCurrentModeController().getMapController().nodeChanged(model.getSource());
		}

		public String getDescription() {
			return "setMiddleLabel";
		}

		public void undo() {
			model.setMiddleLabel(oldLabel);
			Controller.getCurrentModeController().getMapController().nodeChanged(model.getSource());
		}
	}

	private final class PopupEditorKeyListener implements KeyListener {
		private final JPopupMenu arrowLinkPopup;
		private boolean canceled = false;

		private PopupEditorKeyListener(final JPopupMenu arrowLinkPopup) {
			this.arrowLinkPopup = arrowLinkPopup;
		}

		public void keyPressed(final KeyEvent e) {
			if (e.getKeyCode() == KeyEvent.VK_ENTER) {
				arrowLinkPopup.setVisible(false);
				e.consume();
			}
			if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
				canceled = true;
			}
		}

		protected boolean isCanceled() {
			return canceled;
		}

		public void keyReleased(final KeyEvent e) {
		}

		public void keyTyped(final KeyEvent e) {
		}
	}

	/**
	 * @author Dimitry Polivaev
	 */
	private final class NodeDeletionListener implements IMapChangeListener {
		public void mapChanged(final MapChangeEvent event) {
		}

		public void onNodeDeleted(final NodeModel parent, final NodeModel child, final int index) {
		}

		public void onNodeInserted(final NodeModel parent, final NodeModel child, final int newIndex) {
			if (((MModeController) Controller.getCurrentModeController()).isUndoAction()) {
				return;
			}
			EventQueue.invokeLater(new Runnable() {
				public void run() {
					onChange(child, false);
				}
			});
		}

		public void onNodeMoved(final NodeModel oldParent, final int oldIndex, final NodeModel newParent,
		                        final NodeModel child, final int newIndex) {
		}

		public void onPreNodeDelete(final NodeModel oldParent, final NodeModel model, final int oldIndex) {
			onChange(model, true);
		}

		private void onChange(final NodeModel model, final boolean delete) {
			if (((MModeController) Controller.getCurrentModeController()).isUndoAction()) {
				return;
			}
			final MapModel map = model.getMap();
			final MapLinks links = (MapLinks) map.getExtension(MapLinks.class);
			if (links == null) {
				return;
			}
			removeLinksForDeletedSource(links, model, delete);
			removeLinksForDeletedTarget(links, model);
		}

		private void removeLinksForDeletedSource(final MapLinks links, final NodeModel model, final boolean delete) {
			final List<NodeModel> children = model.getChildren();
			for (final NodeModel child : children) {
				removeLinksForDeletedSource(links, child, delete);
			}
			final NodeLinks nodeLinks = NodeLinks.getLinkExtension(model);
			if (nodeLinks == null) {
				return;
			}
			for (final LinkModel link : nodeLinks.getLinks()) {
				if (!(link instanceof NodeLinkModel)) {
					continue;
				}
				final IActor actor = new IActor() {
					public void act() {
						if (delete) {
							delete();
						}
						else {
							insert();
						}
					}

					public void undo() {
						if (delete) {
							insert();
						}
						else {
							delete();
						}
					}

					private void delete() {
						links.remove(link);
					}

					public String getDescription() {
						return null;
					}

					private void insert() {
						links.add(link);
					}
				};
				final MapModel map = model.getMap();
				Controller.getCurrentModeController().execute(actor, map);
			}
		}

		private void removeLinksForDeletedTarget(final MapLinks links, final NodeModel model) {
			final List<NodeModel> children = model.getChildren();
			for (final NodeModel child : children) {
				removeLinksForDeletedTarget(links, child);
			}
			final String id = model.getID();
			if (id == null) {
				return;
			}
			final Set<LinkModel> linkModels = links.get(id);
			if (linkModels == null || linkModels.isEmpty()) {
				return;
			}
			final IActor actor = new IActor() {
				public void act() {
					refresh();
				}

				public void undo() {
					refresh();
				}

				private void refresh() {
					for (final LinkModel link : linkModels) {
						if (link instanceof HyperTextLinkModel) {
							final NodeModel source = ((HyperTextLinkModel) link).getSource();
							Controller.getCurrentModeController().getMapController().delayedNodeRefresh(source, NodeModel.NODE_ICON,
							    null, null);
						}
					}
				}

				public String getDescription() {
					return null;
				}
			};
			final MapModel map = model.getMap();
			Controller.getCurrentModeController().execute(actor, map);
		}

		public void onPreNodeMoved(final NodeModel oldParent, final int oldIndex, final NodeModel newParent,
		                           final NodeModel child, final int newIndex) {
		}
	}

	static private ConnectorColorAction colorArrowLinkAction;
	static private SetLinkByFileChooserAction setLinkByFileChooser;
	static private SetLinkByTextFieldAction setLinkByTextField;

	public MLinkController() {
		super();
		createActions();
		final ModeController modeController = Controller.getCurrentModeController();
		(modeController.getMapController()).addMapChangeListener(new NodeDeletionListener());
	}

	public ConnectorModel addConnector(final NodeModel source, final NodeModel target) {
		return addConnector(source, target.createID());
	}

	public void changeArrowsOfArrowLink(final ConnectorModel link, final ArrowType startArrow, final ArrowType endArrow) {
		final IActor actor = new IActor() {
			final private ArrowType oldEndArrow = link.getEndArrow();
			final private ArrowType oldStartArrow = link.getStartArrow();

			public void act() {
				link.setStartArrow(startArrow);
				link.setEndArrow(endArrow);
				Controller.getCurrentModeController().getMapController().nodeChanged(link.getSource());
			}

			public String getDescription() {
				return "changeArrowsOfArrowLink";
			}

			public void undo() {
				link.setStartArrow(oldStartArrow);
				link.setEndArrow(oldEndArrow);
				Controller.getCurrentModeController().getMapController().nodeChanged(link.getSource());
			}
		};
		Controller.getCurrentModeController().execute(actor, link.getSource().getMap());
	}

	/**
	 *
	 */
	private void createActions() {
		final ModeController modeController = Controller.getCurrentModeController();
		setLinkByFileChooser = new SetLinkByFileChooserAction();
		modeController.addAction(setLinkByFileChooser);
		final AddConnectorAction addArrowLinkAction = new AddConnectorAction();
		modeController.addAction(addArrowLinkAction);
		setLinkByTextField = new SetLinkByTextFieldAction();
		modeController.addAction(setLinkByTextField);
		modeController.addAction(new AddLocalLinkAction());
		modeController.addAction(new AddMenuItemLinkAction());
		modeController.addAction(new ExtractLinkFromTextAction());
	}

	@Override
	protected void createArrowLinkPopup(final ConnectorModel link, final JPopupMenu arrowLinkPopup) {
		super.createArrowLinkPopup(link, arrowLinkPopup);
		arrowLinkPopup.add(new RemoveConnectorAction(this, link));
		
		arrowLinkPopup.addSeparator();
		arrowLinkPopup.add(new ConnectorColorAction(this, link));

		final JSlider transparencySlider = new JSlider(0, 255, link.getAlpha());
		addPopupComponent(arrowLinkPopup, TextUtils.getText("edit_transparency_label"), transparencySlider);
		
		arrowLinkPopup.addSeparator();

		final JMenu connectorArrows = new JMenu(TextUtils.getText("connector_arrows"));
		final ButtonGroup arrowsGroup = new ButtonGroup();
		
		final ChangeConnectorArrowsAction actionNN = new ChangeConnectorArrowsAction(this, "none", link,
		    ArrowType.NONE, ArrowType.NONE);
		final JRadioButtonMenuItem itemnn = new JAutoRadioButtonMenuItem(actionNN);
		connectorArrows.add(itemnn);
		arrowsGroup.add(itemnn);
		
		final ChangeConnectorArrowsAction actionNT = new ChangeConnectorArrowsAction(this, "forward", link,
		    ArrowType.NONE, ArrowType.DEFAULT);
		final JRadioButtonMenuItem itemnt = new JAutoRadioButtonMenuItem(actionNT);
		connectorArrows.add(itemnt);
		arrowsGroup.add(itemnt);
		
		final ChangeConnectorArrowsAction actionTN = new ChangeConnectorArrowsAction(this, "backward", link,
		    ArrowType.DEFAULT, ArrowType.NONE);
		final JRadioButtonMenuItem itemtn = new JAutoRadioButtonMenuItem(actionTN);
		connectorArrows.add(itemtn);
		arrowsGroup.add(itemtn);
		
		final ChangeConnectorArrowsAction actionTT = new ChangeConnectorArrowsAction(this, "both", link,
		    ArrowType.DEFAULT, ArrowType.DEFAULT);
		final JRadioButtonMenuItem itemtt = new JAutoRadioButtonMenuItem(actionTT);
		connectorArrows.add(itemtt);
		arrowsGroup.add(itemtt);
		
		arrowLinkPopup.add(connectorArrows);
		
		final JMenu connectorShapes = new JMenu(TextUtils.getText("connector_shapes"));
		final ButtonGroup shapeGroup = new ButtonGroup();
		
		final ChangeConnectorShapeAction actionCubic = new ChangeConnectorShapeAction(this, link,Shape.CUBIC_CURVE);
		final JRadioButtonMenuItem itemCubic = new JAutoRadioButtonMenuItem(actionCubic);
		connectorShapes.add(itemCubic);
		shapeGroup.add(itemCubic);
		
		final ChangeConnectorShapeAction actionLinear = new ChangeConnectorShapeAction(this, link,Shape.LINE);
		final JRadioButtonMenuItem itemLinear = new JAutoRadioButtonMenuItem(actionLinear);
		connectorShapes.add(itemLinear);
		shapeGroup.add(itemLinear);
		
		final ChangeConnectorShapeAction actionLinearPath = new ChangeConnectorShapeAction(this, link,Shape.LINEAR_PATH);
		final JRadioButtonMenuItem itemLinearPath = new JAutoRadioButtonMenuItem(actionLinearPath);
		connectorShapes.add(itemLinearPath);
		shapeGroup.add(itemLinearPath);
		
		final ChangeConnectorShapeAction actionEdgeLike = new ChangeConnectorShapeAction(this, link,Shape.EDGE_LIKE);
		final JRadioButtonMenuItem itemEdgeLike = new JAutoRadioButtonMenuItem(actionEdgeLike);
		connectorShapes.add(itemEdgeLike);
		shapeGroup.add(itemEdgeLike);
		
		arrowLinkPopup.add(connectorShapes);
	
		final JMenu connectorDashes = new JMenu(TextUtils.getText("connector_lines"));

		final ChangeConnectorDashAction actionD1 = new ChangeConnectorDashAction(this, link, null); 
		final JRadioButtonMenuItem itemD1 = new JAutoRadioButtonMenuItem(actionD1);
		connectorDashes.add(itemD1);

		final ChangeConnectorDashAction actionD2 = new ChangeConnectorDashAction(this, link, new int[]{3, 3}); 
		final JRadioButtonMenuItem itemD2 = new JAutoRadioButtonMenuItem(actionD2);
		connectorDashes.add(itemD2);

		final ChangeConnectorDashAction actionD3 = new ChangeConnectorDashAction(this, link, new int[]{7, 7}); 
		final JRadioButtonMenuItem itemD3 = new JAutoRadioButtonMenuItem(actionD3);
		connectorDashes.add(itemD3);

		final ChangeConnectorDashAction actionD4 = new ChangeConnectorDashAction(this, link, new int[]{2, 7}); 
		final JRadioButtonMenuItem itemD4 = new JAutoRadioButtonMenuItem(actionD4);
		connectorDashes.add(itemD4);

		final ChangeConnectorDashAction actionD5 = new ChangeConnectorDashAction(this, link, new int[]{2, 7, 7, 7}); 
		final JRadioButtonMenuItem itemD5 = new JAutoRadioButtonMenuItem(actionD5);
		connectorDashes.add(itemD5);

		arrowLinkPopup.add(connectorDashes);

		final SpinnerNumberModel spinnerNumberModel = new SpinnerNumberModel(link.getWidth(),1, 32, 1);
		final JSpinner widthSpinner = new JSpinner(spinnerNumberModel);
		addPopupComponent(arrowLinkPopup, TextUtils.getText("edit_width_label"), widthSpinner);
		
		arrowLinkPopup.addSeparator();

		final PopupEditorKeyListener enterListener = new PopupEditorKeyListener(arrowLinkPopup);
		final JTextField sourceLabelEditor = new JTextField(link.getSourceLabel());
		sourceLabelEditor.addKeyListener(enterListener);
		addPopupComponent(arrowLinkPopup, TextUtils.getText("edit_source_label"), sourceLabelEditor);

		final JTextField middleLabelEditor = new JTextField(link.getMiddleLabel());
		middleLabelEditor.addKeyListener(enterListener);
		addPopupComponent(arrowLinkPopup, TextUtils.getText("edit_middle_label"), middleLabelEditor);

		final JTextField targetLabelEditor = new JTextField(link.getTargetLabel());
		targetLabelEditor.addKeyListener(enterListener);
		addPopupComponent(arrowLinkPopup, TextUtils.getText("edit_target_label"), targetLabelEditor);
		
		arrowLinkPopup.addPopupMenuListener(new PopupMenuListener() {
			public void popupMenuCanceled(final PopupMenuEvent e) {
			}

			public void popupMenuWillBecomeInvisible(final PopupMenuEvent e) {
				if (enterListener.isCanceled()) {
					return;
				}
				setSourceLabel(link, sourceLabelEditor.getText());
				setMiddleLabel(link, middleLabelEditor.getText());
				setTargetLabel(link, targetLabelEditor.getText());
				setAlpha(link, transparencySlider.getValue());
				setWidth(link, spinnerNumberModel.getNumber().intValue());
			}

			public void popupMenuWillBecomeVisible(final PopupMenuEvent e) {
			}
		});
		
	}

	private void addPopupComponent(final JPopupMenu arrowLinkPopup, final String label, final JComponent component) {
	    final Box componentBox = Box.createHorizontalBox();
		componentBox.add(Box.createHorizontalStrut(20));
		componentBox.add(new JLabel(label));
		componentBox.add(Box.createHorizontalStrut(10));
		componentBox.add(component);
		arrowLinkPopup.add(componentBox);
    }

	static final private Pattern urlPattern = Pattern.compile("file://[^\\s\"'<>]+|(:?https?|ftp)://[^\\s()'\",;|<>{}]+");
	static private Pattern mailPattern = Pattern.compile("([!+\\-/=~.\\w#]+@[\\w.\\-+?&=%]+)");

	public String findLink(final String text) {
		final Matcher urlMatcher = urlPattern.matcher(text);
		if (urlMatcher.find()) {
			String link = urlMatcher.group();
			try {
				link = new URL(link).toURI().toString();
				return link;
			}
			catch (final MalformedURLException e) {
				return null;
			}
			catch (final URISyntaxException e) {
				return null;
			}
		}
		final Matcher mailMatcher = mailPattern.matcher(text);
		if (mailMatcher.find()) {
			final String link = "mailto:" + mailMatcher.group();
			return link;
		}
		return null;
	}

	public void setConnectorColor(final ConnectorModel arrowLink, final Color color) {
		final Color oldColor = arrowLink.getColor();
		if (color == oldColor || color != null && color.equals(oldColor)) {
			return;
		}
		final IActor actor = new IActor() {
			public void act() {
				arrowLink.setColor(color);
				final NodeModel node = arrowLink.getSource();
				Controller.getCurrentModeController().getMapController().nodeChanged(node);
			}

			public String getDescription() {
				return "setConnectorColor";
			}

			public void undo() {
				arrowLink.setColor(oldColor);
				final NodeModel node = arrowLink.getSource();
				Controller.getCurrentModeController().getMapController().nodeChanged(node);
			}
		};
		Controller.getCurrentModeController().execute(actor, arrowLink.getSource().getMap());
	}

	public void setConnectorDash(final ConnectorModel arrowLink, final int[] dash) {
		final int[] oldDash = arrowLink.getDash();
		if (dash == oldDash || dash != null && dash.equals(oldDash)) {
			return;
		}
		final IActor actor = new IActor() {
			public void act() {
				arrowLink.setDash(dash);
				final NodeModel node = arrowLink.getSource();
				Controller.getCurrentModeController().getMapController().nodeChanged(node);
			}

			public String getDescription() {
				return "setConnectorDash";
			}

			public void undo() {
				arrowLink.setDash(oldDash);
				final NodeModel node = arrowLink.getSource();
				Controller.getCurrentModeController().getMapController().nodeChanged(node);
			}
		};
		Controller.getCurrentModeController().execute(actor, arrowLink.getSource().getMap());
	}

	public void setArrowLinkEndPoints(final ConnectorModel link, final Point startPoint, final Point endPoint) {
		final IActor actor = new IActor() {
			final private Point oldEndPoint = link.getEndInclination();
			final private Point oldStartPoint = link.getStartInclination();

			public void act() {
				link.setStartInclination(startPoint);
				link.setEndInclination(endPoint);
				Controller.getCurrentModeController().getMapController().nodeChanged(link.getSource());
			}

			public String getDescription() {
				return "setArrowLinkEndPoints";
			}

			public void undo() {
				link.setStartInclination(oldStartPoint);
				link.setEndInclination(oldEndPoint);
				Controller.getCurrentModeController().getMapController().nodeChanged(link.getSource());
			}
		};
		Controller.getCurrentModeController().execute(actor, link.getSource().getMap());
	}

	public void setLink(final NodeModel node, final String link, final boolean makeRelative) {
		if (link != null && !"".equals(link)) {
			try {
				final URI uri = new URI(link);
				setLink(node, uri, makeRelative);
			}
			catch (final URISyntaxException e) {
				e.printStackTrace();
			}
			return;
		}
		setLink(node, (URI) null, false);
	}

	private URI relativeLink(final URI argUri, final NodeModel node, final boolean makeRelative) {
	    if (makeRelative && "file".equals(argUri.getScheme())) {
			try {
				final File mapFile = node.getMap().getFile();
	            return LinkController.toRelativeURI(mapFile, new File(argUri));
            }
            catch (Exception e) {
            }
		}
	    return argUri;
    }
	
	public void setLink(final NodeModel node, final URI argUri, final boolean makeRelative) {
		final URI uri = relativeLink(argUri, node, makeRelative);
		final IActor actor = new IActor() {
			private URI oldlink;
			private String oldTargetID;

			public void act() {
				NodeLinks links = NodeLinks.getLinkExtension(node);
				if (links != null) {
					oldlink = links.getHyperLink();
					oldTargetID = links.removeLocalHyperLink(node);
				}
				else {
					links = NodeLinks.createLinkExtension(node);
				}
				if (uri != null && uri.toString().startsWith("#")) {
					links.setLocalHyperlink(node, uri.toString().substring(1));
				}
				links.setHyperLink(uri);
				Controller.getCurrentModeController().getMapController().nodeChanged(node);
			}

			public String getDescription() {
				return "setLink";
			}

			public void undo() {
				final NodeLinks links = NodeLinks.getLinkExtension(node);
				links.setLocalHyperlink(node, oldTargetID);
				links.setHyperLink(oldlink);
				Controller.getCurrentModeController().getMapController().nodeChanged(node);
			}
		};
		Controller.getCurrentModeController().execute(actor, node.getMap());
	}

	public void setLinkByFileChooser() {
		setLinkByFileChooser.setLinkByFileChooser();
	}

	public void setMiddleLabel(final ConnectorModel model, String label) {
		if ("".equals(label)) {
			label = null;
		}
		String oldLabel = model.getMiddleLabel();
		if ("".equals(oldLabel)) {
			oldLabel = null;
		}
		if (label == oldLabel || label != null && label.equals(oldLabel)) {
			return;
		}
		final IActor actor = new MiddleLabelSetter(model, oldLabel, label);
		Controller.getCurrentModeController().execute(actor, model.getSource().getMap());
	}

	public void setSourceLabel(final ConnectorModel model, String label) {
		if ("".equals(label)) {
			label = null;
		}
		String oldLabel = model.getSourceLabel();
		if ("".equals(oldLabel)) {
			oldLabel = null;
		}
		if (label == oldLabel || label != null && label.equals(oldLabel)) {
			return;
		}
		final IActor actor = new SourceLabelSetter(model, label, oldLabel);
		Controller.getCurrentModeController().execute(actor, model.getSource().getMap());
	}

	public void setTargetLabel(final ConnectorModel model, String label) {
		if ("".equals(label)) {
			label = null;
		}
		String oldLabel = model.getTargetLabel();
		if ("".equals(oldLabel)) {
			oldLabel = null;
		}
		if (label == oldLabel || label != null && label.equals(oldLabel)) {
			return;
		}
		final IActor actor = new TargetLabelSetter(oldLabel, label, model);
		Controller.getCurrentModeController().execute(actor, model.getSource().getMap());
	}

	public ConnectorModel addConnector(final NodeModel source, final String targetID) {
		final CreateArrowLinkActor actor = new CreateArrowLinkActor(targetID, source);
		Controller.getCurrentModeController().execute(actor, source.getMap());
		return actor.getArrowLink();
	}

	public void removeArrowLink(final NodeLinkModel arrowLink) {
		final IActor actor = new IActor() {
			public void act() {
				final NodeModel source = arrowLink.getSource();
				final NodeLinks nodeLinks = (NodeLinks) source.getExtension(NodeLinks.class);
				nodeLinks.removeArrowlink(arrowLink);
				Controller.getCurrentModeController().getMapController().nodeChanged(source);
			}

			public String getDescription() {
				return "removeArrowLink";
			}

			public void undo() {
				final NodeModel source = arrowLink.getSource();
				NodeLinks nodeLinks = (NodeLinks) source.getExtension(NodeLinks.class);
				if (nodeLinks == null) {
					nodeLinks = new NodeLinks();
					source.addExtension(nodeLinks);
				}
				nodeLinks.addArrowlink(arrowLink);
				Controller.getCurrentModeController().getMapController().nodeChanged(source);
			}
		};
		Controller.getCurrentModeController().execute(actor, arrowLink.getSource().getMap());
	}

	public void setShape(final ConnectorModel connector, final Shape shape) {
		final Shape oldShape = connector.getShape();
		if (oldShape.equals(shape)) {
			return;
		}
		final IActor actor = new IActor() {
			public void act() {
				connector.setShape(shape);
				final NodeModel node = connector.getSource();
				Controller.getCurrentModeController().getMapController().nodeChanged(node);
			}

			public String getDescription() {
				return "setConnectorShape";
			}

			public void undo() {
				connector.setShape(oldShape);
				final NodeModel node = connector.getSource();
				Controller.getCurrentModeController().getMapController().nodeChanged(node);
			}
		};
		Controller.getCurrentModeController().execute(actor, connector.getSource().getMap());
	}

	public void setWidth(final ConnectorModel connector, final int width) {
		final int oldWidth = connector.getWidth();
		if (oldWidth == width) {
			return;
		}
		final IActor actor = new IActor() {
			public void act() {
				connector.setWidth(width);
				final NodeModel node = connector.getSource();
				Controller.getCurrentModeController().getMapController().nodeChanged(node);
			}

			public String getDescription() {
				return "setConnectorWidth";
			}

			public void undo() {
				connector.setWidth(oldWidth);
				final NodeModel node = connector.getSource();
				Controller.getCurrentModeController().getMapController().nodeChanged(node);
			}
		};
		Controller.getCurrentModeController().execute(actor, connector.getSource().getMap());
	}

	public void setAlpha(final ConnectorModel connector, final int alpha) {
		final int oldAlpha = connector.getAlpha();
		if (oldAlpha == alpha) {
			return;
		}
		final IActor actor = new IActor() {
			public void act() {
				connector.setAlpha(alpha);
				final NodeModel node = connector.getSource();
				Controller.getCurrentModeController().getMapController().nodeChanged(node);
			}

			public String getDescription() {
				return "setConnectorAlpha";
			}

			public void undo() {
				connector.setAlpha(oldAlpha);
				final NodeModel node = connector.getSource();
				Controller.getCurrentModeController().getMapController().nodeChanged(node);
			}
		};
		Controller.getCurrentModeController().execute(actor, connector.getSource().getMap());
	}
}
