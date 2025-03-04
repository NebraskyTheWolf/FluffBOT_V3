package eu.fluffici.bot.api;

import java.util.Properties;

public class EnvironmentProperty extends Properties {

    private final String prefix;

    public EnvironmentProperty(String prefix) {
        this.prefix = prefix;
    }

    @Override
    public String getProperty(String key) {
        key = key.toUpperCase()
                .replaceAll("\\.", "_");
        key = this.prefix + "_" + key;

        return System.getenv(key);
    }

    @Override
    public String getProperty(String key, String defaultValue) {
        key = key.toUpperCase()
                .replaceAll("\\.", "_");
        key = this.prefix + "_" + key;

        String stored = System.getenv(key);
        if (stored == null) return defaultValue;
        return stored;
    }

    @Override
    public Object getOrDefault(Object key, Object defaultValue) {
        if (!(key instanceof String))
            throw new IllegalArgumentException("key is supposed to be a String.");

        key = ((String) key).toUpperCase()
                .replaceAll("\\.", "_");
        key = this.prefix + "_" + key;

        return this.getProperty((String) key, (String) defaultValue);
    }

    public boolean hasVariable(String key) {
        key = key.toUpperCase()
                .replaceAll("\\.", "_");
        key = this.prefix + "_" + key;

        return System.getenv(key) != null;
    }
}
