package org.freeplane.core.resources.components;

import java.net.URI;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.TextUtils;

import com.jgoodies.forms.builder.DefaultFormBuilder;

 public class UriLink implements IPropertyControl {
	private final String name;
 	private final String uriLabel;
 	private final URI uriLink;

 	public UriLink(final String name, final String uriLabel, final URI uri) {
 		super();
 		this.name = name;
 		this.uriLabel = uriLabel;
 		this.uriLink = uri;
 	}

 	public String getDescription() {
 		return null;
 	}

 	public String getLabel() {
 		return uriLabel;
 	}

 	public String getName() {
 		return null;
 	}

 	public void layout(final DefaultFormBuilder builder) {
 		builder.append(new JLabel(TextUtils.getText(name)));
 		
 		JButton uriButton = UITools.createHtmlLinkStyleButton(uriLink, TextUtils.getText(uriLabel));
		uriButton.setHorizontalAlignment(SwingConstants.LEADING);
		builder.append(uriButton);
 	}

 	public void setEnabled(final boolean pEnabled) {
 	}
 }


