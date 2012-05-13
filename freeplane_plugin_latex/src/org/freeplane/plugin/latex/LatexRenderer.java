package org.freeplane.plugin.latex;

import javax.swing.Icon;

import org.freeplane.features.map.NodeModel;
import org.freeplane.features.text.AbstractContentTransformer;
import org.freeplane.features.text.TextController;
import org.freeplane.features.text.TransformationException;
import org.scilab.forge.jlatexmath.TeXConstants;
import org.scilab.forge.jlatexmath.TeXFormula;
import org.scilab.forge.jlatexmath.TeXIcon;

public class LatexRenderer extends AbstractContentTransformer {

	private static final String LATEX = "\\latex";

	public LatexRenderer() {
		super(20);
	}

	public Object transformContent(TextController textController,
			Object content, NodeModel node, Object transformedExtension)
			throws TransformationException {
		return content;
	}

	@Override
	public Icon getIcon(TextController textController, Object content,
			NodeModel node, Object transformedExtension) {
		if(transformedExtension  == node.getUserObject()){
			String string = content.toString();
			if(string.startsWith(LATEX)){
				try {
					TeXFormula teXFormula = new TeXFormula("\\begin{array}{l} \\raisebox{0}{ "
							+string.substring(LATEX.length() + 1)
							+" } \\end{array}"
					);
					TeXIcon icon = teXFormula.createTeXIcon(TeXConstants.STYLE_DISPLAY, LatexViewer.DEFAULT_FONT_SIZE);
					return icon;
				}
				catch (final Exception e) {
					
				}
				
			}
		}
		return null;
	}

}
