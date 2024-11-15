package net.minecraft.server;

import java.io.IOException;
import java.util.Locale;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

public class ChatTypeAdapterFactory implements TypeAdapterFactory {

    public ChatTypeAdapterFactory() {
    }

    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> typetoken) {
        Class<? super T> oclass = typetoken.getRawType();

        if (!oclass.isEnum()) {
            return null;
        } else {
            final Map<String, T> hashmap = new Object2ObjectOpenHashMap<>();
            Object[] aobject = oclass.getEnumConstants();
            int i = aobject.length;

            for (int j = 0; j < i; ++j) {
                Object object = aobject[j];

                hashmap.put(this.a(object), (T) object);
            }

            return new TypeAdapter<T>() {
                public void write(JsonWriter jsonwriter, T t0) throws IOException {
                    if (t0 == null) {
                        jsonwriter.nullValue();
                    } else {
                        jsonwriter.value(ChatTypeAdapterFactory.this.a(t0));
                    }

                }

                public T read(JsonReader jsonreader) throws IOException {
                    if (jsonreader.peek() == JsonToken.NULL) {
                        jsonreader.nextNull();
                        return null;
                    } else {
                        return hashmap.get(jsonreader.nextString());
                    }
                }
            };
        }
    }

    private String a(Object object) {
        return object instanceof Enum ? ((Enum) object).name().toLowerCase(Locale.US)
                : object.toString().toLowerCase(Locale.US);
    }
}
