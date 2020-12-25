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
	
	static class PatternMaker {
		static final Pattern ESCAPED_CHARACTERS = Pattern.compile("[\\[\\]\\-\\&\\\\\\^]");
		static final Pattern WHITE_SPACE = Pattern.compile("\\s+");
		static String escape(final String characters) {
			final String withoutWhiteSpace = WHITE_SPACE.matcher(characters).replaceAll("");
			return ESCAPED_CHARACTERS.matcher(withoutWhiteSpace).replaceAll("\\\\$0");
		}

	}
	
	static{
		ResourceController.getResourceController().setDefaultProperty("SplitToWordsAction.auxiliaryWordList", TextUtils.getText("defaultAuxiliaryWordList"));
	}

	private final int nodeNumberInLine;

	public SplitToWordsAction(int nodeNumberInLine) {
		super(createActionKey(nodeNumberInLine), createActionText(nodeNumberInLine), null);
		auxiliaryWords = Collections.emptySet();
		this.nodeNumberInLine = nodeNumberInLine;
	}

	public static String createActionText(int nodeNumberInLine) {
		final String key = SplitToWordsAction.class.getSimpleName() + ".text";
		return TextUtils.format(key, nodeNumberInLine);
	}

	public static String createActionKey(int nodeNumberInLine) {
		return SplitToWordsAction.class.getSimpleName() + "." + nodeNumberInLine;
	}
	
	private static String charactersAcceptedInWord;
	private static String numberRegularExpression;
	private static String wordRegularExpression;
	private static String wordOrNumberRegularExpression;
	private static Pattern compiledWordPattern;
	private Collection<String> auxiliaryWords;
	private boolean leaveOriginalNodeEmpty;
	private boolean saveOriginalTextAsDetails;
    private boolean capitalizeWords;
	
	
	@Override
	public void actionPerformed(ActionEvent e) {
		compileWordPattern();
		String auxiliaryWordList = ResourceController.getResourceController().getProperty("SplitToWordsAction.auxiliaryWordList").toLowerCase();
		auxiliaryWords = Arrays.asList(auxiliaryWordList.split("\\s*,\\s*"));
		leaveOriginalNodeEmpty = ResourceController.getResourceController().getBooleanProperty("SplitToWordsAction.leaveOriginalNodeEmpty");
		saveOriginalTextAsDetails = ResourceController.getResourceController().getBooleanProperty("SplitToWordsAction.saveOriginalTextAsDetails");
		capitalizeWords = ResourceController.getResourceController().getBooleanProperty("SplitToWordsAction.capitalizeWords");
		super.actionPerformed(e);
	}

	void compileWordPattern() {
		charactersAcceptedInWord = charactersAcceptedInWords();
		wordRegularExpression = "[\\p{L}\\d" + charactersAcceptedInWord + "]+";
		numberRegularExpression = "-?\\d+(?:[,.]\\d+)*";
		final String newRegularExpression = numberRegularExpression+"|"+wordRegularExpression;
		if(! newRegularExpression.equals(wordOrNumberRegularExpression)) {
			wordOrNumberRegularExpression = newRegularExpression;
			compiledWordPattern = Pattern.compile(wordOrNumberRegularExpression);
		}
	}

	private String charactersAcceptedInWords() {
		final String characters = ResourceController.getResourceController().getProperty("SplitToWordsAction.charactersAcceptedInWord");
		return PatternMaker.escape(characters);
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
		final Matcher matcher = compiledWordPattern.matcher(plainText);
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
			
			boolean auxiliaryWord = auxiliaryWords.contains(word.toLowerCase());
			
			if (! auxiliaryWord) {
				textController.setNodeText(currentNode, currentText + capitalize(word));
				newNode = true;
			}
			else {
				textController.setNodeText(currentNode, currentText + word);
				newNode = false;
			}
		}
		if(saveOriginalTextAsDetails) {
			textController.setDetails(currentNode, HtmlUtils.isHtml(details) ?  details : HtmlUtils.plainToHTML(details));
			textController.setIsMinimized(currentNode, true);
		}
	}

	private String capitalize(String word) {
		return capitalizeWords ? word.substring(0, 1).toUpperCase() + word.substring(1) : word;
	}

}
