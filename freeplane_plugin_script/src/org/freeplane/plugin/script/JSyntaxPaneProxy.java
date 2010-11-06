/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2010 dimitry
 *
 *  This file author is dimitry
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
package org.freeplane.plugin.script;

import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

import javax.swing.text.JTextComponent;

import org.freeplane.core.util.Compat;

/**
 * @author Dimitry Polivaev
 * Nov 6, 2010
 */
public class JSyntaxPaneProxy {
	private static URLClassLoader loader;
	private static Class<?> editorKit;
	private static Class<?> actionUtils;
	private static Method getLineNumberMethod;
	private static Method setCaretPositionMethod;

	public static void init() {
		if(loader != null){
			return;
		}
	    final URL jar;
		if(Compat.isLowerJdk(Compat.VERSION_1_6_0)){
			jar = JSyntaxPaneProxy.class.getClassLoader().getResource("/jsyntaxpane-jdk5.jar"); 
		}
		else{
			jar = JSyntaxPaneProxy.class.getClassLoader().getResource("/jsyntaxpane.jar");
		}
		loader = new URLClassLoader(new URL[]{jar});
		final ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
		try {
	        editorKit = loader.loadClass("jsyntaxpane.DefaultSyntaxKit");
	        Thread.currentThread().setContextClassLoader(loader);
	        editorKit.getMethod("initKit").invoke(null);
	        actionUtils = loader.loadClass("jsyntaxpane.actions.ActionUtils");
        }
        catch (Exception e) {
        	throw new RuntimeException(e);
        }
        finally{
	        Thread.currentThread().setContextClassLoader(contextClassLoader);
        }
    }

	public static int getLineOfOffset(JTextComponent mScriptTextField, int caretPosition) {
		try {
			if(getLineNumberMethod == null){
				getLineNumberMethod = actionUtils.getMethod("getLineNumber", JTextComponent.class, int.class);
			}
			return (Integer)getLineNumberMethod.invoke(null, mScriptTextField, caretPosition);
		}
		catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
	}

	public static void gotoPosition(JTextComponent mScriptTextField, int line, int col) {
		try {
			if(setCaretPositionMethod == null){
				setCaretPositionMethod = actionUtils.getMethod("setCaretPosition", JTextComponent.class, int.class);
			}
			setCaretPositionMethod.invoke(null, mScriptTextField, line, col);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
    }
}
