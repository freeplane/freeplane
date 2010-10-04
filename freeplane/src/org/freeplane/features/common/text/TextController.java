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

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.extension.IExtension;
import org.freeplane.core.io.ITreeWriter;
import org.freeplane.core.io.ReadManager;
import org.freeplane.core.io.WriteManager;
import org.freeplane.core.util.HtmlUtils;
import org.freeplane.features.common.filter.FilterController;
import org.freeplane.features.common.map.MapController;
import org.freeplane.features.common.map.ModeController;
import org.freeplane.features.common.map.NodeModel;
import org.freeplane.features.common.note.NoteModel;
import org.freeplane.n3.nanoxml.XMLElement;

/**
 * @author Dimitry Polivaev
 */
public class TextController implements IExtension {
	public enum Direction {
		BACK, BACK_N_FOLD, FORWARD, FORWARD_N_FOLD
	}

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

// 	final private ModeController modeController;

	public TextController() {
		super();
		textTransformers = new LinkedList<ITextTransformer>();
//		this.modeController = modeController;
		final ModeController modeController = Controller.getCurrentModeController();
		final MapController mapController = modeController.getMapController();
		final ReadManager readManager = mapController.getReadManager();
		final WriteManager writeManager = mapController.getWriteManager();
		final NodeTextBuilder textBuilder = new NodeTextBuilder();
		textBuilder.registerBy(readManager, writeManager);
		writeManager.addExtensionElementWriter(DetailTextModel.class, textBuilder);

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

	public String getPlainTextContent(NodeModel nodeModel) {
		final String text = getText(nodeModel);
		return HtmlUtils.htmlToPlain(text);    
	}
}
