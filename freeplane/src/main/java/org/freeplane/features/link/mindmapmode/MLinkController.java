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
import java.awt.EventQueue;
import java.awt.GraphicsEnvironment;
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
import java.util.ArrayList;
import java.util.List;
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
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.LengthUnits;
import org.freeplane.core.ui.components.JComboBoxWithBorder;
import org.freeplane.core.ui.components.RenderedContent;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.undo.IActor;
import org.freeplane.core.util.Quantity;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.link.ArrowType;
import org.freeplane.features.link.ConnectorModel;
import org.freeplane.features.link.DashVariant;
import org.freeplane.features.link.ConnectorModel.Shape;
import org.freeplane.features.link.HyperTextLinkModel;
import org.freeplane.features.link.LinkController;
import org.freeplane.features.link.MapLinks;
import org.freeplane.features.link.NodeLinkModel;
import org.freeplane.features.link.NodeLinks;
import org.freeplane.features.map.IExtensionCopier;
import org.freeplane.features.map.IMapChangeListener;
import org.freeplane.features.map.IMapSelection;
import org.freeplane.features.map.MapChangeEvent;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeDeletionEvent;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.map.NodeMoveEvent;
import org.freeplane.features.map.mindmapmode.DocuMapAttribute;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.freeplane.features.spellchecker.mindmapmode.SpellCheckerController;
import org.freeplane.features.styles.LogicalStyleKeys;
import org.freeplane.features.url.UrlManager;

/**
 * @author Dimitry Polivaev
 */
public class MLinkController extends LinkController {
	private static class StyleCopier implements IExtensionCopier {

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

		public void remove(Object key, NodeModel from) {
			if (!key.equals(LogicalStyleKeys.NODE_STYLE)) {
				return;
			}
			final NodeLinks model = NodeLinks.getLinkExtension(from);
			if(model != null)
				model.setFormatNodeAsHyperlink(null);
        }

		public void remove(Object key, NodeModel from, NodeModel which) {
	        if(NodeLinks.formatNodeAsHyperlink(which) != null)
	        	remove(key, from);
        }
		public void resolveParentExtensions(Object key, NodeModel to) {
        }
	}
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
			NodeLinks nodeLinks = NodeLinks.createLinkExtension(source);
			arrowLink = new ConnectorModel(source, targetID,
				getStandardConnectorArrows(),
				getStandardConnectorColor(), getStandardConnectorAlpha(),
				getStandardConnectorShape(), getStandardConnectorWidth(),
				getStandardLabelFontFamily(), getStandardLabelFontSize());
			nodeLinks.addArrowlink(arrowLink);
			fireNodeConnectorChange(source, arrowLink);
		}

		public String getDescription() {
			return "addLink";
		}

		public void undo() {
			final NodeLinks nodeLinks = NodeLinks.getLinkExtension(source);
			nodeLinks.removeArrowlink(arrowLink);
			fireNodeConnectorChange(source, arrowLink);
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
			fireNodeConnectorChange(model.getSource(), model);
		}

		public String getDescription() {
			return "setTargetLabel";
		}

		public void undo() {
			model.setTargetLabel(oldLabel);
			fireNodeConnectorChange(model.getSource(), model);
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
			fireNodeConnectorChange(model.getSource(), model);
		}

		public String getDescription() {
			return "setSourceLabel";
		}

		public void undo() {
			model.setSourceLabel(oldLabel);
			fireNodeConnectorChange(model.getSource(), model);
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
			fireNodeConnectorChange(model.getSource(), model);
		}

		public String getDescription() {
			return "setMiddleLabel";
		}

		public void undo() {
			model.setMiddleLabel(oldLabel);
			fireNodeConnectorChange(model.getSource(), model);
		}
	}

	/**
	 * @author Dimitry Polivaev
	 */
	private final class MapLinkChanger implements IMapChangeListener {

		public void mapChanged(final MapChangeEvent event) {
		}

		public void onNodeDeleted(NodeDeletionEvent nodeDeletionEvent) {
		}

		public void onNodeInserted(final NodeModel parent, final NodeModel model, final int newIndex) {
			EventQueue.invokeLater(new Runnable() {
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

		public void onNodeMoved(NodeMoveEvent nodeMoveEvent) {
		}

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
					fireNodeConnectorChange(source, (ConnectorModel) link);
			}
		}

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
		(modeController.getMapController()).addMapChangeListener(mapLinkChanger);
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
				fireNodeConnectorChange(link.getSource(), link);
			}

			public String getDescription() {
				return "changeArrowsOfArrowLink";
			}

			public void undo() {
				link.setStartArrow(oldStartArrow);
				link.setEndArrow(oldEndArrow);
				fireNodeConnectorChange(link.getSource(), link);
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
		modeController.addAction(new AddLocalLinkAction());
		modeController.addAction(new AddMenuItemLinkAction());
		modeController.addAction(new AddExecutionLinkAction());
		modeController.addAction(new ExtractLinkFromTextAction());
		modeController.addAction(new SetLinkAnchorAction());
		modeController.addAction(new MakeLinkToAnchorAction());
		modeController.addAction(new MakeLinkFromAnchorAction());
		modeController.addAction(new ClearLinkAnchorAction());
	}

	@Override
	protected void createArrowLinkPopup(final ConnectorModel link, final JComponent arrowLinkPopup) {
		super.createArrowLinkPopup(link, arrowLinkPopup);
		addClosingAction(arrowLinkPopup, new RemoveConnectorAction(this, link));

		addSeparator(arrowLinkPopup);
		addAction(arrowLinkPopup, new ConnectorColorAction(this, link));

		final JSlider transparencySlider = new JSlider(20, 255, link.getAlpha());
		transparencySlider.setMinorTickSpacing(20);
		transparencySlider.setPaintTicks(true);
		transparencySlider.setSnapToTicks(true);
		transparencySlider.setPaintTrack(true);
		addPopupComponent(arrowLinkPopup, TextUtils.getText("edit_transparency_label"), transparencySlider);

		addSeparator(arrowLinkPopup);


		AFreeplaneAction[] arrowActions = new AFreeplaneAction[]{
                new ChangeConnectorArrowsAction(this, "none", link, ArrowType.NONE, ArrowType.NONE),
                new ChangeConnectorArrowsAction(this, "forward", link, ArrowType.NONE, ArrowType.DEFAULT),
                new ChangeConnectorArrowsAction(this, "backward", link, ArrowType.DEFAULT, ArrowType.NONE),
                new ChangeConnectorArrowsAction(this, "both", link, ArrowType.DEFAULT, ArrowType.DEFAULT)
		};
        JComboBoxWithBorder connectorArrows = createActionBox(arrowActions);
		addPopupComponent(arrowLinkPopup, TextUtils.getText("connector_arrows"), connectorArrows);

        final boolean twoNodesConnector = ! link.getSource().equals(link.getTarget());
        AFreeplaneAction[] shapeActions;
        if(twoNodesConnector){
            shapeActions = new AFreeplaneAction[] {
                    new ChangeConnectorShapeAction(this, link, Shape.CUBIC_CURVE),
                    new ChangeConnectorShapeAction(this, link, Shape.LINE),
                    new ChangeConnectorShapeAction(this, link, Shape.LINEAR_PATH),
                    new ChangeConnectorShapeAction(this, link, Shape.EDGE_LIKE)
            };
        }
        else {
            shapeActions = new AFreeplaneAction[] {
                    new ChangeConnectorShapeAction(this, link, Shape.CUBIC_CURVE),
                    new ChangeConnectorShapeAction(this, link, Shape.LINE),
                    new ChangeConnectorShapeAction(this, link, Shape.LINEAR_PATH)
            };
        }
            final JComboBoxWithBorder connectorShapes = createActionBox(shapeActions);
            addPopupComponent(arrowLinkPopup, TextUtils.getText("connector_shapes"), connectorShapes);

            
        ArrayList<AFreeplaneAction> dashActions = new ArrayList<AFreeplaneAction>();
        for (DashVariant  variant : DashVariant.values())
        	dashActions.add(new ChangeConnectorDashAction(this, link, variant));
        final JComboBoxWithBorder connectorDashes = createActionBox(dashActions.toArray(new AFreeplaneAction[dashActions.size()]));
		final int verticalMargin = new Quantity<>(3, LengthUnits.pt).toBaseUnitsRounded();
        connectorDashes.setVerticalMargin(verticalMargin);
        addPopupComponent(arrowLinkPopup, TextUtils.getText("connector_lines"), connectorDashes);

		final SpinnerNumberModel widthModel = new SpinnerNumberModel(link.getWidth(),1, 32, 1);
		final JSpinner widthSpinner = new JSpinner(widthModel);
		addPopupComponent(arrowLinkPopup, TextUtils.getText("edit_width_label"), widthSpinner);

		addSeparator(arrowLinkPopup);

		{
			final GraphicsEnvironment gEnv = GraphicsEnvironment.getLocalGraphicsEnvironment();
			final String[] envFonts = gEnv.getAvailableFontFamilyNames();
			DefaultComboBoxModel fonts = new DefaultComboBoxModel(envFonts);
			fonts.setSelectedItem(link.getLabelFontFamily());
			JComboBox fontBox = new JComboBoxWithBorder(fonts);
			fontBox.setEditable(false);
			addPopupComponent(arrowLinkPopup, TextUtils.getText("edit_label_font_family"), fontBox);
			fontBox.addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent e) {
					final Object item = e.getItem();
					if(item != null)
						setLabelFontFamily(link, item.toString());
				}
			});
		}
		{
			final Integer[] sizes = {4, 6, 8, 10, 12, 14, 16, 18, 24, 36};
			DefaultComboBoxModel sizesModel = new DefaultComboBoxModel(sizes);
			sizesModel.setSelectedItem(link.getLabelFontSize());
			JComboBox sizesBox = new JComboBoxWithBorder(sizesModel);
			sizesBox.setEditable(true);
			addPopupComponent(arrowLinkPopup, TextUtils.getText("edit_label_font_size"), sizesBox);
			sizesBox.addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent e) {
					final Object item = e.getItem();
					if(item != null){
						final int size;
						if(item instanceof Integer)
							size = (Integer)item;
						else{
							try{
								size = Integer.valueOf(item.toString());
								if(size <=0)
									return;
							}
							catch (NumberFormatException ex){
								return;
							}
						}

						setLabelFontSize(link, size);
					}
				}
			});
		}
		final JTextArea sourceLabelEditor;
            sourceLabelEditor = new JTextArea(link.getSourceLabel());
            addTextEditor(arrowLinkPopup, "edit_source_label", sourceLabelEditor);

		final JTextArea middleLabelEditor = new JTextArea(link.getMiddleLabel());
        addTextEditor(arrowLinkPopup, "edit_middle_label"  ,middleLabelEditor);

        final JTextArea targetLabelEditor ;
            targetLabelEditor = new JTextArea(link.getTargetLabel());
        addTextEditor(arrowLinkPopup, "edit_target_label", targetLabelEditor);

		arrowLinkPopup.addHierarchyListener(new HierarchyListener() {
            private Component focusOwner;
            private Window dialog;
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
                    setSourceLabel(link, sourceLabelEditor.getText());
                    setTargetLabel(link, targetLabelEditor.getText());
                setMiddleLabel(link, middleLabelEditor.getText());
                setAlpha(link, transparencySlider.getValue());
                setWidth(link, widthModel.getNumber().intValue());
            }

		});

	}

    @SuppressWarnings("serial")
    protected JComboBoxWithBorder createActionBox(AFreeplaneAction[] items) {
        final JComboBoxWithBorder box = new JComboBoxWithBorder();
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
		final boolean enterConfirms = ResourceController.getResourceController().getBooleanProperty("el__enter_confirms_by_default");
		final KeyStroke close = KeyStroke.getKeyStroke(enterConfirms ? "ENTER" : "alt ENTER");
		inputMap.put(close, CLOSE);
		actionMap.put(CLOSE, new ClosePopupAction(CLOSE));

		final KeyStroke enter = KeyStroke.getKeyStroke(! enterConfirms ? "ENTER" : "alt ENTER");
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
		addPopupComponent(popup, TextUtils.getText(label), scrollPane);
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
				fireNodeConnectorChange(node, arrowLink);
			}

			public String getDescription() {
				return "setConnectorColor";
			}

			public void undo() {
				arrowLink.setColor(oldColor);
				final NodeModel node = arrowLink.getSource();
				fireNodeConnectorChange(node, arrowLink);
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
				fireNodeConnectorChange(node, arrowLink);
			}

			public String getDescription() {
				return "setConnectorDash";
			}

			public void undo() {
				arrowLink.setDash(oldDash);
				final NodeModel node = arrowLink.getSource();
				fireNodeConnectorChange(node, arrowLink);
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
				fireNodeConnectorChange(link.getSource(), link);
			}

			public String getDescription() {
				return "setArrowLinkEndPoints";
			}

			public void undo() {
				link.setStartInclination(oldStartPoint);
				link.setEndInclination(oldEndPoint);
				fireNodeConnectorChange(link.getSource(), link);
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
		final URI uri = relativeLink(argUri, node, linkType);
		final IActor actor = new IActor() {
			private URI oldlink;
			private String oldTargetID;

			public void act() {
				NodeLinks links = NodeLinks.getLinkExtension(node);
				if (links != null) {
					oldlink = links.getHyperLink(node);
					oldTargetID = links.removeLocalHyperLink(node);
				}
				else {
					links = NodeLinks.createLinkExtension(node);
				}
				if (uri != null && uri.toString().startsWith("#")) {
					links.setLocalHyperlink(node, uri.toString().substring(1));
				}
				else
					links.setHyperLink(uri);
				Controller.getCurrentModeController().getMapController().nodeChanged(node, NodeLinks.HYPERLINK_CHANGED, oldlink, uri);

			}

			public String getDescription() {
				return "setLink";
			}

			public void undo() {
				final NodeLinks links = NodeLinks.getLinkExtension(node);
				URI undoneLink = links.getHyperLink(node);
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

	public void removeArrowLink(final ConnectorModel arrowLink) {
		final IActor actor = new IActor() {
			public void act() {
				final NodeModel source = arrowLink.getSource();
				final NodeLinks nodeLinks = NodeLinks.getLinkExtension(source);
				nodeLinks.removeArrowlink(arrowLink);
				fireNodeConnectorChange(source, arrowLink);
			}

			public String getDescription() {
				return "removeArrowLink";
			}

			public void undo() {
				final NodeModel source = arrowLink.getSource();
				NodeLinks nodeLinks = NodeLinks.createLinkExtension(source);
				nodeLinks.addArrowlink(arrowLink);
				fireNodeConnectorChange(source, arrowLink);
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
				fireNodeConnectorChange(node, connector);
			}

			public String getDescription() {
				return "setConnectorShape";
			}

			public void undo() {
				connector.setShape(oldShape);
				final NodeModel node = connector.getSource();
				fireNodeConnectorChange(node, connector);
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
				fireNodeConnectorChange(node, connector);
			}

			public String getDescription() {
				return "setConnectorWidth";
			}

			public void undo() {
				connector.setWidth(oldWidth);
				final NodeModel node = connector.getSource();
				fireNodeConnectorChange(node, connector);
			}
		};
		Controller.getCurrentModeController().execute(actor, connector.getSource().getMap());
	}


	public void setLabelFontSize(final ConnectorModel connector, final int width) {
		final int oldWidth = connector.getLabelFontSize();
		if (oldWidth == width) {
			return;
		}
		final IActor actor = new IActor() {
			public void act() {
				connector.setLabelFontSize(width);
				final NodeModel node = connector.getSource();
				fireNodeConnectorChange(node, connector);
			}

			public String getDescription() {
				return "setConnectorWidth";
			}

			public void undo() {
				connector.setLabelFontSize(oldWidth);
				final NodeModel node = connector.getSource();
				fireNodeConnectorChange(node, connector);
			}
		};
		Controller.getCurrentModeController().execute(actor, connector.getSource().getMap());
	}


	public void setLabelFontFamily(final ConnectorModel connector, final String family) {
		final String oldFamily = connector.getLabelFontFamily();
		if (oldFamily.equals(family)) {
			return;
		}
		final IActor actor = new IActor() {
			public void act() {
				connector.setLabelFontFamily(family);
				final NodeModel node = connector.getSource();
				fireNodeConnectorChange(node, connector);
			}

			public String getDescription() {
				return "setConnectorWidth";
			}

			public void undo() {
				connector.setLabelFontFamily(oldFamily);
				final NodeModel node = connector.getSource();
				fireNodeConnectorChange(node, connector);
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
				fireNodeConnectorChange(node, connector);
			}

			public String getDescription() {
				return "setConnectorAlpha";
			}

			public void undo() {
				connector.setAlpha(oldAlpha);
				final NodeModel node = connector.getSource();
				fireNodeConnectorChange(node, connector);
			}
		};
		Controller.getCurrentModeController().execute(actor, connector.getSource().getMap());
	}

	@Override
    @SuppressWarnings("deprecation")
    public void loadURI(URI uri) {
		UrlManager.getController().loadURL(uri);
    }

	@Override
	protected void loadURL(final NodeModel node, final ActionEvent e) {
		// load as documentation map if the node belongs to a documentation map
		boolean addDocuMapAttribute = node.getMap().containsExtension(DocuMapAttribute.class)
				&& ! modeController.containsExtension(DocuMapAttribute.class);
		if(addDocuMapAttribute){
			modeController.addExtension(DocuMapAttribute.class, DocuMapAttribute.instance);
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
			final String targetMapFileName = targetID.substring( targetID.indexOf("/") +1, targetID.indexOf("#") );

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
				link = targetID.substring(targetID.indexOf("#"));

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
			public void act() {
				links.setFormatNodeAsHyperlink(enabled);
				modeController.getMapController().nodeChanged(node);
			}

			public void undo() {
				links.setFormatNodeAsHyperlink(old);
				modeController.getMapController().nodeChanged(node);
			}


			public String getDescription() {
				return "setFormatNodeAsHyperlink";
			}
		};
		modeController.execute(actor, node.getMap());
	}

	private void fireNodeConnectorChange(NodeModel source, ConnectorModel arrowLink) {
	    Controller.getCurrentModeController().getMapController().nodeChanged(source, NodeLinks.CONNECTOR, arrowLink, arrowLink);
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
}
