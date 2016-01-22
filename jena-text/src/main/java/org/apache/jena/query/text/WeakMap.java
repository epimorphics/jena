package org.apache.jena.query.text;

import java.lang.ref.WeakReference;
import java.util.Map;

public abstract class WeakMap<Key, InternalKey, Value> {

    protected abstract Map<InternalKey, WeakReference<Value>> map();

    public synchronized Value get(Key directory) {
        InternalKey key = key(directory);
        if (map().containsKey(key)) {
            Value result = map().get(key).get();
            if (result == null) map().remove(key);
            return result;
        }
        return null;
    }

    protected abstract InternalKey key(Key key);

    public synchronized void put(Key key, Value value) {
        map().put(key(key), new WeakReference<>(value));
    }

    public synchronized void remove(Key key) {
        map().remove(key);
    }
}
