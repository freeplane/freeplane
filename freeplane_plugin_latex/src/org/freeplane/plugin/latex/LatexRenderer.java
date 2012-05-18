package org.freeplane.plugin.latex;

import javax.swing.Icon;

import org.freeplane.features.format.PatternFormat;
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
			String nodeFormat = textController.getNodeFormat(node);
			if (PatternFormat.IDENTITY_PATTERN.equals(nodeFormat))
				return null;
			final String latext;
			int startLength = LATEX.length() + 1;
			if(string.length() > startLength && string.startsWith(LATEX) && Character.isWhitespace(string.charAt(startLength - 1))){
				latext = string.substring(startLength);
			}
			else if(LatexFormat.LATEX_FORMAT.equals(nodeFormat)){
				latext = string; 
			}
			else
				return null;

			try {
				TeXText teXt = new TeXText(latext);
				TeXIcon icon = teXt.createTeXIcon(TeXConstants.STYLE_DISPLAY, LatexViewer.DEFAULT_FONT_SIZE);
				return icon;
			}
			catch (final Exception e) {

			}

		}
		return null;
	}

}
