package org.freeplane.plugin.latex;

import java.awt.BorderLayout;
import java.awt.Container;

import javax.swing.JFrame;
import javax.swing.JLabel;

import org.scilab.forge.jlatexmath.TeXConstants;
import org.scilab.forge.jlatexmath.TeXFormula;
import org.scilab.forge.jlatexmath.TeXIcon;

public class TeXText
{
    String rawText;

    public TeXText(String t) {
        rawText = t;
    }

	public TeXIcon createTeXIcon(int style, int size, int align, int maxWidth) {
        rawText = rawText.replace("\\begin{align}", "\n\n$\\quad ");
        rawText = rawText.replace("\\end{align}", "$\n\n");

        rawText = rawText.replace("\\begin{align*}", "\n\n$\\quad ");
        rawText = rawText.replace("\\end{align*}", "$\n\n");

        /*
        String[] lines = rawText.split("\n");
        if (lines.length == 1)
        {
    		sb.append("\\text{");
    		sb.append(lines[0]);
    		sb.append("}");
        }
        else
        {
        	sb.append("\\raisebox{0}{ \\begin{array}{l} ");
        	for (int i = 0; i < lines.length; i++)
        	{
        		sb.append("\\text{");
        		sb.append(lines[i]);
        		sb.append("}\\\\ ");
        	}
        	sb.append("\\end{array} }");
        }
        */

        // NOTE: do not use the array env (see above), because array cells cannot be
        // automatically line wrapped! (and since we now have automatic linebreaks,
        // we don't need multiple formulae with one on each line!!)
        StringBuffer sb = new StringBuffer();
		sb.append("\\text{");
		sb.append(rawText);
		sb.append("}");

		TeXFormula tf = new TeXFormula(sb.toString());

        return tf.createTeXIcon(style, size, TeXConstants.UNIT_PIXEL, maxWidth, align, TeXConstants.UNIT_PIXEL, 10f);
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
        jl.setIcon(tf.createTeXIcon(TeXConstants.STYLE_DISPLAY, 16, TeXConstants.ALIGN_CENTER, 100));

        Container cp = jf.getContentPane();
        cp.setLayout(new BorderLayout());

        cp.add(jl, BorderLayout.CENTER);
        jf.pack();
        jf.setVisible(true);
        jf.setBounds(0, 0, 400, 300);
    }

}
