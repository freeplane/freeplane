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


    public TeXIcon createTeXIcon(int size, int align)
    {
        StringBuffer sb = new StringBuffer();
        sb.append("\\begin{array}{l} \\raisebox{0}{");

        rawText = rawText.replace("\\begin{align}", "\n\n$\\quad ");
        rawText = rawText.replace("\\end{align}", "$\n\n");

        rawText = rawText.replace("\\begin{align*}", "\n\n$\\quad ");
        rawText = rawText.replace("\\end{align*}", "$\n\n");

        String[] lines = rawText.split("\n");
        for (int i = 0; i < lines.length; i++)
        {
            sb.append("\\text{");
            sb.append(lines[i]);
            sb.append("}\\\\");
        }

        sb.append("} \\end{array}");

        TeXFormula tf = new TeXFormula(sb.toString());
        return tf.createTeXIcon(TeXConstants.STYLE_DISPLAY, size, align);
    }

    public static void main(String[] argv)
    {
        StringBuffer latex = new StringBuffer();
        latex.append("Sei $A$ ein Vektorraum und $f:A \\rightarrow \\mathcal{R}$ sei linear.\n");
        latex.append("Wir wissen dass $u=\\pi$, und damit haben wir:");
        latex.append("\\begin{align}");
        latex.append("f(\\lambda u) = \\lambda f(u) = 0.");
        latex.append("\\end{align}");

        TeXText tf = new TeXText(latex.toString());

        JFrame jf = new JFrame();
        jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JLabel jl = new JLabel();
        jl.setIcon(tf.createTeXIcon(12, TeXConstants.ALIGN_CENTER));

        Container cp = jf.getContentPane();
        cp.setLayout(new BorderLayout());

        cp.add(jl, BorderLayout.CENTER);
        jf.pack();
        jf.setVisible(true);
        jf.setBounds(0, 0, 400, 300);

        System.err.println("aaa");
    }

}
