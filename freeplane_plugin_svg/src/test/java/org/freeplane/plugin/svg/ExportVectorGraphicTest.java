package org.freeplane.plugin.svg;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.font.TextAttribute;
import java.io.File;
import java.io.StringWriter;
import java.text.AttributedString;
import java.util.ArrayList;
import java.util.List;

import org.apache.batik.svggen.SVGGraphics2D;
import org.freeplane.core.awt.GraphicsHints;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ExportVectorGraphicTest {
    private Controller previousController;
    private ResourceController resources;

    @Before
    public void installResources() {
        previousController = Controller.getCurrentController();
        Controller controller = mock(Controller.class);
        resources = mock(ResourceController.class);
        when(controller.getResourceController()).thenReturn(resources);
        when(resources.getProperty(anyString())).thenReturn("");
        Controller.setCurrentController(controller);
    }

    @After
    public void restoreController() {
        Controller.setCurrentController(previousController);
    }

    @Test
    public void retainsExistingVectorTextAndShapeBehavior() throws Exception {
        ExportVectorGraphic exporter = new ExportVectorGraphic() {
            public void export(List<NodeModel> nodes, File destination) { }
        };
        SVGGraphics2D graphics = exporter.createSVGGraphics2D(new Dimension(240, 120), null);
        assertThat(graphics.getGeneratorContext().getPrecision()).isEqualTo(12);
        assertThat(graphics.getGeneratorContext().getExtensionHandler()).isInstanceOf(GradientExtensionHandler.class);
        assertThat(graphics.getRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING))
                .isSameAs(RenderingHints.VALUE_TEXT_ANTIALIAS_DEFAULT);
        assertThat(graphics.getRenderingHint(RenderingHints.KEY_ANTIALIASING))
                .isSameAs(RenderingHints.VALUE_ANTIALIAS_DEFAULT);
        graphics.drawString("normal text", 10f, 20f);
        graphics.setRenderingHint(GraphicsHints.FORCE_TEXT_TO_SHAPE, Boolean.TRUE);
        graphics.drawString("forced shapes", 10f, 40f);
        graphics.setRenderingHint(GraphicsHints.FORCE_TEXT_TO_SHAPE, Boolean.FALSE);
        AttributedString attributed = new AttributedString("attributed text");
        attributed.addAttribute(TextAttribute.FONT, new Font(Font.SERIF, Font.BOLD, 14));
        graphics.drawString(attributed.getIterator(), 10f, 60f);
        Graphics2D child = (Graphics2D) graphics.create();
        child.drawString("child text", 10f, 80f);
        child.dispose();
        graphics.setPaint(new GradientPaint(0, 0, Color.RED, 100, 100, Color.BLUE));
        graphics.fillRect(0, 90, 100, 20);

        String svg = svg(graphics);
        assertThat(svg).contains("normal text", "attributed text", "child text", "linearGradient", "<path")
                .doesNotContain("forced shapes");
        when(resources.getBooleanProperty(ExportVectorGraphic.SVG_EMBED_FONTS)).thenReturn(true);
        assertThat(new ExportSvg().createSVGGraphics2D(new Dimension(1, 1), null)
                .getGeneratorContext().isEmbeddedFontsOn()).isTrue();
        assertThat(new ExportPdf().createSVGGraphics2D(new Dimension(1, 1), null)
                .getGeneratorContext().isEmbeddedFontsOn()).isFalse();
        when(resources.getBooleanProperty("org.freeplane.plugin.svg.export.pdf.convert_text_to_shapes")).thenReturn(true);
        assertThat(new ExportPdf().createSVGGraphics2D(new Dimension(1, 1), null)
                .getGeneratorContext().isEmbeddedFontsOn()).isTrue();
    }

    @Test
    public void observesTextRunsAcrossChildGraphics() throws Exception {
        ExportVectorGraphic exporter = new ExportVectorGraphic() {
            public void export(List<NodeModel> nodes, File destination) { }
        };
        List<String> text = new ArrayList<>();
        List<Font> fonts = new ArrayList<>();
        SVGGraphics2D graphics = exporter.createSVGGraphics2D(new Dimension(300, 120), (font, run) -> {
            fonts.add(font);
            text.add(run);
        });
        Font plain = new Font(Font.SANS_SERIF, Font.PLAIN, 12);
        Font bold = new Font(Font.SERIF, Font.BOLD, 14);
        Font italic = new Font(Font.MONOSPACED, Font.ITALIC, 13);
        graphics.setFont(plain);
        graphics.drawString("plain", 0f, 15f);
        Graphics2D child = (Graphics2D) graphics.create();
        child.setFont(bold);
        child.drawString("child", 0f, 30f);
        AttributedString attributed = new AttributedString("bolditalic");
        attributed.addAttribute(TextAttribute.FONT, bold, 0, 4);
        attributed.addAttribute(TextAttribute.FONT, italic, 4, 10);
        child.drawString(attributed.getIterator(), 0f, 45f);
        AttributedString familyAttributes = new AttributedString("family attributes");
        familyAttributes.addAttribute(TextAttribute.FAMILY, Font.MONOSPACED);
        familyAttributes.addAttribute(TextAttribute.SIZE, 13f);
        familyAttributes.addAttribute(TextAttribute.WEIGHT, TextAttribute.WEIGHT_REGULAR);
        familyAttributes.addAttribute(TextAttribute.POSTURE, TextAttribute.POSTURE_OBLIQUE);
        child.drawString(familyAttributes.getIterator(), 0f, 55f);
        child.setRenderingHint(GraphicsHints.FORCE_TEXT_TO_SHAPE, Boolean.TRUE);
        child.drawString("hidden shapes", 0f, 60f);
        child.drawString(attributed.getIterator(), 0f, 75f);
        child.dispose();
        graphics.drawString("after child", 0f, 90f);

        assertThat(text).containsExactly("plain", "child", "bold", "italic", "family attributes", "after child");
        assertThat(fonts).containsExactly(plain, bold, bold, italic, italic, plain);
        assertThat(svg(graphics)).contains("plain", "child", "bold", "italic", "family attributes", "after child")
                .doesNotContain("hidden shapes");
    }

    private String svg(SVGGraphics2D graphics) throws Exception {
        StringWriter writer = new StringWriter();
        graphics.stream(writer, true);
        return writer.toString();
    }
}
