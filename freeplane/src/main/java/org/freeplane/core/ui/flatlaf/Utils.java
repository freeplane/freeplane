/*
 * Copyright 2020 FormDev Software GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.freeplane.core.ui.flatlaf;

import java.io.IOException;

import org.freeplane.core.resources.ResourceController;

import com.formdev.flatlaf.IntelliJTheme;
import com.formdev.flatlaf.util.LoggingFacade;

/**
 * @author Karl Tauber
 */
class Utils
{
	static IntelliJTheme loadTheme( String name ) {
		try {
			return new IntelliJTheme(ResourceController.getResourceController().getResourceStream("/laf/"+name));
		} catch( IOException ex ) {
			String msg = "FlatLaf: Failed to load IntelliJ theme '" + name + "'";
			LoggingFacade.INSTANCE.logSevere( msg, ex );
			throw new RuntimeException( msg, ex );
		}
	}
}