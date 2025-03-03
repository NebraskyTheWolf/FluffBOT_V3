/*
---------------------------------------------------------------------------------
File Name : JsonPatcher

Developer : vakea 
Email     : vakea@fluffici.eu
Real Name : Alex Guy Yann Le Roy

Date Created  : 08/07/2024
Last Modified : 08/07/2024

---------------------------------------------------------------------------------
*/

package eu.fluffici.bot.api.json;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("All")
public class JsonPatcher {
    private final Gson gson = new Gson();

    /**
     * Retrieves a Typed object from the given JsonObject.
     *
     * @param data The JsonObject from which the Typed object is retrieved.
     * @param <T>  The type of data stored in the Typed object.
     * @return The Typed object retrieved from the JsonObject.
     */
    public <T> Typed<T> getTypedJson(JsonObject data) {
        return this.gson.fromJson(data, Typed.class);
    }

    /**
     * Converts a Typed object to a JsonObject using Gson library.
     *
     * @param typed The Typed object to be converted.
     * @param <T> The type of data stored in the Typed object.
     * @return The JsonObject representation of the Typed object.
     */
    public <T> JsonObject toJson(Typed<T> typed) {
        return this.gson.toJsonTree(typed).getAsJsonObject();
    }

    /**
     * Converts a JsonObject to a Typed object of the specified class using Gson library.
     *
     * @param data  The JsonObject to convert.
     * @param clazz The class of the data stored in the Typed object.
     * @param <T>   The type of data stored in the Typed object.
     * @return The Typed object created from the JsonObject.
     */
    public <T> Typed<T> fromJson(JsonObject data, Class<T> clazz) {
        return this.gson.fromJson(data, new TypeToken<Typed<T>>() {
        }.getType());
    }

    /**
     * Converts a JSON string to a Typed object of the specified class using Gson library.
     *
     * @param json  The JSON string to convert.
     * @param clazz The class of the data stored in the Typed object.
     * @param <T>   The type of data stored in the Typed object.
     * @return The Typed object created from the JSON string.
     */
    public <T> Typed<T> fromJson(String json, Class<T> clazz) {
        return this.gson.fromJson(json, new TypeToken<Typed<T>>() {
        }.getType());
    }

    /**
     * Converts a Typed object to a JSON string using Gson library.
     *
     * @param typed The Typed object to convert.
     * @param <T> The type of data stored in the Typed object.
     * @return The JSON string representation of the Typed object.
     */
    public <T> String toJsonString(Typed<T> typed) {
        return this.gson.toJson(typed);
    }

    /**
     * Applies a patch to update an old JSON element with a new JSON element using Gson library.
     *
     * @param oldElement The old JSON element to be updated.
     * @param newElement The new JSON element used for updating.
     * @param <T>        The type of data stored in the Typed object.
     * @return The updated JSON element as a Typed object.
     */
    public <T> Typed patch(JsonElement oldElement, JsonElement newElement) {
        return this.gson.fromJson(patchHelper(oldElement, newElement), Typed.class);
    }

    /**
     * Applies a patch to update an old JSON element with a new JSON element.
     *
     * @param oldElement The old JSON element to be updated.
     * @param newElement The new JSON element used for updating.
     * @return The updated JSON element.
     */
    private JsonElement patchHelper(@NotNull JsonElement oldElement, JsonElement newElement) {
        if (oldElement.isJsonObject() && newElement.isJsonObject()) {
            JsonObject oldObject = oldElement.getAsJsonObject();
            JsonObject newObject = newElement.getAsJsonObject();
            for (String key : newObject.keySet()) {
                if (oldObject.has(key)) {
                    oldObject.add(key, patchHelper(oldObject.get(key), newObject.get(key)));
                } else {
                    oldObject.add(key, newObject.get(key));
                }
            }
            return oldObject;
        } else if (oldElement.isJsonArray() && newElement.isJsonArray()) {
            JsonArray oldArray = oldElement.getAsJsonArray();
            JsonArray newArray = newElement.getAsJsonArray();
            return mergeArrays(oldArray, newArray);
        } else {
            return newElement;
        }
    }

    /**
     * Merges two JsonArrays by applying a patch to update the elements
     * in the oldArray with the corresponding elements from the newArray.
     *
     * @param oldArray The original JsonArray to be updated.
     * @param newArray The new JsonArray used for patching.
     * @return The updated JsonArray after applying the patch.
     */
    private JsonArray mergeArrays(JsonArray oldArray, JsonArray newArray) {
        for (int i = 0; i < oldArray.size(); i++) {
            oldArray.set(i, patchHelper(oldArray.get(i), newArray.get(i)));
        }
        return oldArray;
    }
}