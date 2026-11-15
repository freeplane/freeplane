/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2008 Joerg Mueller, Daniel Polansky, Christian Foltin, Dimitry Polivaev
 *
 *  This file author is Christian Foltin
 *  It is modified by Dimitry Polivaev in 2008.
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

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.text.AttributedCharacterIterator;
import java.awt.font.TextAttribute;
import java.util.function.BiConsumer;

import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.batik.svggen.SVGGeneratorContext;
import org.apache.batik.svggen.SVGGeneratorContext.GraphicContextDefaults;
import org.apache.batik.svggen.SVGGraphics2D;
import org.apache.batik.util.SVGConstants;
import org.freeplane.core.awt.GraphicsHints;
import org.freeplane.features.export.mindmapmode.IExportEngine;
import org.freeplane.view.swing.map.MapView;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;

/**
 * @author foltin
 */
abstract class ExportVectorGraphic implements IExportEngine {

    protected static final String SVG_EMBED_FONTS = "org.freeplane.plugin.svg.export.svg.embed_fonts";

    private static class SVGExportGraphics2D extends SVGGraphics2D {
        private final BiConsumer<Font, String> textObserver;
        private boolean suppressObserver;

        private SVGExportGraphics2D(SVGExportGraphics2D graphics) {
            super(graphics);
            textObserver = graphics.textObserver;
            suppressObserver = graphics.suppressObserver;
        }

        private SVGExportGraphics2D(SVGGeneratorContext context, BiConsumer<Font, String> textObserver) {
            super(context, false);
            this.textObserver = textObserver;
        }

        @Override
        public Graphics create() {
            return new SVGExportGraphics2D(this);
        }

        private boolean observesText() {
            return textObserver != null && !suppressObserver && !textAsShapes
                    && getRenderingHint(GraphicsHints.FORCE_TEXT_TO_SHAPE) != Boolean.TRUE;
        }

        @Override
        public void drawString(String text, float x, float y) {
            if (observesText() && !text.isEmpty())
                textObserver.accept(getFont(), text);
            withTextMode(() -> super.drawString(text, x, y));
        }

        @Override
        public void drawString(AttributedCharacterIterator text, float x, float y) {
            if (observesText()) {
                AttributedCharacterIterator runs = (AttributedCharacterIterator) text.clone();
                for (int start = runs.getBeginIndex(); start < runs.getEndIndex();) {
                    runs.setIndex(start);
                    int end = runs.getRunLimit();
                    Font font = (Font) runs.getAttribute(TextAttribute.FONT);
                    if (font == null)
                        font = getFont().deriveFont(runs.getAttributes());
                    StringBuilder run = new StringBuilder();
                    for (int index = start; index < end; index++)
                        run.append(runs.setIndex(index));
                    textObserver.accept(font, run.toString());
                    start = end;
                }
            }
            withTextMode(() -> super.drawString(text, x, y));
        }

        private void withTextMode(Runnable paint) {
            boolean savedTextAsShapes = textAsShapes;
            boolean savedSuppression = suppressObserver;
            try {
                suppressObserver = true;
                if (getRenderingHint(GraphicsHints.FORCE_TEXT_TO_SHAPE) == Boolean.TRUE)
                    textAsShapes = true;
                paint.run();
            } finally {
                textAsShapes = savedTextAsShapes;
                suppressObserver = savedSuppression;
            }
        }
    }

	/**
	 */
	protected SVGGraphics2D createSVGGraphics2D(final MapView view) {
        final SVGGraphics2D graphics = createSVGGraphics2D(new Dimension(), null);
        view.preparePrinting();
        final Rectangle innerBounds = view.getInnerBounds();
        graphics.setSVGCanvasSize(new Dimension(innerBounds.width, innerBounds.height));
        graphics.translate(-innerBounds.x, -innerBounds.y);
        view.print(graphics);
        view.endPrinting();
        return graphics;
    }

    protected SVGGraphics2D createSVGGraphics2D(Dimension size, BiConsumer<Font, String> textObserver) {
	    final DOMImplementation impl = GenericDOMImplementation.getDOMImplementation();
	    final String namespaceURI = SVGConstants.SVG_NAMESPACE_URI;
	    final Document domFactory = impl.createDocument(namespaceURI, "svg", null);
	    final SVGGeneratorContext ctx = createGeneratorContext(domFactory);
	    final GraphicContextDefaults defaults = new GraphicContextDefaults();
	    defaults.setFont(new Font("Arial", Font.PLAIN, 12));
	    ctx.setGraphicContextDefaults(defaults);
	    ctx.setExtensionHandler(new GradientExtensionHandler());
	    ctx.setPrecision(12);
	    final SVGGraphics2D g2d = new SVGExportGraphics2D(ctx, textObserver);

	    g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_DEFAULT);
	    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_DEFAULT);
        g2d.setSVGCanvasSize(size);
	    return g2d;
	}

	protected SVGGeneratorContext createGeneratorContext(final Document domFactory) {
		final SVGGeneratorContext ctx = SVGGeneratorContext.createDefault(domFactory);
		return ctx;
	}
}
