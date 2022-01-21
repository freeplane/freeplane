/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2008 Joerg Mueller, Daniel Polansky, Christian Foltin, Dimitry Polivaev
 *
 *  This file is modified by Dimitry Polivaev in 2008.
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.freeplane.core.resources.components;

import java.awt.Color;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.Optional;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import org.freeplane.core.ui.ColorTracker;
import org.freeplane.core.ui.components.JFreeplaneMenuItem;
import org.freeplane.core.util.ColorUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.clipboard.ClipboardAccessor;

import com.jgoodies.forms.builder.DefaultFormBuilder;

public class ColorProperty extends PropertyBean implements IPropertyControl, ActionListener {
	Color color;
	final private Color defaultColor;
	JColorButton mButton;

	/**
	 * @param name
	 * @param defaultColor
	 */
	public ColorProperty(final String name, final String defaultColor) {
		super(name);
		this.defaultColor = ColorUtils.stringToColor(defaultColor);
		mButton = new JColorButton();
		mButton.addActionListener(this);
		color = null;
	}

	public void actionPerformed(final ActionEvent arg0) {
		final Color result = ColorTracker.showCommonJColorChooserDialog(mButton.getRootPane(), TextUtils
		    .getOptionalText(getLabel()), getColorValue(), defaultColor);
		if(result != null){
			setColorValue(result);
			firePropertyChangeEvent();
		}
	}

	/**
	 */
	public Color getColorValue() {
		return color;
	}

	@Override
	public String getValue() {
		final Color colorValue = getColorValue();
		return colorValue == null ? null : ColorUtils.colorToRGBAString(colorValue);
	}

	public void appendToForm(final DefaultFormBuilder builder) {
		appendToForm(builder, mButton);
		mButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(final MouseEvent evt) {
				if (evt.isPopupTrigger()) {
					showPopupMenu(evt);
				}
			}

			@Override
			public void mouseReleased(final MouseEvent evt) {
				if (evt.isPopupTrigger()) {
					showPopupMenu(evt);
				}
			}
			private void showPopupMenu(final MouseEvent evt) {
				final JPopupMenu menu = new JPopupMenu();
				copyColorItem().ifPresent(menu::add);
				pasteColorItem().ifPresent(menu::add);
				resetColorItem().ifPresent(menu::add);
				menu.show(evt.getComponent(), evt.getX(), evt.getY());
			}

			private Optional<JMenuItem> resetColorItem() {
				if(defaultColor == null || defaultColor.equals(color))
					return Optional.empty();
				final JMenuItem item = new JFreeplaneMenuItem(TextUtils.getText("ColorProperty.ResetColor"));
				item.addActionListener(e -> {
					setColorValue(defaultColor);
					firePropertyChangeEvent();
				});
				return Optional.of(item);
			}
			
			private Optional<JMenuItem> copyColorItem (){
				return Optional.ofNullable(getValue()).map(this::copyColorItem);
			}
			
			private JMenuItem copyColorItem (String value){
				final JMenuItem item = new JFreeplaneMenuItem(TextUtils.getText("ColorProperty.CopyColor"));
				item.addActionListener(e -> ClipboardAccessor.getInstance().setClipboardContents(value));
				return item;
			}

			private Optional<JMenuItem> pasteColorItem (){
				try {
					Transferable t = ClipboardAccessor.getInstance().getClipboardContents();
					if (t == null || !t.isDataFlavorSupported(DataFlavor.stringFlavor))
						return Optional.empty();
					String content = t.getTransferData(DataFlavor.stringFlavor).toString().trim();
					Color color = ColorUtils.stringToColor(content);
					final JMenuItem item = new JFreeplaneMenuItem(TextUtils.getText("ColorProperty.PasteColor"));
					item.addActionListener(e -> {
						setColorValue(color);
						firePropertyChangeEvent();
					});
					return Optional.of(item);
				} catch (NumberFormatException | UnsupportedFlavorException | IOException e) {
					return Optional.empty();
				}
			}
		});
		}

	/**
	 */
	public void setColorValue(Color color) {
	    this.color = color;
	    mButton.setColor(color);
	}

	public void setEnabled(final boolean pEnabled) {
		mButton.setEnabled(pEnabled);
		super.setEnabled(pEnabled);
	}

	@Override
	public void setValue(final String value) {
		setColorValue(value == null ? null : ColorUtils.stringToColor(value));
	}
}
