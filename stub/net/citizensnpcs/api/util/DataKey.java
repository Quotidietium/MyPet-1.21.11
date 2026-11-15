package net.citizensnpcs.api.util;

public interface DataKey {
    String getString(String key);

    String getString(String key, String def);

    double getDouble(String key, double def);

    void setString(String key, String value);

    void setDouble(String key, double value);

    void removeKey(String key);
}
