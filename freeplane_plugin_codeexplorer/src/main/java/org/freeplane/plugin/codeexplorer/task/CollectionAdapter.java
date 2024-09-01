/*
 * Created on 1 Feb 2024
 *
 * author dimitry
 */
package org.freeplane.plugin.codeexplorer.task;

import java.lang.reflect.Type;
import java.util.Collection;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

class CollectionAdapter implements JsonSerializer<Collection<?>> {
    @Override
    public JsonElement serialize(Collection<?> src, Type typeOfSrc, JsonSerializationContext context) {
      if (src == null || src.isEmpty()) // exclusion is made here
        return null;

      JsonArray array = new JsonArray();

      for (Object child : src) {
        JsonElement element = context.serialize(child);
        array.add(element);
      }

      return array;
    }
  }