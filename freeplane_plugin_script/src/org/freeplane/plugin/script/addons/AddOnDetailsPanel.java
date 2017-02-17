package org.freeplane.plugin.script.addons;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.tree.DefaultMutableTreeNode;

import org.apache.commons.lang.StringUtils;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.MenuBuilder;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.HtmlUtils;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.MenuUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.icon.IconNotFound;
import org.freeplane.features.mode.Controller;
import org.freeplane.main.addons.AddOnProperties;
import org.freeplane.plugin.script.ExecuteScriptAction;
import org.freeplane.plugin.script.addons.ScriptAddOnProperties.Script;

import com.jgoodies.forms.layout.FormSpecs;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
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
					text.append(row("td", bold(TextUtils.getText(script.menuTitleKey)), formatMenuLocation(script),
						formatShortcut(script)));
				}
				text.append("</table>");
			}
		}
		text.append("</body></html>");
		final JLabel label = new JLabel(text.toString());
		label.setAutoscrolls(true);
		final ImageIcon icon = IconNotFound.createIconOrReturnNull(addOn.getName() + "-screenshot-1.png");
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
        final String shortcutKey = MenuUtils.makeAcceleratorKey(menuItemKey);
        final String oldShortcut = ResourceController.getResourceController().getProperty(shortcutKey);
        final KeyStroke keyStroke = UITools.getKeyStroke(oldShortcut != null ? oldShortcut : script.keyboardShortcut);
        return UITools.keyStrokeToString(keyStroke);
    }

	private String formatMenuLocation(ScriptAddOnProperties.Script script) {
		final String location = script.menuLocation == null ? "main_menu_scripting" : script.menuLocation;
		MenuBuilder menuBuilder = Controller.getCurrentModeController().getUserInputListenerFactory().getMenuBuilder(MenuBuilder.class);
		// "/menu_bar/edit/menu_extensions" -> [Node Extensions, Edit]
		final List<String> pathElements = getMenuPathElements(menuBuilder, location);
		Collections.reverse(pathElements);
		pathElements.add(TextUtils.getText(script.menuTitleKey));
		//TODO - impl. ribbons contribution
		return StringUtils.join(pathElements.iterator(), "->");
	}

	public static List<String> getMenuPathElements(MenuBuilder menuBuilder, final String location) {
		final ArrayList<String> pathElements = new ArrayList<String>();
		final DefaultMutableTreeNode node = menuBuilder.get(location);
		if (node != null) {
			pathElements.addAll(getMenuPathElements(node));
		}
		else {
			int index = location.lastIndexOf('/');
			if (index != -1) {
				final String lastKey = location.substring(index + 1);
				pathElements.add(TextUtils.getText(lastKey, TextUtils.getText("addons." + lastKey, lastKey)));
				// recurse
				if (index > 1)
					pathElements.addAll(getMenuPathElements(menuBuilder, location.substring(0, index)));
			}
		}
		return pathElements;
	}

	private static List<String> getMenuPathElements(DefaultMutableTreeNode node) {
		ArrayList<String> pathElements = new ArrayList<String>();
		while (node != null) {
			if (node.getUserObject() instanceof JMenuItem)
				pathElements.add(((JMenuItem) node.getUserObject()).getText());
			node = (DefaultMutableTreeNode) node.getParent();
		}
		return pathElements;
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
