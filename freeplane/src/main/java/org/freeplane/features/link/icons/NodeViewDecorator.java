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

	LinkDecorationConfig getDecorationConfig()
	{
		return decorationConfig;
	}

	/**
	 * Returns a list of zero or more icon names, representing the icons that will be added to the <code>nodeView</code>
	 * that owns the given <code>link</code> (not persistently, but only in the view) so as to represent the link held
	 */
	public List<String> getIconsForLink(URI link)
	{
		List<String> icons = new ArrayList<String>();
		if (link != null) {
			addIconsForLink(icons, link.toString());
		}
		return icons;
	}

	void setDecorationConfig(LinkDecorationConfig decorationConfig)
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
	private void addIconsForLink(List<String> icons, String link)
	{
		// STEP 1 - FIND MATCHING RULES
		List<LinkMatchResult> matchResults = new ArrayList<LinkMatchResult>();
		for (LinkDecorationRule rule : decorationConfig.getRules()) {
			LinkMatchResult result = rule.matches(link);
			if (result.matches) {
				matchResults.add(result);
			}
		}
		// STEP 2 - FIND MOST SPECIFIC MATCH RESULT, IF ANY
		LinkMatchResult mostSpecificResult = null;
		for (LinkMatchResult result : matchResults) {
			if (mostSpecificResult == null && result.ruleHasIcon()) {
				mostSpecificResult = result;
			}
			else {
				if (result.matchLength > mostSpecificResult.matchLength && result.ruleHasIcon()) {
					mostSpecificResult = result;
				}
			}
		}
		if (mostSpecificResult != null) {
			icons.add(mostSpecificResult.rule.getIconName());
		}
	}
}
