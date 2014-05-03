/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2014 home
 *
 *  This file author is home
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
package freeplane;

import java.util.ArrayList;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.layout.GridPane;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.freeplane.core.resources.components.IPropertyFXControl;

public class OptionPanelTest {
	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> {
			createFormGUI();
		});
	}

	private static void createFormGUI() {
		JFrame frame = new JFrame();
		JFXPanel panel = new JFXPanel();
		Platform.runLater(() -> {
			initFXPanel(panel);
		});
		frame.setContentPane(panel);
		frame.pack();
		frame.setLocationByPlatform(true);
		frame.setVisible(true);
	}

	private static void initFXPanel(JFXPanel panel) {
		GridPane formPane = new GridPane();
		formPane.setHgap(5.0);
		formPane.setVgap(5.0);
		ArrayList<IPropertyFXControl> controls = createControls();
		int rowCount = 0;
		for (IPropertyFXControl control : controls) {
			control.layout(rowCount, formPane);
			rowCount++;
		}
		Scene scene = new Scene(formPane);
		panel.setScene(scene);
	}

	private static ArrayList<IPropertyFXControl> createControls() {
	    ArrayList<IPropertyFXControl> controls = new ArrayList<>();
	    return controls;
    }

}
