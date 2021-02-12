/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2008 Dimitry Polivaev
 *
 *  This file author is Felix Natter
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
package org.freeplane.features.text.mindmapmode;

import java.util.function.Supplier;

import javax.swing.JEditorPane;
import javax.swing.JScrollPane;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.text.IContentTransformer;
import org.freeplane.features.text.TextController;
import org.freeplane.features.text.TransformationException;
import org.freeplane.features.text.mindmapmode.EditNodeBase.IEditControl;

/**
 * Decorator for IContentTransformer implementations that enables to switch
 * on/off depending on prefs option at transformation time
 * (we do this at transformation in order to not require a restart).
 * 
 * @author Felix Natter
 *
 */
public class ConditionalContentTransformer implements IContentTransformer, IEditBaseCreator {

	private final IContentTransformer target;
	private final String prefsConditionKey;
	
	public ConditionalContentTransformer(IContentTransformer target, final String prefsConditionKey)
	{
		this.target = target;
		this.prefsConditionKey = prefsConditionKey;
	}
	
	@Override
	public int compareTo(IContentTransformer o) {
		return target.compareTo(o);
	}

	@Override
	public Object transformContent(NodeModel node,
			Object nodeProperty, Object content, TextController textController, Mode mode)
			throws TransformationException {
		if (isTransformationActive())
			return target.transformContent(node, nodeProperty, content, textController, mode);
		else
			return content;
	}

	private boolean isTransformationActive() {
		return ResourceController.getResourceController().getBooleanProperty(prefsConditionKey);
	}
	
	

	@Override
	public boolean isFormula(Object content) {
			if (isTransformationActive())
				return target.isFormula(content);
			else
				return false;
	}

	@Override
	public int getPriority() {
		return target.getPriority();
	}

	@Override
	public boolean markTransformation() {
		return target.markTransformation();
	}

	@Override
	public EditNodeBase createEditor(NodeModel node, Object nodeProperty, Object content, IEditControl editControl, boolean editLong) {
		if (target instanceof IEditBaseCreator && isTransformationActive())
			return ((IEditBaseCreator)target).createEditor(node, nodeProperty, content, editControl, editLong);
		else
			return null;
	}

    @Override
    public JEditorPane createTextEditorPane(Supplier<JScrollPane> scrollPaneSupplier, NodeModel node, Object nodeProperty, Object content) {
        if (target instanceof IEditBaseCreator && isTransformationActive())
            return ((IEditBaseCreator)target).createTextEditorPane(scrollPaneSupplier, node, nodeProperty, content);
        else
            return null;
    }

}
