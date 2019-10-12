package org.freeplane.plugin.script.addons;

import java.awt.Dimension;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;

import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.ActionAcceleratorManager;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.ui.menubuilders.FreeplaneResourceAccessor;
import org.freeplane.core.ui.menubuilders.generic.Entry;
import org.freeplane.core.ui.menubuilders.generic.EntryAccessor;
import org.freeplane.core.ui.menubuilders.generic.EntryNavigator;
import org.freeplane.core.util.HtmlUtils;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.icon.IconNotFound;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.mindmapmode.MModeController;
import org.freeplane.main.addons.AddOnProperties;
import org.freeplane.plugin.script.ExecuteScriptAction;
import org.freeplane.plugin.script.ScriptingMenuUtils;
import org.freeplane.plugin.script.addons.ScriptAddOnProperties.Script;

import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.FormSpecs;
import com.jgoodies.forms.layout.RowSpec;

@SuppressWarnings("serial")
public class AddOnDetailsPanel extends JPanel {
	private int maxWidth = 500;
	private String warning;
	public AddOnDetailsPanel(final AddOnProperties addOn, final String warning) {
		this.warning = warning;
		setLayout(new FormLayout(new ColumnSpec[] {
				FormSpecs.DEFAULT_COLSPEC,
				FormSpecs.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("default:grow"),},
			new RowSpec[] {
				FormSpecs.RELATED_GAP_ROWSPEC,
				FormSpecs.DEFAULT_ROWSPEC,
				FormSpecs.RELATED_GAP_ROWSPEC,
				FormSpecs.DEFAULT_ROWSPEC,
				FormSpecs.RELATED_GAP_ROWSPEC,
				FormSpecs.DEFAULT_ROWSPEC,
				FormSpecs.RELATED_GAP_ROWSPEC,
				FormSpecs.DEFAULT_ROWSPEC,
				RowSpec.decode("top:default:grow"),}));
		if (warning != null) {
			JLabel warningLabel = createWarningLabel(addOn);
			add(warningLabel, "3, 2");
		}
		JLabel imageLabel = createImageLabel(addOn);
		add(imageLabel, "1, 4");
		JLabel title = createTitleLabel(addOn);
		add(title, "3, 4");
		JLabel author = createAuthorLabel(addOn);
		add(author, "3, 6");
		final Box box = Box.createHorizontalBox();
		box.add(new JLabel(getText("homepage")));
		box.add(createAddOnHomepageButton(addOn));
		add(box, "3, 8, left, default");
		JComponent details = createDetails(addOn);
		add(details, "3, 9");
	}

	private JLabel createImageLabel(AddOnProperties addOn) {
		final JLabel label = new JLabel("");
		label.setIcon(IconNotFound.createIconOrReturnNotFoundIcon(addOn.getName() + ".png"));
		return label;
	}

	/**
	 * @wbp.parser.constructor
	 */
	public AddOnDetailsPanel(AddOnProperties addOn) {
		this(addOn, null);
	}

	private JLabel createWarningLabel(final AddOnProperties addOn) {
		return new JLabel("<html><body>" + warning.replaceAll("</?(html|body)>", "") + "</body></html>");
	}

	private JLabel createTitleLabel(final AddOnProperties addOn) {
		return new JLabel("<html><body><b><font size='+2'>" + toHtml(addOn.getTranslatedName()) + " "
				+ addOn.getVersion().replaceAll("^v", "") + "</font></b></body></html>");
	}

	private JLabel createAuthorLabel(final AddOnProperties addOn) {
		final String text = addOn.getAuthor() == null ? "" : "<html><body><strong><font size='-1'>"
				+ getText("authored.by", toHtml(addOn.getAuthor())) + "</font></strong></body></html>";
		return new JLabel(text);
	}

	private JComponent createAddOnHomepageButton(final AddOnProperties addOn) {
		// parse the URI on creation of the dialog to test the URI syntax early
		try {
			return UITools.createHtmlLinkStyleButton(addOn.getHomepage().toURI(), addOn.getHomepage().toString());
		}
		catch (Exception e) {
			LogUtils.warn("add-on " + addOn + " has no valid homepage: " + e);
			return new JLabel("-");
		}
	}

	private JComponent createDetails(final AddOnProperties addOn) {
		final StringBuilder text = new StringBuilder(1024);
		text.append("<html><body>");
		text.append(toHtml(addOn.getDescription()));
		text.append("<p>");
		if (addOn instanceof ScriptAddOnProperties) {
			List<Script> scripts = ((ScriptAddOnProperties) addOn).getScripts();
			if (!scripts.isEmpty()) {
				text.append("<table border='1'>");
				text.append(row("th", getText("header.function"), getText("header.menu"), getText("header.shortcut")));
				for (ScriptAddOnProperties.Script script : scripts) {
					text.append(row("td", bold(TextUtils.getText(script.menuTitleKey)), HtmlUtils.toXMLEscapedText(formatMenuLocation(script)),
						formatShortcut(script)));
				}
				text.append("</table>");
			}
		}
		text.append("</body></html>");
		final JLabel label = new JLabel(text.toString());
		label.setAutoscrolls(true);
		final Icon icon = IconNotFound.createIconOrReturnNull(addOn.getName() + "-screenshot-1.png");
		if (icon != null)
			label.setIcon(icon);
		return label;
	}

    private String toHtml(String htmlOrPlainText) {
        if (HtmlUtils.isHtmlNode(htmlOrPlainText))
            return htmlOrPlainText.replaceAll("</?(html|body)>", "");
        else
            return HtmlUtils.toHTMLEscapedText(htmlOrPlainText);
    }

    private String formatShortcut(final Script script) {
        final String menuItemKey = ExecuteScriptAction.makeMenuItemKey(script.menuTitleKey, script.executionMode);
        final ActionAcceleratorManager acceleratorManager = ResourceController.getResourceController().getAcceleratorManager();
        final KeyStroke userDefinedKeystroke = acceleratorManager.getAccelerator(menuItemKey);
        final KeyStroke keyStroke = userDefinedKeystroke != null ?   userDefinedKeystroke : UITools.getKeyStroke(script.keyboardShortcut);
        return UITools.keyStrokeToString(keyStroke);
    }

	private String formatMenuLocation(ScriptAddOnProperties.Script script) {
		final MModeController modeController = (MModeController) Controller.getCurrentModeController();
		Entry top = modeController.getUserInputListenerFactory().getGenericMenuStructure();
		final String canonicalPath = EntryNavigator.instance().replaceAliases(script.menuLocation);
		final String[] pathElements = canonicalPath.split("/");
		Entry entry = top;
		final ListIterator<String> pathIterator = Arrays.asList(pathElements).listIterator();
		while (pathIterator.hasNext()) {
			String name = pathIterator.next();
			if (!name.isEmpty()) {
				final Entry child = entry.getChild(name);
				if (child == null){
					pathIterator.previous();
					break;
				}
				entry = child;
			}
		}
		if(entry == null)
			return script.menuLocation;
		final FreeplaneResourceAccessor resourceAccessor = new FreeplaneResourceAccessor();
		final EntryAccessor entryAccessor = new EntryAccessor(resourceAccessor);
		final String entryLocationDescription = entryAccessor.getLocationDescription(entry);
		if(!pathIterator.hasNext())
			return entryLocationDescription;
		StringBuilder menuLocationDescription = new StringBuilder(entryLocationDescription);
		while(pathIterator.hasNext()){
			menuLocationDescription.append(EntryAccessor.MENU_ELEMENT_SEPARATOR);
			menuLocationDescription.append(ScriptingMenuUtils.scriptNameToMenuItemTitle(pathIterator.next()));
		}
		return menuLocationDescription.toString();
	}

	private String bold(final String text) {
		return "<b>" + text + "</b>";
	}

	private String row(final String td, final Object... columns) {
		final String separator = "</" + td + "><" + td + ">";
		return "<tr><" + td + ">" + org.apache.commons.lang.StringUtils.join(columns, separator) + "</" + td + "></tr>";
	}

	private static String getText(String key, Object... parameters) {
		if (parameters.length == 0)
			return TextUtils.getRawText(getResourceKey(key));
		else
			return TextUtils.format(getResourceKey(key), parameters);
	}

	private static String getResourceKey(final String key) {
		return "AddOnDetailsPanel." + key;
	}

	public String getWarning() {
		return warning;
	}

	public void setWarning(String warning) {
		this.warning = warning;
	}

	public int getMaxWidth() {
		return maxWidth;
	}

	public void setMaxWidth(int maxWidth) {
		this.maxWidth = maxWidth;
	}

	@Override
	public Dimension getPreferredSize() {
		final Dimension preferredSize = super.getPreferredSize();
		preferredSize.width = Math.min(preferredSize.width, maxWidth);
		return preferredSize;
	}
}
