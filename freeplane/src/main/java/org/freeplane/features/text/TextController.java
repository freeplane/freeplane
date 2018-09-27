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
package org.freeplane.features.text;

import org.freeplane.core.extension.IExtension;
import org.freeplane.core.io.ReadManager;
import org.freeplane.core.io.WriteManager;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.components.html.CssRuleBuilder;
import org.freeplane.core.util.HtmlProcessor;
import org.freeplane.core.util.HtmlUtils;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.filter.FilterController;
import org.freeplane.features.format.PatternFormat;
import org.freeplane.features.map.ITooltipProvider;
import org.freeplane.features.map.MapController;
import org.freeplane.features.map.NodeChangeEvent;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.freeplane.features.nodestyle.NodeSizeModel;
import org.freeplane.features.nodestyle.NodeStyleController;
import org.freeplane.features.styles.MapStyleModel;
import org.freeplane.view.swing.map.MainView;

import javax.swing.*;
import java.awt.*;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Dimitry Polivaev
 */
public class TextController implements IExtension {
	public static final String DETAILS_HIDDEN = "DETAILS_HIDDEN";
	public static final String FILTER_NODE = "filter_node";
	public static final String FILTER_ANYTEXT = "filter_any_text";
	public static final String FILTER_NOTE = "filter_note";
	public static final String FILTER_PARENT = "filter_parent";
	public static final String FILTER_DETAILS = "filter_details";
	private static final Integer NODE_TOOLTIP = 1;
	private static final Integer DETAILS_TOOLTIP = 2;
	private final List<IContentTransformer> textTransformers;
	protected final ModeController modeController;
	private boolean nodeNumberingEnabled = true;
	public static final String MARK_TRANSFORMED_TEXT = "highlight_formulas";

	public static boolean isMarkTransformedTextSet() {
		return Controller.getCurrentController().getResourceController().getBooleanProperty(MARK_TRANSFORMED_TEXT);
	}

	public static TextController getController() {
		final ModeController modeController = Controller.getCurrentModeController();
		return getController(modeController);
	}

	public static TextController getController(ModeController modeController) {
		return modeController.getExtension(TextController.class);
	}

	public static void install() {
		FilterController.getCurrentFilterController().getConditionFactory().addConditionController(5,
		    new NodeTextConditionController());
	}

	public void install(final ModeController modeController) {
		modeController.addExtension(TextController.class, this);
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
		// this IContentTransformer is unconditional because its outcome
		// is explicitly defined by the user (assigning a format)!
		addTextTransformer(new FormatContentTransformer(this, 50));
		registerDetailsTooltip();
		registerNodeTextTooltip();
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

	public Object getTransformedObject(Object object, final NodeModel nodeModel, Object extension)
	        throws TransformationException {
		if (object instanceof String) {
			String string = (String) object;
			if (string.length() > 0 && string.charAt(0) == '\'') {
				if (nodeModel != null && extension == nodeModel.getUserObject() && isTextFormattingDisabled(nodeModel))
					return string;
				else
					return string.substring(1);
			}
		}
		boolean markTransformation = false;
		for (IContentTransformer textTransformer : getTextTransformers()) {
			try {
				Object in = object;
				object = textTransformer.transformContent(this, in, nodeModel, extension);
				markTransformation = markTransformation || textTransformer.markTransformation() && !in.equals(object);
			}
			catch (RuntimeException e) {
				throw new TransformationException(e);
			}
		}
		if (markTransformation)
			return new HighlightedTransformedObject(object);
		else
			return object;
	}

	public boolean isFormula(Object object, final NodeModel nodeModel, Object extension) {
		if (object instanceof String) {
			String string = (String) object;
			if (string.length() > 0 && string.charAt(0) == '\'') {
				return false;
			}
		}
		for (IContentTransformer textTransformer : getTextTransformers()) {
			if (textTransformer.isFormula(this, object, nodeModel, extension))
				return true;
		}
		return false;
	}

	public Icon getIcon(Object object, final NodeModel nodeModel, Object extension) {
		if (object instanceof HighlightedTransformedObject) {
			return getIcon(((HighlightedTransformedObject) object).getObject(), nodeModel, extension);
		}
		for (IContentTransformer textTransformer : getTextTransformers()) {
			Icon icon = textTransformer.getIcon(this, object, nodeModel, extension);
			if (icon != null)
				return icon;
		}
		return null;
	}

	public boolean isTextFormattingDisabled(final NodeModel nodeModel) {
		return PatternFormat.IDENTITY_PATTERN.equals(getNodeFormat(nodeModel));
	}

	/** returns an error message instead of a normal result if something goes wrong. */
	public Object getTransformedObjectNoFormattingNoThrow(Object data, final NodeModel node, Object extension) {
		try {
			final Object transformedObject = getTransformedObject(data, node, extension);
			if (transformedObject instanceof HighlightedTransformedObject)
				return ((HighlightedTransformedObject) transformedObject).getObject();
			else
				return transformedObject;
		}
		catch (Throwable e) {
			LogUtils.warn(e.getMessage(), e);
			return TextUtils.format("MainView.errorUpdateText", data, e.getLocalizedMessage());
		}
	}

	public Object getTransformedObject(NodeModel node) throws TransformationException {
		final Object userObject = node.getUserObject();
		return getTransformedObject(userObject, node, userObject);
	}

	public Object getTransformedObjectNoThrow(NodeModel node) {
		final Object userObject = node.getUserObject();
		return getTransformedObjectNoFormattingNoThrow(userObject, node, userObject);
	}

	/** convenience method for getTransformedText().toString. */
	public String getTransformedText(Object text, final NodeModel nodeModel, Object extension)
	        throws TransformationException {
		text = getTransformedObject(text, nodeModel, extension);
		return text.toString();
	}

	public String getTransformedTextNoThrow(Object text, final NodeModel nodeModel, Object extension) {
		text = getTransformedObjectNoFormattingNoThrow(text, nodeModel, extension);
		return text.toString();
	}

	public boolean isMinimized(NodeModel node) {
		final ShortenedTextModel shortened = ShortenedTextModel.getShortenedTextModel(node);
		return shortened != null;
	}

	/** returns transformed text converted to plain text. */
	public String getPlainTransformedText(NodeModel nodeModel) {
		final String text = getTransformedTextNoThrow(nodeModel);
		return HtmlUtils.htmlToPlain(text);
	}

	public String getPlainTransformedTextWithoutNodeNumber(NodeModel node) {
		final boolean nodeNumberingWasEnabled = nodeNumberingEnabled;
		nodeNumberingEnabled = false;
		try {
			return getPlainTransformedText(node);
		}
		finally {
			nodeNumberingEnabled = nodeNumberingWasEnabled;
		}
	}

	public String getTransformedTextNoThrow(NodeModel nodeModel) {
		final Object userObject = nodeModel.getUserObject();
		final Object input;
		if (userObject instanceof String && HtmlUtils.isHtmlNode((String) userObject))
			input = HtmlUtils.htmlToPlain((String) userObject);
		else
			input = userObject;
		final String text = getTransformedTextNoThrow(input, nodeModel, userObject);
		return text;
	}

	public String getShortPlainText(NodeModel nodeModel, int maximumCharacters, String continuationMark) {
		String adaptedText = getPlainTransformedTextWithoutNodeNumber(nodeModel);
		return TextUtils.getShortText(adaptedText, maximumCharacters, continuationMark);
	}

	public String getShortPlainText(NodeModel nodeModel) {
		return getShortPlainText(nodeModel, 40, " ...");
	}

	public String getShortText(String longText) {
		String text;
		final boolean isHtml = HtmlUtils.isHtmlNode(longText);
		if (isHtml) {
			text = HtmlUtils.htmlToPlain(longText).trim();
		}
		else {
			text = longText;
		}
		int length = text.length();
		final int eolPosition = text.indexOf('\n');
		final int maxShortenedNodeWidth = ResourceController.getResourceController()
		    .getIntProperty("max_shortened_text_length");
		if (eolPosition == -1 || eolPosition >= length || eolPosition >= maxShortenedNodeWidth) {
			if (length <= maxShortenedNodeWidth) {
				return longText;
			}
			length = maxShortenedNodeWidth;
		}
		else {
			length = eolPosition;
		}
		if (isHtml)
			return new HtmlProcessor(longText).htmlSubstring(0, length);
		else
			return text.substring(0, length);
	}

	public void setDetailsHidden(NodeModel node, boolean isHidden) {
		final DetailTextModel details = DetailTextModel.createDetailText(node);
		if (isHidden == details.isHidden()) {
			return;
		}
		details.setHidden(isHidden);
		node.addExtension(details);
		final NodeChangeEvent nodeChangeEvent = new NodeChangeEvent(node, DETAILS_HIDDEN, !isHidden, isHidden, true, false);
		Controller.getCurrentModeController().getMapController().nodeRefresh(nodeChangeEvent);
	}

	private void registerDetailsTooltip() {
		modeController.addToolTipProvider(DETAILS_TOOLTIP, new ITooltipProvider() {
			@Override
			public String getTooltip(final ModeController modeController, NodeModel node, Component view) {
				return getTooltip(modeController, node, (MainView) view);
			}

			private String getTooltip(final ModeController modeController, NodeModel node, MainView view) {
				final DetailTextModel detailText = DetailTextModel.getDetailText(node);
				if (detailText == null || !(detailText.isHidden() || ShortenedTextModel.isShortened(node))) {
					return null;
				}
				final NodeStyleController style = modeController.getExtension(NodeStyleController.class);
				final MapStyleModel model = MapStyleModel.getExtension(node.getMap());
				final NodeModel detailStyleNode = model.getStyleNodeSafe(MapStyleModel.DETAILS_STYLE);
				Font detailFont = style.getFont(detailStyleNode);
				Color detailBackground = style.getBackgroundColor(detailStyleNode);
				Color detailForeground = style.getColor(detailStyleNode);
				final int alignment = style.getHorizontalTextAlignment(detailStyleNode).swingConstant;
				float zoom = view.getNodeView().getMap().getZoom();
				final StringBuilder htmlBodyStyle = new StringBuilder("<body><div style=\"")
				    .append(new CssRuleBuilder()
				        .withHTMLFont(detailFont)
				        .withColor(detailForeground)
				        .withBackground(detailBackground)
				        .withAlignment(alignment)
				        .withMaxWidthAsPt(zoom, NodeSizeModel.getMaxNodeWidth(detailStyleNode),
				            style.getMaxWidth(node)))
				    .append("\">");
				String noteText = detailText.getHtml();
				final String tooltipText = noteText.replaceFirst("<body>", htmlBodyStyle.toString())
				    .replaceFirst("</body>", "</div></body>");
				return tooltipText;
			}
		});
	}

	private void registerNodeTextTooltip() {
		modeController.addToolTipProvider(NODE_TOOLTIP, new ITooltipProvider() {
			@Override
			public String getTooltip(final ModeController modeController, NodeModel node, Component view) {
				return getTooltip(modeController, node, (MainView) view);
			}

			private String getTooltip(final ModeController modeController, NodeModel node, MainView view) {
				if (!ShortenedTextModel.isShortened(node)) {
					return null;
				}
				final NodeStyleController style = modeController.getExtension(NodeStyleController.class);
				final Font font = style.getFont(node);
				float zoom = view.getNodeView().getMap().getZoom();
				final StringBuilder htmlBodyStyle = new StringBuilder("<body><div style=\"")
				    .append(new CssRuleBuilder().withHTMLFont(font)
				        .withColor(view.getForeground())
				        .withBackground(view.getNodeView().getTextBackground())
				        .withAlignment(view.getHorizontalAlignment())
				        .withMaxWidthAsPt(zoom, style.getMaxWidth(node)));
				final Object data = node.getUserObject();
				String text;
				try {
					text = getTransformedText(data, node, data);
					if (text.equals(getShortText(text)))
						return null;
				}
				catch (Exception e) {
					text = TextUtils.format("MainView.errorUpdateText", data, e.getLocalizedMessage());
					htmlBodyStyle.append("color:red;");
				}
				htmlBodyStyle.append("\">");
				if (!HtmlUtils.isHtmlNode(text)) {
					text = HtmlUtils.plainToHTML(text);
				}
				final String tooltipText = text.replaceFirst("<body>", htmlBodyStyle.toString())
				    .replaceFirst("</body>", "</div></body>");
				return tooltipText;
			}
		});
	}

	public void setIsMinimized(NodeModel node, boolean shortened) {
		boolean oldState = ShortenedTextModel.getShortenedTextModel(node) != null;
		if (oldState == shortened) {
			return;
		}
		if (shortened) {
			ShortenedTextModel.createShortenedTextModel(node);
		}
		else {
			node.removeExtension(ShortenedTextModel.class);
		}
		Controller.getCurrentModeController().getMapController().nodeChanged(node, "SHORTENER", oldState, shortened);
	}

	public boolean parseData() {
		return false;
	}

	public String getNodeFormat(NodeModel node) {
		return modeController.getExtension(NodeStyleController.class).getNodeFormat(node);
	}

	public boolean getNodeNumbering(NodeModel node) {
		return nodeNumberingEnabled && modeController.getExtension(NodeStyleController.class).getNodeNumbering(node);
	}

	public ModeController getModeController() {
		return modeController;
	}

	public boolean canEdit() {
		return false;
	}
}
