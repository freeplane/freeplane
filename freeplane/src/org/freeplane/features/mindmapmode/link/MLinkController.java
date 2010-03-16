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
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JCheckBoxMenuItem;
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
import org.freeplane.core.ui.components.JAutoRadioButtonMenuItem;
import org.freeplane.core.undo.IActor;
import org.freeplane.features.common.link.ArrowType;
import org.freeplane.features.common.link.ConnectorModel;
import org.freeplane.features.common.link.HyperTextLinkModel;
import org.freeplane.features.common.link.LinkController;
import org.freeplane.features.common.link.LinkModel;
import org.freeplane.features.common.link.MapLinks;
import org.freeplane.features.common.link.NodeLinkModel;
import org.freeplane.features.common.link.NodeLinks;
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

		private CreateArrowLinkActor(String targetID, NodeModel source) {
			this.targetID = targetID;
			this.source = source;
		}

		public void act() {
			NodeLinks nodeLinks = (NodeLinks) source.getExtension(NodeLinks.class);
			if (nodeLinks == null) {
				nodeLinks = new NodeLinks();
				source.addExtension(nodeLinks);
			}
			arrowLink = new ConnectorModel(source, targetID);
			nodeLinks.addArrowlink(arrowLink);
			getModeController().getMapController().nodeChanged(source);
		}

		public String getDescription() {
			return "addLink";
		}

		public void undo() {
			final NodeLinks nodeLinks = (NodeLinks) source.getExtension(NodeLinks.class);
			nodeLinks.removeArrowlink(arrowLink);
			getModeController().getMapController().nodeChanged(source);
		}
	}

	private final class TargetLabelSetter implements IActor {
		private final String oldLabel;
		private final String label;
		private final ConnectorModel model;

		private TargetLabelSetter(String oldLabel, String label, ConnectorModel model) {
			this.oldLabel = oldLabel;
			this.label = label;
			this.model = model;
		}

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
	}

	private final class SourceLabelSetter implements IActor {
		private final ConnectorModel model;
		private final String label;
		private final String oldLabel;

		private SourceLabelSetter(ConnectorModel model, String label, String oldLabel) {
			this.model = model;
			this.label = label;
			this.oldLabel = oldLabel;
		}

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
	}

	private final class MiddleLabelSetter implements IActor {
		private final ConnectorModel model;
		private final String oldLabel;
		private final String label;

		private MiddleLabelSetter(ConnectorModel model, String oldLabel, String label) {
			this.model = model;
			this.oldLabel = oldLabel;
			this.label = label;
		}

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
	}

	private final class PopupEditorKeyListener implements KeyListener {
		private final JPopupMenu arrowLinkPopup;
		private boolean canceled = false;

		private PopupEditorKeyListener(JPopupMenu arrowLinkPopup) {
			this.arrowLinkPopup = arrowLinkPopup;
		}

		public void keyPressed(KeyEvent e) {
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

		public void keyReleased(KeyEvent e) {
		}

		public void keyTyped(KeyEvent e) {
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
	        if (((MModeController) getModeController()).isUndoAction()) {
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

		private void onChange(final NodeModel model, boolean delete) {
	        if (((MModeController) getModeController()).isUndoAction()) {
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
			for (NodeModel child : children) {
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
						if (delete)
							delete();
						else
							insert();
					}

					public void undo() {
						if (delete)
							insert();
						else
							delete();
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
				getModeController().execute(actor, map);
			}
		}

		private void removeLinksForDeletedTarget(final MapLinks links, final NodeModel model) {
			final List<NodeModel> children = model.getChildren();
			for (NodeModel child : children) {
				removeLinksForDeletedTarget(links, child);
			}
			final String id = model.getID();
			if(id == null){
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
					for(LinkModel link : linkModels){
						if(link instanceof HyperTextLinkModel){
							final NodeModel source = ((HyperTextLinkModel) link).getSource();
							getModeController().getMapController().delayedNodeRefresh(source);
						}
					}
                }

				public String getDescription() {
					return null;
				}

			};
			final MapModel map = model.getMap();
			getModeController().execute(actor, map);
		}

		public void onPreNodeMoved(NodeModel oldParent, int oldIndex, NodeModel newParent, NodeModel child, int newIndex) {
		}
	}

	static private ConnectorColorAction colorArrowLinkAction;
	static private EdgeLikeConnectorAction edgeLikeLinkAction;
	static private SetLinkByFileChooserAction setLinkByFileChooser;
	static private SetLinkByTextFieldAction setLinkByTextField;

	public MLinkController(final MModeController modeController) {
		super(modeController);
		createActions(modeController);
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
				getModeController().getMapController().nodeChanged(link.getSource());
			}

			public String getDescription() {
				return "changeArrowsOfArrowLink";
			}

			public void undo() {
				link.setStartArrow(oldStartArrow);
				link.setEndArrow(oldEndArrow);
				getModeController().getMapController().nodeChanged(link.getSource());
			}
		};
		getModeController().execute(actor, link.getSource().getMap());
	}

	/**
	 *
	 */
	private void createActions(final ModeController modeController) {
		final Controller controller = modeController.getController();
		setLinkByFileChooser = new SetLinkByFileChooserAction(controller);
		modeController.addAction(setLinkByFileChooser);
		final AddConnectorAction addArrowLinkAction = new AddConnectorAction(controller);
		modeController.addAction(addArrowLinkAction);
		modeController.addAction(new RemoveConnectorAction(this, null));
		colorArrowLinkAction = new ConnectorColorAction(this, null);
		modeController.addAction(colorArrowLinkAction);
		edgeLikeLinkAction = new EdgeLikeConnectorAction(this, null);
		modeController.addAction(edgeLikeLinkAction);
		setLinkByTextField = new SetLinkByTextFieldAction(controller);
		modeController.addAction(setLinkByTextField);
		modeController.addAction(new AddLocalLinkAction(controller));
		modeController.addAction(new ExtractLinkFromTextAction(controller));
	}

	@Override
	protected void createArrowLinkPopup(final ConnectorModel link, final JPopupMenu arrowLinkPopup) {
		super.createArrowLinkPopup(link, arrowLinkPopup);
		((RemoveConnectorAction) getModeController().getAction("RemoveConnectorAction")).setArrowLink(link);
		arrowLinkPopup.add(new RemoveConnectorAction(this, link));
		arrowLinkPopup.add(new ConnectorColorAction(this, link));
		final EdgeLikeConnectorAction action = new EdgeLikeConnectorAction(this, link);
		final JCheckBoxMenuItem menuItem = new JCheckBoxMenuItem(action);
		menuItem.setSelected(link.isEdgeLike());
		arrowLinkPopup.add(menuItem);
		arrowLinkPopup.addSeparator();
		arrowLinkPopup.add(new JLabel(ResourceBundles.getText("edit_source_label")));
		final PopupEditorKeyListener enterListener = new PopupEditorKeyListener(arrowLinkPopup);
		final JTextField sourceLabelEditor = new JTextField(link.getSourceLabel());
		sourceLabelEditor.addKeyListener(enterListener);
		arrowLinkPopup.add(sourceLabelEditor);
		arrowLinkPopup.addSeparator();
		arrowLinkPopup.add(new JLabel(ResourceBundles.getText("edit_middle_label")));
		final JTextField middleLabelEditor = new JTextField(link.getMiddleLabel());
		middleLabelEditor.addKeyListener(enterListener);
		arrowLinkPopup.add(middleLabelEditor);
		arrowLinkPopup.addSeparator();
		arrowLinkPopup.add(new JLabel(ResourceBundles.getText("edit_target_label")));
		final JTextField targetLabelEditor = new JTextField(link.getTargetLabel());
		targetLabelEditor.addKeyListener(enterListener);
		arrowLinkPopup.add(targetLabelEditor);
		arrowLinkPopup.addSeparator();
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
			}

			public void popupMenuWillBecomeVisible(final PopupMenuEvent e) {
			}
		});
		final ChangeConnectorArrowsAction actionNN = new ChangeConnectorArrowsAction(this, "none", link,
		    ArrowType.NONE, ArrowType.NONE);
		final JRadioButtonMenuItem itemnn = new JAutoRadioButtonMenuItem(actionNN);
		arrowLinkPopup.add(itemnn);
		final ChangeConnectorArrowsAction actionNT = new ChangeConnectorArrowsAction(this, "forward", link,
		    ArrowType.NONE, ArrowType.DEFAULT);
		final JRadioButtonMenuItem itemnt = new JAutoRadioButtonMenuItem(actionNT);
		arrowLinkPopup.add(itemnt);
		final ChangeConnectorArrowsAction actionTN = new ChangeConnectorArrowsAction(this, "backward", link,
		    ArrowType.DEFAULT, ArrowType.NONE);
		final JRadioButtonMenuItem itemtn = new JAutoRadioButtonMenuItem(actionTN);
		arrowLinkPopup.add(itemtn);
		final ChangeConnectorArrowsAction actionTT = new ChangeConnectorArrowsAction(this, "both", link,
		    ArrowType.DEFAULT, ArrowType.DEFAULT);
		final JRadioButtonMenuItem itemtt = new JAutoRadioButtonMenuItem(actionTT);
		arrowLinkPopup.add(itemtt);
	}

	static final private Pattern urlPattern = Pattern.compile("file://[^\\s" + File.pathSeparatorChar + "]+|(:?https?|ftp)://[^\\s()'\",;|<>{}]+");
	static private Pattern mailPattern = Pattern.compile("([!+\\-/=~.\\w#]+@[\\w.\\-+?&=%]+)");
	public String findLink(final String text) {
		final Matcher urlMatcher = urlPattern.matcher(text);
		if (urlMatcher.find()) {
			String link = urlMatcher.group();
			try {
				link = new URL(link).toURI().toString();
				return link;
			} catch (MalformedURLException e) {
				return null;
			} catch (URISyntaxException e) {
				return null;
			}
		}
		final Matcher mailMatcher = mailPattern.matcher(text);
		if (mailMatcher.find()) {
			String link = "mailto:" + mailMatcher.group();
			return link;
		}
		return null;
	}

	public void setArrowLinkColor(final ConnectorModel arrowLink, final Color color) {
		colorArrowLinkAction.setArrowLinkColor(arrowLink, color);
	}

	public void setArrowLinkEndPoints(final ConnectorModel link, final Point startPoint, final Point endPoint) {
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

	public void setLink(final NodeModel node, final String link, boolean makeRelative) {
		if (link != null && ! "".equals(link)) {
			try {
				final URI uri = new URI(link);
				setLink(node, uri, makeRelative);
			}
			catch (final URISyntaxException e) {
				e.printStackTrace();
			}
			return;
		}
		setLink(node, (URI)null, false);
	}

	public void setLink(final NodeModel node, final URI argUri, boolean makeRelative) {
		final URI uri;
		if(makeRelative && "file".equals(argUri.getScheme())){
			File mapFile = node.getMap().getFile();
			uri = LinkController.toRelativeURI(mapFile, new File(argUri));
		}
		else{
			uri = argUri;
		}
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
				getModeController().getMapController().nodeChanged(node);
			}
		
			public String getDescription() {
				return "setLink";
			}
		
			public void undo() {
				final NodeLinks links = NodeLinks.getLinkExtension(node);
				links.setLocalHyperlink(node, oldTargetID);
				links.setHyperLink(oldlink);
				getModeController().getMapController().nodeChanged(node);
			}
		};
		setLinkByTextField.getModeController().execute(actor, node.getMap());
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
		getModeController().execute(actor, model.getSource().getMap());
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
		getModeController().execute(actor, model.getSource().getMap());
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
		getModeController().execute(actor, model.getSource().getMap());
	}

	public ConnectorModel addConnector(final NodeModel source, final String targetID) {
		final CreateArrowLinkActor actor = new CreateArrowLinkActor(targetID, source);
		getModeController().execute(actor, source.getMap());
		return actor.getArrowLink();
	}

	public void removeArrowLink(final NodeLinkModel arrowLink) {
		final IActor actor = new IActor() {
			public void act() {
				final NodeModel source = arrowLink.getSource();
				final NodeLinks nodeLinks = (NodeLinks) source.getExtension(NodeLinks.class);
				nodeLinks.removeArrowlink(arrowLink);
				getModeController().getMapController().nodeChanged(source);
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
				getModeController().getMapController().nodeChanged(source);
			}
		};
		getModeController().execute(actor, arrowLink.getSource().getMap());
	}

	public void setEdgeLike(final ConnectorModel connector, final boolean edgeLike) {
		final boolean alreadyEdgeLike = connector.isEdgeLike();
		if (alreadyEdgeLike == edgeLike) {
			return;
		}
		final IActor actor = new IActor() {
			public void act() {
				connector.setEdgeLike(edgeLike);
				final NodeModel node = connector.getSource();
				getModeController().getMapController().nodeChanged(node);
			}

			public String getDescription() {
				return "setEdgeLike";
			}

			public void undo() {
				connector.setEdgeLike(alreadyEdgeLike);
				final NodeModel node = connector.getSource();
				getModeController().getMapController().nodeChanged(node);
			}
		};
		getModeController().execute(actor, connector.getSource().getMap());
	}
}
