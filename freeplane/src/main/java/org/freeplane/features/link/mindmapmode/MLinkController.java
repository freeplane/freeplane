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
package org.freeplane.features.link.mindmapmode;

import java.awt.Color;
import java.awt.Component;
import java.awt.Point;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.Vector;

import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.DefaultComboBoxModel;
import javax.swing.FocusManager;
import javax.swing.InputMap;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.commandtonode.CommandToNodeHotKeyAction;
import org.freeplane.core.ui.components.JComboBoxFactory;
import org.freeplane.core.ui.components.RenderedContent;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.undo.IActor;
import org.freeplane.core.util.Hyperlink;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.link.ConnectorArrows;
import org.freeplane.features.link.ConnectorModel;
import org.freeplane.features.link.ConnectorShape;
import org.freeplane.features.link.HyperTextLinkModel;
import org.freeplane.features.link.LinkController;
import org.freeplane.features.link.MapLinks;
import org.freeplane.features.link.NodeLinkModel;
import org.freeplane.features.link.NodeLinks;
import org.freeplane.features.link.mindmapmode.editor.ConnectorEditorPanel;
import org.freeplane.features.map.DocuMapAttribute;
import org.freeplane.features.map.IExtensionCopier;
import org.freeplane.features.map.IMapChangeListener;
import org.freeplane.features.map.IMapSelection;
import org.freeplane.features.map.MapChangeEvent;
import org.freeplane.features.map.MapController;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeDeletionEvent;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.map.NodeMoveEvent;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.freeplane.features.spellchecker.mindmapmode.SpellCheckerController;
import org.freeplane.features.styles.IStyle;
import org.freeplane.features.styles.LogicalStyleController;
import org.freeplane.features.styles.LogicalStyleKeys;
import org.freeplane.features.styles.LogicalStyleModel;
import org.freeplane.features.styles.MapStyleModel;

/**
 * @author Dimitry Polivaev
 */
public class MLinkController extends LinkController {
	private static class StyleCopier implements IExtensionCopier {

		@Override
		public void copy(Object key, NodeModel from, NodeModel to) {
			if (!key.equals(LogicalStyleKeys.NODE_STYLE)) {
				return;
			}
			copy(from, to);
        }

		public void copy(NodeModel from, NodeModel to) {
	        final Boolean formatNodeAsHyperlink = NodeLinks.formatNodeAsHyperlink(from);
			if(formatNodeAsHyperlink != null)
				NodeLinks.createLinkExtension(to).setFormatNodeAsHyperlink(formatNodeAsHyperlink);

        }

		@Override
		public void remove(Object key, NodeModel from) {
			if (!key.equals(LogicalStyleKeys.NODE_STYLE)) {
				return;
			}
			final NodeLinks model = NodeLinks.getLinkExtension(from);
			if(model != null)
				model.setFormatNodeAsHyperlink(null);
        }

		@Override
		public void remove(Object key, NodeModel from, NodeModel which) {
	        if(NodeLinks.formatNodeAsHyperlink(which) != null)
	        	remove(key, from);
        }
	}
	private final class AddArrowLinkActor implements IActor {
		private final ConnectorModel arrowLink;

		private AddArrowLinkActor(ConnectorModel arrowLink) {
            this.arrowLink = arrowLink;
		}

		@Override
		public void act() {
			NodeModel source = arrowLink.getSource();
            NodeLinks nodeLinks = NodeLinks.createLinkExtension(source);
			nodeLinks.addArrowlink(arrowLink);
			fireNodeConnectorChange(arrowLink);
		}

		@Override
		public String getDescription() {
			return "addLink";
		}

		@Override
		public void undo() {
		    NodeModel source = arrowLink.getSource();
			final NodeLinks nodeLinks = NodeLinks.getLinkExtension(source);
			nodeLinks.removeArrowlink(arrowLink);
			fireNodeConnectorChange(arrowLink);
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

		@Override
		public void act() {
			model.setTargetLabel(label);
			fireNodeConnectorChange(model);
		}

		@Override
		public String getDescription() {
			return "setTargetLabel";
		}

		@Override
		public void undo() {
			model.setTargetLabel(oldLabel);
			fireNodeConnectorChange(model);
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

		@Override
		public void act() {
			model.setSourceLabel(label);
			fireNodeConnectorChange(model);
		}

		@Override
		public String getDescription() {
			return "setSourceLabel";
		}

		@Override
		public void undo() {
			model.setSourceLabel(oldLabel);
			fireNodeConnectorChange(model);
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

		@Override
		public void act() {
			model.setMiddleLabel(label);
			fireNodeConnectorChange(model);
		}

		@Override
		public String getDescription() {
			return "setMiddleLabel";
		}

		@Override
		public void undo() {
			model.setMiddleLabel(oldLabel);
			fireNodeConnectorChange(model);
		}
	}

	/**
	 * @author Dimitry Polivaev
	 */
	private final class MapLinkChanger implements IMapChangeListener {

		@Override
		public void mapChanged(final MapChangeEvent event) {
		}

		@Override
		public void onNodeDeleted(NodeDeletionEvent nodeDeletionEvent) {
		}

		@Override
		public void onNodeInserted(final NodeModel parent, final NodeModel model, final int newIndex) {
			Controller.getCurrentController().getViewController().invokeLater(new Runnable() {
				@Override
				public void run() {
					final MapModel map = model.getMap();
					final MapLinks links = map.getExtension(MapLinks.class);
					if (links != null) {
						insertMapLinks(links, model);
						updateMapLinksForTargetTree(links, model);
					}
				}
			});
		}

		@Override
		public void onNodeMoved(NodeMoveEvent nodeMoveEvent) {
		}

		@Override
		public void onPreNodeDelete(NodeDeletionEvent nodeDeletionEvent) {
			NodeModel model = nodeDeletionEvent.node;
			final MapModel map = model.getMap();
			final MapLinks links = map.getExtension(MapLinks.class);
			if (links != null) {
				deleteMapLinks(links, model, model);
				updateMapLinksForTargetTree(links, model);
			}
		}

		private void insertMapLinks(final MapLinks links, final NodeModel model) {
			final List<NodeModel> children = model.getChildren();
			for (final NodeModel child : children) {
				insertMapLinks(links, child);
			}
			insertMapLinksForInsertedSourceNode(links, model);
		}

		private void insertMapLinksForInsertedSourceNode(MapLinks links, NodeModel model) {
	        final NodeLinks nodeLinks = NodeLinks.getLinkExtension(model);
	        if (nodeLinks != null) {
	        	for (final NodeLinkModel link : nodeLinks.getLinks()) {
	        		links.add(link);
	        	}
	        }
        }

		private void deleteMapLinks(final MapLinks links, final NodeModel deletionRoot, NodeModel node) {
			final List<NodeModel> children = node.getChildren();
			for (final NodeModel child : children) {
				deleteMapLinks(links, deletionRoot, child);
			}
			final NodeLinks nodeLinks = NodeLinks.getLinkExtension(node);
			if (nodeLinks != null) {
				nodeLinks.replaceMapLinksForDeletedSourceNode(links, deletionRoot, node);
			}
		}

		private void updateMapLinksForTargetTree(final MapLinks links, final NodeModel model) {
			final List<NodeModel> children = model.getChildren();
			for (final NodeModel child : children) {
				updateMapLinksForTargetTree(links, child);
			}
			final String id = model.getID();
			if (id == null) {
				return;
			}
			final Set<NodeLinkModel> linkModels = links.get(id);
			if (linkModels == null || linkModels.isEmpty()) {
				return;
			}
			for (final NodeLinkModel link : linkModels) {
				final NodeModel source = link.getSource();
				if (link instanceof HyperTextLinkModel)
					Controller.getCurrentModeController().getMapController().delayedNodeRefresh(source, NodeModel.NODE_ICON,
						null, null);
				else if(link instanceof ConnectorModel)
					fireNodeConnectorChange((ConnectorModel) link);
			}
		}

		@Override
		public void onPreNodeMoved(NodeMoveEvent nodeMoveEvent) {
		}
	}

	static private SetLinkByFileChooserAction setLinkByFileChooser;
	static private SetLinkByTextFieldAction setLinkByTextField;
	private String anchorID;
	final private MapLinkChanger mapLinkChanger;
	public MLinkController(ModeController modeController) {
		super(modeController);
		this.anchorID = "";
		mapLinkChanger = new MapLinkChanger();
	}

	@Override
    protected void init() {
		super.init();
		this.anchorID = "";
		createActions();
		modeController.registerExtensionCopier(new StyleCopier());
		(modeController.getMapController()).addUIMapChangeListener(mapLinkChanger);
	}

	public ConnectorModel addConnector(final NodeModel source, final NodeModel target) {
		ConnectorModel connector = addConnector(source, target.createID());
		setNodeDependantStyle(source, target, connector);
		return connector;
	}

	public void changeArrowsOfArrowLink(final ConnectorModel link, final Optional<ConnectorArrows> arrows) {
		final IActor actor = new IActor() {
			final private Optional<ConnectorArrows> oldArrows = link.getArrows();

			@Override
			public void act() {
				link.setArrows(arrows);
				fireNodeConnectorChange(link);
			}

			@Override
			public String getDescription() {
				return "changeArrowsOfArrowLink";
			}

			@Override
			public void undo() {
				link.setArrows(oldArrows);
				fireNodeConnectorChange(link);
			}
		};
		Controller.getCurrentModeController().execute(actor, link.getSource().getMap());
	}

	/**
	 *
	 */
	private void createActions() {
		setLinkByFileChooser = new SetLinkByFileChooserAction();
		modeController.addAction(setLinkByFileChooser);
		final AddConnectorAction addArrowLinkAction = new AddConnectorAction();
		modeController.addAction(addArrowLinkAction);
		setLinkByTextField = new SetLinkByTextFieldAction();
		modeController.addAction(setLinkByTextField);
        modeController.addAction(new RemoveLinkAction());
        modeController.addAction(new AddLocalLinkAction());
		modeController.addAction(new AddMenuItemLinkAction());
		modeController.addAction(new AddExecutionLinkAction());
		modeController.addAction(new ExtractLinkFromTextAction());
		modeController.addAction(new SetLinkAnchorAction());
		modeController.addAction(new MakeLinkToAnchorAction());
		modeController.addAction(new MakeLinkFromAnchorAction());
        modeController.addAction(new ClearLinkAnchorAction());
        modeController.addAction(new AddSelfConnectorAction());
        modeController.addAction(new CommandToNodeHotKeyAction());
	}

	@Override
	protected void createArrowLinkPopup(final ConnectorModel link, final JComponent arrowLinkPopup) {
		super.createArrowLinkPopup(link, arrowLinkPopup);
		boolean isDefault = MapStyleModel.isDefaultStyleNode(link.getSource());
        if(! isDefault) {
		    addClosingAction(arrowLinkPopup, new RemoveConnectorAction(this, link));
		    addSeparator(arrowLinkPopup);
        }
        ConnectorEditorPanel comp = new ConnectorEditorPanel(this, link);

		comp.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        arrowLinkPopup.add(comp);
		addSeparator(arrowLinkPopup);

		final JTextArea sourceLabelEditor;
		final JTextArea middleLabelEditor;
		final JTextArea targetLabelEditor ;
		if(! isDefault) {
		    sourceLabelEditor = new JTextArea(link.getSourceLabel().orElse(""));
		    addTextEditor(arrowLinkPopup, "edit_source_label", sourceLabelEditor);

		    middleLabelEditor = new JTextArea(link.getMiddleLabel().orElse(""));
		    addTextEditor(arrowLinkPopup, "edit_middle_label"  ,middleLabelEditor);

		    targetLabelEditor = new JTextArea(link.getTargetLabel().orElse(""));
		    addTextEditor(arrowLinkPopup, "edit_target_label", targetLabelEditor);
		}
		else {
		    sourceLabelEditor = middleLabelEditor = targetLabelEditor = null;
		}

		arrowLinkPopup.addHierarchyListener(new HierarchyListener() {
            private Component focusOwner;
            private Window dialog;
            @Override
			public void hierarchyChanged(HierarchyEvent e) {
                final JComponent component = (JComponent) e.getComponent();
                if(component.isShowing()){
                    if(dialog == null){
                        dialog =  SwingUtilities.getWindowAncestor(component);
                        dialog.addWindowListener(new WindowAdapter() {

                            @Override
                            public void windowClosing(WindowEvent e) {
                                component.putClientProperty(CANCEL, Boolean.TRUE);
                            }
                        });
                    }
                    if(focusOwner == null)
                        focusOwner = FocusManager.getCurrentManager().getFocusOwner();
                    return;
                }
                if(focusOwner == null || ! focusOwner.isShowing())
                    return;
                focusOwner.requestFocus();
                if (Boolean.TRUE.equals(component.getClientProperty(CANCEL))) {
                    return;
                }
                final IMapSelection selection = Controller.getCurrentController().getSelection();
                if (selection == null || selection.getSelected() == null)
                    return;
                if(! isDefault) {
                    setSourceLabel(link, sourceLabelEditor.getText());
                    setTargetLabel(link, targetLabelEditor.getText());
                    setMiddleLabel(link, middleLabelEditor.getText());
                }
            }
		});

	}

    public void setConnectorStyle(ConnectorModel link, IStyle style) {
        final IStyle oldStyle = link.getStyle();
        if (style.equals(oldStyle)) {
            return;
        }
        final IActor actor = new IActor() {
            @Override
            public void act() {
                link.setStyle(style);
                final NodeModel node = link.getSource();
                fireNodeConnectorChange(link);
            }

            @Override
            public String getDescription() {
                return "setConnectorStyle";
            }

            @Override
            public void undo() {
                link.setStyle(oldStyle);
                final NodeModel node = link.getSource();
                fireNodeConnectorChange(link);
            }
        };
        Controller.getCurrentModeController().execute(actor, link.getSource().getMap());

    }

    @SuppressWarnings("serial")
    protected JComboBox createActionBox(AFreeplaneAction[] items) {
        final JComboBox box = JComboBoxFactory.create();
        box.setEditable(false);
        Vector<RenderedContent<AFreeplaneAction>> renderedContent = RenderedContent.of(items);
		box.setModel(new DefaultComboBoxModel<>(renderedContent));
        for(RenderedContent<AFreeplaneAction> item : renderedContent){
            if(item.value.isSelected()){
                box.setSelectedItem(item);
                break;
            }
        }
        box.setRenderer(RenderedContent.createRenderer());
        box.addItemListener(new ItemListener() {
            @Override
			public void itemStateChanged(ItemEvent e) {
            	RenderedContent<AFreeplaneAction> item = (RenderedContent<AFreeplaneAction>)e.getItem();
                final JComboBox box = (JComboBox) e.getSource();
                item.value.actionPerformed(new ActionEvent(box, ActionEvent.ACTION_PERFORMED, null));
            }
        });
        return box;
    }

	private void addSeparator(JComponent arrowLinkPopup) {
    }

    private void addTextEditor(final JComponent popup, final String label, final JTextArea editor) {
		final InputMap inputMap = editor.getInputMap();
		final ActionMap actionMap = editor.getActionMap();
		final KeyStroke close = KeyStroke.getKeyStroke("ENTER");
		inputMap.put(close, CLOSE);
		actionMap.put(CLOSE, new ClosePopupAction(CLOSE));

		final KeyStroke enter = KeyStroke.getKeyStroke("alt ENTER");
		final KeyStroke enter2 = KeyStroke.getKeyStroke("shift ENTER");
		inputMap.put(enter, "INSERT_EOL");
		inputMap.put(enter2, "INSERT_EOL");
		actionMap.put("INSERT_EOL", new UITools.InsertEolAction());
		editor.setRows(5);
		editor.setColumns(30);

		final JPopupMenu popupMenu = new JPopupMenu();
        SpellCheckerController spellCheckerController = SpellCheckerController.getController();
        spellCheckerController.addSpellCheckerMenu(popupMenu );
        spellCheckerController.enableAutoSpell(editor, true);
        editor.addMouseListener(new MouseAdapter() {

            @Override
            public void mousePressed(MouseEvent e) {
                handlePopup(e);
             }

            @Override
            public void mouseReleased(MouseEvent e) {
                handlePopup(e);
            }

            private void handlePopup(MouseEvent e) {
                if(e.isPopupTrigger()){
                    e.consume();
                    popupMenu.show(e.getComponent(), e.getX(), e.getY());
                }

            }

        });


		final JScrollPane scrollPane = new JScrollPane(editor, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		UITools.setScrollbarIncrement(scrollPane);
		addPopupComponent(popup, TextUtils.getText(label), scrollPane);
	}

	public void setConnectorColor(final ConnectorModel arrowLink, final Optional<Color> color) {
		final  Optional<Color> oldColor = arrowLink.getColor();
		if (color.equals(oldColor)) {
			return;
		}
		final IActor actor = new IActor() {
			@Override
			public void act() {
				arrowLink.setColor(color);
				final NodeModel node = arrowLink.getSource();
				fireNodeConnectorChange(arrowLink);
			}

			@Override
			public String getDescription() {
				return "setConnectorColor";
			}

			@Override
			public void undo() {
				arrowLink.setColor(oldColor);
				final NodeModel node = arrowLink.getSource();
				fireNodeConnectorChange(arrowLink);
			}
		};
		Controller.getCurrentModeController().execute(actor, arrowLink.getSource().getMap());
	}

	public void setConnectorDashArray(final ConnectorModel arrowLink, final Optional<int[]> dash) {
		final Optional<int[]> oldDash = arrowLink.getDash();
		if (dash.equals(oldDash)) {
			return;
		}
		final IActor actor = new IActor() {
			@Override
			public void act() {
				arrowLink.setDash(dash);
				final NodeModel node = arrowLink.getSource();
				fireNodeConnectorChange(arrowLink);
			}

			@Override
			public String getDescription() {
				return "setConnectorDash";
			}

			@Override
			public void undo() {
				arrowLink.setDash(oldDash);
				final NodeModel node = arrowLink.getSource();
				fireNodeConnectorChange(arrowLink);
			}
		};
		Controller.getCurrentModeController().execute(actor, arrowLink.getSource().getMap());
	}

	public void setArrowLinkEndPoints(final ConnectorModel link, final Point startPoint, final Point endPoint) {
		final IActor actor = new IActor() {
			final private Point oldEndPoint = link.getEndInclination();
			final private Point oldStartPoint = link.getStartInclination();

			@Override
			public void act() {
				link.setStartInclination(startPoint);
				link.setEndInclination(endPoint);
				fireNodeConnectorChange(link);
			}

			@Override
			public String getDescription() {
				return "setArrowLinkEndPoints";
			}

			@Override
			public void undo() {
				link.setStartInclination(oldStartPoint);
				link.setEndInclination(oldEndPoint);
				fireNodeConnectorChange(link);
			}
		};
		Controller.getCurrentModeController().execute(actor, link.getSource().getMap());
	}

	public void setLink(final NodeModel node, final String link, final int linkType) {
		if (link != null && !"".equals(link)) {
			try {
				final URI uri = new URI(link);
				setLink(node, uri, linkType);
			}
			catch (final URISyntaxException e) {
				e.printStackTrace();
			}
			return;
		}
		setLink(node, (URI) null, LINK_ABSOLUTE);
	}

	private URI relativeLink(final URI argUri, final NodeModel node, final int linkType) {
		if (linkType == LINK_RELATIVE_TO_MINDMAP && "file".equals(argUri.getScheme())) {
			try {
				final File mapFile = node.getMap().getFile();
				return createRelativeURI(mapFile, argUri);
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
		return argUri;
	}

	public void setLinkTypeDependantLink(final NodeModel node, final URI argUri) {
		setLink(node, argUri, getLinkType());
	}

	public void setLinkTypeDependantLink(final NodeModel node, final File file) {
		setLink(node, file.toURI(), getLinkType());
	}

	public void setLinkTypeDependantLink(final NodeModel node, final String link) {
		setLink(node, link, getLinkType());
	}

	public void setLink(final NodeModel node, final URI argUri, final int linkType) {
		final Hyperlink hyperlink = argUri != null ? new Hyperlink(relativeLink(argUri, node, linkType)) : null;
		setLink(node, hyperlink);
	}


	public void setLink(NodeModel node, Hyperlink hyperlink) {
		final IActor actor = new IActor() {
			private Hyperlink oldlink;
			private String oldTargetID;

			@Override
			public void act() {
				NodeLinks links = NodeLinks.getLinkExtension(node);
				if (links != null) {
					oldlink = links.getHyperLink(node);
					oldTargetID = links.removeLocalHyperLink(node);
				}
				else {
					links = NodeLinks.createLinkExtension(node);
				}
				if (hyperlink != null && hyperlink.toString().startsWith("#")) {
					links.setLocalHyperlink(node, hyperlink.toString().substring(1));
				}
				else
					links.setHyperLink(hyperlink);
				Controller.getCurrentModeController().getMapController().nodeChanged(node, NodeLinks.HYPERLINK_CHANGED, oldlink, hyperlink);

			}

			@Override
			public String getDescription() {
				return "setLink";
			}

			@Override
			public void undo() {
				final NodeLinks links = NodeLinks.getLinkExtension(node);
				Hyperlink undoneLink = links.getHyperLink(node);
				links.setLocalHyperlink(node, oldTargetID);
				links.setHyperLink(oldlink);
				Controller.getCurrentModeController().getMapController().nodeChanged(node, NodeLinks.HYPERLINK_CHANGED, undoneLink, oldlink);
			}
		};
		Controller.getCurrentModeController().execute(actor, node.getMap());
	}


	public void setLinkByFileChooser() {
		setLinkByFileChooser.setLinkByFileChooser();
	}

	public void setMiddleLabel(final ConnectorModel model, String label) {
		if (label == null) {
			label = "";
		}
		String oldLabel = model.getMiddleLabel().orElse("");
		if (label.equals(oldLabel)) {
			return;
		}
		final IActor actor = new MiddleLabelSetter(model, oldLabel, label);
		Controller.getCurrentModeController().execute(actor, model.getSource().getMap());
	}

	public void setSourceLabel(final ConnectorModel model, String label) {
        if (label == null) {
            label = "";
        }
        String oldLabel = model.getSourceLabel().orElse("");
        if (label.equals(oldLabel)) {
            return;
        }
		final IActor actor = new SourceLabelSetter(model, label, oldLabel);
		Controller.getCurrentModeController().execute(actor, model.getSource().getMap());
	}

	public void setTargetLabel(final ConnectorModel model, String label) {
        if (label == null) {
            label = "";
        }
        String oldLabel = model.getTargetLabel().orElse("");
        if (label.equals(oldLabel)) {
            return;
        }
		final IActor actor = new TargetLabelSetter(oldLabel, label, model);
		Controller.getCurrentModeController().execute(actor, model.getSource().getMap());
	}

	public ConnectorModel addConnector(final NodeModel source, final String targetID) {
		ConnectorModel connector = new ConnectorModel(source, targetID);
        final AddArrowLinkActor actor = new AddArrowLinkActor(connector);
 		Controller.getCurrentModeController().execute(actor, source.getMap());
		return connector;
	}

	public void removeArrowLink(final ConnectorModel arrowLink) {
		final IActor actor = new IActor() {
			@Override
			public void act() {
				final NodeModel source = arrowLink.getSource();
				final NodeLinks nodeLinks = NodeLinks.getLinkExtension(source);
				nodeLinks.removeArrowlink(arrowLink);
				fireNodeConnectorChange(arrowLink);
			}

			@Override
			public String getDescription() {
				return "removeArrowLink";
			}

			@Override
			public void undo() {
				final NodeModel source = arrowLink.getSource();
				NodeLinks nodeLinks = NodeLinks.createLinkExtension(source);
				nodeLinks.addArrowlink(arrowLink);
				fireNodeConnectorChange(arrowLink);
			}
		};
		Controller.getCurrentModeController().execute(actor, arrowLink.getSource().getMap());
	}

	public void setShape(final ConnectorModel connector, final Optional<ConnectorShape> shape) {
		final Optional<ConnectorShape> oldShape = connector.getShape();
		if (oldShape.equals(shape)) {
			return;
		}
		final IActor actor = new IActor() {
			@Override
			public void act() {
				connector.setShape(shape);
				final NodeModel node = connector.getSource();
				fireNodeConnectorChange(connector);
			}

			@Override
			public String getDescription() {
				return "setConnectorShape";
			}

			@Override
			public void undo() {
				connector.setShape(oldShape);
				final NodeModel node = connector.getSource();
				fireNodeConnectorChange(connector);
			}
		};
		Controller.getCurrentModeController().execute(actor, connector.getSource().getMap());
	}

	public void setWidth(final ConnectorModel connector, final Optional<Integer> width) {
		final Optional<Integer> oldWidth = connector.getWidth();
		if (oldWidth.equals(width)) {
			return;
		}
		final IActor actor = new IActor() {
			@Override
			public void act() {
				connector.setWidth(width);
				final NodeModel node = connector.getSource();
				fireNodeConnectorChange(connector);
			}

			@Override
			public String getDescription() {
				return "setConnectorWidth";
			}

			@Override
			public void undo() {
				connector.setWidth(oldWidth);
				final NodeModel node = connector.getSource();
				fireNodeConnectorChange(connector);
			}
		};
		Controller.getCurrentModeController().execute(actor, connector.getSource().getMap());
	}


	public void setLabelFontSize(final ConnectorModel connector, final Optional<Integer> width) {
		final Optional<Integer> oldWidth = connector.getLabelFontSize();
		if (oldWidth.equals(width)) {
			return;
		}
		final IActor actor = new IActor() {
			@Override
			public void act() {
				connector.setLabelFontSize(width);
				final NodeModel node = connector.getSource();
				fireNodeConnectorChange(connector);
			}

			@Override
			public String getDescription() {
				return "setConnectorWidth";
			}

			@Override
			public void undo() {
				connector.setLabelFontSize(oldWidth);
				final NodeModel node = connector.getSource();
				fireNodeConnectorChange(connector);
			}
		};
		Controller.getCurrentModeController().execute(actor, connector.getSource().getMap());
	}


	public void setLabelFontFamily(final ConnectorModel connector, final Optional<String> family) {
		final Optional<String> oldFamily = connector.getLabelFontFamily();
		if (oldFamily.equals(family)) {
			return;
		}
		final IActor actor = new IActor() {
			@Override
			public void act() {
				connector.setLabelFontFamily(family);
				final NodeModel node = connector.getSource();
				fireNodeConnectorChange(connector);
			}

			@Override
			public String getDescription() {
				return "setConnectorWidth";
			}

			@Override
			public void undo() {
				connector.setLabelFontFamily(oldFamily);
				final NodeModel node = connector.getSource();
				fireNodeConnectorChange(connector);
			}
		};
		Controller.getCurrentModeController().execute(actor, connector.getSource().getMap());
	}

	public void setOpacity(final ConnectorModel connector,  final Optional<Integer> alpha) {
		final Optional<Integer> oldAlpha = connector.getAlpha();
		if (oldAlpha.equals(alpha)) {
			return;
		}
		final IActor actor = new IActor() {
			@Override
			public void act() {
				connector.setAlpha(alpha);
				final NodeModel node = connector.getSource();
				fireNodeConnectorChange(connector);
			}

			@Override
			public String getDescription() {
				return "setConnectorAlpha";
			}

			@Override
			public void undo() {
				connector.setAlpha(oldAlpha);
				final NodeModel node = connector.getSource();
				fireNodeConnectorChange(connector);
			}
		};
		Controller.getCurrentModeController().execute(actor, connector.getSource().getMap());
	}

	@Override
	protected void loadURL(final NodeModel node, final ActionEvent e) {
		// load as documentation map if the node belongs to a documentation map
		boolean addDocuMapAttribute = node.getMap().containsExtension(DocuMapAttribute.class)
				&& ! modeController.containsExtension(DocuMapAttribute.class);
		if(addDocuMapAttribute){
			modeController.addExtension(DocuMapAttribute.class, DocuMapAttribute.INSTANCE);
		}
		try{
			super.loadURL(node, e);
		}
		finally{
			if(addDocuMapAttribute){
				modeController.removeExtension(DocuMapAttribute.class);
			}
		}
	}

	@Override
	public void loadURI(NodeModel node, Hyperlink uri) {
		// load as documentation map if the node belongs to a documentation map
		boolean addDocuMapAttribute = node.getMap().containsExtension(DocuMapAttribute.class)
				&& ! modeController.containsExtension(DocuMapAttribute.class);
		if(addDocuMapAttribute){
			modeController.addExtension(DocuMapAttribute.class, DocuMapAttribute.INSTANCE);
		}
		try{
			super.loadURI(node, uri);
		}
		finally{
			if(addDocuMapAttribute){
				modeController.removeExtension(DocuMapAttribute.class);
			}
		}
	}
	public String getAnchorID() {
		return anchorID;
	}

	public void setAnchorID(final String anchorID) {
		this.anchorID = anchorID;
		final String tooltip;
		AFreeplaneAction setLinkAnchorAction = modeController.getAction("SetLinkAnchorAction");
		final boolean anchored = isAnchored();
		if(anchored)
			tooltip = TextUtils.format(setLinkAnchorAction.getTooltipKey() + "_anchored", anchorID);
		else
			tooltip = TextUtils.getRawText(setLinkAnchorAction.getTooltipKey());
		setLinkAnchorAction.putValue(Action.SHORT_DESCRIPTION, tooltip);
		setLinkAnchorAction.putValue(Action.LONG_DESCRIPTION, tooltip);
		setLinkAnchorAction.setSelected(anchored);
		modeController.getAction("ClearLinkAnchorAction").setEnabled(anchored);
		modeController.getAction("MakeLinkToAnchorAction").setEnabled(anchored);
		modeController.getAction("MakeLinkFromAnchorAction").setEnabled(anchored);
	}

	public boolean isAnchored() {
		return anchorID != null && !anchorID.isEmpty();
	}

	public String getAnchorIDforNode(final NodeModel node) {
	    String targetID = getAnchorID();
	    final String link;
		// check if anchorID is valid, then set link in current node
		if (isAnchored() && ! targetID.matches("\\w+://")) {

			// extract fileName from target map
			final String targetMapFileName = targetID.substring( targetID.indexOf("/") +1, targetID.lastIndexOf("#") );

			// get fileName of selected node (source)
			final File sourceMapFile = node.getMap().getFile();
			if(sourceMapFile == null) {
				UITools.errorMessage(TextUtils.getRawText("map_not_saved"));
				return null;
			}

			// check if target and source reside within same map
			final String sourceMapFileNameURI = sourceMapFile.toURI().toString();
			if( sourceMapFileNameURI.substring(sourceMapFileNameURI.indexOf("/")+1).equals(targetMapFileName) ) {

				// insert only targetNodeID as link
				link = targetID.substring(targetID.lastIndexOf("#"));

			} else {

				// insert whole targetPath (including targetNodeID) as link for current node
				link = targetID;
			}
		}
		else{
			link = null;
		}
	    return link;
    }

	public void setFormatNodeAsHyperlink(final NodeModel node, final Boolean enabled){
		final NodeLinks links = NodeLinks.createLinkExtension(node);
		IActor actor = new IActor() {
			final Boolean old = links.formatNodeAsHyperlink();
			@Override
			public void act() {
				links.setFormatNodeAsHyperlink(enabled);
				modeController.getMapController().nodeChanged(node);
			}

			@Override
			public void undo() {
				links.setFormatNodeAsHyperlink(old);
				modeController.getMapController().nodeChanged(node);
			}


			@Override
			public String getDescription() {
				return "setFormatNodeAsHyperlink";
			}
		};
		modeController.execute(actor, node.getMap());
	}

	private void fireNodeConnectorChange(ConnectorModel arrowLink) {
	    MapController mapController = Controller.getCurrentModeController().getMapController();
        mapController.nodeChanged(arrowLink.getSource(), NodeLinks.CONNECTOR, arrowLink, arrowLink);
        NodeModel target = arrowLink.getTarget();
        if(target != null)
            mapController.nodeRefresh(target);
    }

	public void deleteMapLinksForClone(final NodeModel model){
		final MapModel map = model.getMap();
		final MapLinks mapLinks = map.getExtension(MapLinks.class);
		if(mapLinks != null){
			IActor actor = new IActor() {
				@Override
				public void undo() {
					mapLinkChanger.insertMapLinks(mapLinks, model);
				}

				@Override
				public String getDescription() {
					return "deleteMapLinks";
				}

				@Override
				public void act() {
					mapLinkChanger.deleteMapLinks(mapLinks, model, model);
				}
			};
			modeController.execute(actor, map);
		}
	}


	public void insertMapLinksForClone(final NodeModel model){
		final MapModel map = model.getMap();
		final MapLinks mapLinks = map.getExtension(MapLinks.class);
		if(mapLinks != null){
			IActor actor = new IActor() {
				@Override
				public void undo() {
					mapLinkChanger.deleteMapLinks(mapLinks, model, model);
				}

				@Override
				public String getDescription() {
					return "deleteMapLinks";
				}

				@Override
				public void act() {
					mapLinkChanger.insertMapLinks(mapLinks, model);
				}
			};
			modeController.execute(actor, map);
		}
	}


	private void setNodeDependantStyle(NodeModel source, final NodeModel target,
			ConnectorModel connector) {
		if(ResourceController.getResourceController().getBooleanProperty("assignsNodeDependantStylesToNewConnectors")) {
			boolean nodeStyleWasSetToConnector = setConnectorStyleSameAsNodeStyleIfAvailable(connector, source);
			if (!nodeStyleWasSetToConnector && source != target)
				setConnectorStyleSameAsNodeStyleIfAvailable(connector, target);
		}
	}

	private boolean setConnectorStyleSameAsNodeStyleIfAvailable(ConnectorModel connector, NodeModel node) {
		IStyle styleExplicitlyAssigned = LogicalStyleModel.getStyle(node);
		IStyle style = styleExplicitlyAssigned != null ? styleExplicitlyAssigned : LogicalStyleController.getController().getFirstStyle(node);
		if(MapStyleModel.DEFAULT_STYLE.equals(style))
			return false;
		MapModel map = node.getMap();
		MapStyleModel mapStyles = MapStyleModel.getExtension(map);
		NodeModel styleNode = mapStyles.getStyleNode(style);
		if(NodeLinks.getSelfConnector(styleNode).isPresent()) {
			setConnectorStyle(connector, style);
			return true;
		}
		return false;
	}
}
