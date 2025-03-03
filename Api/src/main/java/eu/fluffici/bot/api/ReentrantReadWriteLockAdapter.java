/*
---------------------------------------------------------------------------------
File Name : ReentrantReadWriteLockAdapter

Developer : vakea 
Email     : vakea@fluffici.eu
Real Name : Alex Guy Yann Le Roy

Date Created  : 16/07/2024
Last Modified : 16/07/2024

---------------------------------------------------------------------------------
*/

package eu.fluffici.bot.api;

import com.google.gson.JsonObject;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ReentrantReadWriteLockAdapter extends TypeAdapter<ReentrantReadWriteLock> {

    @Override
    public void write(JsonWriter jsonWriter, ReentrantReadWriteLock lock) throws IOException {
        jsonWriter.beginObject();
        jsonWriter.name("fair").value(lock.isFair());
        jsonWriter.endObject();
    }

    @Override
    public ReentrantReadWriteLock read(JsonReader jsonReader) throws IOException {
        JsonObject jsonObject = new JsonObject();
        jsonReader.beginObject();
        boolean fair = false;

        while (jsonReader.hasNext()) {
            if (jsonReader.nextName().equals("fair")) {
                fair = jsonReader.nextBoolean();
            } else {
                jsonReader.skipValue();
            }
        }
        jsonReader.endObject();

        return new ReentrantReadWriteLock(fair);
    }
}