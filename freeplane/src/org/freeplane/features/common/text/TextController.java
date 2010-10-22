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
import java.util.LinkedList;
import java.util.List;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.extension.IExtension;
import org.freeplane.core.io.ReadManager;
import org.freeplane.core.io.WriteManager;
import org.freeplane.core.util.HtmlUtils;
import org.freeplane.features.common.filter.FilterController;
import org.freeplane.features.common.map.ITooltipProvider;
import org.freeplane.features.common.map.MapController;
import org.freeplane.features.common.map.ModeController;
import org.freeplane.features.common.map.NodeModel;
import org.freeplane.features.common.nodestyle.NodeStyleController;
import org.freeplane.features.common.note.NoteModel;


/**
 * @author Dimitry Polivaev
 */
public class TextController implements IExtension {
	public enum Direction {
		BACK, BACK_N_FOLD, FORWARD, FORWARD_N_FOLD
	}
	public static final String FILTER_NODE = "filter_node";
	public static final String FILTER_PARENT = "filter_parent";
	public static final String FILTER_DETAILS = "filter_details";
	private static final Integer NODE_TOOLTIP = 1;
	private static final Integer DETAILS_TOOLTIP = 2;

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

	final private ModeController modeController;

// 	final private ModeController modeController;

	public TextController(final ModeController modeController) {
		super();
		textTransformers = new LinkedList<ITextTransformer>();
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
	}

	public void addTextTransformer(ITextTransformer textTransformer) {
	    textTransformers.add(textTransformer);
    }
	public List<ITextTransformer> getTextTransformers() {
	    return textTransformers;
	}
	public void removeTextTransformer(ITextTransformer textTransformer) {
	    textTransformers.remove(textTransformer);
    }

	final private List<ITextTransformer> textTransformers;
	
	public String getText(NodeModel nodeModel) {
		String nodeText = nodeModel.getText();
		for (ITextTransformer textTransformer : getTextTransformers()) {
			nodeText = textTransformer.transform(nodeText, nodeModel);
		}
		return nodeText;
	}

	public boolean getIsShortened(NodeModel node){
		final ShortenedTextModel shortened = ShortenedTextModel.getShortenedTextModel(node);
		return shortened != null;
	}

	public String getPlainTextContent(NodeModel nodeModel) {
		final String text = getText(nodeModel);
		return HtmlUtils.htmlToPlain(text);    
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
				public String getTooltip() {
					 if (! (detailText.isHidden() || ShortenedTextModel.isShortened(node)) ){
						 return null;
					 }
					final NodeStyleController style = (NodeStyleController) Controller.getCurrentModeController().getExtension(
					    NodeStyleController.class);
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
			public String getTooltip() {
				if (! ShortenedTextModel.isShortened(node)){
					return null;
				}
				final NodeStyleController style = (NodeStyleController) Controller.getCurrentModeController().getExtension(
					NodeStyleController.class);
				final Font font = style.getFont(node);
				final StringBuilder rule = new StringBuilder();
				rule.append("font-family: " + font.getFamily() + ";");
				rule.append("font-size: " + font.getSize() + "pt;");
				rule.append("margin-top:0;");
				String text = getText(node);
				if(! HtmlUtils.isHtmlNode(text)) {
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
}
