/*
 * Copyright 2000-2016 JetBrains s.r.o.
 * Modified 2019 Dimitry Polivaev
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.freeplane.core.ui.components;

import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.plaf.ComponentUI;

import com.bulenkov.darcula.ui.DarculaButtonUI;
import com.bulenkov.iconloader.util.GraphicsConfig;
import com.bulenkov.iconloader.util.GraphicsUtil;


public class FixDarculaToggleButtonUI extends DarculaButtonUI {
  @SuppressWarnings("MethodOverridesStaticMethodOfSuperclass")
  public static ComponentUI createUI(JComponent c) {
    return new FixDarculaToggleButtonUI();
  }
  
  @Override
  public void paint(Graphics g, JComponent c) {
    final AbstractButton button = (AbstractButton) c;
    final ButtonModel model = button.getModel();
    final Border border = c.getBorder();
    final GraphicsConfig config = GraphicsUtil.setupAAPainting(g);
    final boolean square = isSquare(c);
    if (c.isEnabled() && border != null && button.isContentAreaFilled()) {
      final Insets ins = border.getBorderInsets(c);
      final int yOff = (ins.top + ins.bottom) / 4;
      if (!square) {
        if (c instanceof JButton && ((JButton)c).isDefaultButton() || model.isSelected()) {
          ((Graphics2D)g).setPaint(new GradientPaint(0, 0, getSelectedButtonColor1(), 0, c.getHeight(), getSelectedButtonColor2()));
        }
        else {
          ((Graphics2D)g).setPaint(new GradientPaint(0, 0, getButtonColor1(), 0, c.getHeight(), getButtonColor2()));
        }
      }
      g.fillRoundRect(square ? 2 : 4, yOff, c.getWidth() - 2 * 4, c.getHeight() - 2 * yOff, square ? 3 : 5, square ? 3 : 5);
    }
    config.restore();
    super.paint(g, c);
  }

}
