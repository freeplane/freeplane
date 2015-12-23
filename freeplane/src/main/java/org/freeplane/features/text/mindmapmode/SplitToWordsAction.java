package org.freeplane.features.text.mindmapmode;

import java.awt.event.ActionEvent;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.AMultipleNodeAction;
import org.freeplane.core.util.HtmlUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.map.mindmapmode.MMapController;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.freeplane.features.text.TextController;
import org.freeplane.features.text.TransformationException;

@SuppressWarnings("serial")
public class SplitToWordsAction extends AMultipleNodeAction{
	static{
		ResourceController.getResourceController().setDefaultProperty("SplitToWordsAction.auxillaryWordList", TextUtils.getText("defaultAuxillaryWordList"));
	}

	private final int nodeNumberInLine;

	public SplitToWordsAction(int nodeNumberInLine) {
		super(createActionKey(nodeNumberInLine), createActionText(nodeNumberInLine), null);
		auxillaryWords = Collections.emptySet();
		this.nodeNumberInLine = nodeNumberInLine;
	}

	public static String createActionText(int nodeNumberInLine) {
		final String key = SplitToWordsAction.class.getSimpleName() + ".text";
		return TextUtils.format(key, nodeNumberInLine);
	}

	public static String createActionKey(int nodeNumberInLine) {
		return SplitToWordsAction.class.getSimpleName() + "." + nodeNumberInLine;
	}
	
	static final Pattern WORD_PATTERN = Pattern.compile("-?\\d+(?:[,.]\\d+)*|[\\p{L}\\d][\\p{L}\\d-]*");
	private Collection<String> auxillaryWords;
	private boolean leaveOriginalNodeEmpty;
	private boolean saveOriginalTextAsDetails;
	
	
	@Override
	public void actionPerformed(ActionEvent e) {
		String auxillaryWordList = ResourceController.getResourceController().getProperty("SplitToWordsAction.auxillaryWordList").toLowerCase();
		auxillaryWords = Arrays.asList(auxillaryWordList.split("\\s*,\\s*"));
		leaveOriginalNodeEmpty = ResourceController.getResourceController().getBooleanProperty("SplitToWordsAction.leaveOriginalNodeEmpty");
		saveOriginalTextAsDetails = ResourceController.getResourceController().getBooleanProperty("SplitToWordsAction.saveOriginalTextAsDetails");
		super.actionPerformed(e);
	}

	@Override
	protected void actionPerformed(ActionEvent e, NodeModel node) {
		
		final ModeController modeController = Controller.getCurrentModeController();
		MTextController textController = (MTextController) modeController.getExtension(TextController.class);
		final MMapController mapController = (MMapController) modeController.getMapController();
		String details;
		try {
			details = textController.getTransformedObject(node).toString();
		} catch (TransformationException e1) {
			return;
		}
		String plainText = HtmlUtils.htmlToPlain(details).trim();
		
		
		int nodeCountInLine;
		boolean newNode;
		
		if(leaveOriginalNodeEmpty){
			nodeCountInLine = 0;
			newNode = true;
			textController.setNodeText(node, "");
		}
		else{
			nodeCountInLine = -1;
			newNode = false;
		}
		
		NodeModel currentNode = node;
		final Matcher matcher = WORD_PATTERN.matcher(plainText);
		while (matcher.find()){
			String word = matcher.group();
			final String currentText;
		    if(newNode) {
				if (nodeCountInLine == nodeNumberInLine) {
					nodeCountInLine = 0;
					currentNode = node;
				}
				currentNode = mapController.addNewNode(currentNode, currentNode.getChildCount(), currentNode.isLeft());
				nodeCountInLine++;
				currentText = "";
			}
			else if (nodeCountInLine == -1){
				nodeCountInLine = 0;
				currentNode = node;
				currentText = "";
			}
			else
				currentText = currentNode.getText() + ' ';
			
			boolean auxillaryWord = auxillaryWords.contains(word.toLowerCase());
			
			if (! auxillaryWord) {
				textController.setNodeText(currentNode, currentText + capitalize(word));
				newNode = true;
			}
			else {
				textController.setNodeText(currentNode, currentText + word);
				newNode = false;
			}
		}
		if(saveOriginalTextAsDetails) {
			textController.setDetails(currentNode, HtmlUtils.isHtmlNode(details) ?  details : HtmlUtils.plainToHTML(details));
			textController.setIsMinimized(currentNode, true);
		}
	}

	private String capitalize(String word) {
		return word.substring(0, 1).toUpperCase() + word.substring(1);
	}

}
