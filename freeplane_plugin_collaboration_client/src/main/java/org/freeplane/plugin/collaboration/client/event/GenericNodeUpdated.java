/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2018 dimitry
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
package org.freeplane.plugin.collaboration.client.event;

import java.util.Optional;

import org.freeplane.plugin.collaboration.client.event.MapUpdated.ContentType;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class GenericNodeUpdated {

	private ObjectNode json;

	public GenericNodeUpdated(ObjectNode json) {
		this.json = json;
	}
	
	public ContentType contentType() {
		return ContentType.valueOf(json.get("contentType").asText());
	}
	
	Optional<String> nodeId() {
		return Optional.ofNullable(json.get("nodeId")).map(JsonNode::asText);
	}
}
