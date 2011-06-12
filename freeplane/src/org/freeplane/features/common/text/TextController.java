/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2008 Dimitry Polivaev
 *
 *  This file author is Dimitry Polivaev
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
package org.freeplane.features.common.text;

import java.awt.Font;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.extension.IExtension;
import org.freeplane.core.io.ReadManager;
import org.freeplane.core.io.WriteManager;
import org.freeplane.core.util.HtmlUtils;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.common.filter.FilterController;
import org.freeplane.features.common.map.ITooltipProvider;
import org.freeplane.features.common.map.MapController;
import org.freeplane.features.common.map.ModeController;
import org.freeplane.features.common.map.NodeModel;
import org.freeplane.features.common.nodestyle.NodeStyleController;
import org.freeplane.features.common.nodestyle.NodeStyleModel;
import org.freeplane.features.common.styles.IStyle;
import org.freeplane.features.common.styles.LogicalStyleController;
import org.freeplane.features.common.styles.MapStyleModel;


/**
 * @author Dimitry Polivaev
 */
public class TextController implements IExtension {
	public static final String FILTER_NODE = "filter_node";
	public static final String FILTER_PARENT = "filter_parent";
	public static final String FILTER_DETAILS = "filter_details";
	private static final Integer NODE_TOOLTIP = 1;
	private static final Integer DETAILS_TOOLTIP = 2;
	private final List<IContentTransformer> textTransformers;
	protected final ModeController modeController;

	public static TextController getController() {
		final ModeController modeController = Controller.getCurrentModeController();
		return getController(modeController);
	}

	public static TextController getController(ModeController modeController) {
		return (TextController) modeController.getExtension(TextController.class);
    }

	public static void install() {
		FilterController.getCurrentFilterController().getConditionFactory().addConditionController(0,
		    new NodeTextConditionController());
	}

	public static void install( final TextController textController) {
		final ModeController modeController = Controller.getCurrentModeController();
		modeController.addExtension(TextController.class, textController);
	}

	public TextController(final ModeController modeController) {
		super();
		textTransformers = new LinkedList<IContentTransformer>();
		this.modeController = modeController;
		final MapController mapController = modeController.getMapController();
		final ReadManager readManager = mapController.getReadManager();
		final WriteManager writeManager = mapController.getWriteManager();
		final NodeTextBuilder textBuilder = new NodeTextBuilder();
		textBuilder.registerBy(readManager, writeManager);
		writeManager.addExtensionElementWriter(DetailTextModel.class, textBuilder);
		writeManager.addExtensionAttributeWriter(ShortenedTextModel.class, textBuilder);

		modeController.addAction(new ToggleDetailsAction());
		modeController.addAction(new SetShortenerStateAction());
//		modeController.addAction(new ToggleNodeNumberingAction());
		addTextTransformer(new FormatContentTransformer(this, 50));
	}

	public void addTextTransformer(IContentTransformer textTransformer) {
	    textTransformers.add(textTransformer);
	    Collections.sort(textTransformers);
    }

	public List<IContentTransformer> getTextTransformers() {
	    return textTransformers;
	}
	public void removeTextTransformer(IContentTransformer textTransformer) {
	    textTransformers.remove(textTransformer);
    }

	public String getText(NodeModel nodeModel) {
		return nodeModel.getText();
	}
	
	/** @throws RuntimeException if something goes wrong. */
	public String getTransformedText(Object text, final NodeModel nodeModel, Object extension) {
		text = getTransformedObject(text, nodeModel, extension);
		return text.toString();
	}

	public Object getTransformedObject(Object object, final NodeModel nodeModel, Object extension) {
	    for (IContentTransformer textTransformer : getTextTransformers()) {
			object = textTransformer.transformContent(object, nodeModel, extension);
		}
	    return object;
    }
	
	/** returns an error message instead of a normal result if something goes wrong. */
	public String getTransformedTextNoThrow(Object data, final NodeModel node, Object extension) {
		try {
			return getTransformedText(data, node, extension);
		}
		catch (Throwable e) {
			LogUtils.warn(e.getMessage(), e);
			    return TextUtils.format("MainView.errorUpdateText", data, e.getLocalizedMessage());
		}
	}

	public boolean getIsShortened(NodeModel node){
		final ShortenedTextModel shortened = ShortenedTextModel.getShortenedTextModel(node);
		return shortened != null;
	}

	// FIXME: This should be getPlainTransformedText() since getText() does not transform too
	/** returns transformed text converted to plain text. */
	public String getPlainTextContent(NodeModel nodeModel) {
		final Object userObject = nodeModel.getUserObject();
		final Object input;
		if(userObject instanceof String &&  HtmlUtils.isHtmlNode((String) userObject))
			input = HtmlUtils.htmlToPlain((String) userObject);
		else
			input = userObject;
		final String text = getTransformedTextNoThrow(input, nodeModel, userObject);
		return HtmlUtils.htmlToPlain(text);    
	}

	public String getShortText(NodeModel nodeModel) {
		String adaptedText = TextController.getController().getPlainTextContent(nodeModel);
		if (adaptedText.length() > 40) {
			adaptedText = adaptedText.substring(0, 40) + " ...";
		}
		return adaptedText;
	}

	public void setDetailsHidden(NodeModel node, boolean isHidden) {
		final DetailTextModel details = DetailTextModel.createDetailText(node);
		if(isHidden == details.isHidden()){
			return;
		}
		details.setHidden(isHidden);
		node.addExtension(details);
		setDetailsTooltip(node);
		Controller.getCurrentModeController().getMapController().nodeChanged(node, "DETAILS_HIDDEN", ! isHidden, isHidden);    
	}

	protected void setDetailsTooltip(final NodeModel node) {
		final DetailTextModel detailText = DetailTextModel.getDetailText(node);
		if (detailText != null) {
			(Controller.getCurrentModeController().getMapController()).setToolTip(node, DETAILS_TOOLTIP, new ITooltipProvider() {
				public String getTooltip(ModeController modeController) {
					 if (! (detailText.isHidden() || ShortenedTextModel.isShortened(node)) ){
						 return null;
					 }
					final NodeStyleController style = (NodeStyleController) modeController.getExtension(NodeStyleController.class);
					final Font font = style.getFont(node);
					final StringBuilder rule = new StringBuilder();
					rule.append("font-family: " + font.getFamily() + ";");
					rule.append("font-size: " + font.getSize() + "pt;");
					rule.append("margin-top:0;");
					String noteText= detailText.getHtml();
					final String tooltipText = noteText.replaceFirst("<body>", "<body><div style=\"" + rule + "\">")
					    .replaceFirst("</body>", "</div></body>");
					return tooltipText;
				}
			});
			return;
		}
		(Controller.getCurrentModeController().getMapController()).setToolTip(node, DETAILS_TOOLTIP, null);
	}

	protected void setNodeTextTooltip(final NodeModel node) {
		(Controller.getCurrentModeController().getMapController()).setToolTip(node, NODE_TOOLTIP, new ITooltipProvider() {
			    public String getTooltip(final ModeController modeController) {
				    if (!ShortenedTextModel.isShortened(node)) {
					    return null;
				    }
				    final NodeStyleController style = (NodeStyleController) modeController.getExtension(NodeStyleController.class);
				    final Font font = style.getFont(node);
				    final StringBuilder rule = new StringBuilder();
				    rule.append("font-family: " + font.getFamily() + ";");
				    rule.append("font-size: " + font.getSize() + "pt;");
				    rule.append("margin-top:0;");
				    final Object data = node.getUserObject();
				    String text;
				    try {
					    text = getTransformedText(data, node, data);
				    }
				    catch (Exception e) {
					    text = TextUtils.format("MainView.errorUpdateText", data, e.getLocalizedMessage());
					    rule.append("color:red;");
				    }
				    if (!HtmlUtils.isHtmlNode(text)) {
					    text = HtmlUtils.plainToHTML(text);
				    }
				    final String tooltipText = text.replaceFirst("<body>", "<body><div style=\"" + rule + "\">")
				        .replaceFirst("</body>", "</div></body>");
				    return tooltipText;
			    }
		});
	}

	public void setIsShortened(NodeModel node, boolean shortened) {
		boolean oldState = ShortenedTextModel.getShortenedTextModel(node) != null;
		if(oldState == shortened){
			return;
		}
		if(shortened){
			ShortenedTextModel.createShortenedTextModel(node);
			setNodeTextTooltip(node);
		}
		else{
			node.removeExtension(ShortenedTextModel.class);
		}
		Controller.getCurrentModeController().getMapController().nodeChanged(node, "SHORTENER", oldState, shortened);   
	}

	public void toggleShortened(NodeModel node) {
		setIsShortened(node, ! getIsShortened(node)); 
    }

	public String getNodeFormat(NodeModel node) {
		Collection<IStyle> collection = LogicalStyleController.getController(modeController).getStyles(node);
		final MapStyleModel model = MapStyleModel.getExtension(node.getMap());
		for(IStyle styleKey : collection){
			final NodeModel styleNode = model.getStyleNode(styleKey);
			if (styleNode == null) {
				continue;
			}
			final String format = NodeStyleModel.getNodeFormat(styleNode);
			if (format != null) {
				return format;
			}
		}
		return null;
    }

	public boolean getNodeNumbering(NodeModel node) {
		Collection<IStyle> collection = LogicalStyleController.getController(modeController).getStyles(node);
		final MapStyleModel model = MapStyleModel.getExtension(node.getMap());
		for(IStyle styleKey : collection){
			final NodeModel styleNode = model.getStyleNode(styleKey);
			if (styleNode == null) {
				continue;
			}
			final Boolean numbering = NodeStyleModel.getNodeNumbering(styleNode);
			if (numbering != null) {
				return numbering;
			}
		}
		return false;
    }

	public Object getTransformedObject(NodeModel node) {
		final Object userObject = node.getUserObject();
		return getTransformedObject(userObject, node, userObject);
    }
}
