/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2008 Joerg Mueller, Daniel Polansky, Christian Foltin, Dimitry Polivaev
 *
 *  This file is modified by Dimitry Polivaev in 2008.
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
package org.freeplane.features.cloud;

import java.awt.Color;

import org.freeplane.core.extension.IExtension;
import org.freeplane.features.map.NodeModel;

public class CloudModel implements IExtension {
	public enum Shape{
		ARC, STAR, RECT, ROUND_RECT
	};
	public static CloudModel getModel(final NodeModel node) {
		return (CloudModel) node.getExtension(CloudModel.class);
	}

	public static CloudModel createModel(final NodeModel node) {
		final CloudModel extension = (CloudModel) node.getExtension(CloudModel.class);
		if (extension != null) {
			return extension;
		}
		final CloudModel cloudModel = new CloudModel();
		node.addExtension(cloudModel);
		return cloudModel;
	}

	public static void setModel(final NodeModel node, final CloudModel cloud) {
		final CloudModel oldCloud = CloudModel.getModel(node);
		if (cloud != null && oldCloud == null) {
			node.addExtension(cloud);
		}
		else if (cloud == null && oldCloud != null) {
			node.removeExtension(CloudModel.class);
		}
	}

	private Color color;
	
	private Shape shape;

	public Shape getShape() {
    	return shape;
    }

	public void setShape(Shape shape) {
    	this.shape = shape;
    }

	public CloudModel() {
		color = CloudController.getStandardColor();
		shape = Shape.ARC;
	}

	public Color getColor() {
		return color;
	}

	public void setColor(final Color color) {
		this.color = color != null ? color : CloudController.getStandardColor();
	}
}
