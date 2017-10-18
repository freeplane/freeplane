/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2017 Joerg Mueller, Daniel Polansky, Christian Foltin, Dimitry Polivaev
 *
 *  This file author is Martin Steiger (2012), with fixes by Felix Natter (2017),
 *  see https://gist.github.com/msteiger/4509119.
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
package org.freeplane.plugin.svg;

import static org.apache.batik.util.SVGConstants.*;

import java.awt.Color;
import java.awt.LinearGradientPaint;
import java.awt.MultipleGradientPaint;
import java.awt.Paint;
import java.awt.RadialGradientPaint;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;

import org.apache.batik.svggen.DefaultExtensionHandler;
import org.apache.batik.svggen.SVGColor;
import org.apache.batik.svggen.SVGGeneratorContext;
import org.apache.batik.svggen.SVGPaintDescriptor;
import org.w3c.dom.Element;

/**
 * batik 1.9 does not natively support exporting java.awt.LinearGradientPaint/java.awt.RadialGradientPaint
 * as SVG. Our SVG rendering library, SVGSalamander, uses these classes to render gradients.
 * This class adds support for these paints in batik 1.9 and thus fixes export of maps to PDF/SVG with SVG icons.
 */
public class GradientExtensionHandler extends DefaultExtensionHandler
{
  @Override
	public SVGPaintDescriptor handlePaint(Paint paint, SVGGeneratorContext genCtx)
	{
		// Handle LinearGradientPaint
		if (paint instanceof LinearGradientPaint)
			return getLgpDescriptor((LinearGradientPaint) paint, genCtx);
			
		// Handle RadialGradientPaint
		if (paint instanceof RadialGradientPaint)
			return getRgpDescriptor((RadialGradientPaint) paint, genCtx);
		
		return super.handlePaint(paint, genCtx);
	}

	private SVGPaintDescriptor getRgpDescriptor(RadialGradientPaint gradient, SVGGeneratorContext genCtx)
	{
		Element gradElem = genCtx.getDOMFactory().createElementNS(SVG_NAMESPACE_URI, SVG_RADIAL_GRADIENT_TAG);

		// Create and set unique XML id
		String id = genCtx.getIDGenerator().generateID("gradient");
		gradElem.setAttribute(SVG_ID_ATTRIBUTE, id);

		// Set x,y pairs
		Point2D centerPt = gradient.getCenterPoint();
		gradElem.setAttribute("cx", String.valueOf(centerPt.getX()));
		gradElem.setAttribute("cy", String.valueOf(centerPt.getY()));

		Point2D focusPt = gradient.getFocusPoint();
		gradElem.setAttribute("fx", String.valueOf(focusPt.getX()));
		gradElem.setAttribute("fy", String.valueOf(focusPt.getY()));

		gradElem.setAttribute("r", String.valueOf(gradient.getRadius()));

		addMgpAttributes(gradElem, genCtx, gradient);

		return new SVGPaintDescriptor("url(#" + id + ")", SVG_OPAQUE_VALUE, gradElem);
	}
	
	private SVGPaintDescriptor getLgpDescriptor(LinearGradientPaint gradient, SVGGeneratorContext genCtx)
	{
		Element gradElem = genCtx.getDOMFactory().createElementNS(SVG_NAMESPACE_URI, SVG_LINEAR_GRADIENT_TAG);

		// Create and set unique XML id
		String id = genCtx.getIDGenerator().generateID("gradient");
		gradElem.setAttribute(SVG_ID_ATTRIBUTE, id);

		// Set x,y pairs
		Point2D startPt = gradient.getStartPoint();
		gradElem.setAttribute("x1", String.valueOf(startPt.getX()));
		gradElem.setAttribute("y1", String.valueOf(startPt.getY()));

		Point2D endPt = gradient.getEndPoint();
		gradElem.setAttribute("x2", String.valueOf(endPt.getX()));
		gradElem.setAttribute("y2", String.valueOf(endPt.getY()));

		addMgpAttributes(gradElem, genCtx, gradient);

		return new SVGPaintDescriptor("url(#" + id + ")", SVG_OPAQUE_VALUE, gradElem);
	}
	
	private void addMgpAttributes(Element gradElem, SVGGeneratorContext genCtx, MultipleGradientPaint gradient)
	{
		gradElem.setAttribute(SVG_GRADIENT_UNITS_ATTRIBUTE, SVG_USER_SPACE_ON_USE_VALUE);

		// Set cycle method
		switch (gradient.getCycleMethod())
		{
		case REFLECT:
			gradElem.setAttribute(SVG_SPREAD_METHOD_ATTRIBUTE, SVG_REFLECT_VALUE);
			break;
		case REPEAT:
			gradElem.setAttribute(SVG_SPREAD_METHOD_ATTRIBUTE, SVG_REPEAT_VALUE);
			break;
		case NO_CYCLE:
			gradElem.setAttribute(SVG_SPREAD_METHOD_ATTRIBUTE, SVG_PAD_VALUE);	// this is the default
			break;
		}

		// Set color space
		switch (gradient.getColorSpace())
		{
		case LINEAR_RGB:
			gradElem.setAttribute(SVG_COLOR_INTERPOLATION_ATTRIBUTE, SVG_LINEAR_RGB_VALUE);
			break;
			
		case SRGB:
			gradElem.setAttribute(SVG_COLOR_INTERPOLATION_ATTRIBUTE, SVG_SRGB_VALUE);
			break;
		}

		// Set transform matrix if not identity
		AffineTransform tf = gradient.getTransform();
		if (!tf.isIdentity())
		{
			String matrix = "matrix(" + 
			tf.getScaleX() + " " + tf.getShearY() + " " + tf.getShearX() + " " + 
			tf.getScaleY() + " " + tf.getTranslateX() + " " + tf.getTranslateY() + ")";
			gradElem.setAttribute(SVG_GRADIENT_TRANSFORM_ATTRIBUTE, matrix);
		}

		// Convert gradient stops
		Color[] colors = gradient.getColors();
		float[] fracs = gradient.getFractions();
		
		for (int i = 0; i < colors.length; i++)
		{
			Element stop = genCtx.getDOMFactory().createElementNS(SVG_NAMESPACE_URI, SVG_STOP_TAG);
			SVGPaintDescriptor pd = SVGColor.toSVG(colors[i], genCtx);

			stop.setAttribute(SVG_OFFSET_ATTRIBUTE, (int) (fracs[i] * 100.0f) + "%");
			stop.setAttribute(SVG_STOP_COLOR_ATTRIBUTE, pd.getPaintValue());

			if (colors[i].getAlpha() != 255)
			{
				stop.setAttribute(SVG_STOP_OPACITY_ATTRIBUTE, pd.getOpacityValue());
			}
			
			gradElem.appendChild(stop);
		}		
	}	
}