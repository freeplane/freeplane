package org.freeplane.features.explorer;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class ExploringStepBuilder {
	private final AccessedNodes accessedNodes;

	private final static String DOUBLE_QUOTED_STRING = "\"(?:[^\\\\\"]|\\\\.)*\"";
	private final static String SINGLE_QUOTED_STRING = "'(?:[^\\\\']|\\\\.)*'";
	private final static String ALIAS = "~(?>\\w|[-:+* ])+";
	private final static String SPACE = "\\s+";
	private final static String SEPARATOR = "/";
	private final static String GLOBAL = ":";
	private static final String ANCESTOR = "..";
	private static final String DESCENDANT = "**";
	private final static Pattern regex = Pattern.compile( SPACE + "|" + ALIAS + "|"
	+ SINGLE_QUOTED_STRING + "|" + DOUBLE_QUOTED_STRING + "|"
			+ SEPARATOR  + "|^" + GLOBAL  + "|" + Pattern.quote("*") + "{1,2}|" + Pattern.quote(ANCESTOR));
	private Matcher matcher;

	ExploringStepBuilder(String path, AccessedNodes accessedNodes) {
		this.matcher = regex.matcher(path.trim());
		this.accessedNodes = accessedNodes;

	}

	List<Command> buildSteps(){
		final ArrayList<Command> commands = new ArrayList<>();
		final int pathLength = matcher.regionEnd();
		boolean separatorFound = true;
		ExploringStep nextStep = ExploringStep.CHILD;
		for(;matcher.lookingAt(); matcher.region(matcher.end(), pathLength)) {
			final String group = matcher.group();
			if(Character.isWhitespace(group.charAt(0)))
				continue;
			else if(group.contentEquals(SEPARATOR)) {
				if (matcher.start() == 0)
					commands.add(command(ExploringStep.ROOT, ""));
				else if(separatorFound)
					throw new IllegalArgumentException("Unexpected " + SEPARATOR + " at pos " + matcher.start());
				else if(nextStep == ExploringStep.ANCESTOR) {
					commands.add(command(ExploringStep.PARENT, ""));
					nextStep = ExploringStep.CHILD;
				}
				separatorFound = true;
			}
			else if(group.contentEquals(GLOBAL)) {
				nextStep = ExploringStep.GLOBAL;
			}
			else  if (!separatorFound && nextStep != ExploringStep.ANCESTOR) {
				throw new IllegalArgumentException("Expected " + SEPARATOR + " not found at pos " + matcher.start());
			}
			else {
				if(group.contentEquals(DESCENDANT)) {
					nextStep = ExploringStep.DESCENDANT;
				}
				else if(group.contentEquals(ANCESTOR)) {
					if (nextStep == ExploringStep.ANCESTOR)
					throw new IllegalArgumentException("Unexpected " + ANCESTOR + " at pos " + matcher.start());
					nextStep = ExploringStep.ANCESTOR;
				}
				else {
					if(group.contentEquals("*")) {
						commands.add(command(nextStep, "'...'"));
					}
					else
						commands.add(command(nextStep, group));
					nextStep = ExploringStep.CHILD;
				}
				separatorFound = false;
			}
		}
		if(matcher.regionStart() != pathLength)
			throw new IllegalArgumentException("Illegal element at pos " + matcher.regionStart());
		else if (nextStep == ExploringStep.DESCENDANT)
			commands.add(command(ExploringStep.DESCENDANT, "'...'"));
		else if(nextStep == ExploringStep.ANCESTOR)
			commands.add(command(ExploringStep.PARENT, ""));

		return commands;

	}

	private Command command(ExploringStep exploringStep, String searchedString) {
		return new Command(exploringStep, searchedString, accessedNodes);
	}
}
