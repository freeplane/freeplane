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

import java.util.LinkedList;
import java.util.List;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.extension.IExtension;
import org.freeplane.core.io.ReadManager;
import org.freeplane.core.io.WriteManager;
import org.freeplane.core.util.HtmlUtils;
import org.freeplane.features.common.filter.FilterController;
import org.freeplane.features.common.map.MapController;
import org.freeplane.features.common.map.ModeController;
import org.freeplane.features.common.map.NodeModel;
import org.freeplane.features.common.text.ShortenedTextModel.State;

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
		modeController.addAction(new SetShortenerStateAction(null));
		for (State s : State.values()){
			modeController.addAction(new SetShortenerStateAction(s));
		}
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
	
	public State getShortenerState(NodeModel node){
		{
			final ShortenedTextModel shortened = ShortenedTextModel.getShortenedTextModel(node);
			if (shortened != null) {
				return shortened.getState();
			}
		}
		return State.DISABLED;
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
		Controller.getCurrentModeController().getMapController().nodeChanged(node, "DETAILS_HIDDEN", ! isHidden, isHidden);    }

	public void setShortenerState(NodeModel node, State newState) {
		final ShortenedTextModel shortener = ShortenedTextModel.createShortenedTextModel(node);
		final State oldState = shortener.getState();
		if(newState == oldState || newState != null && newState.equals(oldState)){
			return;
		}
		shortener.setState(newState);
		node.addExtension(shortener);
		Controller.getCurrentModeController().getMapController().nodeChanged(node, "SHORTENER", oldState, newState);   
    }
}
