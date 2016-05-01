package com.android.systemui.statusbar;

import com.android.internal.statusbar.StatusBarPanelCustomTile;

import android.util.ArrayMap;

/**
 * Custom tile data to keep track of created 3rd party tiles
 */
public class CustomTileData {
    public static final class Entry {
        public final String key;
        public final StatusBarPanelCustomTile sbc;

        public Entry(StatusBarPanelCustomTile sbc) {
            this.key = sbc.getKey();
            this.sbc = sbc;
        }
    }

    private final ArrayMap<String, Entry> mEntries = new ArrayMap<>();

    public ArrayMap<String, Entry> getEntries() {
        return mEntries;
    }

    public void add(Entry entry) {
        mEntries.put(entry.key, entry);
    }

    public Entry remove(String key) {
        Entry removed = mEntries.remove(key);
        if (removed == null) return null;
        return removed;
    }

    public Entry get(String key) {
        return mEntries.get(key);
    }

    public Entry get(int i) {
        return mEntries.valueAt(i);
    }

    public void clear() {
        mEntries.clear();
    }

    public int size() {
        return mEntries.size();
    }
}