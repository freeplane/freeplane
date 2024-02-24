/*
 * Created on 1 Feb 2024
 *
 * author dimitry
 */
package org.freeplane.plugin.codeexplorer.task;

import java.lang.reflect.Type;
import java.util.Map;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

class MapAdapter implements JsonSerializer<Map<?, ?>> {
    @Override
    public JsonElement serialize(Map<?, ?> src, Type typeOfSrc,JsonSerializationContext context) {
        if (src == null || src.isEmpty())
            return null;
        JsonObject obj = new JsonObject();
        for (Map.Entry<?, ?> entry : src.entrySet()) {
            obj.add(entry.getKey().toString(), context.serialize(entry.getValue()));
        }
    return obj;
    }
}