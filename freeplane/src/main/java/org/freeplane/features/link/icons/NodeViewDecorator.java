package org.freeplane.features.link.icons;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.freeplane.view.swing.map.NodeView;

/**
 * Provides services, used by {@link NodeView} to decorate nodes based on the link they hold, etc.
 * 
 * @author Stuart Robertson <stuartro@gmail.com>
 */
public class NodeViewDecorator
{
	private LinkDecorationConfig decorationConfig;
	
	public static NodeViewDecorator INSTANCE = new NodeViewDecorator(new LinkDecorationConfig());

	private NodeViewDecorator(LinkDecorationConfig decorationConfig)
	{
		setDecorationConfig(decorationConfig);
	}

	/**
	 * Returns a list of zero or more icon names, representing the icons that will be added to the <code>nodeView</code>
	 * that owns the given <code>link</code> (not persistently, but only in the view) so as to represent the link held
	 */
	public List<String> getIconsForLink(URI link)
	{
		List<String> icons = new ArrayList<String>();
		if (link != null) {
			addLinkIcon(icons, link.toString());
		}
		return icons;
	}

	private void setDecorationConfig(LinkDecorationConfig decorationConfig)
	{
		this.decorationConfig = decorationConfig;
	}

	/**
	 * Uses decorator patterns, specified in <code>FREEMIND_HOME/conf/linkDecoration.ini</code>, to find a list of 0 or
	 * more icons (names) to return.
	 * 
	 * @param icons The list of icon names to which 0 or more new icon names will be added.
	 * @param link The link from the node under consideration.
	 */
	private void addLinkIcon(List<String> icons, String link)
	{
		List<LinkMatchResult> matchResults = findMatchingResults(link);
		LinkMatchResult mostSpecificResult = findResultWithHighestScore(matchResults);
		if (mostSpecificResult != null) {
			icons.add(mostSpecificResult.getIconName());
		}
	}



    private LinkMatchResult findResultWithHighestScore(List<LinkMatchResult> matchResults) {
        LinkMatchResult mostSpecificResult = null;
		for (LinkMatchResult result : matchResults) {
			if (mostSpecificResult == null || result.getScore() > mostSpecificResult.getScore()) {
				mostSpecificResult = result;
			}
		}
        return mostSpecificResult;
    }

    private List<LinkMatchResult> findMatchingResults(String link) {
        List<LinkMatchResult> matchResults = new ArrayList<LinkMatchResult>();
		int matchedScore = 0;
		for (LinkDecorationRule rule : decorationConfig.getRules()) {
		    if(rule.getMaximalScore() < matchedScore)
		        break;
			LinkMatchResult result = rule.matches(link);
			if (result.matches()) {
				matchResults.add(result);
				matchedScore = result.getScore();
			}
		}
        return matchResults;
    }
}
