package org.freeplane.plugin.latex;

import java.awt.BorderLayout;
import java.awt.Container;

import javax.swing.JFrame;
import javax.swing.JLabel;

import org.freeplane.api.LengthUnit;
import org.freeplane.api.Quantity;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.features.mode.Controller;
import org.scilab.forge.jlatexmath.TeXConstants;
import org.scilab.forge.jlatexmath.TeXFormula;
import org.scilab.forge.jlatexmath.TeXIcon;

public class TeXText
{
	private static final String LATEX_MACROS = "latex_macros";
    private static final String LATEX_INTERLINE_SPACING = "latex_interline_spacing";

    String rawText;

    public TeXText(String t) {
        rawText = t;
    }

	public TeXIcon createTeXIcon(int style, int size, int align, int maxWidth) {

        StringBuffer sb = new StringBuffer();
        if (Controller.getCurrentController() != null && ResourceController.getResourceController() != null)
        {
            final String predefinedMacros = ResourceController.getResourceController().getProperty(LATEX_MACROS);
            if (predefinedMacros != null) {
                sb.append(predefinedMacros + "\n\n");
            }
        }
        sb.append("\n\n")
		.append("\\text{")
		.append(rawText)
		.append("}");

//        LogUtils.severe(String.format("TeX='%s'", sb.toString()));

		TeXFormula tf = new TeXFormula(sb.toString());

        final Quantity<LengthUnit> latexInterlineSpacingQuantity = ResourceController.getResourceController().getLengthQuantityProperty(LATEX_INTERLINE_SPACING);
        int latexInterlineSpacingUnit = -1;
        float latexInterlineSpacingValue = (float)latexInterlineSpacingQuantity.value;
        if (latexInterlineSpacingQuantity.unit == LengthUnit.px)
        {
            latexInterlineSpacingUnit = TeXConstants.UNIT_PIXEL;
        }
        else if (latexInterlineSpacingQuantity.unit == LengthUnit.in)
        {
            latexInterlineSpacingUnit = TeXConstants.UNIT_IN;
        }
        else if (latexInterlineSpacingQuantity.unit == LengthUnit.mm)
        {
            latexInterlineSpacingUnit = TeXConstants.UNIT_MM;
        }
        else if (latexInterlineSpacingQuantity.unit == LengthUnit.cm)
        {
            latexInterlineSpacingUnit = TeXConstants.UNIT_CM;
        }
        else if (latexInterlineSpacingQuantity.unit == LengthUnit.pt)
        {
            latexInterlineSpacingUnit = TeXConstants.UNIT_PT;
        }

        //tf.createTeXIcon(style, size, TeXConstants.UNIT_PIXEL, maxWidth, align, TeXConstants.UNIT_PIXEL, 40f);
		return tf.new TeXIconBuilder()
			.setStyle(style)
			.setSize(size)
			.setWidth(TeXConstants.UNIT_PIXEL, maxWidth, align)
			.setIsMaxWidth(true)
//			.setInterLineSpacing(TeXConstants.UNIT_PIXEL, /*40f*/size * 1.2F)
			.setInterLineSpacing(latexInterlineSpacingUnit, latexInterlineSpacingValue)
			.build();
    }


    public static void main(String[] argv)
    {
        StringBuffer latex = new StringBuffer();
        latex.append("Sei $A$ ein Vektorraum und $f:A \\rightarrow \\mathcal{R}$ sei linear.\n");
        latex.append("Wir wissen dass $u=\\pi$, und damit haben wir:");
        latex.append("\\begin{align}");
        latex.append("f(\\lambda u) = \\lambda f(u) = 0.");
        latex.append("\\end{align}");

        //TeXText tf = new TeXText(latex.toString());
        TeXText tf = new TeXText("my formula: $x_2=3$hello world hello world hello world hello world hello world hello world hello world hello world hello world ");

        JFrame jf = new JFrame();
        jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JLabel jl = new JLabel();
//        jl.setIcon(tf.createTeXIcon(12, TeXConstants.ALIGN_CENTER));
        jl.setIcon(tf.createTeXIcon(TeXConstants.STYLE_DISPLAY, 16, TeXConstants.ALIGN_LEFT, 400));

        Container cp = jf.getContentPane();
        cp.setLayout(new BorderLayout());

        cp.add(jl, BorderLayout.CENTER);
        jf.pack();
        jf.setVisible(true);
        //jf.setBounds(0, 0, 400, 300);
    }

}
